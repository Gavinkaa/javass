package ch.epfl.javass.gui;

import ch.epfl.javass.jass.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Represents a player that's controlled by the graphical user interface.
 * <p>
 * This wraps a graphical interface in {@link GraphicalPlayer} in order to
 * have a controllable player. This implements the Player interface completely.
 * The graphical interface will first be created when {@link Player::setPlayers} is called
 * for the first time.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public class GraphicalPlayerAdapter implements Player {
    private final ScoreBean score = new ScoreBean();
    private final TrickBean trick = new TrickBean();
    private final HandBean hand = new HandBean();
    private final AnnounceBean announce = new AnnounceBean();
    private final SimpleBooleanProperty mustChooseTrump = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty canDelegate = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty canAnnounce = new SimpleBooleanProperty(false);
    private CardSet handSet;
    private PlayerId ownId;
    private GraphicalPlayer graphicalPlayer;
    private final BlockingQueue<Card> cardQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Integer> trumpQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<CardSet> announceQueue = new ArrayBlockingQueue<>(1);
    private final Stage stage;

    public GraphicalPlayerAdapter(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        try {
            return this.cardQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Card.Color chooseTrump(CardSet hand, boolean canDelegate) {
        this.mustChooseTrump.setValue(true);
        this.canDelegate.setValue(canDelegate);
        try {
            int trumpIndex = this.trumpQueue.take();
            Card.Color trump = trumpIndex >= 4 ? null : Card.Color.ALL.get(trumpIndex);
            this.mustChooseTrump.setValue(false);
            return trump;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CardSet announce(CardSet hand) {
        this.canAnnounce.setValue(true);
        try {
            CardSet choice = this.announceQueue.take();
            this.canAnnounce.setValue(false);
            return choice;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.ownId = ownId;
        this.graphicalPlayer = new GraphicalPlayer(ownId, playerNames, this.cardQueue, this.trumpQueue, this.announceQueue, this.mustChooseTrump, this.canDelegate, this.canAnnounce, this.score, this.trick, this.hand, this.announce);
        Platform.runLater(() -> this.graphicalPlayer.addToStage(stage).show());
    }

    @Override
    public void updateHand(CardSet newHand) {
        this.handSet = newHand;
        Platform.runLater(() -> this.hand.setHand(newHand));
    }

    @Override
    public void setTrump(Card.Color trump) {
        Platform.runLater(() -> this.trick.setTrump(trump));
    }

    @Override
    public void updateTrick(Trick newTrick) {
        Platform.runLater(() -> {
            this.trick.setTrick(newTrick);
            if (!newTrick.isFull()) {
                boolean amPlaying = newTrick.player(newTrick.size()) == this.ownId;
                if (amPlaying) {
                    this.hand.setPlayableCards(newTrick.playableCards(this.handSet));
                } else {
                    this.hand.setPlayableCards(CardSet.EMPTY);
                }
            } else {
                this.hand.setPlayableCards(CardSet.EMPTY);
            }
        });
    }

    @Override
    public void updateScore(Score score) {
        Platform.runLater(() -> {
            for (TeamId t : TeamId.ALL) {
                this.score.setTurnPoints(t, score.turnPoints(t));
                this.score.setGamePoints(t, score.gamePoints(t));
                this.score.setTotalPoints(t, score.totalPoints(t));
            }
        });
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        Platform.runLater(() -> this.score.setWinningTeam(winningTeam));
    }

    @Override
    public void setAnnounce(Map<PlayerId, CardSet> announces, TeamId winner) {
        System.out.println("Set announce " + announces + " " + winner);
        Platform.runLater(() -> {
            this.announce.setAnnounces(announces);
            this.announce.setWinningTeam(winner);
            this.announce.setAnnouncesVisible(true);
        });
    }
}
