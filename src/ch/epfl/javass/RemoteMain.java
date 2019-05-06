package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

public final class RemoteMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Player player = new GraphicalPlayerAdapter();
        Thread serverThread = new Thread(() -> {
            RemotePlayerServer server = new RemotePlayerServer(player);
            server.run();
        });
        serverThread.setDaemon(true);
        serverThread.start();
        System.out.println("La partie commencera à la connexion du client");
    }
}
