package ch.epfl.javass.jass;

import java.util.List;

/**
 * Represents a set of cards, can be used to represent a hand
 */
public final class CardSet {

    /**
     * Represents a set of no cards
     */
    public static final CardSet EMPTY = new CardSet(PackedCardSet.EMPTY);

    /**
     * Represents the set of all cards
     */
    public static final CardSet ALL_CARDS = new CardSet(PackedCardSet.ALL_CARDS);


    private final long packed;

    private CardSet(long packed) {
        this.packed = packed;
    }

    /**
     * Make a new set of cards form a list a cards to include
     * @param cards the list of cards to include
     * @return the new set containing just the cards in the list
     */
    public static CardSet of(List<Card> cards){
        long packed = PackedCardSet.EMPTY;

        for (Card card: cards) {
            packed = PackedCardSet.add(packed, card.packed());
        }
        return new CardSet(packed);
    }


    /**
     * Make a new set of cards from a binary representation to use
     * @param packed the binary representation
     * @return a set matching with the binary representation
     * @throws IllegalArgumentException: if the representation isn't valid
     */
    public static CardSet ofPacked(long packed){
        if(!PackedCardSet.isValid(packed)){
            throw new IllegalArgumentException("Binary representation of the packed card set wasn't valid");
        }
        return new CardSet(packed);
    }

    /**
     * @return the binary representation of this card set
     */
    public long packed(){
        return packed;
    }

    /**
     * @return true if this is the empty set
     */
    public boolean isEmpty(){
        return PackedCardSet.isEmpty(packed);
    }

    /**
     * @return the number of cards contained in this set
     */
    public int size(){
        return PackedCardSet.size(packed);
    }

    /**
     * Returns the ith card in this set according to the canonical ordering of cards: color and then rank
     * @param index the index to fetch
     * @return the ith card
     */
    public Card get(int index){
        return Card.ofPacked(PackedCardSet.get(packed, index));
    }

    /**
     * Return a new set with a new card in it
     * @param card the new card to add to this set
     * @return a new set containing that card and all the other cards of this set
     */
    public CardSet add(Card card){
        return CardSet.ofPacked(PackedCardSet.add(packed, card.packed()));
    }

    /**
     * Return a new set without a card in it
     * @param card the card to remove to this set
     * @return a new set all the other cards of this set except that card
     */
    public CardSet remove(Card card){
        return CardSet.ofPacked(PackedCardSet.remove(packed, card.packed()));
    }

    /**
     * Check if a card is inside this set
     * @param card the card of which to check memberchip
     * @return true if the card is in the set, false otherwise
     */
    public boolean contains(Card card){
        return PackedCardSet.contains(packed, card.packed());
    }

    /**
     * Return a new set containing everything but the elements contained in the current set.
     * @return the complement of this set
     */
    public CardSet complement(){
        return CardSet.ofPacked(PackedCardSet.complement(packed));
    }

    /**
     * Return the union of the given set and this, that is to say, a set containing every element
     * in this or that, or both.
     *
     * @param that the set with which to union
     * @return the union of both sets
     */
    public CardSet union(CardSet that){
        return CardSet.ofPacked(PackedCardSet.union(this.packed, that.packed));
    }

    /**
     * Calculate the intersection of the given set and this, i.e., all elements that are in both sets.
     * @return the intersection of that set and this set
     */
    public CardSet intersection(CardSet that){
        return CardSet.ofPacked(PackedCardSet.intersection(this.packed, that.packed));
    }

    public CardSet difference(CardSet that){
        return CardSet.ofPacked(PackedCardSet.difference(this.packed, that.packed));
    }

    public CardSet subsetOfColor(Card.Color color){
        return CardSet.ofPacked(PackedCardSet.subsetOfColor(packed, color));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardSet cardSet = (CardSet) o;
        return packed == cardSet.packed;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(packed);
    }

    @Override
    public String toString() {
        return "CardSet(" + PackedCardSet.toString(packed) + ")";
    }
}
