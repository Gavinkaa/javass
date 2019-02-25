package ch.epfl.javass.jass;

/**
 * Provides utility methods for working with the binary representation
 * of a set of cards.
 */
public final class PackedCardSet {
    /**
     * Represents an empty set of Cards
     */
    public static final long EMPTY = 0;
    /**
     * Represents the set containing all possible cards
     */
    public static final long ALL_CARDS = 0b1_1111_1111_0000_0001_1111_1111_0000_0001_1111_1111_0000_0001_1111_1111L;

    private PackedCardSet() {
    }

    /**
     * Return true if the binary representation is valid
     * @param pkCardSet the set for which to check the validity
     * @return true if the representation was valid
     */
    public static boolean isValid(long pkCardSet) {
        return (pkCardSet & (~ALL_CARDS)) == 0;
    }

    // Used for trumpAbove
    private static long[] betterPacked = {
            0b1_1111_1110, // for 6
            0b1_1111_1100, // for 7
            0b1_1111_1000, // for 8
            0b0_0010_0000, // for 9
            0b1_1110_0000, // for 10
            0b0_0000_0000, // for J
            0b1_1010_1000, // for Q
            0b1_0010_1000, // for K
            0b0_0010_1000, // for A
    };

    /**
     * Return the set of cards strictly stronger than the given card,
     * under the assumption that that card is a trump card.
     * @param pkCard The card with which to compare
     * @return a set representing all cards that are better
     */
    public static long trumpAbove(int pkCard) {
        long pattern = betterPacked[PackedCard.rank(pkCard).ordinal()];
        // A switch might be prettier, but this makes sure we have a jump table
        return pattern << (PackedCard.color(pkCard).ordinal() * 16);
    }

    /**
     * Return the representation of a set containing just pkCard,
     * and no other cards
     * @param pkCard the lonely card in the set
     * @return the set with just that card
     */
    public static long singleton(int pkCard) {
        assert PackedCard.isValid(pkCard);

        int colorShift = PackedCard.color(pkCard).ordinal() * 16;
        int rankShift = PackedCard.rank(pkCard).ordinal();
        long pkSet = 1L << (colorShift + rankShift);
        assert isValid(pkSet);
        return pkSet;
    }

    public static boolean isEmpty(long pkCardSet) {
        return false;
    }

    public static int size(long pkCardSet) {
        return 0;
    }

    public static int get(long pkCardSet, int index) {
        return 0;
    }

    public static long add(long pkCardSet, int pkCard) {
        return 0;
    }

    public static long remove(long pkCardSet, int pkCard) {
        return 0;
    }

    public static boolean contains(long pkCardSet, int pkCard) {
        return false;
    }

    public static long complement(long pkCardSet) {
        return 0;
    }

    public static long union(long pkCardSet1, long pkCardSet2) {
        return 0;
    }

    public static long intersection(long pkcCardSet1, long pkCardSet2) {
        return 0;
    }

    public static long difference(long pkCardSet1, long pkCardSet2) {
        return 0;
    }

    public static long subsetOfColor(long pkCardSet, Card.Color color) {
        return 0;
    }

    public static String toString(long pkCardSet) {
        return "I don't care";
    }
}
