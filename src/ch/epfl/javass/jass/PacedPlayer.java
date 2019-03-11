package ch.epfl.javass.jass;

/**
 * This can be used to wrap around another player,
 * and ensure that their decisions always take a certain time.
 */
public final class PacedPlayer implements Player {
    private final Player underlyingPlayer;
    private final double minTime;

    public PacedPlayer(Player underlyingPlayer, double minTime) {
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = minTime;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long start = System.currentTimeMillis();
        Card card = underlyingPlayer.cardToPlay(state, hand);
        long now = System.currentTimeMillis();
        long milliDiff = now - start;
        double diff = ((double)milliDiff) / 1000;
        if (diff < minTime) {
            try {
                Thread.sleep(milliDiff);
            } catch (InterruptedException e) { /* ignore */ }
        }
        return card;
    }
}
