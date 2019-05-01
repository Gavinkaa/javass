package ch.epfl.javass;

import ch.epfl.javass.gui.*;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.PrintingPlayer;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public class Main extends Application {
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
                new GraphicalPlayer(PlayerId.PLAYER_2, ns, new ArrayBlockingQueue<Card>(1), sB, tB, hB);
        g.createStage().show();
        RemotePlayerServer player = new RemotePlayerServer(new PrintingPlayer(new GraphicalPlayerAdapter(sB, tB, hB)));
        new Thread(player::run).start();
    }
}
