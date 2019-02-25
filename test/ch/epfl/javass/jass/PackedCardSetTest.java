package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackedCardSetTest {
    @Test
    void isValidReturnsFalseWhenBitsAreStrownAround() {
        for (int i = 9; i < 16; ++i) {
            for (int j = 0; j < 4; ++j) {
                long bad = 1L << (i + 16 * j);
                assertFalse(PackedCardSet.isValid(bad));
            }
        }
    }

    @Test
    void trumpAboveIsValidForAllCards() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long pkSet = PackedCardSet.trumpAbove(pkCard);
                assertTrue(PackedCardSet.isValid(pkSet));
            }
        }
    }

    @Test
    void trumpAboveOnlyContainsBetterCards() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                Card us = Card.of(color, rank);
                long pkSet = PackedCardSet.trumpAbove(us.packed());
                int shift = (new int[]{ 0, 16, 32, 48 })[color.ordinal()];
                for (int i = 0; i < 16; ++i) {
                    if ((pkSet & (1L << (i + shift))) != 0) {
                        Card supposedlyBetter = Card.of(color, Card.Rank.ALL.get(i));
                        assertTrue(supposedlyBetter.isBetter(color, us));
                    }
                }
            }
        }
    }

    @Test
    void trumpAboveReturnsNoCardsOfaDifferentColor() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long pkSet = PackedCardSet.trumpAbove(pkCard);
                int shift = (new int[]{0, 16, 32, 48})[color.ordinal()];
                long notOurColor = pkSet & ~Bits64.mask(shift, 16);
                assertEquals(0, notOurColor);
            }
        }
    }

    @Test
    void eachSingletonIsBiggerThanTheLast() {
        long last = PackedCardSet.EMPTY;
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                long now = PackedCardSet.singleton(Card.of(color, rank).packed());
                assertTrue(now > last);
                last = now;
            }
        }
    }

    @Test
    void singletonContainsOnlyASingle1() {
         for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                long pkSet = PackedCardSet.singleton(Card.of(color, rank).packed());
                int count = 0;
                for (int i = 0; i < 64; ++i) {
                    count += Bits64.extract(pkSet, i, 1);
                }
                assertEquals(count, 1);
            }
        }
    }
}