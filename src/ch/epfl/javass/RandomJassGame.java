package ch.epfl.javass;

import ch.epfl.javass.jass.*;
import ch.epfl.javass.net.RemotePlayerClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * RandomJassGame exists for testing purposes.
 * <p>
 * It allows us to run a full game with varying configurations
 * of players.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RandomJassGame {
    public static void main(String[] args) throws IOException {
        Map<PlayerId, Player> players = new HashMap<>();
        Map<PlayerId, String> playerNames = new HashMap<>();
        try (RemotePlayerClient p = new RemotePlayerClient("128.179.187.228")) {

            for (PlayerId pId : PlayerId.ALL) {
                Player player;
                if (pId == PlayerId.PLAYER_1) {
                    player = new PrintingPlayer(p);
                } else {
                    player = new RandomPlayer(2019);
                }
                players.put(pId, player);
                playerNames.put(pId, pId.name());
            }

            JassGame g = new JassGame(2019, players, playerNames);
            long then = System.currentTimeMillis();
            while (!g.isGameOver()) {
                g.advanceToEndOfNextTrick();
                System.out.println("----");
                long now = System.currentTimeMillis();
                System.out.println("Time Elapsed: " + (now - then));
                then = now;
            }
        }
    }
}
