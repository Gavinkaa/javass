package ch.epfl.javass.jass;

import java.util.Map;

/**
 * This can be used to wrap around another player,
 * and ensure that their decisions always take a certain time.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class PacedPlayer implements Player {
    private final Player underlyingPlayer;
    private final double minTime;

    /**
     * Construct a new PacedPlayer over another player
     *
     * @param underlyingPlayer the other player to wrap
     * @param minTime          the minimum time the player should take to make decisions
     */
    public PacedPlayer(Player underlyingPlayer, double minTime) {
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = minTime;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long start = System.currentTimeMillis();
        Card card = this.underlyingPlayer.cardToPlay(state, hand);
        long now = System.currentTimeMillis();
        long milliDiff = now - start;
        double diff = ((double) milliDiff) / 1000;
        if (diff < this.minTime) {
            try {
                Thread.sleep((long) ((this.minTime - diff) * 1000));
            } catch (InterruptedException e) { /* ignore */ }
        }
        return card;
    }

    @Override
    public Card.Color chooseTrump(CardSet hand, boolean canDelegate) {
        return underlyingPlayer.chooseTrump(hand, canDelegate);
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.underlyingPlayer.setPlayers(ownId, playerNames);
    }

    @Override
    public void updateHand(CardSet newHand) {
        this.underlyingPlayer.updateHand(newHand);
    }

    @Override
    public void setTrump(Card.Color trump) {
        this.underlyingPlayer.setTrump(trump);
    }

    @Override
    public void updateTrick(Trick newTrick) {
        this.underlyingPlayer.updateTrick(newTrick);
    }

    @Override
    public void updateScore(Score score) {
        this.underlyingPlayer.updateScore(score);
    }

    @Override
    public void setWinningTeam(TeamId winningTeam)
    {
        this.underlyingPlayer.setWinningTeam(winningTeam);
    }

    @Override
    public CardSet announce(CardSet hand) {
        return this.underlyingPlayer.announce(hand);
    }

    @Override
    public void setAnnounce(Map<PlayerId, CardSet> announces, TeamId winner) {
        this.underlyingPlayer.setAnnounce(announces, winner);
    }
}
