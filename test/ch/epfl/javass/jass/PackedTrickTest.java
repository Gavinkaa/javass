package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void isEmptyReturnFalseIfThereIsACard(){
        assertFalse(PackedTrick.isEmpty(0));
    }

    @Test
    void isEmptyReturnTrueIffAllCardsInvalid(){
        int pkTrick = Bits32.mask(0, 24);
        assertTrue(PackedTrick.isEmpty(pkTrick));

        for (int i = 0; i < 4; i++) {
            pkTrick ^= Bits32.mask(i*6, 6);
            assertFalse(PackedTrick.isEmpty(pkTrick));
        }
    }

    @Test
    void isFullReturnTrueIffAllCardsAreThere(){
        int pkTrick = Bits32.mask(0, 24);
        for (int i = 0; i < 4; i++) {
            pkTrick ^= Bits32.mask(i*6, 6);
            assertFalse(PackedTrick.isEmpty(pkTrick));
        }

        assertTrue(PackedTrick.isFull(pkTrick));
    }
}
