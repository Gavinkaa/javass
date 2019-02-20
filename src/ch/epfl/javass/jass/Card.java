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

        public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(Rank.values()));
        public static final int COUNT = ALL.size();

        @Override
        public String toString() {
            return repr;
        }

        public int trumpOrdinal() {
            return trumpOrdinal;
        }
    }

    private final int packed;

    private Card(Color c, Rank r) {
        packed = PackedCard.pack(c, r);
    }

    private Card(int packed) {
        this.packed = packed;
    }

    public static Card of(Color c, Rank r) {
        return new Card(c, r);
    }

    public static Card ofPacked(int packed) {
        if (!PackedCard.isValid(packed)) {
            throw new IllegalArgumentException("The packed card isn't valid");
        }
        return new Card(packed);
    }

    public int packed() {
        return packed;
    }

    public Color color() {
        return PackedCard.color(packed);
    }

    public Rank rank() {
        return PackedCard.rank(packed);
    }

    public boolean isBetter(Color trump, Card that) {
        return PackedCard.isBetter(trump, packed, that.packed);
    }

    public int points(Color trump) {
        return PackedCard.points(trump, packed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return packed == card.packed;
    }

    @Override
    public int hashCode() {
        return packed;
    }

    @Override
    public String toString() {
        return PackedCard.toString(packed);
    }
}
