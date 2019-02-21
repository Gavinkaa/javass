package ch.epfl.javass.jass;

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
       return 0l; 
    }
    
    public static int turnTricks(long pkScore, TeamId t) {
        return 0;
    }
    
    int turnPoints(long pkScore, TeamId t) {
        return 0;
    }
    
    int gamePoints(long pkScore, TeamId t) {
        return 0;
    }
    
    public static int totalPoints(long pkScore, TeamId t) {
        return 0;
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
