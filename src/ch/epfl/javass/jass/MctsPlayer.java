package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */

public final class MctsPlayer implements Player{

    private static class Node {
        private TurnState turnState;
        private Node[] children;
        private long unusedCards;
        private int totalPoints;
        private int numberOfFinishedTurn;
    }


    MctsPlayer(PlayerId ownId, long rngSeed, int iterations){
        Preconditions.checkArgument(iterations >= 9);

    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        return null;
    }


}
