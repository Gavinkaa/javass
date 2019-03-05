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
                int shift = (new int[]{0, 16, 32, 48})[color.ordinal()];
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
                assertEquals(Long.bitCount(pkSet), 1);
            }
        }
    }

    @Test
    void sizeIsCorrectForAllCards() {
        assertEquals(36, PackedCardSet.size(PackedCardSet.ALL_CARDS));
    }

    @Test
    void getReturnsTheSameCardForSingleton() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long pkSet = PackedCardSet.singleton(pkCard);
                assertEquals(pkCard, PackedCardSet.get(pkSet, 0));
            }
        }
    }

    @Test
    void addingToAnEmptySetIsSingleton() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long added = PackedCardSet.add(PackedCardSet.EMPTY, pkCard);
                assertEquals(PackedCardSet.singleton(pkCard), added);
            }
        }
    }

    @Test
    void addingTwiceIsTheSameAsOnce() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long added = PackedCardSet.add(PackedCardSet.EMPTY, pkCard);
                long addedTwice = PackedCardSet.add(added, pkCard);
                assertEquals(added, addedTwice);
            }
        }
    }

    @Test
    void removingASingletonGivesEmpty() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                assertEquals(PackedCardSet.EMPTY, PackedCardSet.remove(single, pkCard));
            }
        }
    }

    @Test
    void removingTwiceIsSameAsOne() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                long once = PackedCardSet.remove(single, pkCard);
                assertEquals(once, PackedCardSet.remove(once, pkCard));
            }
        }
    }

    @Test
    void singletonContainsItsCard() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                assertTrue(PackedCardSet.contains(single, pkCard));
            }
        }
    }

    @Test
    void complementOfSingleTonContains35() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                long complement = PackedCardSet.complement(single);
                assertEquals(35, PackedCardSet.size(complement));
            }
        }
    }

    @Test
    void complementDoesntContainSingleton() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                long complement = PackedCardSet.complement(single);
                assertFalse(PackedCardSet.contains(complement, pkCard));
            }
        }
    }

    @Test
    void unionOfSetAndComplementIsAll() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                long complement = PackedCardSet.complement(single);
                assertEquals(PackedCardSet.ALL_CARDS, PackedCardSet.union(single, complement));
            }
        }
    }

    @Test
    void intersectionOfSetAndComplementIsEmpty() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                long complement = PackedCardSet.complement(single);
                assertEquals(PackedCardSet.EMPTY, PackedCardSet.intersection(single, complement));
            }
        }
    }

    @Test
    void differenceBetweenSetAndItselfIsEmpty() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                assertEquals(PackedCardSet.EMPTY, PackedCardSet.difference(single, single));
            }
        }
    }

    @Test
    void subsetOfColorIsTheSameForSingletonOfSameColor() {
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                int pkCard = Card.of(color, rank).packed();
                long single = PackedCardSet.singleton(pkCard);
                assertEquals(single, PackedCardSet.subsetOfColor(single, color));
            }
        }
    }

    @Test
    void toStringReturnsTheRightThing() {
        long s = PackedCardSet.EMPTY;
        int c1 = PackedCard.pack(Card.Color.HEART, Card.Rank.SIX);
        int c2 = PackedCard.pack(Card.Color.SPADE, Card.Rank.ACE);
        int c3 = PackedCard.pack(Card.Color.SPADE, Card.Rank.SIX);
        s = PackedCardSet.add(s, c1);
        s = PackedCardSet.add(s, c2);
        s = PackedCardSet.add(s, c3);
        assertEquals("{♠6,♠A,♥6}", PackedCardSet.toString(s));
    }
}