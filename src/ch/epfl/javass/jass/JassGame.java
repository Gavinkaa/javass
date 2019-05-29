package ch.epfl.javass.jass;

import java.util.*;

/**
 * Represents a Game of Jass, capable of
 * keeping track of and advancing the state of the game
 * as players take their turns.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class JassGame {
    private final Map<PlayerId, Player> players;
    private final Map<PlayerId, String> playerNames;
    private final Random shuffleRng;
    // State
    // This can be null, indicating that we have yet to start the game
    private TurnState turnState;
    // The interior cardsets are mutable
    private final Map<PlayerId, CardSet> playerHands = new EnumMap<>(PlayerId.class);
    private PlayerId lastTurnStarter;
    private boolean gameOver;
    // Whether or not this is the first trick of a given turn
    // we need to keep track of this to handle announces.
    private boolean firstTrick = false;


    public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));
        for (PlayerId id : PlayerId.ALL) {
            this.players.get(id).setPlayers(id, this.playerNames);
        }
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
    }

    private Card.Color nextTrump(PlayerId chooser) {
        Card.Color trump = players.get(chooser).chooseTrump(playerHands.get(chooser), true);
        if (trump == null) {
            PlayerId mate = PlayerId.ALL.get((chooser.ordinal() + 2) % 4);
            trump = players.get(mate).chooseTrump(playerHands.get(mate), false);
        }
        for (Player player : players.values()) {
            player.setTrump(trump);
        }
        return trump;
    }

    private List<Card> shuffleDeck() {
        List<Card> deck = new ArrayList<>(Card.Color.COUNT * Card.Rank.COUNT);
        for (Card.Color color : Card.Color.ALL) {
            for (Card.Rank rank : Card.Rank.ALL) {
                deck.add(Card.of(color, rank));
            }
        }
        Collections.shuffle(deck, this.shuffleRng);
        return deck;
    }

    private void setHand(PlayerId id, CardSet hand) {
        this.playerHands.put(id, hand);
        this.players.get(id).updateHand(hand);
    }

    private void initializeHands() {
        List<Card> deck = shuffleDeck();
        int i = 0;
        for (PlayerId id : PlayerId.values()) {
            int next_i = i + Jass.HAND_SIZE;
            setHand(id, CardSet.of(deck.subList(i, next_i)));
            i = next_i;
        }
    }

    private PlayerId firstPlayerBySeven() {
        for (PlayerId id : PlayerId.values()) {
            if (this.playerHands.get(id).contains(Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN))) {
                return id;
            }
        }
        throw new Error("Unreachable code");
    }

    private void initializeTurnState() {
        initializeHands();
        if (this.lastTurnStarter == null) {
            this.lastTurnStarter = firstPlayerBySeven();
        } else {
            this.lastTurnStarter = PlayerId.ALL.get((this.lastTurnStarter.ordinal() + 1) % PlayerId.COUNT);
        }
        Score score = this.turnState == null ? Score.INITIAL : this.turnState.score().nextTurn();
        this.turnState = TurnState.initial(nextTrump(this.lastTurnStarter), score, this.lastTurnStarter);
        informOfScore();
        this.firstTrick = true;
    }

    private void informOfTrick() {
        for (Player player : this.players.values()) {
            player.updateTrick(this.turnState.trick());
        }
    }

    private void informOfScore() {
        for (Player player : this.players.values()) {
            player.updateScore(this.turnState.score());
        }
    }

    private void handleAnnounces(Map<PlayerId, CardSet> announces, Collection<PlayerId> announceOrder) {
        AnnounceValue bestAnnounce = AnnounceValue.fromSet(CardSet.EMPTY);
        PlayerId bestPlayer = PlayerId.PLAYER_1;
        for (PlayerId playerId : announceOrder) {
            AnnounceValue announce = announces.get(playerId).announceValue();
            if (announce.compareTo(bestAnnounce) > 0) {
                bestAnnounce = announce;
                bestPlayer = playerId;
            }
        }
        TeamId winning = bestPlayer.team();
        for (Player player : this.players.values()) {
            player.setAnnounce(announces, winning);
        }
    }

    /**
     * @return true if we've reached the end of the game, i.e., someone has reached 1000+ points
     */
    public boolean isGameOver() {
        return this.gameOver;
    }

    private void checkWinningTeam() {
        if (this.turnState == null) return;

        for (TeamId id : TeamId.ALL) {
            if (this.turnState.score().totalPoints(id) >= Jass.WINNING_POINTS) {
                this.gameOver = true;

                for (Player player : this.players.values()) {
                    player.setWinningTeam(id);
                }
                return;
            }
        }
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
        if (this.turnState == null) {
            initializeTurnState();
            informOfTrick();
        }

        if (this.turnState.trick().isFull()) {
            this.turnState = this.turnState.withTrickCollected();
            informOfScore();
            checkWinningTeam();
            if (isGameOver()) {
                return;
            }
            if (this.turnState.isTerminal()) {
                initializeTurnState();
            }
            informOfTrick();
        }

        List<PlayerId> announceOrder = new ArrayList<>(PlayerId.COUNT);
        Map<PlayerId, CardSet> announces = new EnumMap<>(PlayerId.class);
        while (!this.turnState.trick().isFull()) {
            PlayerId nextId = this.turnState.nextPlayer();
            Player next = this.players.get(nextId);
            CardSet hand = this.playerHands.get(nextId);
            if (this.firstTrick) {
                announceOrder.add(nextId);
                announces.put(nextId, next.announce(hand));
            }
            Card choice = next.cardToPlay(this.turnState, hand);
            this.turnState = this.turnState.withNewCardPlayed(choice);
            setHand(nextId, hand.remove(choice));
            informOfTrick();
        }
        if (this.firstTrick) {
            handleAnnounces(announces, announceOrder);
            this.firstTrick = false;
        }
    }
}
