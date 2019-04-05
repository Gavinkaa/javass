// Javass stage 1

package ch.epfl.test;

import java.util.SplittableRandom;

public final class TestRandomizer {
    // Fix random seed to guarantee reproducibility.
    public final static long SEED = 2019;

    public final static int RANDOM_ITERATIONS = 1_000;

    public static SplittableRandom newRandom() {
        return new SplittableRandom(SEED);
    }

    public static char randomChar(SplittableRandom rng) {
        // so that we have a single byte
        return (char) rng.nextInt(Character.MIN_VALUE, 0xD7FF);
    }

    public static String randomString(SplittableRandom rng, int length) {
        char[] chars = new char[length];
        for (int j = 0; j < chars.length; ++j) {
            chars[j] = randomChar(rng);
        }
        return new String(chars);
    }

}
