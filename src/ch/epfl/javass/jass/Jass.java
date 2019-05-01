package ch.epfl.javass.jass;

/**
 * Contains useful constants for the game of Jass
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public interface Jass {
    //public final static is implicit, the IDE suggests not to put it
    int HAND_SIZE = 9;
    int TRICKS_PER_TURN = 9;
    int WINNING_POINTS = 1000;
    int MATCH_ADDITIONAL_POINTS = 100;
    int LAST_TRICK_ADDITIONAL_POINTS = 5;
}
