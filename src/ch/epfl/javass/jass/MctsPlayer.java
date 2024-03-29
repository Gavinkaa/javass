package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.SplittableRandom;

/**
 * MctsPlayer is a player that makes decisions on which cards to play
 * based on a Monte Carlo tree search algorithm. Every time it needs to
 * play a card, it constructs a new search tree, and evaluates it with
 * a certain depth.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class MctsPlayer implements Player {

    // turnState must not be terminal when calling this method
    private static long packedNextHand(TurnState turnState, PlayerId ownId, long firstHand) {
        assert !turnState.isTerminal();

        boolean mePlaying = turnState.nextPlayer() == ownId;
        long cardSet;
        if (mePlaying) {
            cardSet = PackedCardSet.intersection(firstHand, turnState.packedUnplayedCards());
        } else {
            cardSet = PackedCardSet.difference(turnState.packedUnplayedCards(), firstHand);
        }
        return PackedTrick.playableCards(turnState.packedTrick(), cardSet);
    }

    private static final class Node {
        private final TurnState turnState;
        private final Node[] children;
        private final long unusedCards;
        // This will be unused in the case of the root node
        private int totalPoints = 0;
        private int numberOfFinishedTurns = 0;

        private Node(TurnState turnState, long hand) {
            this.turnState = turnState;
            this.children = new Node[PackedCardSet.size(hand)];
            this.unusedCards = hand;
        }

        private double vScore(Node child, double c) {
            if (child.numberOfFinishedTurns > 0) {
                double vScore = ((double) child.totalPoints) / child.numberOfFinishedTurns;
                vScore += c * Math.sqrt(2 * Math.log(this.numberOfFinishedTurns) / child.numberOfFinishedTurns);
                return vScore;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        private int bestChild(double c) {
            if (this.children.length == 1) {
                return 0;
            }
            double bestScore = Double.NEGATIVE_INFINITY;
            int bestIndex = -1;
            for (int i = 0; i < this.children.length; i++) {
                Node child = this.children[i];
                if (child != null) {
                    double vScore = vScore(child, c);
                    if (vScore > bestScore) {
                        bestScore = vScore;
                        bestIndex = i;
                    }
                }
            }
            return bestIndex;
        }

        // Returns null if we can't add a node
        private Collection<Node> addNode(long firstHand, PlayerId ownId) {
            return realAddNode(this, firstHand, ownId, new ArrayDeque<>());
        }

        private static Collection<Node> realAddNode(Node root, long firstHand, PlayerId ownId, ArrayDeque<Node> path) {
            Node currentNode = root;
            for (; ; ) {
                // Try and insert directly below the currentNode
                for (int i = 0; i < currentNode.children.length; ++i) {
                    Node child = currentNode.children[i];
                    if (child == null) {
                        Card toPlay = Card.ofPacked(PackedCardSet.get(currentNode.unusedCards, i));
                        TurnState nextTurnState = currentNode.turnState.withNewCardPlayedAndTrickCollected(toPlay);
                        Node newNode;
                        if (nextTurnState.isTerminal()) {
                            newNode = new Node(nextTurnState, PackedCardSet.EMPTY);
                        } else {
                            long nextHand = packedNextHand(nextTurnState, ownId, firstHand);
                            newNode = new Node(nextTurnState, nextHand);
                        }
                        currentNode.children[i] = newNode;
                        path.addFirst(currentNode);
                        path.addFirst(currentNode.children[i]);
                        return path;
                    }
                }
                path.addFirst(currentNode);
                // Recurse with the most promising direct child
                int bestIndex = currentNode.bestChild(CURIOSITY);
                if (bestIndex < 0) {
                    return path;
                }
                currentNode = currentNode.children[bestIndex];
            }
        }
    }

    private final PlayerId ownId;
    private final SplittableRandom rng;
    private final int iterations;
    private static final int CURIOSITY = 40;

    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        Preconditions.checkArgument(iterations >= Jass.HAND_SIZE);
        this.ownId = ownId;
        this.rng = new SplittableRandom(rngSeed);
        this.iterations = iterations;
    }

    private Score sampleEndTurnScore(TurnState turnState, long firstHand) {
        while (!turnState.isTerminal()) {
            long cardSet = packedNextHand(turnState, this.ownId, firstHand);
            int cardToPlay = PackedCardSet.get(cardSet, this.rng.nextInt(PackedCardSet.size(cardSet)));
            turnState = turnState.withNewCardPlayedAndTrickCollected(Card.ofPacked(cardToPlay));
        }
        return turnState.score();
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long packedHand = hand.packed();
        long playableHand = PackedTrick.playableCards(state.packedTrick(), packedHand);
        // performance shortcut
        if (PackedCardSet.size(playableHand) == 1) {
            return Card.ofPacked(PackedCardSet.get(playableHand, 0));
        }
        Node root = new Node(state, playableHand);
        for (int i = 0; i < this.iterations; i++) {
            Collection<Node> path = root.addNode(packedHand, this.ownId);
            Iterator<Node> iter = path.iterator();
            Node nextNode = iter.next();
            Score score = sampleEndTurnScore(nextNode.turnState, packedHand);
            // Propagate scores
            while (iter.hasNext()) {
                Node thisNode = nextNode;
                nextNode = iter.next();
                TeamId thisTeam = nextNode.turnState.nextPlayer().team();
                int relevant = score.totalPoints(thisTeam);
                thisNode.totalPoints += relevant;
                thisNode.numberOfFinishedTurns++;
            }
            // we don't need to propagate points to the root
            root.numberOfFinishedTurns++;
        }
        return Card.ofPacked(PackedCardSet.get(playableHand, root.bestChild(0)));
    }

    private final static int[] TRUMP_CARD_VALUES = {4, 4, 4, 7, 5, 7, 6, 6, 7};
    private final int DELEGATE_LIMIT = 15;

    @Override
    public Card.Color chooseTrump(CardSet hand, boolean canDelegate) {
        int[] trumpValues = new int[Card.Color.COUNT];
        for (int i = 0; i < hand.size(); ++i) {
            Card card = hand.get(i);
            trumpValues[card.color().ordinal()] += TRUMP_CARD_VALUES[card.rank().ordinal()];
        }
        int i = 0;
        int max = trumpValues[i];
        for (;i < Card.Color.COUNT; ++i) {
            if (trumpValues[i] > max) {
                max = trumpValues[i];
            }
        }
        if (canDelegate && max < DELEGATE_LIMIT) {
            return null;
        } else {
            return Card.Color.ALL.get(rng.nextInt(Card.Color.COUNT));
        }
    }

    @Override
    public CardSet announce(CardSet hand) {
        return AnnounceValue.bestAnnounce(hand);
    }
}
