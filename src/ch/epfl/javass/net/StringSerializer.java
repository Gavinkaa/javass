package ch.epfl.javass.net;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class StringSerializer {
    private static final int RADIX = 16;
    private StringSerializer() {
    }

    /**
     * Serialize a number using the base 16 representation
     *
     * @param num the number to serialize
     * @return the serialized number
     */
    public static String serializeInt(int num) {
        return Integer.toUnsignedString(num, RADIX);
    }

    /**
     * Deserialize a number from the base 16 representation
     * this is the opposite of {@link #serializeInt(int)}
     *
     * @param num the text to deserialize
     * @return the deserialized number
     */
    public static int deserializeInt(String num) {
        return Integer.parseUnsignedInt(num, RADIX);
    }

    /**
     * Similar to {@link #serializeInt(int)} except it can operate on longer numbers
     */
    public static String serializeLong(long num) {
        return Long.toUnsignedString(num, RADIX);
    }

    /**
     * Similar to {@link #deserializeInt(String)} except it can operate on longer numbers
     */
    public static long deserializeLong(String num) {
        return Long.parseUnsignedLong(num, RADIX);
    }
}
