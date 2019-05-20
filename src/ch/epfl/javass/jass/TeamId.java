package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to identify one of the 2 teams of the game
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public enum TeamId {
    TEAM_1(true),
    TEAM_2(false);

    private final boolean isTeam1;

    TeamId(boolean isTeam1) {
        this.isTeam1 = isTeam1;
    }


    /**
     * Holds all the possible Team ids
     */
    public static final List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(TeamId.values()));

    /**
     * Holds the number of different Team ids
     */
    public static final int COUNT = ALL.size();


    /**
     * @return the team id this instance isn't
     */
    public TeamId other() {
        return this.isTeam1 ? TEAM_2 : TEAM_1;
    }
}
