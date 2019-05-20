package ch.epfl.javass.jass;

import java.util.Random;

/**
 * RandomPlayer will randomly choose a card to play each turn.
 * <p>
 * This is mainly useful for testing the correct execution of a game.
 * </p>
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RandomPlayer implements Player {
    private final Random rng;

    /**
     * Construct a new RandomPlayer with a given seed
     */
    public RandomPlayer(long rngSeed) {
        this.rng = new Random(rngSeed);
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        CardSet playable = state.trick().playableCards(hand);
        return playable.get(this.rng.nextInt(playable.size()));
    }
}
