package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;

import java.io.IOException;
import java.util.*;

public final class PlayerBuilder implements AutoCloseable {
    private PlayerId currentID = PlayerId.PLAYER_1;
    private final Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, String> names = new EnumMap<>(PlayerId.class);
    private final List<RemotePlayerClient> remotes = new ArrayList<>(4);

    public PlayerBuilder() {
        names.put(PlayerId.PLAYER_1, "Aline");
        names.put(PlayerId.PLAYER_2, "Bastien");
        names.put(PlayerId.PLAYER_3, "Colette");
        names.put(PlayerId.PLAYER_4, "David");
    }

    public Map<PlayerId, Player> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public Map<PlayerId, String> getNames() {
        return Collections.unmodifiableMap(names);
    }

    public String nextPlayer(long seed, String info) {
        if (currentID == null) {
            throw new IllegalStateException("No players to initialize");
        }
        String[] parts = info.split(":");
        if (parts.length < 1) return "Not enough parts in player information";
        if (parts.length > 3) return "Too many parts in player information";
        switch (parts[0]) {
            case "h":
                return nextHuman(parts);
            case "s":
                return nextSimulated(seed, parts);
            case "r":
                return nextRemote(parts);
            default:
                return "Unrecognized player type";
        }
    }

    private void insertNext(String name, Player player) {
        if (!name.isEmpty()) {
            names.put(currentID, name);
        }
        players.put(currentID, player);
        if (currentID == PlayerId.PLAYER_4) {
            currentID = null;
        } else {
            currentID = PlayerId.ALL.get(currentID.ordinal() + 1);
        }
    }

    private String nextHuman(String[] parts) {
        if (parts.length == 3) return "Too many parts for local human player";
        String name = parts.length == 2 ? parts[1] : "";
        Player player = new GraphicalPlayerAdapter();
        insertNext(name, player);
        return null;
    }

    private String nextSimulated(long seed, String[] parts) {
        int iterations = 10000;
        if (parts.length == 3) {
            try {
                iterations = Integer.parseInt(parts[2]);
                if (iterations < 9) {
                    return "MCTS iterations must be at least 9";
                }
            } catch (NumberFormatException e) {
                return "Invalid iteration number for MCTS";
            }
        }
        String name = parts.length >= 2 ? parts[1] : "";
        Player player = new MctsPlayer(currentID, seed, iterations);
        insertNext(name, new PacedPlayer(player, 2));
        return null;
    }

    private String nextRemote(String[] parts) {
        if (parts.length == 3) {
            if (!parts[2].matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}")) {
                return "Invalid IPV4 address";
            }
        }
        String name = parts.length >= 2 ? parts[1] : "";
        String hostname = parts.length == 3 ? parts[2] : "localhost";
        Player player;
        try {
            RemotePlayerClient remote = new RemotePlayerClient(hostname);
            remotes.add(remote);
            player = remote;
        } catch (IOException e) {
            return "Failed to connect to " + hostname + " : " + e.toString();
        }
        insertNext(name, player);
        return null;
    }

    @Override
    public void close() throws Exception {
        for (RemotePlayerClient client : remotes) {
            client.close();
        }
    }
}