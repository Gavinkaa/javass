package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;

/**
 * Contains utility functions for working with a single trick,
 * i.e., a round of gameplay where each player has played one of their cards.
 * Each packed representation contains each of the 4 cards the player
 * put down,
 */
public final class PackedTrick {
    /**
     * Represents a completely invalid packed representation
     */
    public static final int INVALID = -1;

    private PackedTrick() {
    }

    /**
     * Check if the packed representation trick is valid
     * @return true if the packed representation is valid
     */
    public static boolean isValid(int packedTrick) {
        // Check if the cards are correct
        boolean foundValid = false;
        for (int i = 3; i >= 0; --i) {
            int pkCard = Bits32.extract(packedTrick, i * 6, 6);
            if (foundValid && !PackedCard.isValid(pkCard)) {
                return false;
            }
            if (PackedCard.isValid(pkCard)) {
                foundValid = true;
            }
        }
        int index = Bits32.extract(packedTrick, 24, 4);
        if (index > 8) {
            return false;
        }
        return true;
    }
}
