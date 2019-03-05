package ch.epfl.javass.jass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackedScoreTest {
    @Test
    void isValidWorksForAllValidScores() {
        for (int turnTricks = 0; turnTricks <= 9; ++turnTricks) {
            for (int points = 0; points <= 257; ++points) {
                for (int total = 0; total <= 2000; ++total) {
                    long half = (total << 13) | (points << 4) | turnTricks;
                    long pkScore = (half << 32) | half;
                    assertTrue(PackedScore.isValid(pkScore));
                }
            }
        }
    }

    @Test
    void isValidIsSometimesFalse() {
        for (int shift = 24; shift < 32; ++shift) {
            assertFalse(PackedScore.isValid(1L << shift));
            assertFalse(PackedScore.isValid(1L << (shift + 32)));
        }
    }

    @Test
    void isValidChecksRanges() {
        for (int points = 257; points <= 258; ++points) {
            for (int total = 2000; total <= 2001; ++total) {
                long half = (total << 13) | (points << 4) | 10;
                long pkScore = (half << 32) | half;
                assertFalse(PackedScore.isValid(pkScore));
            }
        }
        long half = (1000 << 13) | (200 << 4) | 10;
        long pkScore = (half << 32) | half;
        assertFalse(PackedScore.isValid(pkScore));
    }

    @Test
    void packedIsValidForTheWholeRange() {
        for (int turnTricks = 0; turnTricks <= 9; ++turnTricks) {
            for (int points = 0; points <= 257; ++points) {
                for (int total = 0; total <= 2000; ++total) {
                    assertTrue(PackedScore.isValid(PackedScore.pack(turnTricks, points, total, turnTricks, points, total)));
                }
            }
        }
    }

    @Test
    void extractorMethodsWorkForTheWholeRange() {
        for (int turnTricks = 0; turnTricks <= 9; ++turnTricks) {
            for (int points = 0; points <= 257; ++points) {
                for (int total = 0; total <= 2000; ++total) {
                    long pkScore = PackedScore.pack(turnTricks, points, total, turnTricks, points, total);
                    assertEquals(turnTricks, PackedScore.turnTricks(pkScore, TeamId.TEAM_1));
                    assertEquals(points, PackedScore.turnPoints(pkScore, TeamId.TEAM_1));
                    assertEquals(total, PackedScore.gamePoints(pkScore, TeamId.TEAM_1));
                    assertEquals(total + points, PackedScore.totalPoints(pkScore, TeamId.TEAM_1));
                    assertEquals(turnTricks, PackedScore.turnTricks(pkScore, TeamId.TEAM_2));
                    assertEquals(points, PackedScore.turnPoints(pkScore, TeamId.TEAM_2));
                    assertEquals(total, PackedScore.gamePoints(pkScore, TeamId.TEAM_2));
                    assertEquals(total + points, PackedScore.totalPoints(pkScore, TeamId.TEAM_2));
                }
            }
        }
    }

    @Test
    void withAdditionalTrickWorksWithTheRange() {
         for (int turnTricks = 0; turnTricks <= 8; ++turnTricks) {
            for (int points = 0; points <= 100; ++points) {
                for (int total = 0; total <= 2000; ++total) {
                    long pkScore = PackedScore.pack(turnTricks, points, total, turnTricks, points, total);
                    int extra = turnTricks == 8 ? 100 : 0;
                    long diff1 = PackedScore.pack(turnTricks + 1, points + extra + 10, total, turnTricks, points, total);
                    long added1 = PackedScore.withAdditionalTrick(pkScore, TeamId.TEAM_1, 10);
                    assertEquals(diff1, added1);
                    long diff2 = PackedScore.pack(turnTricks, points, total, turnTricks + 1, points + extra + 10, total);
                    long added2 = PackedScore.withAdditionalTrick(pkScore, TeamId.TEAM_2, 10);
                    assertEquals(diff2, added2);
                }
            }
        }
    }

    @Test
    void nextTurnClearsOtherFields() {
        long pkScore = PackedScore.pack(1, 1, 1, 2, 2, 2);
        long result = PackedScore.nextTurn(pkScore);
        assertEquals(0, PackedScore.turnTricks(result, TeamId.TEAM_1));
        assertEquals(0, PackedScore.turnPoints(result, TeamId.TEAM_1));
        assertEquals(0, PackedScore.turnTricks(result, TeamId.TEAM_2));
        assertEquals(0, PackedScore.turnPoints(result, TeamId.TEAM_2));
    }

    @Test
    void nextTurnAddsPointsToGamePoints() {
        for (int turnTricks = 0; turnTricks <= 8; ++turnTricks) {
            for (int points = 0; points <= 100; ++points) {
                for (int total = 0; total <= 1000; ++total) {
                    long pkScore = PackedScore.pack(turnTricks, points, total, turnTricks, points, total);
                    long result = PackedScore.nextTurn(pkScore);
                    assertEquals(points + total, PackedScore.gamePoints(result, TeamId.TEAM_1));
                    assertEquals(points + total, PackedScore.gamePoints(result, TeamId.TEAM_2));
                }
            }
        }
    }
}