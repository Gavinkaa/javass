package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.*;
import ch.epfl.javass.net.RemotePlayerClient;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * This class is used to construct new players.
 * <p>
 * It implements auto closeable in order to automically close
 * the remote players it creates.
 * <p>
 * The usage of this class involves creating new players by passing arguments
 * representing the different type of players that exist.
 */
public final class PlayerBuilder implements AutoCloseable {
    private static final int DEFAULT_ITERATIONS = 10000;
    private PlayerId currentID = PlayerId.PLAYER_1;
    private final Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
    private final Map<PlayerId, String> names = new EnumMap<>(PlayerId.class);
    private final List<RemotePlayerClient> remotes = new ArrayList<>(4);
    private final Stage stage;

    public PlayerBuilder(Stage stage) {
        this.names.put(PlayerId.PLAYER_1, "Aline");
        this.names.put(PlayerId.PLAYER_2, "Bastien");
        this.names.put(PlayerId.PLAYER_3, "Colette");
        this.names.put(PlayerId.PLAYER_4, "David");
        this.stage = stage;
    }

    public Map<PlayerId, Player> getPlayers() {
        return Collections.unmodifiableMap(this.players);
    }

    public Map<PlayerId, String> getNames() {
        return Collections.unmodifiableMap(this.names);
    }

    public String nextPlayer(long seed, String info) {
        if (this.currentID == null) {
            throw new IllegalStateException("Pas de joueurs à initialisr");
        }
        String error = validatePlayer(info);
        if (error != null) return error;
        String[] parts = info.split(":");
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

    public static String validatePlayer(String info) {
        String[] parts = info.split(":");
        if (parts.length < 1) return "Pas assez de parties dans l'information du joueur";
        if (parts.length > 3) return "Trop de parties dans l'information du joueur";
        switch (parts[0]) {
            case "h":
                return validateHuman(parts);
            case "s":
                return validateSimulated(parts);
            case "r":
                return validateRemote(parts);
            default:
                return "Type de joueur inconnu";
        }
    }

    private void insertNext(String name, Player player) {
        if (!name.isEmpty()) {
            this.names.put(this.currentID, name);
        }
        this.players.put(this.currentID, player);
        if (this.currentID == PlayerId.PLAYER_4) {
            this.currentID = null;
        } else {
            this.currentID = PlayerId.ALL.get(this.currentID.ordinal() + 1);
        }
    }

    private String nextHuman(String[] parts) {
        String error = validateHuman(parts);
        if (error != null) return error;
        String name = parts.length == 2 ? parts[1] : "";
        Player player = new GraphicalPlayerAdapter(stage);
        insertNext(name, player);
        return null;
    }

    private static String validateHuman(String[] parts) {
        if (parts.length == 3) return "Trop de parties pour le joueur local";
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
        Player player = new MctsPlayer(this.currentID, seed, iterations);
        insertNext(name, new PacedPlayer(player, 2));
        return null;
    }

    private static String validateSimulated(String[] parts) {
        if (parts.length == 3) {
            try {
                int iterations = Integer.parseInt(parts[2]);
                if (iterations < Jass.HAND_SIZE) {
                    return "Le nombre d'itérations de MCTS doit être >= 9";
                }
            } catch (NumberFormatException e) {
                return "Nombre d'iterations invalide pour MCTS";
            }
        }
        return null;
    }

    private String nextRemote(String[] parts) {
        String error = validateRemote(parts);
        if (error != null) return error;
        String name = parts.length >= 2 ? parts[1] : "";
        String hostname = parts.length == 3 ? parts[2] : "localhost";
        Player player;
        try {
            RemotePlayerClient remote = new RemotePlayerClient(hostname);
            this.remotes.add(remote);
            player = remote;
        } catch (IOException e) {
            return "Connexion échouée " + hostname + " : " + e.toString();
        }
        insertNext(name, player);
        return null;
    }

    private static String validateRemote(String[] parts) {
        if (parts.length == 3) {
            if (!parts[2].matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}") && !parts[2].equals("localhost")) {
                return "Adresse IPV4 invalide";
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        for (RemotePlayerClient client : this.remotes) {
            client.close();
        }
    }
}
