package ch.epfl.javass;

public final class Bits32 {
    private Bits32() {
    }

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
        int index = 0;
        int ret = 0;

        ret |= Bits32.packAt(v1, s1, index);
        index += s1;

        ret |= Bits32.packAt(v2, s2, index);

        return ret;
    }

    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        int index = s1 + s2;
        int ret = Bits32.pack(v1, s1, v2 ,s2);

        ret |= Bits32.packAt(v3, s3, index);

        return ret;
    }

    public static int pack(
            int v1, int s1, int v2, int s2, int v3, int s3,
            int v4, int s4, int v5, int s5, int v6, int s6,
            int v7, int s7
    ) {
        int index = s1 + s2 + s3 ;
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
