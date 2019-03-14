package ch.epfl.javass.jass;

import java.util.*;

/**
 * Represents a Game of Jass, capable of
 * keeping track of and advancing a game of Jass
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
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
    private PlayerId lastTurnStarter;
    private boolean gameOver;


    public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));
        for (PlayerId id : PlayerId.ALL) {
            this.players.get(id).setPlayers(id, this.playerNames);
        }
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.playerHands = new EnumMap<>(PlayerId.class);
        for (PlayerId id : PlayerId.ALL) {
            setHand(id, CardSet.EMPTY);
        }
        this.trumpRng = new Random(rng.nextLong());
        this.deck = new ArrayList<>(Card.Color.COUNT * Card.Rank.COUNT);
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                this.deck.add(Card.of(color, rank));
            }
        }
        this.turnState = null;
        this.lastTurnStarter = null;
        this.gameOver = false;
    }

    private Card.Color nextTrump() {
        Card.Color nextTrump = Card.Color.ALL.get(trumpRng.nextInt(Card.Color.COUNT));
        for (Player player : players.values()) {
            player.setTrump(nextTrump);
        }
        return nextTrump;
    }

    private void shuffleDeck() {
        Collections.shuffle(deck, shuffleRng);
    }

    private void setHand(PlayerId id, CardSet hand) {
        playerHands.put(id, hand);
        players.get(id).updateHand(hand);
    }

    private void initializeHands() {
        shuffleDeck();
        int i = 0;
        for (PlayerId id : PlayerId.values()) {
            int next_i = i + Jass.HAND_SIZE;
            setHand(id, CardSet.of(deck.subList(i, next_i)));
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

    private void initializeTurnState() {
        initializeHands();
        if (lastTurnStarter == null) {
            lastTurnStarter = firstPlayerBySeven();
        } else {
            lastTurnStarter = PlayerId.ALL.get((lastTurnStarter.ordinal() + 1) % 4);
        }
        Score score = turnState == null ? Score.INITIAL : turnState.score().nextTurn();
        turnState = TurnState.initial(nextTrump(), score, lastTurnStarter);
        informOfScore();
    }

    private void informOfTrick() {
        for (Player player : players.values()) {
            player.updateTrick(turnState.trick());
        }
    }

    private void informOfScore() {
        for (Player player : players.values()) {
            player.updateScore(turnState.score());
        }
    }

    /**
     * @return true if we've reached the end of the game, i.e., someone has reached 1000+ points
     */
    public boolean isGameOver() {
        if (gameOver) {
            return true;
        }
        if (turnState == null) {
            return false;
        }
        for (TeamId id : TeamId.ALL) {
            if (turnState.score().totalPoints(id) >= Jass.WINNING_POINTS) {
                gameOver = true;

                for(Player player: players.values()){
                    player.setWinningTeam(id);
                }

                return true;
            }
        }
        return false;
    }

    /**
     * This collect the current trick, and advance the game
     * up until the end of the next trick.
     * This can be used after first initialising the game,
     * in which case it will play out the first trick of the first turn.
     * This will also automatically advance turns as well.
     */
    public void advanceToEndOfNextTrick() {
        if (isGameOver()) {
            return;
        }
        if (turnState == null) {
            initializeTurnState();
            informOfTrick();
        }
        if (turnState.trick().isFull()) {
            boolean wasTerminal = turnState.isTerminal();
            turnState = turnState.withTrickCollected();
            informOfScore();
            if (wasTerminal) {
                initializeTurnState();
            }
            informOfTrick();
        }
        while (!turnState.trick().isFull()) {
            PlayerId nextId = turnState.nextPlayer();
            Player next = players.get(nextId);
            Card choice = next.cardToPlay(turnState, playerHands.get(nextId));
            turnState = turnState.withNewCardPlayed(choice);
            informOfTrick();
        }
    }
}
