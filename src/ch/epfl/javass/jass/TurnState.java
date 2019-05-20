package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;


/**
 * TurnState represents the state of a single turn.
 * <p>
 * Each turn is composed of 9 tricks.
 * This class provides methods to advance the state of the turn
 * by playing a card inside a trick, or collecting a full trick.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class TurnState {
    private final long pkScore;
    private final long pkUnplayedCard;
    private final int pkTrick;

    private TurnState(long pkScore, long pkUnplayedCard, int pkTrick) {
        this.pkScore = pkScore;
        this.pkUnplayedCard = pkUnplayedCard;
        this.pkTrick = pkTrick;
    }

    /**
     * Return the TurnState of the beginning of a turn
     *
     * @param trump       the trump color for the turn
     * @param score       the score of the game so far
     * @param firstPlayer the first player to play
     * @return the initial TurnState
     */
    public static TurnState initial(Card.Color trump, Score score, PlayerId firstPlayer) {
        return new TurnState(score.packed(), PackedCardSet.ALL_CARDS, PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * Return a TurnState filled with the given components,
     * this method can be used at any point in the game
     *
     * @param pkScore         the score at this point, packed
     * @param pkUnplayedCards the cards that have not be played yet, packed
     * @param pkTrick         the state of the ongoing trick, packed
     * @return the TurnState with the current information
     * @throws IllegalArgumentException if any of the packed components are invalid
     */
    public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick) {
        Preconditions.checkArgument(PackedScore.isValid(pkScore));
        Preconditions.checkArgument(PackedCardSet.isValid(pkUnplayedCards));
        Preconditions.checkArgument(PackedTrick.isValid(pkTrick));

        return new TurnState(pkScore, pkUnplayedCards, pkTrick);
    }

    /**
     * @return the packed version of the score
     */
    public long packedScore() {
        return this.pkScore;
    }

    /**
     * @return the packed version of the unplayed cards
     */
    public long packedUnplayedCards() {
        return this.pkUnplayedCard;
    }

    /**
     * @return the packed version of the trick
     */
    public int packedTrick() {
        return this.pkTrick;
    }

    /**
     * @return the current score for this turn
     */
    public Score score() {
        return Score.ofPacked(this.pkScore);
    }

    /**
     * @return the set of unplayed cards of this turn
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(this.pkUnplayedCard);
    }

    /**
     * @return the trick of this turn
     */
    public Trick trick() {
        return Trick.ofPacked(this.pkTrick);
    }

    /**
     * @return true if the last trick has been played and collected
     */
    public boolean isTerminal() {
        return this.pkTrick == PackedTrick.INVALID;
    }

    /**
     * @return the id of the next player that need to play
     * @throws IllegalStateException if the trick is full
     */
    public PlayerId nextPlayer() {
        checkTrickNotFull();

        return PackedTrick.player(this.pkTrick, PackedTrick.size(this.pkTrick));
    }

    /**
     * @param card the card to be played
     * @return a new TurnState after having played that card
     * @throws IllegalStateException if the trick is full
     */
    public TurnState withNewCardPlayed(Card card) {
        checkTrickNotFull();

        if (!unplayedCards().contains(card)) {
            throw new IllegalStateException("the card has already been played");
        }

        long newUnplayedCards = PackedCardSet.remove(this.pkUnplayedCard, card.packed());
        int newTrick = PackedTrick.withAddedCard(this.pkTrick, card.packed());

        return new TurnState(this.pkScore, newUnplayedCards, newTrick);
    }

    /**
     * Return a new TurnState after having collected
     * up the cards for a given trick and moved on to the subsequent one
     *
     * @return the next TurnState after this trick has been completed
     * @throws IllegalStateException if the trick isn't full
     */
    public TurnState withTrickCollected() {
        if (!PackedTrick.isFull(this.pkTrick)) {
            throw new IllegalStateException("the trick isn't full");
        }
        TeamId winningTeam = PackedTrick.winningPlayer(this.pkTrick).team();
        int trickPoints = PackedTrick.points(this.pkTrick);
        long newPkScore = PackedScore.withAdditionalTrick(this.pkScore, winningTeam, trickPoints);
        int newTrick = PackedTrick.nextEmpty(this.pkTrick);

        return new TurnState(newPkScore, this.pkUnplayedCard, newTrick);
    }

    /**
     * This combines playing a card and collecting up a trick.
     * If after playing the card, the trick isn't full, this method
     * has no further effect.
     *
     * @param card the card to play
     * @return the state of the TurnState after applying one or both operations
     * @throws IllegalStateException if the trick is full, and we can't add a card
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
        checkTrickNotFull();
        TurnState withCard = withNewCardPlayed(card);

        if (PackedTrick.isFull(withCard.packedTrick())) {
            withCard = withCard.withTrickCollected();
        }

        return withCard;
    }

    private void checkTrickNotFull() {
        if (PackedTrick.isFull(this.pkTrick)) {
            throw new IllegalStateException("the trick was full");
        }
    }


}
