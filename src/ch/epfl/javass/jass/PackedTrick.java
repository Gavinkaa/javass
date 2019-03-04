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
    public static boolean isValid(int pkTrick) {
        assert PackedTrick.isValid(pkTrick);

        // Check if the cards are correct
        boolean foundValid = false;
        for (int i = 3; i >= 0; --i) {
            int pkCard = Bits32.extract(pkTrick, i * 6, 6);
            if (foundValid && !PackedCard.isValid(pkCard)) {
                return false;
            }
            if (PackedCard.isValid(pkCard)) {
                foundValid = true;
            }
        }
        int index = Bits32.extract(pkTrick, 24, 4);
        if (index > 8) {
            return false;
        }
        return true;
    }

    /**
     * Construct a packed representation of a trick with no played cards,
     * and the given trump and firstPlayer.
     * @param trump the current trump color
     * @param firstPlayer the first player to play this turn
     */
    public static int firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        return Bits32.pack(
                PackedCard.INVALID, 6,
                PackedCard.INVALID, 6,
                PackedCard.INVALID, 6,
                PackedCard.INVALID, 6,
                0, 4,
                firstPlayer.ordinal(), 2,
                trump.ordinal(), 2
        );
    }

    /**
     * Return the next trick after clearing all the cards and incrementing the index.
     * If the index is at the last one, this returns INVALID instead
     * @param pkTrick the packed representation to use
     * @return the empty trick right after this one
     */
    public static int nextEmpty(int pkTrick) {
        assert PackedTrick.isValid(pkTrick);

        int nextIndex = Bits32.extract(pkTrick, 24, 4) + 1;
        if (nextIndex > 8) {
            return PackedTrick.INVALID;
        }
        Card.Color trump = Card.Color.ALL.get(Bits32.extract(pkTrick, 30, 2));
        int winningIndex = 0;
        int winningCard = Bits32.extract(pkTrick, 0, 6);
        for (int i = 1; i <= 3; ++i) {
            int card = Bits32.extract(pkTrick, i * 6, 6);
            if (PackedCard.isBetter(trump, card, winningCard)) {
                winningCard = card;
                winningIndex = i;
            }
        }
        PlayerId winningPlayer = PlayerId.ALL.get((Bits32.extract(pkTrick, 28, 2) + winningIndex) % 4);
        // Index is 0 so this works
        return firstEmpty(trump, winningPlayer) | (nextIndex << 24);
    }

    /**
     * @return true if this is the last trick of a turn
     */
    public static boolean isLast(int pkTrick) {
        assert PackedTrick.isValid(pkTrick);

        return Bits32.extract(pkTrick, 24, 4) == 8;
    }
}
