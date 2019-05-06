package ch.epfl.javass;

import ch.epfl.javass.jass.JassGame;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;

public final class LocalMain extends Application {
    private void fatal(String message) {
        System.err.println("Erreur : " + message);
        System.exit(1);
    }

    private long parseNumber(String number) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException e) {
            fatal("Invalid Number: " + e.toString());
        }
        throw new RuntimeException("Unreachable code");
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> args = getParameters().getRaw();
        int size = args.size();
        if (size != 4 && size != 5) {
            fatal("Expected 4 or 5 arguments got: " + size);
        }
        Random rng;
        if (size == 5) {
            rng = new Random(parseNumber(args.get(4)));
        } else {
            rng = new Random();
        }
        long jassGameSeed = rng.nextLong();
        try (PlayerBuilder pb = new PlayerBuilder()) {
            for (int i = 0; i < 4; ++i) {
                String msg = pb.nextPlayer(rng.nextLong(), args.get(i));
                if (msg != null) fatal(msg + " : " + args.get(i));
            }
            Thread gameThread = new Thread(() -> {
                JassGame g = new JassGame(jassGameSeed, pb.getPlayers(), pb.getNames());
                while (!g.isGameOver()) {
                    g.advanceToEndOfNextTrick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            });
            gameThread.setDaemon(true);
            gameThread.start();
        }
    }
}
