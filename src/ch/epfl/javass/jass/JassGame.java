package ch.epfl.javass.jass;

import java.util.*;

/**
 * Represents a Game of Jass, capable of
 * keeping track of and advancing a game of Jass
 */
public final class JassGame {
    private final Map<PlayerId, Player> players;
    private final Map<PlayerId, String> playerNames;
    private final Random shuffleRng;
    private final Random trumpRng;
    // State
    // This can be null, indicating that we have yet to start the game
    private TurnState turnState;
    // The interior cardsets are mutable
    private final Map<PlayerId, CardSet> playerHands;
    private final List<Card> deck;


    public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.playerHands = new EnumMap<>(PlayerId.class);
        for (PlayerId id : PlayerId.ALL) {
            playerHands.put(id, CardSet.EMPTY);
        }
        this.trumpRng = new Random(rng.nextLong());
        this.deck = new ArrayList<>(Card.Color.COUNT * Card.Rank.COUNT);
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                this.deck.add(Card.of(color, rank));
            }
        }
        this.turnState = null;
    }


    private Card.Color nextTrump() {
        return Card.Color.ALL.get(trumpRng.nextInt(Card.Color.COUNT));
    }

    private void shuffleDeck() {
        Collections.shuffle(deck, shuffleRng);
    }

    private void initializeHands() {
        shuffleDeck();
        int i = 0;
        for (PlayerId id : PlayerId.values()) {
            int next_i = i + Jass.HAND_SIZE;
            playerHands.put(id, CardSet.of(deck.subList(i, next_i)));
            i = next_i;
        }
    }

    private PlayerId firstPlayerBySeven() {
        for (PlayerId id : PlayerId.values()) {
            if (playerHands.get(id).contains(Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN))) {
                return id;
            }
        }
        throw new Error("Unreachable code");
    }

    /**
     * @return true if we've reached the end of the game, i.e., someone has reached 1000+ points
     */
    public boolean isGameOver() {
        if (turnState == null) {
            return false;
        }
        for (TeamId id : TeamId.ALL) {
            if (turnState.score().totalPoints(id) >= Jass.WINNING_POINTS)  {
                return true;
            }
        }
        return false;
    }

    public void advanceToEndOfNextTrick() {
        if (isGameOver()) {
            return;
        }
        initializeHands();
        if (turnState == null) {
            turnState = TurnState.initial(nextTrump(), Score.INITIAL, firstPlayerBySeven());
        }
        while (!turnState.trick().isFull()) {
            PlayerId nextId = turnState.nextPlayer();
            Player next = players.get(nextId);
            Card choice = next.cardToPlay(turnState, playerHands.get(nextId));
            turnState = turnState.withNewCardPlayed(choice);
        }
    }
}
