package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;

/**
 * This class provides static utility methods to work
 * with a binary representations of cards in Jass
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class PackedCard {
    public static final int INVALID = 0b111111;

    private PackedCard() {
    }

    /**
     * Check the validity of the binary representation.
     * In our representation, only the first portion of bits are utilised.
     * Any other bits being set is an indicator of the pattern not being respected.
     *
     * @param pkCard the bit pattern to check
     * @return true if the pattern is valid, false otherwise.
     */
    public static boolean isValid(int pkCard) {
        int rank = Bits32.extract(pkCard, 0, 4);
        if (rank > 8) {
            return false;
        }
        int zeroed = Bits32.extract(pkCard, 6, 26);
        return zeroed == 0;
    }

    /**
     * Created a packed representation of a card based on key information.
     *
     * @param c the color of the card
     * @param r the rank of the card
     * @return a bit pattern encoding this card
     */
    public static int pack(Card.Color c, Card.Rank r) {
        return Bits32.pack(r.ordinal(), 4, c.ordinal(), 2);
    }

    /**
     * Extract out the color from a packed card pattern
     *
     * @param pkCard the encoded card
     * @return the color of that card
     */
    public static Card.Color color(int pkCard) {
        assert isValid(pkCard);

        return Card.Color.ALL.get(Bits32.extract(pkCard, 4, 2));
    }

    /**
     * Extract out the rank of an encoded card.
     *
     * @param pkCard the encoded card
     * @return the rank of that card
     */
    public static Card.Rank rank(int pkCard) {
        assert isValid(pkCard);

        return Card.Rank.ALL.get(Bits32.extract(pkCard, 0, 4));
    }

    /**
     * Check if one card is better than another, depending
     * on which color is currently the trump color.
     *
     * @param trump   the trump color currently in play
     * @param pkCardL the card to check the superiority of
     * @param pkCardR the card to compare against
     * @return true if pkCardL is better than pkCardR
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        assert isValid(pkCardL);
        assert isValid(pkCardR);

        boolean leftTrump = color(pkCardL) == trump;
        boolean rightTrump = color(pkCardR) == trump;
        if (leftTrump && !rightTrump) {
            return true;
        }
        if (!leftTrump && rightTrump) {
            return false;
        }
        if (leftTrump && rightTrump) {
            return rank(pkCardL).trumpOrdinal() > rank(pkCardR).trumpOrdinal();
        }
        if (color(pkCardL) == color(pkCardR)) {
            return rank(pkCardL).ordinal() > rank(pkCardR).ordinal();
        }
        return false;
    }

    /**
     * Calculate the value of a card, based on the current trump color.
     *
     * @param trump  the current trump color
     * @param pkCard the binary encoded card
     * @return the number of points this card is currently worth
     */
    public static int points(Card.Color trump, int pkCard) {
        assert isValid(pkCard);

        Card.Color color = color(pkCard);
        boolean isTrump = color == trump;
        switch (rank(pkCard)) {
            case SIX:
                return 0;
            case SEVEN:
                return 0;
            case EIGHT:
                return 0;
            case NINE:
                return isTrump ? 14 : 0;
            case TEN:
                return 10;
            case JACK:
                return isTrump ? 20 : 2;
            case QUEEN:
                return 3;
            case KING:
                return 4;
            case ACE:
                return 11;
        }
        // Unreachable
        throw new RuntimeException("Unreachable code, invalid Rank");
    }

    /**
     * Calculate a string representation of this card.
     *
     * @param pkCard the binary representation to get the string of
     * @return a string representing that card
     */
    public static String toString(int pkCard) {
        assert isValid(pkCard);

        return color(pkCard).toString() + rank(pkCard).toString();
    }
}
