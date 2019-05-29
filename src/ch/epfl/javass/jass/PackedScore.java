package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;


/**
 * Provides utility method for working with the binary representation of scores.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class PackedScore {
    /**
     * The initial value of a packed Score
     */
    public static final long INITIAL = 0;

    private static final int COUNT_SIZE = 4;
    private static final int TURN_POINTS_START = 4;
    private static final int TURN_POINTS_SIZE = 9;
    private static final int GAME_POINTS_START = 13;
    private static final int GAME_POINTS_SIZE = 11;
    private static final int HALF_SIZE = 32;

    private static final int MAX_TURN_POINTS = 257;
    private static final int MAX_GAME_POINTS = 2000;

    private PackedScore() {
    }

    private static boolean isValidHalf(long pkScore, TeamId team) {
        if (turnTricks(pkScore, team) > Jass.TRICKS_PER_TURN) {
            return false;
        }
        if (turnPoints(pkScore, team) > MAX_TURN_POINTS) {
            return false;
        }
        if (gamePoints(pkScore, team) > MAX_GAME_POINTS) {
            return false;
        }
        int start = team == TeamId.TEAM_1 ? 0 : HALF_SIZE;
        int zero_start = COUNT_SIZE + TURN_POINTS_SIZE + GAME_POINTS_SIZE;
        int zero_size = HALF_SIZE - zero_start;
        return Bits64.extract(pkScore, start + zero_start, zero_size) == 0;
    }

    /**
     * Check of the binary representation of a score is valid.
     * This method checks if bits are only present in the right fields, and if
     * those fields are in the valid ranges.
     * turnTricks must be <= 9, turnPoints must be <= 257 and gamePoints must be <= 2000
     *
     * @param pkScore the bit pattern to check
     * @return true of the representation was valid, and false otherwise
     */
    public static boolean isValid(long pkScore) {
        return isValidHalf(pkScore, TeamId.TEAM_1) && isValidHalf(pkScore, TeamId.TEAM_2);
    }

    /**
     * Pack the different components of a score into its binary representation.
     * For each player, this method takes the tricks they've won that turn, the number of
     * points they've earned that turn, and the total number of points they've earned over the game.
     *
     * @return the binary representation composed of these fields
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2, int gamePoints2) {
        long fstHalf = Bits32.pack(turnTricks1, COUNT_SIZE, turnPoints1, TURN_POINTS_SIZE, gamePoints1, GAME_POINTS_SIZE);
        long sndHalf = Bits32.pack(turnTricks2, COUNT_SIZE, turnPoints2, TURN_POINTS_SIZE, gamePoints2, GAME_POINTS_SIZE);
        long pkScore = Bits64.pack(fstHalf, HALF_SIZE, sndHalf, HALF_SIZE);
        assert PackedScore.isValid(pkScore);
        return pkScore;
    }

    /**
     * Extract the turn tricks from the binary representation of a score
     *
     * @param pkScore the binary representation from which to extract
     * @param t       the team to extract from
     * @return the number of tricks the team has won that turn
     */
    public static int turnTricks(long pkScore, TeamId t) {
        int shift = t == TeamId.TEAM_1 ? 0 : HALF_SIZE;
        return (int) Bits64.extract(pkScore, shift, COUNT_SIZE);
    }

    /**
     * Extract out the number of points a player has won so far in a turn
     *
     * @param pkScore the binary representation from which to extract
     * @param t       the team to look at
     * @return the number of points that turn has won so far this turn
     */
    public static int turnPoints(long pkScore, TeamId t) {
        int shift = t == TeamId.TEAM_1 ? 0 : HALF_SIZE;
        return (int) Bits64.extract(pkScore, shift + TURN_POINTS_START, TURN_POINTS_SIZE);
    }

    /**
     * Extract out the number of points a team has won in the game so far,
     * not taking into account the points they've won on this turn.
     *
     * @param pkScore the binary representation of the score
     * @param t       the team to look at
     * @return the number of points that team had won before this turn
     */
    public static int gamePoints(long pkScore, TeamId t) {
        int shift = t == TeamId.TEAM_1 ? 0 : HALF_SIZE;
        return (int) Bits64.extract(pkScore, shift + GAME_POINTS_START, GAME_POINTS_SIZE);
    }

    /**
     * Return the sum of gamePoints and turnPoints for a given team
     *
     * @param pkScore the binary representation of the score
     * @param t       the team to look at
     */
    public static int totalPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        return PackedScore.turnPoints(pkScore, t) + PackedScore.gamePoints(pkScore, t);
    }

    /**
     * Add the points of winning a trick to a team's score this turn
     *
     * @param pkScore     the score to operate on
     * @param winningTeam the team that won this trick
     * @param trickPoints the number of points this trick was worth
     * @return the new score as a result of applying this win to a team
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
        assert PackedScore.isValid(pkScore);
        int shift = winningTeam == TeamId.TEAM_1 ? 0 : HALF_SIZE;
        long turnPoints = PackedScore.turnPoints(pkScore, winningTeam);
        long turnTricks = PackedScore.turnTricks(pkScore, winningTeam);
        if (turnTricks == Jass.TRICKS_PER_TURN - 1) {
            turnPoints += Jass.MATCH_ADDITIONAL_POINTS;
        }
        turnPoints += trickPoints;
        ++turnTricks;
        pkScore &= ~Bits64.mask(shift, COUNT_SIZE + TURN_POINTS_SIZE);
        pkScore |= (turnTricks << shift);
        pkScore |= (turnPoints << (shift + TURN_POINTS_START));

        assert PackedScore.isValid(pkScore);
        return pkScore;
    }

    /**
     * After each turn, the current points of that turn are
     * added to total for each team, and the points are then reset
     * to 0. This function applies this action to a packed score.
     *
     * @param pkScore the score on which to apply the transformation
     * @return the packed score after the transformation
     */
    public static long nextTurn(long pkScore) {
        assert PackedScore.isValid(pkScore);

        int points1 = PackedScore.totalPoints(pkScore, TeamId.TEAM_1);
        int points2 = PackedScore.totalPoints(pkScore, TeamId.TEAM_2);
        return PackedScore.pack(0, 0, points1, 0, 0, points2);
    }

    public static long withAnnounce(long pkScore, AnnounceValue value, TeamId winningTeam) {
        assert PackedScore.isValid(pkScore);
        int team1extra = 0;
        int team2extra = 0;
        if (winningTeam == TeamId.TEAM_1) {
            team1extra = value.points();
        } else {
            team2extra = value.points();
        }
        return PackedScore.pack(
                PackedScore.turnTricks(pkScore, TeamId.TEAM_1),
                PackedScore.turnPoints(pkScore, TeamId.TEAM_1),
                PackedScore.totalPoints(pkScore, TeamId.TEAM_1) + team1extra,
                PackedScore.turnTricks(pkScore, TeamId.TEAM_2),
                PackedScore.turnPoints(pkScore, TeamId.TEAM_2),
                PackedScore.totalPoints(pkScore, TeamId.TEAM_2) + team2extra
        );
    }

    /**
     * Return a string representation of a packed score
     *
     * @param pkScore the score to represent
     * @return a string representing what the score looks like
     */
    public static String toString(long pkScore) {
        return String.format(
                "Team 1 {tricks: %d, turnPoints: %d, points: %d } Team 2 {tricks: %d, turnPoints: %d points: %d}",
                PackedScore.turnTricks(pkScore, TeamId.TEAM_1),
                PackedScore.turnPoints(pkScore, TeamId.TEAM_1),
                PackedScore.gamePoints(pkScore, TeamId.TEAM_1),
                PackedScore.turnTricks(pkScore, TeamId.TEAM_2),
                PackedScore.turnPoints(pkScore, TeamId.TEAM_2),
                PackedScore.gamePoints(pkScore, TeamId.TEAM_2)
        );
    }
}
