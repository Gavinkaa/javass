package ch.epfl.javass;

import ch.epfl.javass.jass.*;

import java.util.HashMap;
import java.util.Map;

/**
 * RandomJassGame exists for testing purposes.
 *
 * It allows us to run a full game with varying configurations
 * of players.
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RandomJassGame {
    public static void main(String[] args) {
        Map<PlayerId, Player> players = new HashMap<>();
        Map<PlayerId, String> playerNames = new HashMap<>();

        for (PlayerId pId: PlayerId.ALL) {
            Player player = new MctsPlayer(pId, 2019, 100_000);
            if (pId == PlayerId.PLAYER_1)
                player = new PrintingPlayer(player);
            players.put(pId, player);
            playerNames.put(pId, pId.name());
        }

        JassGame g = new JassGame(2019, players, playerNames);
        long then = System.currentTimeMillis();
        while (! g.isGameOver()) {
            g.advanceToEndOfNextTrick();
            System.out.println("----");
            long now = System.currentTimeMillis();
            System.out.println("Time Elapsed: " + (now - then));
            then = now;
        }
    }
}
