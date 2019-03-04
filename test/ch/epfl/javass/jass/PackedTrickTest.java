package ch.epfl.javass.jass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

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
}