package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;


public final class PackedScore {
    /**
     * The initial value of a packed Score
     */
    public static long INITIAL = 0;

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

    public static boolean isValid(long pkScore) {
        return isValidHalf(pkScore, 0) && isValidHalf(pkScore, 32);
    }

    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2, int gamePoints2) {
        long fstHalf = Bits32.pack(turnTricks1, 4, turnPoints1, 9, gamePoints1, 11);
        long sndHalf = Bits32.pack(turnTricks2, 4, turnPoints2, 9, gamePoints2, 11);
        long pkScore = Bits64.pack(fstHalf, 32, sndHalf, 32);
        assert PackedScore.isValid(pkScore);
        return pkScore;
    }

    public static int turnTricks(long pkScore, TeamId t) {
        assert isValid(pkScore);

        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int) Bits64.extract(pkScore, shift, 4);
    }

    public static int turnPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int) Bits64.extract(pkScore, shift + 4, 9);
    }

    public static int gamePoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int) Bits64.extract(pkScore, shift + 13, 11);
    }

    public static int totalPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        return PackedScore.turnPoints(pkScore, t) + PackedScore.gamePoints(pkScore, t);
    }

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
