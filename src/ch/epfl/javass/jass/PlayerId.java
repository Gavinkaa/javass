package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to identify one of the 4 players of the game
 */
public enum PlayerId {
    PLAYER_1,
    PLAYER_2,
    PLAYER_3,
    PLAYER_4;

    /**
     * Holds all the possible Player ids
     */
    public static final List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(PlayerId.values()));

    /**
     * Holds the number of different Player ids
     */
    public static final int COUNT = ALL.size();

    /**
     * Each player belongs to a team in an hardcoded way
     * @return team1 if the player is 1 or 3 otherwise team2
     */
    public TeamId team() {
        return this == PLAYER_1 || this == PLAYER_3 ? TeamId.TEAM_1 : TeamId.TEAM_2;
    }
}
