package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

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
     * @param trump the trump color for the turn
     * @param score the score of the game so far
     * @param firstPlayer the first player to play
     * @return the initial TurnState
     */
    public TurnState initial(Card.Color trump, Score score, PlayerId firstPlayer){
        return new TurnState(score.packed(), PackedCardSet.ALL_CARDS, PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * Return a TurnState filled with the given components,
     * this method can be used at any point in the game
     * @param pkScore the score at this point, packed
     * @param pkUnplayedCards the cards that have not be played yet, packed
     * @param pkTrick the state of the ongoing trick, packed
     * @return the TurnState with the current information
     * @throws IllegalArgumentException if any of the packed components are invalid
     */
    public TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick){
        Preconditions.checkArgument(PackedScore.isValid(pkScore));
        Preconditions.checkArgument(PackedCardSet.isValid(pkUnplayedCards));
        Preconditions.checkArgument(PackedTrick.isValid(pkTrick));

        return new TurnState(pkScore, pkUnplayedCards, pkTrick);
    }

    /**
     * @return the packed version of the score
     */
    public long packedScore() {
        return pkTrick;
    }

    /**
     * @return the packed version of the unplayed cards
     */
    public long packedUnplayedCards() {
        return pkUnplayedCard;
    }

    /**
     * @return the packed version of the trick
     */
    public int packedTrick() {
        return pkTrick;
    }

    /**
     * @return the current score for this turn
     */
    public Score score(){
        return Score.ofPacked(pkScore);
    }

    /**
     * @return the set of unplayed cards of this turn
     */
    public CardSet unplayedCards(){
        return CardSet.ofPacked(pkUnplayedCard);
    }

    /**
     * @return the trick of this turn
     */
    public Trick trick(){
        return Trick.ofPacked(pkTrick);
    }

    /**
     * @return true if the
     */
    public boolean isTerminal(){
        return PackedTrick.isLast(pkTrick);
    }

}
