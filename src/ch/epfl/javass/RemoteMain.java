package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class is used to an existent game on another computer.
 * <p>
 * This starts up a server, and then waits for that local game to start, and us to be contacted.
 * This class will also start up a graphical interface so we can play the game.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RemoteMain extends Application {
    private RemotePlayerServer server;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Player player = new GraphicalPlayerAdapter(primaryStage);
        Thread serverThread = new Thread(() -> {
            server = new RemotePlayerServer(player);
            try {
                server.run();
            } catch (IOException e) {
                System.out.println("IOException dans RemoteMain: " + e.toString());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        server.close();
    }
}
