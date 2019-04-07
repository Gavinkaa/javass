package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * This class provides utility functions for working with 32 bits.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class Bits32 {
    private Bits32() {
    }

    /**
     * Create a bit mask with a range of bits set to 1, and the rest to 0
     *
     * @param start the index where the 1 field starts
     * @param size  the size of the 1 mask
     * @return an integer with bits [start;start+size[ all 1, and the rest 0
     */
    public static int mask(int start, int size) {
        Preconditions.checkArgument(0 <= start && 0 <= size && start + size <= Integer.SIZE);
        if (size == Integer.SIZE) {
            return -1;
        }
        return ((1 << size) - 1) << start;
    }

    /**
     * Extract out `size` bits from `bits`, starting from `start`
     *
     * @param bits  the bits from which to extract a range
     * @param start the index to start extracting from
     * @param size  the number of bits to extract
     * @return a number whose first size bits correspond to the extracted range
     */
    public static int extract(int bits, int start, int size) {
        int m = Bits32.mask(start, size);
        return (bits & m) >>> start;
    }

    /**
     * Pack multiple small numbers into a larger bit-pattern
     *
     * @param v1 the first number to pack
     * @param s1 the number of bits in this number to pack
     * @param v2 the second number to pack
     * @param s2 the number of bits in this second number to pack
     * @return an integer corresponding to the concatenation of s1;s2
     * @throws IllegalArgumentException if the packing would exceed the size of an integer
     */
    public static int pack(int v1, int s1, int v2, int s2) {
        int index = 0;
        int ret = 0;

        ret |= Bits32.packAt(v1, s1, index);
        index += s1;

        ret |= Bits32.packAt(v2, s2, index);

        return ret;
    }

    /**
     * See the other version of {@link #pack(int, int, int, int)}
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        int index = s1 + s2;
        int ret = Bits32.pack(v1, s1, v2, s2);

        ret |= Bits32.packAt(v3, s3, index);

        return ret;
    }

    /**
     * See the other versions of `pack`
     */
    public static int pack(
            int v1, int s1, int v2, int s2, int v3, int s3,
            int v4, int s4, int v5, int s5, int v6, int s6,
            int v7, int s7
    ) {
        int index = s1 + s2 + s3;
        int ret = Bits32.pack(v1, s1, v2, s2, v3, s3);

        ret |= Bits32.packAt(v4, s4, index);
        index += s4;

        ret |= Bits32.packAt(v5, s5, index);
        index += s5;

        ret |= Bits32.packAt(v6, s6, index);
        index += s6;

        ret |= Bits32.packAt(v7, s7, index);
        return ret;
    }

    private static int packAt(int v, int s, int index) {
        Preconditions.checkArgument(1 <= s && s < Integer.SIZE);
        Preconditions.checkArgument(0 <= index && index + s <= Integer.SIZE);
        Preconditions.checkArgument(v >>> s == 0);
        return Bits32.extract(v, 0, s) << index;
    }
}
