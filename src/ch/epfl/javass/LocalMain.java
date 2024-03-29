package ch.epfl.javass;

import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.PlayerId;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;

/**
 * This class is used to start a local game.
 * <p>
 * When starting a local game, we hold all the logic for the game,
 * and reach out to remote players waiting to join a game. We decide
 * which players get what names, and what kind of player is used for each id.
 * <p>
 * This class is designed to be run as the main program, and given arguments
 * specifying how to run the game.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class LocalMain extends Application {
    private static final int END_TURN_SLEEPING_TIME = 1000;
    private static final int ANNOUNCE_TURN_SLEEPING_TIME = 5000;
    private static final int MIN_ARG_COUNT = PlayerId.COUNT;
    private static final int MAX_ARG_COUNT = MIN_ARG_COUNT + 1;

    private final static String USAGE = "Utilisation: java ch.epfl.javass.LocalMain <j1>..<j4> [graine] où :\n" +
            "  <jN> spécifie le joueur N, un de:\n" +
            "    h:<nom> un joueur humain nommé <nom>\n" +
            "    r:<nom>:<hôte> un joueur en ligne, nommé <nom>, connecté au réseau sur <host>\n" +
            "    s:<nom>:<iterations> un joueur simulé par MCTS, nommé <nom>, faisant <iterations> part décision\n" +
            " [graine] si donné va rendre l'aléatoire du jeu déterministe, avec cette graine comme incipit\n";

    private List<String> args = null;

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


    public LocalMain(List<String> args) {
        this.args = args;
    }

    // This constructor is needed for Javafx
    public LocalMain() {
    }


    @Override
    public void start(Stage primaryStage) {
        if (args == null) {
            args = getParameters().getRaw();
        }
        int size = args.size();
        if (size < MIN_ARG_COUNT || size > MAX_ARG_COUNT) {
            System.out.println(USAGE);
            System.exit(1);
        }
        Random rng;
        if (size == MAX_ARG_COUNT) {
            rng = new Random(parseNumber(args.get(4)));
        } else {
            rng = new Random();
        }
        long jassGameSeed = rng.nextLong();
        Thread gameThread = new Thread(() -> {
            try (PlayerBuilder pb = new PlayerBuilder(primaryStage)) {
                for (int i = 0; i < 4; ++i) {
                    String msg = pb.nextPlayer(rng.nextLong(), args.get(i));
                    if (msg != null) fatal(msg + " : " + args.get(i));
                }
                JassGame g = new JassGame(jassGameSeed, pb.getPlayers(), pb.getNames());
                while (!g.isGameOver()) {
                    if (g.advanceToEndOfNextTrick()) {
                        Thread.sleep(ANNOUNCE_TURN_SLEEPING_TIME);
                    } else {
                        Thread.sleep(END_TURN_SLEEPING_TIME);
                    }
                }
            } catch (Exception e) {
                try {
                    this.stop();
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
                throw new RuntimeException(e);
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }
}
