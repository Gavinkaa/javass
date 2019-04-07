package ch.epfl.javass.jass;

import java.util.Map;

/**
 * Represents an autonomous player that can decide
 * on which card to play, given the state of the game.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public interface Player {
    /**
     * Have the player decide on a card to play, given the current state of the turn,
     * and the card the player has in its hand.
     *
     * @param state the state of the turn
     * @param hand  the set of cards the player has
     * @return the card the player has decided to play
     */
    Card cardToPlay(TurnState state, CardSet hand);

    /**
     * This method should be called once at the beginning of a game,
     * to inform the player of which role he has, and what names the
     * other players have
     *
     * @param ownId       the id of this player
     * @param playerNames a map from ids to the names of each player
     */
    default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
    }

    /**
     * This method is used to inform the player of a change in its hand
     *
     * @param newHand the new hand the player has
     */
    default void updateHand(CardSet newHand) {
    }

    /**
     * This is used to inform the player of a change in the current trump color
     *
     * @param trump the new trump color
     */
    default void setTrump(Card.Color trump) {
    }

    /**
     * This is used to inform the player of a change in trick
     *
     * @param newTrick the new trick the player should be informed of
     */
    default void updateTrick(Trick newTrick) {
    }

    /**
     * This is used to inform the player of a change in game score
     *
     * @param score the new score for the game
     */
    default void updateScore(Score score) {
    }

    /**
     * This is used to inform the player of a change in winning team.
     * This method is only called once, when the game ends as one team wins
     *
     * @param winningTeam the team that has won the game
     */
    default void setWinningTeam(TeamId winningTeam) {
    }
}
