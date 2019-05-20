package ch.epfl.javass.net;

import ch.epfl.javass.jass.*;
import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

public class EntireGameTest {
    private static SplittableRandom rng = TestRandomizer.newRandom();

    @Test
    void entireGameTest() throws IOException {
        Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);

        for (PlayerId id : PlayerId.ALL) {
            players.put(id, new RandomPlayer(TestRandomizer.SEED));
        }

        Score normalScore = playAGameWith(players);

        RemotePlayerServer server = new RemotePlayerServer(new RandomPlayer(TestRandomizer.SEED));
        Thread serverThread = new Thread(() -> {
            try {
                server.run();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        serverThread.start();


        try (RemotePlayerClient playerClient = new RemotePlayerClient("localhost")) {
            for (PlayerId id : PlayerId.ALL) {
                if (id != PlayerId.PLAYER_1) {
                    players.put(id, new RandomPlayer(TestRandomizer.SEED));
                } else {
                    players.put(id, playerClient);
                }
            }
            assertEquals(normalScore, playAGameWith(players));
        }


    }

    private static Score playAGameWith(Map<PlayerId, Player> playerMap) {
        TestPlayer testPlayer = new TestPlayer(PlayerId.PLAYER_4, null);
        playerMap.put(PlayerId.PLAYER_4, testPlayer);

        Map<PlayerId, String> names = new EnumMap<>(PlayerId.class);
        for (PlayerId id : PlayerId.ALL) {
            names.put(id, TestRandomizer.randomString(rng, 5));
        }

        JassGame g = new JassGame(2019, playerMap, names);
        while (!g.isGameOver()) {
            g.advanceToEndOfNextTrick();
        }
        return testPlayer.updateScoreScore;
    }
}
