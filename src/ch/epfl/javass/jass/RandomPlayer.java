package ch.epfl.javass.jass;

import java.util.Random;

public final class RandomPlayer implements Player {
    private final Random rng;

    public RandomPlayer(long rngSeed) {
        this.rng = new Random(rngSeed);
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        CardSet playable = state.trick().playableCards(hand);
        return playable.get(rng.nextInt(playable.size()));
    }
}
