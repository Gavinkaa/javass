package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayer;
import ch.epfl.javass.gui.HandBean;
import ch.epfl.javass.gui.ScoreBean;
import ch.epfl.javass.gui.TrickBean;
import ch.epfl.javass.jass.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class GuiTest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Map<PlayerId, String> ns = new EnumMap<>(PlayerId.class);
        PlayerId.ALL.forEach(p -> ns.put(p, p.name()));
        ScoreBean sB = new ScoreBean();
        TrickBean tB = new TrickBean();
        HandBean hB = new HandBean();
        GraphicalPlayer g =
                new GraphicalPlayer(PlayerId.PLAYER_2, ns, new ArrayBlockingQueue<Card>(1), sB, tB, new HandBean());
        g.createStage().show();

        new AnimationTimer() {
            long now0 = 0;
            TurnState s = TurnState.initial(Card.Color.SPADE,
                    Score.INITIAL,
                    PlayerId.PLAYER_3);
            CardSet d = CardSet.ALL_CARDS;

            @Override
            public void handle(long now) {
                if (now - now0 < 1_000_000_000L || s.isTerminal())
                    return;
                now0 = now;

                s = s.withNewCardPlayed(d.get(0));
                d = d.remove(d.get(0));
                tB.setTrump(s.trick().trump());
                tB.setTrick(s.trick());

                if (s.trick().isFull()) {
                    s = s.withTrickCollected();
                    for (TeamId t: TeamId.ALL)
                        sB.setTurnPoints(t, s.score().turnPoints(t));
                }
            }
        }.start();
    }
}
