package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Card {
    public enum Color {
        SPADE("♠"),
        HEART("♥"),
        DIAMOND("♦"),
        CLUB("♣");

        private String symbol;

        Color(String symbol) {
            this.symbol = symbol;
        }

        public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(Color.values()));
        public static final int COUNT = ALL.size();

        @Override
        public String toString() {
            return symbol;
        }
    }

    public enum Rank {
        SIX("6", 0),
        SEVEN("7", 1),
        EIGHT("8", 2),
        NINE("9", 7),
        TEN("10", 3),
        JACK("J", 8),
        QUEEN("Q", 4),
        KING("K", 5),
        ACE("A", 6);

        private String repr;
        private int trumpOrdinal;

        Rank(String repr, int trumpOrdinal) {
            this.repr = repr;
            this.trumpOrdinal = trumpOrdinal;
        }

        public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(Color.values()));
        public static final int COUNT = ALL.size();

        @Override
        public String toString() {
            return repr;
        }

        int trumpOrdinal() {
            return trumpOrdinal;
        }
    }
}
