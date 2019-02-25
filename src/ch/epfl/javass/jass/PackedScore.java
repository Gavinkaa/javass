package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;


/**
 * Provides utility method for working with the binary representation of scores.
 */
public final class PackedScore {
    /**
     * The initial value of a packed Score
     */
    public static long INITIAL = 0;

    private PackedScore() {
    }

    private static boolean isValidHalf(long halfScore, int start) {
        if (Bits64.extract(halfScore, start, 4) > 9) {
            return false;
        }
        if (Bits64.extract(halfScore, start + 4, 9) > 257) {
            return false;
        }
        if (Bits64.extract(halfScore, start + 13, 11) > 2000) {
            return false;
        }
        return Bits64.extract(halfScore, start + 24, 8) == 0;
    }

    /**
     * Check of the binary representation of a score is valid.
     * This method checks if bits are only present in the right fields, and if
     * those fields are in the valid ranges.
     * turnTricks must be <= 9, turnPoints must be <= 257 and gamePoints must be <= 2000
     * @param pkScore the bit pattern to check
     * @return true of the representation was valid, and false otherwise
     */
    public static boolean isValid(long pkScore) {
        return isValidHalf(pkScore, 0) && isValidHalf(pkScore, 32);
    }

    /**
     * Pack the different components of a score into its binary representation.
     * For each player, this method takes the tricks they've won that turn, the number of
     * points they've earned that turn, and the total number of points they've earned over the game.
     * @return the binary representation composed of these fields
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2, int gamePoints2) {
        long fstHalf = Bits32.pack(turnTricks1, 4, turnPoints1, 9, gamePoints1, 11);
        long sndHalf = Bits32.pack(turnTricks2, 4, turnPoints2, 9, gamePoints2, 11);
        long pkScore = Bits64.pack(fstHalf, 32, sndHalf, 32);
        assert PackedScore.isValid(pkScore);
        return pkScore;
    }

    /**
     * Extract the turn tricks from the binary representation of a score
     * @param pkScore the binary representation from which to extract
     * @param t the team to extract from
     * @return the number of tricks the team has won that turn
     */
    public static int turnTricks(long pkScore, TeamId t) {
        assert isValid(pkScore);

        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int) Bits64.extract(pkScore, shift, 4);
    }

    /**
     * Extract out the number of points a player has won so far in a turn
     * @param pkScore the binary representation from which to extract
     * @param t the team to look at
     * @return the number of points that turn has won so far this turn
     */
    public static int turnPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int) Bits64.extract(pkScore, shift + 4, 9);
    }

    /**
     * Extract out the number of points a team has won in the game so far,
     * not taking into account the points they've won on this turn.
     * @param pkScore the binary representation of the score
     * @param t the team to look at
     * @return the number of points that team had won before this turn
     */
    public static int gamePoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int) Bits64.extract(pkScore, shift + 13, 11);
    }

    /**
     * Return the sum of gamePoints and turnPoints for a given team
     * @param pkScore the binary representation of the score
     * @param t the team to look at
     */
    public static int totalPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        return PackedScore.turnPoints(pkScore, t) + PackedScore.gamePoints(pkScore, t);
    }

    /**
     * Add the points of winning a trick to a team's score this turn
     * @param pkScore the score to operate on
     * @param winningTeam the team that won this trick
     * @param trickPoints the number of points this trick was worth
     * @return the new score as a result of applying this win to a team
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
        assert PackedScore.isValid(pkScore);

        int shift = winningTeam == TeamId.TEAM_1 ? 0 : 32;
        long turnPoints = PackedScore.turnPoints(pkScore, winningTeam);
        long turnTricks = PackedScore.turnTricks(pkScore, winningTeam);
        if (turnTricks == Jass.TRICKS_PER_TURN - 1) {
            turnPoints += Jass.MATCH_ADDITIONAL_POINTS;
        }
        turnPoints += trickPoints;
        ++turnTricks;
        pkScore &= ~Bits64.mask(shift, 13);
        pkScore |= (turnTricks << shift);
        pkScore |= (turnPoints << (shift + 4));

        assert PackedScore.isValid(pkScore);
        return pkScore;
    }

    /**
     * After each turn, the current points of that turn are
     * added to total for each team, and the points are then reset
     * to 0. This function applies this action to a packed score.
     *
     * @param pkScore the score on which to apply the transformation
     * @return the packed score after the tranformation
     */
    public static long nextTurn(long pkScore) {
        assert PackedScore.isValid(pkScore);

        int points1 = PackedScore.totalPoints(pkScore, TeamId.TEAM_1);
        int points2 = PackedScore.totalPoints(pkScore, TeamId.TEAM_2);
        return PackedScore.pack(0, 0, points1, 0, 0, points2);
    }

    /**
     * Return a string representation of a packed score
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
