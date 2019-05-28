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
    private ServerSocket server;

    /**
     * Construct a new RemotePlayerServer,
     * given an underlying player to inform of changes in the game.
     *
     * @param player the player to make play the game
     */
    public RemotePlayerServer(Player player) {
        this.local = player;
    }

    public void close() throws IOException {
        server.close();
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
    public void run() throws IOException {
        try {
            System.out.println("Creating a new socket");
            server = new ServerSocket(Constants.PORT);
            Socket s = server.accept();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(s.getInputStream(), StandardCharsets.US_ASCII)
            );
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(s.getOutputStream(), StandardCharsets.US_ASCII)
            );
            while (!Thread.interrupted() && interactWith(r, w)) {
            }
        } finally {
            server.close();
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
            case ANNC:
                handleANNC(components, w);
                break;
            case INAN:
                handleINAN(components);
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
        this.local.setPlayers(ownId, players);
    }

    private void handleTRMP(String[] components) {
        int trumpOrd = StringSerializer.deserializeInt(components[1]);
        this.local.setTrump(Card.Color.ALL.get(trumpOrd));
    }

    private void handleHAND(String[] components) {
        long pkHand = StringSerializer.deserializeLong(components[1]);
        this.local.updateHand(CardSet.ofPacked(pkHand));
    }

    private void handleTRCK(String[] components) {
        int pkTrick = StringSerializer.deserializeInt(components[1]);
        this.local.updateTrick(Trick.ofPacked(pkTrick));
    }

    private void handleCARD(String[] components, Writer w) throws IOException {
        String[] stArgs = StringSerializer.split(',', components[1]);
        TurnState st = TurnState.ofPackedComponents(
                StringSerializer.deserializeLong(stArgs[0]),
                StringSerializer.deserializeLong(stArgs[1]),
                StringSerializer.deserializeInt(stArgs[2])
        );
        long pkHand = StringSerializer.deserializeLong(components[2]);
        Card played = this.local.cardToPlay(st, CardSet.ofPacked(pkHand));
        writeFlush(w, StringSerializer.serializeInt(played.packed()));
    }

    private void handleSCOR(String[] components) {
        long pkScore = StringSerializer.deserializeLong(components[1]);
        this.local.updateScore(Score.ofPacked(pkScore));
    }

    private void handleWINR(String[] components) {
        int teamOrd = StringSerializer.deserializeInt(components[1]);
        this.local.setWinningTeam(TeamId.ALL.get(teamOrd));
    }

    private void handleCHST(String[] components, Writer w) throws IOException {
        long pkHand = StringSerializer.deserializeLong(components[1]);
        boolean canDelegate = components[2].equals("T");
        Card.Color trump = local.chooseTrump(CardSet.ofPacked(pkHand), canDelegate);
        int ordinal = trump == null ? 5 : trump.ordinal();
        writeFlush(w, StringSerializer.serializeInt(ordinal));
    }

    private void handleANNC(String[] components, Writer w) throws IOException {
        long pkHand = StringSerializer.deserializeLong(components[1]);
        CardSet announce = local.announce(CardSet.ofPacked(pkHand));
        writeFlush(w, StringSerializer.serializeLong(announce.packed()));
    }

    private void handleINAN(String[] components) {
        TeamId winningID = TeamId.ALL.get(StringSerializer.deserializeInt(components[1]));
        Map<PlayerId, CardSet> announces = new EnumMap<>(PlayerId.class);
        String[] announceStrings = StringSerializer.split(',', components[2]);
        for (int i = 0; i < PlayerId.COUNT; ++i) {
            PlayerId p = PlayerId.ALL.get(i);
            long packed = StringSerializer.deserializeLong(announceStrings[i]);
            announces.put(p, CardSet.ofPacked(packed));
        }
        this.local.setAnnounce(announces, winningID);
    }

    private static void writeFlush(Writer w, String s) throws IOException {
        w.write(s);
        w.write('\n');
        w.flush();
    }
}
