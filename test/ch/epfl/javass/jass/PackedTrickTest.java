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
}