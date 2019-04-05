package ch.epfl.javass;

import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.PrintingPlayer;
import ch.epfl.javass.net.RemotePlayerServer;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public class Main {
    public static void main(String[] args) {
        RemotePlayerServer player = new RemotePlayerServer(new PrintingPlayer(new MctsPlayer(PlayerId.PLAYER_1,2019, 100_000)));
        player.run();
    }
}
