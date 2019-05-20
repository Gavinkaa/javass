package ch.epfl.javass.net;

import ch.epfl.javass.jass.*;
import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RemoteGameTest {
    private TestPlayer local;
    private TestPlayer remote;
    private RemotePlayerServer server;
    private RemotePlayerClient client;
    private Thread serverThread;

    private void remoteSame(Function<TestPlayer, Object> function) {
        try {
            //This is necessary to make sure remote is updated
            Thread.sleep(4); //Disgusting but works
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(function.apply(local), function.apply(remote));
    }

    @BeforeAll
    void setUp() {
        try {
            this.local = new TestPlayer(PlayerId.PLAYER_1, null);
            this.remote = new TestPlayer(PlayerId.PLAYER_1, null);
            this.server = new RemotePlayerServer(remote);

            this.serverThread = new Thread(() -> {
                try {
                    server.run();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            this.serverThread.start();
            Thread.sleep(20);
            this.client = new RemotePlayerClient("localhost");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    void closeEverything() throws IOException, InterruptedException {
        client.close();
        serverThread.join();
    }

    @Test
    void setPlayerDoTheSameInLocalAndRemote() {
        SplittableRandom rng = TestRandomizer.newRandom();

        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS / 20; i++) {

            Map<PlayerId, String> names = new EnumMap<>(PlayerId.class);
            for (PlayerId id : PlayerId.ALL) {
                names.put(id, TestRandomizer.randomString(rng, 5));
            }
            local.setPlayers(PlayerId.PLAYER_1, names);
            client.setPlayers(PlayerId.PLAYER_1, names);

            remoteSame(p -> p.setPlayersCallCount);
            remoteSame(p -> p.setPlayersOwnId);
            for (PlayerId id : PlayerId.ALL) {
                remoteSame(p -> p.setPlayersPlayerNames.get(id));
            }
        }
    }

    @Test
    void updateHandDoTheSame() {
        SplittableRandom rng = TestRandomizer.newRandom();
        //we divide the number the of operations to make it faster
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS / 20; i++) {

            CardSet hand = CardSet.EMPTY;
            while (hand.size() < 9) {
                Card.Rank r = Card.Rank.ALL.get(rng.nextInt(Card.Rank.COUNT));
                Card.Color c = Card.Color.ALL.get(rng.nextInt(Card.Color.COUNT));
                hand = hand.add(Card.of(c, r));
            }

            local.updateHand(hand);
            client.updateHand(hand);
            remoteSame(p -> p.updateHandCallCount);
            remoteSame(p -> p.updateHandNewHand);
            remoteSame(p -> p.updateHandInitialHand);
        }
    }

    @Test
    void setTrumpDoTheSame() {
        SplittableRandom rng = TestRandomizer.newRandom();

        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS / 20; i++) {
            Card.Color trump = Card.Color.ALL.get(rng.nextInt(Card.Color.COUNT));
            local.setTrump(trump);
            client.setTrump(trump);
            remoteSame(p -> p.setTrumpCallCount);
            remoteSame(p -> p.setTrumpTrump);
        }
    }

    @Test
    void updateTrickDoTheSame() {
        SplittableRandom rng = TestRandomizer.newRandom();
        Trick trick = Trick.firstEmpty(Card.Color.HEART, PlayerId.PLAYER_1);
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS / 20; i++) {
            if (trick.isFull()) {
                trick = Trick.firstEmpty(Card.Color.HEART, PlayerId.PLAYER_1);
            }
            Card.Rank r = Card.Rank.ALL.get(rng.nextInt(Card.Rank.COUNT));
            Card.Color c = Card.Color.ALL.get(rng.nextInt(Card.Color.COUNT));
            trick = trick.withAddedCard(Card.of(c, r));


            local.updateTrick(trick);
            client.updateTrick(trick);

            remoteSame(p -> p.updateTrickNewTrick);
            remoteSame(p -> p.updateTrickCallCount);
        }
    }

    @Test
    void updateScoreDoTheSame() {
        SplittableRandom rng = TestRandomizer.newRandom();

        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS / 20; i++) {
            Score s = Score.ofPacked(PackedScore.pack(
                    rng.nextInt(10), rng.nextInt(258), rng.nextInt(2001),
                    rng.nextInt(10), rng.nextInt(258), rng.nextInt(2001)
            ));

            local.updateScore(s);
            client.updateScore(s);

            remoteSame(p -> p.updateScoreScore);
            remoteSame(p -> p.updateScoreCallCount);
        }
    }

    @Test
    void setWinningTeamDoTheSame(){
        SplittableRandom rng = TestRandomizer.newRandom();

        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS / 20; i++) {
            TeamId id = TeamId.ALL.get(rng.nextInt(2));

            local.setWinningTeam(id);
            client.setWinningTeam(id);

            remoteSame(p -> p.setWinningTeamWinningTeam);
            remoteSame(p -> p.setWinningTeamCallCount);
        }
    }
}
