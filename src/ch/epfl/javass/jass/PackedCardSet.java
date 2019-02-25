package ch.epfl.javass.jass;

/**
 * Provides utility methods for working with the binary representation
 * of a set of cards.
 */
public final class PackedCardSet {
    public static final long EMPTY = 0;
    public static final long ALL_CARDS = 0b1111_1111_0000000_1111_1111_0000000_1111_1111_0000000_1111_1111L;

    private PackedCardSet() {
    }

    public static boolean isValid(long pkCardSet) {
        return false;
    }

    long trumpAbove(int pkCard) {
        return 0L;
    }

    public static long singleton(int pkCard) {
        return 0;
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

    }
}
