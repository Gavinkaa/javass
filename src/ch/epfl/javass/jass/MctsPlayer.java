package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

import java.util.*;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */

public final class MctsPlayer implements Player{

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

        private Node bestChild(double c) {
            double bestScore = Double.NEGATIVE_INFINITY;
            Node bestNode = null;
            for (Node child : children) {
                if (child != null) {
                    double vScore;
                    if (child.numberOfFinishedTurns > 0) {
                        vScore = ((double)child.totalPoints) / child.numberOfFinishedTurns;
                        vScore += c * Math.sqrt(2 * Math.log(numberOfFinishedTurns) / child.numberOfFinishedTurns);
                    } else {
                        vScore = Double.POSITIVE_INFINITY;
                    }
                    if (vScore > bestScore) {
                        bestScore = vScore;
                        bestNode = child;
                    }
                }
            }
            return bestNode;
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
                    return Collections.singletonList(this);
                }
            }
            Node child = bestChild(40);
            if (child == null) {
                return null;
            }
            List<Node> path = child.addNode(firstHand);
            if (path == null) {
                return null;
            }
            path.add(this);
            return path;
        }
    }

    MctsPlayer(PlayerId ownId, long rngSeed, int iterations){
        Preconditions.checkArgument(iterations >= 9);

    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        return null;
    }


}
