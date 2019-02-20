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
}
