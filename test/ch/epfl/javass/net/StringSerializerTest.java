package ch.epfl.javass.net;

import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringSerializerTest {

    @Test
    void serializeIntWorksOnExample() {
        assertEquals("1", StringSerializer.serializeInt(1));
        assertEquals("10", StringSerializer.serializeInt(16));
        assertEquals("20", StringSerializer.serializeInt(32));
        assertEquals("100", StringSerializer.serializeInt(256));
        assertEquals("b", StringSerializer.serializeInt(11));
    }

    @Test
    void deserializeIntWorksOnExample() {
        assertEquals(1, StringSerializer.deserializeInt("1"));
        assertEquals(16, StringSerializer.deserializeInt("10"));
        assertEquals(32, StringSerializer.deserializeInt("20"));
        assertEquals(256, StringSerializer.deserializeInt("100"));
        assertEquals(11, StringSerializer.deserializeInt("b"));
    }

    @Test
    void serializeIntRoundTrips() {
        SplittableRandom rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; ++i) {
            int num = rng.nextInt();
            assertEquals(num, StringSerializer.deserializeInt(StringSerializer.serializeInt(num)));
        }
    }

    @Test
    void serializeLong() {
        //SAME AS INT
        assertEquals("1", StringSerializer.serializeLong(1));
        assertEquals("10", StringSerializer.serializeLong(16));
        assertEquals("20", StringSerializer.serializeLong(32));
        assertEquals("100", StringSerializer.serializeLong(256));
        assertEquals("b", StringSerializer.serializeLong(11));
        //---
        assertEquals("100000000", StringSerializer.serializeLong(0x1_0000_0000L));
        assertEquals("12345678944", StringSerializer.serializeLong(0x12345678944L));
    }

    @Test
    void deserializeLong() {
        //SAME AS INT
        assertEquals(1, StringSerializer.deserializeLong("1"));
        assertEquals(16, StringSerializer.deserializeLong("10"));
        assertEquals(32, StringSerializer.deserializeLong("20"));
        assertEquals(256, StringSerializer.deserializeLong("100"));
        assertEquals(11, StringSerializer.deserializeLong("b"));
        //---
        assertEquals(0x1_0000_0000L, StringSerializer.deserializeLong("100000000"));
        assertEquals(0x12345678944L, StringSerializer.deserializeLong("12345678944"));
    }

    @Test
    void serializeLongRoundTrips() {
        SplittableRandom rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; ++i) {
            long num = rng.nextLong();
            assertEquals(num, StringSerializer.deserializeLong(StringSerializer.serializeLong(num)));
        }
    }

    @Test
    void serializeStringWorksOnExamples() {
        assertEquals("THVkb3ZpYyBCdXJuaWVy", StringSerializer.serializeString("Ludovic Burnier"));
        assertEquals("TMO6Y8OhcyBDcsOtb3N0w7NpciBNZWllcg==", StringSerializer.serializeString("Lúcás Críostóir Meier"));
    }

    @Test
    void deserializeStringWorks() {
        assertEquals("Ludovic Burnier", StringSerializer.deserializeString("THVkb3ZpYyBCdXJuaWVy"));
        assertEquals("Lúcás Críostóir Meier", StringSerializer.deserializeString("TMO6Y8OhcyBDcsOtb3N0w7NpciBNZWllcg=="));
    }

    @Test
    void serializeStringRoundTrips() {
        SplittableRandom rng = TestRandomizer.newRandom();
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; ++i) {
            char[] chars = new char[20];
            for (int j = 0; i < chars.length; ++i) {
                chars[j]  = (char)rng.nextInt(Character.MIN_VALUE, Character.MAX_VALUE);
            }
            String s = new String(chars);
            String encoded = StringSerializer.serializeString(s);
            String decoded = StringSerializer.deserializeString(encoded);
            assertEquals(s, decoded);
        }
    }
}