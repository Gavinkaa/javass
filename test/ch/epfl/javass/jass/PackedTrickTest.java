package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackedTrickTest {
    @Test
    void isValidIsFalseForINVALID() {
        assertFalse(PackedTrick.isValid(PackedTrick.INVALID));
    }

    @Test
    void isValidIsFalseForInvalidCards() {
        int packed = 0;
        for (int i = 0; i < 3; ++i) {
            assertFalse(PackedTrick.isValid(packed | (PackedCard.INVALID << (i * 6))));
        }
    }

    @Test
    void isValidIsFalseWhenIndexisTooBig() {
        for (int i = 9; i < 16; ++i) {
            assertFalse(PackedTrick.isValid(i << 24));
        }
    }

    @Test
    void firstEmptyIsAlwaysValid() {
        for (Card.Color trump : Card.Color.ALL) {
            for (PlayerId player : PlayerId.ALL) {
                int first = PackedTrick.firstEmpty(trump, player);
                assertTrue(PackedTrick.isValid(first));
            }
        }
    }

    @Test
    void firstEmptyHasTheRightTrumpAndPlayer() {
        for (Card.Color trump : Card.Color.ALL) {
            for (PlayerId player : PlayerId.ALL) {
                int first = PackedTrick.firstEmpty(trump, player);
                assertEquals(trump.ordinal(), Bits32.extract(first, 30, 2));
                assertEquals(player.ordinal(), Bits32.extract(first, 28, 2));
            }
        }
    }

    @Test
    void firstEmptyIsNotLast() {
        int first = PackedTrick.firstEmpty(Card.Color.SPADE, PlayerId.PLAYER_1);
        assertFalse(PackedTrick.isLast(first));
    }

    @Test
    void isLastIstrueIfIndexIs8() {
        int last = 8 << 24;
        assertTrue(PackedTrick.isLast(last));
    }

    @Test
    void nextEmptyReturnsInvalidIfIndexIsTooBig() {
        int trick = 8 << 24;
        assertEquals(PackedTrick.INVALID, PackedTrick.nextEmpty(trick));
    }

    @Test
    void nextEmptyIncrementsIndex() {
        for (int i = 0; i < 8; ++i) {
            int extract = Bits32.extract(PackedTrick.nextEmpty(i << 24), 24, 4);
            assertEquals(i + 1, extract);
        }
    }

    @Test
    void isEmptyReturnFalseIfThereIsACard() {
        assertFalse(PackedTrick.isEmpty(0));
    }

    @Test
    void isEmptyReturnTrueIffAllCardsInvalid() {
        int pkTrick = Bits32.mask(0, 24);
        assertTrue(PackedTrick.isEmpty(pkTrick));

        for (int i = 0; i < 4; i++) {
            pkTrick ^= Bits32.mask(i * 6, 6);
            assertFalse(PackedTrick.isEmpty(pkTrick));
        }
    }

    @Test
    void isFullReturnTrueIffAllCardsAreThere() {
        int pkTrick = Bits32.mask(0, 24);
        for (int i = 0; i < 4; i++) {
            pkTrick ^= Bits32.mask(i * 6, 6);
            assertFalse(PackedTrick.isEmpty(pkTrick));
        }

        assertTrue(PackedTrick.isFull(pkTrick));
    }

    @Test
    void sizeReturnTheCorrectSize() {
        int pkTrick = Bits32.mask(0, 24);
        for (int i = 0; i < 4; i++) {
            pkTrick ^= Bits32.mask(i * 6, 6);
            assertEquals(i + 1, PackedTrick.size(pkTrick));
        }
    }

    @Test
    void withAddedCardSizeIncreased() {
        int pkTrick = PackedTrick.firstEmpty(Card.Color.SPADE, PlayerId.PLAYER_1);
        for (int i = 0; i < 4; i++) {
            assertEquals(i, PackedTrick.size(pkTrick));
            pkTrick = PackedTrick.withAddedCard(pkTrick, 0);
        }
    }

    @Test
    void baseColorReturnsTheColorOfFirstCard() {
        int pkTrick = 0;
        assertEquals(Card.Color.SPADE, PackedTrick.baseColor(pkTrick));
    }

    private int addAllCards(int pkTrick, Card card1, Card card2, Card card3) {
        pkTrick = PackedTrick.withAddedCard(pkTrick, card1.packed());
        pkTrick = PackedTrick.withAddedCard(pkTrick, card2.packed());
        pkTrick = PackedTrick.withAddedCard(pkTrick, card3.packed());
        return pkTrick;
    }

    @Test
    void playableCardsWorksOnSomeExamples() {
        int trick1 = addAllCards(
                PackedTrick.firstEmpty(Card.Color.HEART, PlayerId.PLAYER_1),
                Card.of(Card.Color.HEART, Card.Rank.SIX),
                Card.of(Card.Color.SPADE, Card.Rank.SEVEN),
                Card.of(Card.Color.HEART, Card.Rank.TEN)
        );
        CardSet hand1 = CardSet.EMPTY;
        hand1.add(Card.of(Card.Color.HEART, Card.Rank.JACK));
        assertEquals(hand1.packed(), PackedTrick.playableCards(trick1, hand1.packed()));
        hand1.add(Card.of(Card.Color.SPADE, Card.Rank.SIX));
        assertEquals(hand1.packed(), PackedTrick.playableCards(trick1, hand1.packed()));
        hand1.add(Card.of(Card.Color.HEART, Card.Rank.EIGHT));
        CardSet target = CardSet.EMPTY;
        target.add(Card.of(Card.Color.HEART, Card.Rank.EIGHT));
        target.add(Card.of(Card.Color.HEART, Card.Rank.JACK));
        assertEquals(target.packed(), PackedTrick.playableCards(trick1, hand1.packed()));
    }

    @Test
    void addingAllCardsGivesUs157() {
        int total = 0;
        int i = 0;
        int pkTrick = PackedTrick.firstEmpty(Card.Color.HEART, PlayerId.PLAYER_1);
        for (Card.Color c : Card.Color.ALL) {
            for (Card.Rank r : Card.Rank.ALL) {
                Card card = Card.of(c, r);
                pkTrick = PackedTrick.withAddedCard(pkTrick, card.packed());
                ++i;
                if (i == 4) {
                    total += PackedTrick.points(pkTrick);
                    pkTrick = PackedTrick.nextEmpty(pkTrick);
                    i = 0;
                }
            }
        }
        assertEquals(157, total);
    }

    @Test
    void winningPlayerWorksOnSomeExamples() {
         int trick1 = addAllCards(
                PackedTrick.firstEmpty(Card.Color.HEART, PlayerId.PLAYER_3),
                Card.of(Card.Color.HEART, Card.Rank.SIX),
                Card.of(Card.Color.SPADE, Card.Rank.SEVEN),
                Card.of(Card.Color.HEART, Card.Rank.TEN)
         );
         assertEquals(PlayerId.PLAYER_1, PackedTrick.winningPlayer(trick1));
    }
}
