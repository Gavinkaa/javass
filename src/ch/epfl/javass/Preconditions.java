package ch.epfl.javass;

/**
 * This class provides static methods to check preconditions in functions.
 * This is mainly useful in testing situations.
 */
public final class Preconditions {
    private Preconditions() {}

    /**
     * Check if a condition is true, throwing an IllegalArgumentException
     * if it isn't.
     */
    public static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Check if an index is contained in the range [0;size[,
     * throwing an IndexOutOfBoundsException if this is not the case.
     * @param index the index to verify
     * @param size the exclusive end point of the range
     * @return index if it is in range
     */
    public static int checkIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        } else {
            return index;
        }
    }
}
