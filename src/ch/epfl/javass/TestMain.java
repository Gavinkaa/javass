package ch.epfl.javass;


import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.gui.HandBean;
import ch.epfl.javass.gui.ScoreBean;
import ch.epfl.javass.gui.TrickBean;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;

import static ch.epfl.javass.jass.PlayerId.*;

public final class TestMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        RemotePlayerClient p = new RemotePlayerClient("128.179.187.63");
        Map<PlayerId, Player> ps = new EnumMap<>(PlayerId.class);
        ps.put(PLAYER_1, new GraphicalPlayerAdapter(new ScoreBean(), new TrickBean(), new HandBean()));
        ps.put(PLAYER_2, new MctsPlayer(PLAYER_2, 123, 100_000));
        ps.put(PLAYER_3, p);
        ps.put(PLAYER_4, new MctsPlayer(PLAYER_4, 789, 100_000));

        Map<PlayerId, String> ns = new EnumMap<>(PlayerId.class);
        PlayerId.ALL.forEach(i -> ns.put(i, i.name()));

        new Thread(() -> {
            JassGame g = new JassGame(0, ps, ns);
            while (!g.isGameOver()) {
                g.advanceToEndOfNextTrick();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }).start();
    }
}