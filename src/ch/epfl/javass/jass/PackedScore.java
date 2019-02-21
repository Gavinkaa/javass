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
        return Bits64.pack(fstHalf, 32, sndHalf, 32);
    }
    
    public static int turnTricks(long pkScore, TeamId t) {
        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int)Bits64.extract(pkScore, shift, 4);
    }
    
    public static int turnPoints(long pkScore, TeamId t) {
        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int)Bits64.extract(pkScore, shift + 4, 9);
    }
    
    public static int gamePoints(long pkScore, TeamId t) {
        int shift = t == TeamId.TEAM_1 ? 0 : 32;
        return (int)Bits64.extract(pkScore, shift + 13, 11);
    }
    
    public static int totalPoints(long pkScore, TeamId t) {
        return PackedScore.turnPoints(pkScore, t) + PackedScore.gamePoints(pkScore, t);
    }
    
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
        return 0L;
    }
    
    public static long nextTurn(long pkScore) {
        return 0L; 
    }
    
    public static String toString(long pkScore) {
        return "";
    }
}
