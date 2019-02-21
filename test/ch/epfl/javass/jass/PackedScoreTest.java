package ch.epfl.javass.jass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackedScoreTest {
    @Test
    void isValidWorksForAllValidScores() {
        for (int hands = 0; hands <= 9; ++hands) {
            for (int points = 0; points <= 257; ++points) {
                for (int total = 0; total <= 2000; ++total) {
                    long half = (total << 13) | (points << 4) | hands;
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
}