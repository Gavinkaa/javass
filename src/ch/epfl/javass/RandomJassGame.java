package ch.epfl.javass;

import ch.epfl.javass.jass.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RandomJassGame {
    public static void main(String[] args) {
        Map<PlayerId, Player> players = new HashMap<>();
        Map<PlayerId, String> playerNames = new HashMap<>();

        for (PlayerId pId: PlayerId.ALL) {
            Player player = new RandomPlayer(2019);
            if (pId == PlayerId.PLAYER_1)
                player = new PrintingPlayer(player);
            players.put(pId, player);
            playerNames.put(pId, pId.name());
        }

        JassGame g = new JassGame(2019, players, playerNames);
        while (! g.isGameOver()) {
            g.advanceToEndOfNextTrick();
            System.out.println("----");
        }
    }
}
