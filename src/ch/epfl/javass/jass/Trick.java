package ch.epfl.javass.jass;

/**
 * Represents a Trick in the game of Jass,
 * a round of the game where each player takes turns
 * laying cards down one at a time.
 *
 * This class stores each of the cards as they're played,
 * using Card.INVALID to represent the slots of cards that have yet to
 * be played.
 * This class also holds the trump card for that round, as well as the
 * first player to play that round.
 */
public final class Trick {
    private final int packed;

    private Trick(int packed) {
        this.packed = packed;
    }

    public static final Trick INVALID = new Trick(PackedTrick.INVALID);

    /**
     * Construct a new empty with the given trump color and first player
     * @return a new Trick with that trump color and player
     */
    public static Trick firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * Construct a new trick from a packed representation
     * @return a new trick with the same attributes as that representation
     * @throws IllegalArgumentException if the packed representation wasn't valid
     */
    public static Trick ofPacked(int packed) {
        if (!PackedTrick.isValid(packed)) {
            throw new IllegalArgumentException("Packed trick is invalid");
        }
        return new Trick(packed);
    }

    /**
     * Get the packed representation of this Trick as an integer
     * @return the binary representation of this Trick
     */
    public int packed() {
        return packed;
    }

    /**
     * Check if this Trick is empty, i.e.,
     * no player has put down a card yet
     * @return true if this Trick is empty
     */
    public boolean isEmpty() {
        return PackedTrick.isEmpty(packed);
    }

    /**
     * Check if this Trick is full, i.e,
     * every player has put down a card.
     * Note that this method is not the same as !isEmpty()
     * since when a trick has some cards played, but not
     * all 4, then it is neither full nor empty
     * @return true if every player has put down a card in this trick
     */
    public boolean isFull() {
        return PackedTrick.isFull(packed);
    }

    /**
     * At the end of each Trick, this method should be called to
     * return the state of the Trick at the beginning of the next round
     * of play.
     * This method clear out all cards, increments the index,
     * and sets the starting player to the player who won this Trick
     * @throws IllegalStateException if the trick wasn't full
     */
    public Trick nextEmpty() {
        if (!isFull()) {
            throw new IllegalStateException("Trick must be full to call nextEmpty()");
        }
        // TODO: maybe have an exception instead of returning invalid
        return new Trick(PackedTrick.nextEmpty(packed));
    }

    /**
     * Check if this trick is the last one in a round,
     * this is useful to handle transitioning logic between rounds.
     * @return true if this trick is the last one in the round, based on index
     */
    public boolean isLast() {
        return PackedTrick.isLast(packed);
    }

    /**
     * Return the size of this trick, i.e.,
     * the number of cards that have been played this turn.
     * @return the number of cards played this turn.
     */
    public int size() {
        return PackedTrick.size(packed);
    }

    /**
     * @return the trump color for this Trick.
     */
    public Card.Color trump() {
        return PackedTrick.trump(packed);
    }

    /**
     * Return the 0 based index of this trick, i.e.,
     * what number trick is this in a round of 9 tricks
     * @return the index of this trick
     */
    public int index() {
        return PackedTrick.index(packed);
    }

    /**
     * Get the player playing for the indexth slot
     * in the trick (0 based)
     * @param index the index of the player to get
     * @return the player that plays at that index
     * @throws IndexOutOfBoundsException if the index is not in [0;4[
     */
    public PlayerId player(int index) {
        if (index < 0 || index >= 4) {
            throw new IndexOutOfBoundsException("Index was not in [0;4[");
        }
        return PackedTrick.player(packed, index);
    }

    /**
     * Return the card played at a certain index.
     * @param index in the range [0;size()[
     * @return the card that has been played at that index
     * @throws IndexOutOfBoundsException if the index was outside the valid range
     */
    public Card card(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index was not in [0;size()[");
        }
        return Card.ofPacked(PackedTrick.card(packed, index));
    }

    /**
     * This is the main way to add a card to a Trick, simulating
     * what happens when a player puts down a card.
     * @param c the card to add to the Trick
     * @return a new Trick with the card added to it, if possible
     * @throws IllegalStateException if the card couldn't be added to the Trick
     */
    public Trick withAddedCard(Card c) {
        if (isFull()) {
            throw new IllegalStateException("withAddedCard called on a full Trick");
        }
        return new Trick(PackedTrick.withAddedCard(packed, c.packed()));
    }

    /**
     * The color of the first card played is important in scoring the trick,
     * and players must try and follow that color.
     * @return the color of the first card played, if it exists
     * @throws IllegalStateException if this trick is empty, and no cards have been played
     */
    public Card.Color baseColor() {
        if (isEmpty()) {
            throw new IllegalStateException("baseColor() called on an empty Trick");
        }
        return PackedTrick.baseColor(packed);
    }

    /**
     * This method is useful to let a player calculate what subset of their hand
     * is playable based on the state of the Trick. If the Trick is full,
     * this method can do no useful work, and throws an exception.
     * @param hand The set of cards the player has in hand
     * @return the subset of cards they can play.
     * @throws IllegalStateException if the Trick is full, and no more cards are to be played
     */
    public CardSet playableCards(CardSet hand) {
        if (isFull()) {
            throw new IllegalStateException("playableCards() called on a full Trick");
        }
        return CardSet.ofPacked(PackedTrick.playableCards(packed, hand.packed()));
    }

    /**
     * Calculate the value of this Trick, by tallying up the value of each card,
     * and adding an extra bonus of this is the last Trick of a round.
     * @return the number of points this Trick is worth
     */
    public int points() {
        // TODO: Figure out what to do if the Trick isn't full, an assertion will fail beneath us
        return PackedTrick.points(packed);
    }

    /**
     * Calculate the player winning this Trick so far.
     * It's possible to call this method before the end of the Trick
     * @return the player with the best card so far
     * @throws IllegalStateException if the Trick is empty
     */
    public PlayerId winningPlayer() {
        if (isEmpty()) {
            throw new IllegalStateException("winningPlayer() called on empty Trick");
        }
        return PackedTrick.winningPlayer(packed);
    }
}
