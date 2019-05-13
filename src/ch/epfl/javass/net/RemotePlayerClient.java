package ch.epfl.javass.net;


import ch.epfl.javass.jass.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class RemotePlayerClient implements Player, AutoCloseable {
    private final Socket sock;
    // BufferedReader since we need readLine
    private final BufferedReader r;
    private final Writer w;


    /**
     * Create a new RemotePlayerClient by connecting to a remote host.
     * This should be accomponied by {@link #close()} at some point later on
     *
     * @param hostName the name of the host to try and connect
     * @throws IOException if an IOException was thrown when constructing
     */
    public RemotePlayerClient(String hostName) throws IOException {
        sock = new Socket(hostName, 5108);
        r = new BufferedReader(
                new InputStreamReader(sock.getInputStream(), StandardCharsets.US_ASCII)
        );
        w = new BufferedWriter(
                new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.US_ASCII)
        );
    }

    // appends a newline and wraps the exception
    private void writeMessage(JassCommand cmd, String... components) {
        String[] args = new String[components.length + 1];
        System.arraycopy(components, 0, args, 1, components.length);
        args[0] = cmd.name();
        try {
            w.write(StringSerializer.combine(' ', args));
            w.write('\n');
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        String stateString = StringSerializer.combine(',',
                StringSerializer.serializeLong(state.packedScore()),
                StringSerializer.serializeLong(state.packedUnplayedCards()),
                StringSerializer.serializeInt(state.packedTrick())
        );
        String handString = StringSerializer.serializeLong(hand.packed());
        writeMessage(JassCommand.CARD, stateString, handString);
        try {
            String resp = r.readLine();
            return Card.ofPacked(StringSerializer.deserializeInt(resp));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        String idString = StringSerializer.serializeInt(ownId.ordinal());
        String[] names = new String[PlayerId.COUNT];
        for (PlayerId p : PlayerId.ALL) {
            names[p.ordinal()] = StringSerializer.serializeString(playerNames.get(p));
        }
        String nameString = StringSerializer.combine(',', names);
        writeMessage(JassCommand.PLRS, idString, nameString);
    }

    @Override
    public void updateHand(CardSet newHand) {
        writeMessage(JassCommand.HAND, StringSerializer.serializeLong(newHand.packed()));
    }

    @Override
    public void setTrump(Card.Color trump) {
        writeMessage(JassCommand.TRMP, StringSerializer.serializeInt(trump.ordinal()));
    }

    @Override
    public void updateTrick(Trick newTrick) {
        writeMessage(JassCommand.TRCK, StringSerializer.serializeInt(newTrick.packed()));
    }

    @Override
    public void updateScore(Score score) {
        writeMessage(JassCommand.SCOR, StringSerializer.serializeLong(score.packed()));
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        writeMessage(JassCommand.WINR, StringSerializer.serializeInt(winningTeam.ordinal()));
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }
}
