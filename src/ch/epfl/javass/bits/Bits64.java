package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * Provides utility functions for working with packed 64 bit patterns.
 * <p>
 * This is very similar to {@link Bits32}
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 **/
public final class Bits64 {
    private Bits64() {
    }

    /**
     * Create a bit mask with a range of bits set to 1, and the rest to 0
     *
     * @param start the index where the 1 field starts
     * @param size  the size of the 1 mask
     * @return an integer with bits [start;start+size[ all 1, and the rest 0
     */
    public static long mask(int start, int size) {
        Preconditions.checkArgument(0 <= start && 0 <= size && start + size <= Long.SIZE);
        if (size == Long.SIZE) {
            return -1;
        }
        return ((1L << size) - 1) << start;
    }

    /**
     * Extract out `size` bits from `bits`, starting from `start`
     *
     * @param bits  the bits from which to extract a range
     * @param start the index to start extracting from
     * @param size  the number of bits to extract
     * @return a number whose first size bits correspond to the extracted range
     */
    public static long extract(long bits, int start, int size) {
        long m = Bits64.mask(start, size);
        return (bits & m) >>> start;
    }

    /**
     * Pack multiple small numbers into a larger bitpattern
     *
     * @param v1 the first number to pack
     * @param s1 the number of bits in this number to pack
     * @param v2 the second number to pack
     * @param s2 the number of bits in this second number to pack
     * @return an integer corresponding to the concatenation of s1;s2
     * @throws IllegalArgumentException if the packing would exceed the size of an integer
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        int index = 0;
        long ret = 0;

        ret |= Bits64.packAt(v1, s1, index);
        index += s1;

        ret |= Bits64.packAt(v2, s2, index);

        return ret;
    }


    private static long packAt(long v, int s, int index) {
        Preconditions.checkArgument(1 <= s && s < Long.SIZE);
        Preconditions.checkArgument(0 <= index && index + s <= Long.SIZE);
        Preconditions.checkArgument(v >>> s == 0);
        return Bits64.extract(v, 0, s) << index;
    }
}
