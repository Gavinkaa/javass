package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

import java.util.*;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */

public final class MctsPlayer implements Player {

    private static class Node {
        private TurnState turnState;
        private Node[] children;
        private CardSet unusedCards;
        // This will be unused in the case of the root node
        private int totalPoints = 0;
        private int numberOfFinishedTurns = 0;

        public Node(TurnState turnState, CardSet hand) {
            this.turnState = turnState;
            this.children = new Node[hand.size()];
            this.unusedCards = hand;
        }

        private int bestChild(double c) {
            double bestScore = Double.NEGATIVE_INFINITY;
            int bestIndex = -1;
            for (int i = 0; i < children.length; i++) {
                Node child = children[i];
                if (child != null) {
                    double vScore;
                    if (child.numberOfFinishedTurns > 0) {
                        vScore = ((double) child.totalPoints) / child.numberOfFinishedTurns;
                        vScore += c * Math.sqrt(2 * Math.log(numberOfFinishedTurns) / child.numberOfFinishedTurns);
                    } else {
                        vScore = Double.POSITIVE_INFINITY;
                    }
                    if (vScore > bestScore) {
                        bestScore = vScore;
                        bestIndex = i;
                    }
                }
            }
            return bestIndex;
        }

        // Returns null if we can't add a node
        public Collection<Node> addNode(CardSet firstHand, PlayerId ownId) {
            return realAddNode(this, firstHand, ownId, new ArrayDeque<>());
        }

        private static Collection<Node> realAddNode(Node root, CardSet firstHand, PlayerId ownId, ArrayDeque<Node> path) {
            Node currentNode = root;
            for (;;) {
                for (int i = 0; i < currentNode.children.length; ++i) {
                    Node child = currentNode.children[i];
                    if (child == null) {
                        Card toPlay = currentNode.unusedCards.get(i);
                        TurnState nextTurnState = currentNode.turnState.withNewCardPlayedAndTrickCollected(toPlay);
                        CardSet nextHand;
                        if (nextTurnState.nextPlayer() == ownId) {
                            nextHand = firstHand.intersection(nextTurnState.unplayedCards());
                        } else {
                            nextHand = nextTurnState.unplayedCards().difference(firstHand);
                        }
                        if (nextTurnState.isTerminal()) {
                            currentNode.children[i] = new Node(nextTurnState, CardSet.EMPTY);
                        } else {
                            currentNode.children[i] = new Node(nextTurnState, nextTurnState.trick().playableCards(nextHand));
                        }
                        path.addFirst(currentNode);
                        path.addFirst(currentNode.children[i]);
                        return path;
                    }
                }
                int bestIndex = currentNode.bestChild(CURIOSITY);
                if (bestIndex < 0) {
                    return null;
                }
                Node child = currentNode.children[bestIndex];

                path.addFirst(currentNode);
                currentNode = child;
            }
        }
    }

    //--------------------------------------------------------------------------------
    private PlayerId ownId;
    private SplittableRandom rng;
    private int interations;
    private static final int CURIOSITY = 40;

    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        Preconditions.checkArgument(iterations >= 9);
        this.ownId = ownId;
        this.rng = new SplittableRandom(rngSeed);
        this.interations = iterations;
    }

    private Score sampleEndTurnScore(TurnState turnState, CardSet firstHand) {
        firstHand = firstHand.intersection(turnState.unplayedCards());

        while (!turnState.isTerminal()) {
            boolean mePlaying = turnState.nextPlayer() == ownId;
            CardSet cardSet;

            if (mePlaying) {
                cardSet = firstHand;
            } else {
                cardSet = turnState.unplayedCards().difference(firstHand);
            }
            Card cardToPlay = cardSet.get(rng.nextInt(cardSet.size()));
            if (mePlaying) {
                firstHand = firstHand.remove(cardToPlay);
            }

            turnState = turnState.withNewCardPlayedAndTrickCollected(cardToPlay);
        }
        return turnState.score();
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        Node root = new Node(state, state.trick().playableCards(hand));
        for (int i = 0; i < interations; i++) {
            Collection<Node> path = root.addNode(hand, ownId);
            if (path == null) {
                break;
            }

            Iterator<Node> iter = path.iterator();
            Node nextNode = iter.next();
            Score score = sampleEndTurnScore(nextNode.turnState, hand);
            while (iter.hasNext()) {
                Node thisNode = nextNode;
                nextNode = iter.next();

                TeamId thisTeam = nextNode.turnState.nextPlayer().team();
                int relevant = score.totalPoints(thisTeam);

                thisNode.totalPoints += relevant;
                thisNode.numberOfFinishedTurns++;
            }
            root.numberOfFinishedTurns++;
            root.totalPoints += score.totalPoints(ownId.team());
        }
        return hand.get(root.bestChild(CURIOSITY));
    }


}
