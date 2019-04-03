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
        sock = new Socket(hostName, 5018);
        r = new BufferedReader(
                new InputStreamReader(sock.getInputStream(), StandardCharsets.US_ASCII)
        );
        w = new BufferedWriter(
                new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.US_ASCII)
        );
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        String stateString = StringSerializer.combine(',',
                StringSerializer.serializeLong(state.packedScore()),
                StringSerializer.serializeLong(state.packedUnplayedCards()),
                StringSerializer.serializeInt(state.packedTrick())
        );
        String handString = StringSerializer.serializeLong(hand.packed());
        String msg = StringSerializer.combine(' ', JassCommand.CARD.name(), stateString, handString);
        try {
            w.write(msg);
            w.write('\n');
            return Card.ofPacked(StringSerializer.deserializeInt(r.readLine()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
    }

    @Override
    public void updateHand(CardSet newHand) {

    }

    @Override
    public void setTrump(Card.Color trump) {

    }

    @Override
    public void updateTrick(Trick newTrick) {

    }

    @Override
    public void updateScore(Score score) {

    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {

    }

    @Override
    public void close() throws IOException {
        sock.close();
    }
}
