package ch.epfl.javass.jass;

import java.util.Map;

// BONUS METHODS:
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

    // BONUS METHOD
    /**
     * Make this player choose a new trump color.
     *
     * In a true game of Jass, trumps are chosen by the player that
     * starts that turn, instead of randomly. This augments a player with the ability
     * to choose which trump they want for that turn.
     *
     * @param hand the hand the player has at that point
     * @param canDelegate whether or not the player can delegate the choice to its teammate
     * @return the color the player decides to be the new trump
     */
    Card.Color chooseTrump(CardSet hand, boolean canDelegate);

    /**
     * This method should be called at the start of a turn, to let a player make announces.
     *
     * An announce allows a player to attempt to gain points for their team by showing
     * a specific set of cards to the other players.
     *
     * @param hand the player's hand
     * @return a set containing the cards the player wants to announce.
     */
    default CardSet announce(CardSet hand) {
        return CardSet.EMPTY;
    }

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

    /**
     * This is used to inform the player of a final round of announces.
     *
     * @param announces the announces different players have made
     * @param winner the team that won this round of announces
     */
    default void setAnnounce(Map<PlayerId, CardSet> announces, TeamId winner) {
    }
}
