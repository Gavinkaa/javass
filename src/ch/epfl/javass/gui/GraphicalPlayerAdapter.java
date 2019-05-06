package ch.epfl.javass.gui;

import ch.epfl.javass.jass.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GraphicalPlayerAdapter implements Player {
    private final ScoreBean score = new ScoreBean();
    private final TrickBean trick = new TrickBean();
    private final HandBean hand = new HandBean();
    private final SimpleBooleanProperty mustChooseTrump = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty canDelegate = new SimpleBooleanProperty(false);
    private CardSet handSet;
    private PlayerId ownId;
    private GraphicalPlayer graphicalPlayer;
    private final BlockingQueue<Card> cardQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Card.Color> trumpQueue = new ArrayBlockingQueue<>(1);

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        try {
            return cardQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Card.Color chooseTrump(CardSet hand, boolean canDelegate) {
        this.mustChooseTrump.setValue(true);
        this.canDelegate.setValue(true);
        try {
            Card.Color trump = trumpQueue.take();
            this.mustChooseTrump.setValue(false);
            return trump;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.ownId = ownId;
        this.graphicalPlayer = new GraphicalPlayer(ownId, playerNames, cardQueue, trumpQueue, mustChooseTrump, canDelegate, score, trick, hand);
        Platform.runLater(() -> this.graphicalPlayer.createStage().show());
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
                    this.hand.setPlayableCards(newTrick.playableCards(handSet));
                } else {
                    this.hand.setPlayableCards(CardSet.EMPTY);
                }
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
}
