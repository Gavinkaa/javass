package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single Card in the game of Jass.
 * Cards are uniquely determined by their Color and Rank.
 */
public final class Card {
    /**
     * Represents one of the Colors a card can have
     */
    public enum Color {
        SPADE("♠"),
        HEART("♥"),
        DIAMOND("♦"),
        CLUB("♣");

        private String symbol;

        Color(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Holds all the values of the this enumeration
         */
        public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(Color.values()));
        /**
         * The number of colors available in this enumeration
         */
        public static final int COUNT = ALL.size();

        @Override
        public String toString() {
            return symbol;
        }
    }

    /**
     * Represents one of the ranks a card can have
     */
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

        /**
         * Holds all the possible Ranks
         */
        public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(Rank.values()));
        /**
         * Holds the number of different Ranks
         */
        public static final int COUNT = ALL.size();

        @Override
        public String toString() {
            return repr;
        }

        /**
         * The ordering of Ranks changes depending on whether or not the Color
         * is the trump color, so this ordering should be used in that case.
         * Otherwise, Enum.ordinal suffices.
         * @return the special ordinal for the trump color
         */
        public int trumpOrdinal() {
            return trumpOrdinal;
        }
    }

    private final int packed;

    private Card(int packed) {
        this.packed = packed;
    }

    /**
     * Create a new Card with a certain rank and color
     * @param c the color the card will have
     * @param r the rank the card will have
     * @return a new Card with the specified properties
     */
    public static Card of(Color c, Rank r) {
        return new Card(PackedCard.pack(c, r));
    }

    /**
     * Create a new card from a packed representation
     * @param packed the binary representation of the card to create
     * @throws IllegalArgumentException if the representation is invalid
     * @return a new card matching the passed representation
     */
    public static Card ofPacked(int packed) {
        Preconditions.checkArgument(PackedCard.isValid(packed));
        return new Card(packed);
    }

    /**
     * See this card in binary form.
     * @return the packed representation of this card
     */
    public int packed() {
        return packed;
    }

    /**
     * @return the color of this card
     */
    public Color color() {
        return PackedCard.color(packed);
    }

    /**
     * @return the rank of this card
     */
    public Rank rank() {
        return PackedCard.rank(packed);
    }

    /**
     * Check if this card is better than that one.
     * This depends on what Color is currently the trump color.
     * @param trump which color is currently the trump color
     * @param that the other card with which to compare
     * @return true if this card beats the other. false otherwise
     */
    public boolean isBetter(Color trump, Card that) {
        return PackedCard.isBetter(trump, packed, that.packed);
    }

    /**
     * Return the number of points this card is worth, which
     * depends on the current trump card.
     * @param trump the current trump color
     * @return an integer tallying the points this card is worth
     */
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
        return "Card(" + PackedCard.toString(packed) + ")";
    }
}
