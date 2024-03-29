package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits64;

import java.util.StringJoiner;

/**
 * Provides utility methods for working with the binary representation
 * of a set of cards.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class PackedCardSet {
    /**
     * Represents an empty set of Cards
     */
    public static final long EMPTY = 0;
    /**
     * Represents the set containing all possible cards
     */
    public static final long ALL_CARDS = 0x1FF_01FF_01FF_01FFL;

    private static final int COLOR_SIZE = 16;

    private PackedCardSet() {
    }

    /**
     * Return true if the binary representation is valid
     *
     * @param pkCardSet the set for which to check the validity
     * @return true if the representation was valid
     */
    public static boolean isValid(long pkCardSet) {
        return (pkCardSet & (~ALL_CARDS)) == 0;
    }

    private static long betterCards(Card card) {
        int shift = COLOR_SIZE * card.color().ordinal();
        long pattern = 0;
        int spike = 1;
        for (Card.Rank r : Card.Rank.ALL) {
            Card candidate = Card.of(card.color(), r);
            if (candidate.isBetter(card.color(), card)) {
                pattern |= spike;
            }
            spike <<= 1;
        }
        return pattern << shift;
    }

    private static long[] makeBetterPacked() {
        long[] arr = new long[Card.Color.COUNT * Card.Rank.COUNT];
        int i = 0;
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                arr[i++] = betterCards(Card.of(color, rank));
            }
        }
        return arr;
    }

    private static final long[] betterPacked = makeBetterPacked();

    /**
     * Return the set of cards strictly stronger than the given card,
     * under the assumption that that card is a trump card.
     *
     * @param pkCard The card with which to compare
     * @return a set representing all cards that are better
     */
    public static long trumpAbove(int pkCard) {
        assert PackedCard.isValid(pkCard);
        int column = Card.Rank.COUNT * PackedCard.color(pkCard).ordinal();
        int row = PackedCard.rank(pkCard).ordinal();
        return betterPacked[column + row];
    }

    /**
     * Return the representation of a set containing just pkCard,
     * and no other cards
     *
     * @param pkCard the lonely card in the set
     * @return the set with just that card
     */
    public static long singleton(int pkCard) {
        assert PackedCard.isValid(pkCard);

        long pkSet = 1L << pkCard;
        assert isValid(pkSet);
        return pkSet;
    }

    /**
     * Returns true if this represents the empty set
     *
     * @param pkCardSet the representation to check for emptiness
     * @return true if the representation corresponds to the empty set
     */
    public static boolean isEmpty(long pkCardSet) {
        assert isValid(pkCardSet);
        return pkCardSet == PackedCardSet.EMPTY;
    }

    /**
     * Return the number of cards contained in this set
     *
     * @param pkCardSet the representation of the set to count the size of
     */
    public static int size(long pkCardSet) {
        assert isValid(pkCardSet);
        return Long.bitCount(pkCardSet);
    }

    /**
     * Get the ith card, starting from the right, in this set
     *
     * @param pkCardSet the binary representation of the set
     * @param index     the ith card to get
     * @return the packed representation of that card
     */
    public static int get(long pkCardSet, int index) {
        assert isValid(pkCardSet);
        assert index < size(pkCardSet);
        for (int i = 0; i < index; ++i) {
            pkCardSet = pkCardSet ^ Long.lowestOneBit(pkCardSet);
        }
        // The index of the card corresponds exactly with the packed representation
        return Long.numberOfTrailingZeros(pkCardSet);
    }

    /**
     * Insert a new card into this set
     *
     * @param pkCardSet the representation of the into which to insert
     * @param pkCard    the card to insert into the set
     * @return a new set combining the old set and the new card
     */
    public static long add(long pkCardSet, int pkCard) {
        assert isValid(pkCardSet);
        return pkCardSet | PackedCardSet.singleton(pkCard);
    }

    /**
     * Remove a card from a given set.
     *
     * @param pkCardSet the binary representation of the set on which to operate
     * @param pkCard    the card to remove from the set
     * @return a new set where the offending card is absent
     */
    public static long remove(long pkCardSet, int pkCard) {
        assert isValid(pkCardSet);
        return pkCardSet & ~PackedCardSet.singleton(pkCard);
    }

    /**
     * Check if a card is inside a set
     *
     * @param pkCardSet the binary representation of a set in which to check membership
     * @param pkCard    the card to check the membership of
     * @return true if the card is in the set, false otherwise
     */
    public static boolean contains(long pkCardSet, int pkCard) {
        assert isValid(pkCardSet);
        assert PackedCard.isValid(pkCard);
        return (pkCardSet & PackedCardSet.singleton(pkCard)) != 0;
    }

    /**
     * Return the set containing everything but the elements contained in the current set.
     *
     * @param pkCardSet the set of which to take the complement
     * @return the complement of that set
     */
    public static long complement(long pkCardSet) {
        assert isValid(pkCardSet);
        // works only because there are no zeroes in ALL_CARDS
        return PackedCardSet.ALL_CARDS ^ pkCardSet;
    }

    /**
     * Return the union of 2 sets, that is to say, a set containing every element
     * in one or the other, or both.
     *
     * @param pkCardSet1 the first set
     * @param pkCardSet2 the second set
     * @return the union of both sets
     */
    public static long union(long pkCardSet1, long pkCardSet2) {
        assert isValid(pkCardSet1);
        assert isValid(pkCardSet2);

        return pkCardSet1 | pkCardSet2;
    }

    /**
     * Calculate the intersection of 2 sets, i.e., all elements that are in both sets.
     *
     * @return the intersection of the 2 sets given to this method
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
        assert isValid(pkCardSet1);
        assert isValid(pkCardSet2);
        return pkCardSet1 & pkCardSet2;
    }

    /**
     * Calculate the set difference between two sets, that is to say,
     * all the elements that are in the first set but not the second
     *
     * @param pkCardSet1 the set to remove from
     * @param pkCardSet2 the set of elements to remove from the former set
     * @return a set consisting of all elements in the first set but not the second
     */
    public static long difference(long pkCardSet1, long pkCardSet2) {
        assert isValid(pkCardSet1);
        assert isValid(pkCardSet2);
        return pkCardSet1 & ~pkCardSet2;
    }

    // used for subsetOfColor
    private static final long[] COLOR_MASKS = {
            Bits64.mask(0, COLOR_SIZE),
            Bits64.mask(COLOR_SIZE, COLOR_SIZE),
            Bits64.mask(2 * COLOR_SIZE, COLOR_SIZE),
            Bits64.mask(3 * COLOR_SIZE, COLOR_SIZE)
    };

    /**
     * Returns a subset of this set, looking at the cards that are of a certain color,
     * and discarding all the reset
     *
     * @param pkCardSet the representation of the set
     * @param color     the color we're interested in
     * @return a new set with only cards of the given color
     */
    public static long subsetOfColor(long pkCardSet, Card.Color color) {
        assert isValid(pkCardSet);
        return pkCardSet & COLOR_MASKS[color.ordinal()];
    }

    /**
     * Return a string representation of this set.
     */
    public static String toString(long pkCardSet) {
        assert isValid(pkCardSet);
        StringJoiner j = new StringJoiner(",", "{", "}");
        for (int i = 0; i < PackedCardSet.size(pkCardSet); ++i) {
            j.add(PackedCard.toString(PackedCardSet.get(pkCardSet, i)));
        }
        return j.toString();
    }
}
