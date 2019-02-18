package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public final class PackedCardTest {
    private static Card.Color[] getAllColors() {
        return new Card.Color[] {
                Card.Color.SPADE,
                Card.Color.HEART,
                Card.Color.DIAMOND,
                Card.Color.CLUB
        };
    }

    private static Card.Rank[] getAllRanks() {
        return new Card.Rank[] {
                Card.Rank.SIX,
                Card.Rank.SEVEN,
                Card.Rank.EIGHT,
                Card.Rank.NINE,
                Card.Rank.TEN,
                Card.Rank.JACK,
                Card.Rank.QUEEN,
                Card.Rank.KING,
                Card.Rank.ACE,
        };
    }

    private static Card.Rank[] getAllRanksTrumpOrdered() {
        return new Card.Rank[] {
                Card.Rank.SIX,
                Card.Rank.SEVEN,
                Card.Rank.EIGHT,
                Card.Rank.TEN,
                Card.Rank.QUEEN,
                Card.Rank.KING,
                Card.Rank.ACE,
                Card.Rank.NINE,
                Card.Rank.JACK,
        };
    }

    @Test
    void isValidWorksForAllValidCards() {
        for (int c = 0; c < 4; ++c) {
            for (int r = 0; r < 9; ++r) {
                assertTrue(PackedCard.isValid(c << 4 | r));
            }
        }
    }

    @Test
    void isValidWorksForSomeInvalidCards() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(16);
            int rest = (r <= 8) ? rng.nextInt(1, 1 << 26) : 0;
            int invalidCard = (((rest << 2) | c) << 4) | r;
            assertFalse(PackedCard.isValid(invalidCard));
        }
    }

    @Test
    void packAndColorAndRankWork() throws Exception {
        for (Card.Color c: getAllColors()) {
            for (Card.Rank r: getAllRanks()) {
                int pkCard = PackedCard.pack(c, r);
                assertEquals(c, PackedCard.color(pkCard));
                assertEquals(r, PackedCard.rank(pkCard));
            }
        }
    }

    @Test
    void isBetterWorksWithTrumpAndNonTrumpCards() throws Exception {
        for (Card.Color trump: getAllColors()) {
            for (Card.Rank trumpRank: getAllRanks()) {
                int pkTrumpCard = PackedCard.pack(trump, trumpRank);
                for (Card.Color c: getAllColors()) {
                    if (c == trump)
                        continue;
                    for (Card.Rank r: getAllRanks()) {
                        int pkCard = PackedCard.pack(c, r);
                        assertTrue(PackedCard.isBetter(trump, pkTrumpCard, pkCard));
                        assertFalse(PackedCard.isBetter(trump, pkCard, pkTrumpCard));
                    }
                }
            }
        }
    }

    @Test
    void isBetterWorksWithTrumpCards() throws Exception {
        Card.Rank[] trumpRanks = getAllRanksTrumpOrdered();
        for (Card.Color trump: getAllColors()) {
            for (int i = 0; i < trumpRanks.length - 1; ++i) {
                int pkCardLow = PackedCard.pack(trump, trumpRanks[i]);
                for (int j = i + 1; j < trumpRanks.length; ++j) {
                    int pkCardHigh = PackedCard.pack(trump, trumpRanks[j]);
                    assertTrue(PackedCard.isBetter(trump, pkCardHigh, pkCardLow));
                    assertFalse(PackedCard.isBetter(trump, pkCardLow, pkCardHigh));
                }
            }
        }
    }

    @Test
    void isBetterWorksWitNonTrumpCards() throws Exception {
        Card.Rank[] nonTrumpRanks = getAllRanks();
        for (Card.Color trump: getAllColors()) {
            for (Card.Color c: getAllColors()) {
                if (c == trump)
                    continue;
                for (int i = 0; i < nonTrumpRanks.length - 1; ++i) {
                    int pkCardLow = PackedCard.pack(c, nonTrumpRanks[i]);
                    for (int j = i + 1; j < nonTrumpRanks.length; ++j) {
                        int pkCardHigh = PackedCard.pack(c, nonTrumpRanks[j]);
                        assertTrue(PackedCard.isBetter(trump, pkCardHigh, pkCardLow));
                        assertFalse(PackedCard.isBetter(trump, pkCardLow, pkCardHigh));
                    }
                }
            }
        }
    }

    @Test
    void isBetterWorksWithIncomparableCards() throws Exception {
        for (Card.Color c1: getAllColors()) {
            for (Card.Color c2: getAllColors()) {
                if (c2 == c1)
                    continue;
                Card.Color trump = null;
                for (Card.Color t: getAllColors()) {
                    if (t != c1 && t != c2) {
                        trump = t;
                        break;
                    }
                }
                for (Card.Rank r1: getAllRanks()) {
                    int pkCard1 = PackedCard.pack(c1, r1);
                    for (Card.Rank r2: getAllRanks()) {
                        int pkCard2 = PackedCard.pack(c2, r2);
                        assertFalse(PackedCard.isBetter(trump, pkCard1, pkCard2));
                        assertFalse(PackedCard.isBetter(trump, pkCard2, pkCard1));
                    }
                }
            }
        }
    }

    @Test
    void pointsSumTo152() throws Exception {
        for (Card.Color trump: getAllColors()) {
            int s = 0;
            for (Card.Color c: getAllColors()) {
                for (Card.Rank r: getAllRanks()) {
                    s += PackedCard.points(trump, PackedCard.pack(c, r));
                }
            }
            assertEquals(152, s);
        }
    }
}
