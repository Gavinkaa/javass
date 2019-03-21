package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

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
        public List<Node> addNode(CardSet firstHand) {
            for (int i = 0; i < children.length; ++i) {
                Node child = children[i];
                if (child == null) {
                    Card toPlay = unusedCards.get(i);
                    TurnState nextTurnState = turnState.withNewCardPlayedAndTrickCollected(toPlay);
                    CardSet nextHand = nextTurnState.unplayedCards().difference(firstHand);
                    children[i] = new Node(nextTurnState, nextHand);
                    List<Node> path = new ArrayList<>(2);
                    path.add(children[i]);
                    path.add(this);
                    return path;
                }
            }
            int bestIndex = bestChild(C);
            if (bestIndex < 0) {
                return null;
            }
            Node child = children[bestIndex];

            List<Node> path = child.addNode(firstHand);
            if (path == null) {
                return null;
            }
            path.add(this);
            return path;
        }
    }

    //--------------------------------------------------------------------------------
    private PlayerId ownId;
    private SplittableRandom rng;
    private int interations;
    private static final int C = 40;

    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        Preconditions.checkArgument(iterations >= 9);
        this.ownId = ownId;
        this.rng = new SplittableRandom(rngSeed);
        this.interations = iterations;
    }

    private Score sampleEndTurnScore(TurnState turnState, CardSet firstHand) {
        System.out.println(firstHand);
        firstHand = firstHand.intersection(turnState.unplayedCards());

        while (!turnState.isTerminal()) {
            boolean mePlaying = turnState.nextPlayer() == ownId;
            CardSet cardSet;

            if (mePlaying) {
                cardSet = firstHand;
            } else {
                cardSet = turnState.unplayedCards().difference(firstHand);
            }
            System.out.println(ownId + " " + turnState.nextPlayer() + " " + cardSet);
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
        Node root = new Node(state, hand);
        for (int i = 0; i < interations; i++) {
            List<Node> path = root.addNode(hand);
            if (path == null) {
                break;
            }
            Score score = sampleEndTurnScore(path.get(0).turnState, hand);

            for (int j = 0; j < path.size() - 1; ) {
                Node thisNode = path.get(j);
                Node nextNode = path.get(++j);

                TeamId thisTeam = nextNode.turnState.nextPlayer().team();
                int relevant = score.totalPoints(thisTeam);

                thisNode.totalPoints += relevant;
                thisNode.numberOfFinishedTurns++;
            }
            root.numberOfFinishedTurns++;
            root.totalPoints += score.totalPoints(ownId.team());
        }
        return hand.get(root.bestChild(C));
    }


}
