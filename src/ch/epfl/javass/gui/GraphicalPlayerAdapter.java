package ch.epfl.javass.gui;

import ch.epfl.javass.jass.*;
import javafx.application.Platform;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GraphicalPlayerAdapter implements Player {
    private final ScoreBean score = new ScoreBean();
    private final TrickBean trick = new TrickBean();
    private final HandBean hand = new HandBean();
    private GraphicalPlayer graphicalPlayer;
    private final BlockingQueue<Card> queue = new ArrayBlockingQueue<>(1);

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.graphicalPlayer = new GraphicalPlayer(ownId, playerNames, this.queue, score, trick, this.hand);
        Platform.runLater(() -> this.graphicalPlayer.createStage().show());
    }

    @Override
    public void updateHand(CardSet newHand) {
        Platform.runLater(() -> this.hand.setHand(newHand));
    }

    @Override
    public void setTrump(Card.Color trump) {
        Platform.runLater(() -> this.trick.setTrump(trump));
    }

    @Override
    public void updateTrick(Trick newTrick) {
        Platform.runLater(() -> this.trick.setTrick(newTrick));
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
