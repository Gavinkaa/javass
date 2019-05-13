package ch.epfl.javass.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * StringSerializer contains utilities for working with the serialization
 * of various things to and from Strings. It provides utilities for converting
 * anything into a valid ASCII string via Base64, as well as converting ints and longs.
 *
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

    /**
     * This will take a string, take its UTF-8 bytes,
     * and then return the Base64 encoding of those bytes.
     * Even though we return a Java String, which is stored internally
     * as UTF-16, all the characters composing that string are
     * ASCII.
     *
     * @param str the string to serialize
     * @return the base64 of the string's UTF-8 bytes
     */
    public static String serializeString(String str) {
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(utf8);
    }

    /**
     * This is the opposite of {@link #serializeString(String)}.
     * It takes a Base64 encoded string, decodes it
     * into UTF-8 bytes, and then transcribes those into a Java String
     *
     * @param serialized the base64 encoded String
     * @return a String (UTF-16) containining the decoded text.
     */
    public static String deserializeString(String serialized) {
        byte[] utf8 = Base64.getDecoder().decode(serialized);
        return new String(utf8, StandardCharsets.UTF_8);
    }

    /**
     * This is useful to concatenate a bunch of strings together.
     * Note that this won't do any serialization for you, the other
     * methods in the class exist for that purpose.
     * Care must be taken to ensure that the seperator is not inside
     * any of the strings.
     *
     * @param sep     the separation character to use
     * @param strings the strings to collate together
     * @return a new string containing all the strings interseperated by sep
     */
    public static String combine(char sep, String... strings) {
        return String.join(String.valueOf(sep), strings);
    }

    /**
     * This does the opposite of {@link #combine(char, String...)}.
     * Like that method, it also does no deserialization of Strings.
     *
     * @param sep      the seperation character between Strings
     * @param combined the collated Strings to split
     * @return a
     */
    public static String[] split(char sep, String combined) {
        return combined.split(String.valueOf(sep));
    }
}
