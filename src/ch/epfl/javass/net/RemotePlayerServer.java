package ch.epfl.javass.net;

import ch.epfl.javass.jass.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * This class allows us to wrap a client,
 * in order to keep them informed on the state of the game
 * on another computer. We accept a connection from that computer
 * in order to listen for events, and inform the player we're
 * keeping hold of.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RemotePlayerServer {
    private final Player local;

    /**
     * Construct a new RemotePlayerServer,
     * given an underlying player to inform of changes in the game.
     *
     * @param player the player to make play the game
     */
    public RemotePlayerServer(Player player) {
        local = player;
    }

    /**
     * This function will listen for a connection,
     * and then enter into an infinite game loop with that connection.
     * It will listen for messages, informing its underlying player,
     * and responding with its own messages when necessary
     * <p>
     * If some IOException gets raised during the loop,
     * this function will exit with an Unchecked wrapper around it.
     *
     * @throws UncheckedIOException if we caught an exception in the loop
     */
    public void run() {
        try (ServerSocket server = new ServerSocket(Constants.PORT)) {
            Socket s = server.accept();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(s.getInputStream(), StandardCharsets.US_ASCII)
            );
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(s.getOutputStream(), StandardCharsets.US_ASCII)
            );
            while (interactWith(r, w)) {
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean interactWith(BufferedReader r, Writer w) throws IOException {
        String msg = r.readLine();
        if (msg == null) {
            return false;
        }
        String[] components = StringSerializer.split(' ', msg);
        JassCommand cmd = JassCommand.valueOf(components[0]);
        switch (cmd) {
            case PLRS:
                handlePLRS(components);
                break;
            case TRMP:
                handleTRMP(components);
                break;
            case HAND:
                handleHAND(components);
                break;
            case TRCK:
                handleTRCK(components);
                break;
            case CARD:
                handleCARD(components, w);
                break;
            case SCOR:
                handleSCOR(components);
                break;
            case WINR:
                handleWINR(components);
                break;
            case CHST:
                handleCHST(components, w);
                break;
        }
        return true;
    }

    private void handlePLRS(String[] components) {
        PlayerId ownId = PlayerId.ALL.get(StringSerializer.deserializeInt(components[1]));
        Map<PlayerId, String> players = new EnumMap<>(PlayerId.class);
        String[] names = StringSerializer.split(',', components[2]);
        for (int i = 0; i < PlayerId.COUNT; ++i) {
            PlayerId p = PlayerId.ALL.get(i);
            players.put(p, StringSerializer.deserializeString(names[i]));
        }
        local.setPlayers(ownId, players);
    }

    private void handleTRMP(String[] components) {
        int trumpOrd = StringSerializer.deserializeInt(components[1]);
        local.setTrump(Card.Color.ALL.get(trumpOrd));
    }

    private void handleHAND(String[] components) {
        long pkHand = StringSerializer.deserializeLong(components[1]);
        local.updateHand(CardSet.ofPacked(pkHand));
    }

    private void handleTRCK(String[] components) {
        int pkTrick = StringSerializer.deserializeInt(components[1]);
        local.updateTrick(Trick.ofPacked(pkTrick));
    }

    private void handleCARD(String[] components, Writer w) throws IOException {
        String[] stArgs = StringSerializer.split(',', components[1]);
        TurnState st = TurnState.ofPackedComponents(
                StringSerializer.deserializeLong(stArgs[0]),
                StringSerializer.deserializeLong(stArgs[1]),
                StringSerializer.deserializeInt(stArgs[2])
        );
        long pkHand = StringSerializer.deserializeLong(components[2]);
        Card played = local.cardToPlay(st, CardSet.ofPacked(pkHand));
        w.write(StringSerializer.serializeInt(played.packed()));
        w.write('\n');
        w.flush();
    }

    private void handleSCOR(String[] components) {
        long pkScore = StringSerializer.deserializeLong(components[1]);
        local.updateScore(Score.ofPacked(pkScore));
    }

    private void handleWINR(String[] components) {
        int teamOrd = StringSerializer.deserializeInt(components[1]);
        local.setWinningTeam(TeamId.ALL.get(teamOrd));
    }

    private void handleCHST(String[] components, Writer w) throws IOException {
        long pkHand = StringSerializer.deserializeLong(components[1]);
        boolean canDelegate = components[2].equals("T");
        Card.Color trump = local.chooseTrump(CardSet.ofPacked(pkHand), canDelegate);
        int ordinal = trump == null ? 5 : trump.ordinal();
        w.write(StringSerializer.serializeInt(ordinal));
        w.write('\n');
        w.flush();
    }
}
