package ch.epfl.javass;

public final class Bits32 {
    private Bits32() {}

    public static int mask(int start, int size) {
        Preconditions.checkArgument(0 <= start && start <= Integer.SIZE - size);
        Preconditions.checkArgument(0 <= size && size <= Integer.SIZE - start);
         if (size == 32) {
             return -1;
         }
        return ((1 << size) - 1) << start;
    }

    public static int extract(int bits, int start, int size) {
        int m = Bits32.mask(start, size);
        return (bits & m) >>> start;
    }

    public static int pack(int v1, int s1, int v2, int s2) {
        return 0;
    }

    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        return 0;
    }

    public static int pack(
            int v1, int s1, int v2, int s2, int v3, int s3,
            int v4, int s4, int v5, int s5, int v6, int s6,
            int v7, int s7
    ) {
        return 0;
    }
}
