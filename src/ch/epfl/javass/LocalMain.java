package ch.epfl.javass;

import ch.epfl.javass.jass.JassGame;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;

public final class LocalMain extends Application {
    private static String makeUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Utilisation: java ch.epfl.javass.LocalMain <j1>..<j4> [graine] où :\n");
        sb.append("  <jN> spécifie le joueur N, un de:\n");
        sb.append("    h:<nom> un joueur humain nommé <nom>\n");
        sb.append("    r:<nom>:<hôte> un joueur en ligne, nommé <nom>, connecté au réseau sur <host>\n");
        sb.append("    s:<nom>:<iterations> un joueur simulé par MCTS, nommé <nom>, faisant <iterations> part décision\n");
        sb.append(" [graine] si donné va rendre l'aléatoire du jeu déterministe, avec cette graine comme incipit\n");
        return sb.toString();
    }

    private final static String USAGE = makeUsage();

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
            System.out.println(USAGE);
            System.exit(1);
        }
        Random rng;
        if (size == 5) {
            rng = new Random(parseNumber(args.get(4)));
        } else {
            rng = new Random();
        }
        long jassGameSeed = rng.nextLong();
        Thread gameThread = new Thread(() -> {
            try (PlayerBuilder pb = new PlayerBuilder()) {
                for (int i = 0; i < 4; ++i) {
                    String msg = pb.nextPlayer(rng.nextLong(), args.get(i));
                    if (msg != null) fatal(msg + " : " + args.get(i));
                }
                JassGame g = new JassGame(jassGameSeed, pb.getPlayers(), pb.getNames());
                while (!g.isGameOver()) {
                    g.advanceToEndOfNextTrick();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }
}
