package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.*;
import ch.epfl.javass.net.RemotePlayerClient;

import java.io.IOException;
import java.util.*;

/**
 * This class is used to construct new players.
 *
 * It implements auto closeable in order to automically close
 * the remote players it creates.
 *
 * The usage of this class involves creating new players by passing arguments
 * representing the different type of players that exist.
 */
public final class PlayerBuilder implements AutoCloseable {
    private final int DEFAULT_ITERATIONS = 10000;
    private PlayerId currentID = PlayerId.PLAYER_1;
    private final Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, String> names = new EnumMap<>(PlayerId.class);
    private final List<RemotePlayerClient> remotes = new ArrayList<>(PlayerId.COUNT);

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
            throw new IllegalStateException("Pas de joueurs à initialisr");
        }
        String[] parts = info.split(":");
        if (parts.length < 1) return "Pas assez de parties dans l'information du joueur";
        if (parts.length > 3) return "Trop de parties dans l'information du joueur";
        switch (parts[0]) {
            case "h":
                return nextHuman(parts);
            case "s":
                return nextSimulated(seed, parts);
            case "r":
                return nextRemote(parts);
            default:
                return "Type de joueur inconnu";
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
        if (parts.length == 3) return "Trop de parties pour le joueur local";
        String name = parts.length == 2 ? parts[1] : "";
        Player player = new GraphicalPlayerAdapter();
        insertNext(name, player);
        return null;
    }

    private String nextSimulated(long seed, String[] parts) {
        int iterations = DEFAULT_ITERATIONS;
        if (parts.length == 3) {
            try {
                iterations = Integer.parseInt(parts[2]);
                if (iterations < Jass.HAND_SIZE) {
                    return "Le nombre d'itérations de MCTS doit être >= 9";
                }
            } catch (NumberFormatException e) {
                return "Nombre d'iterations invalide pour MCTS";
            }
        }
        String name = parts.length >= 2 ? parts[1] : "";
        Player player = new MctsPlayer(currentID, seed, iterations);
        insertNext(name, new PacedPlayer(player, 2));
        return null;
    }

    private String nextRemote(String[] parts) {
        if (parts.length == 3) {
            if (!parts[2].matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}") && !parts[2].equals("localhost")) {
                return "Adresse IPV4 invalide";
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
            return "Connexion échouée " + hostname + " : " + e.toString();
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
