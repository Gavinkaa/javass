package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a bean holding information about announces in the game of Jass.
 * <p>
 * This is used to hold information about what player's have announced which cards,
 * as well as whether or not the announces for a given turn have finished.
 */
public final class AnnounceBean {
    private final BooleanProperty announcesVisible = new SimpleBooleanProperty(false);
    private final Map<PlayerId, ObservableList<Card>> announces;
    private final Map<PlayerId, SimpleIntegerProperty> points;
    private final ObjectProperty<TeamId> winningTeam = new SimpleObjectProperty<>(null);

    public AnnounceBean() {
        this.announces = new EnumMap<>(PlayerId.class);
        this.points = new EnumMap<>(PlayerId.class);
        for (PlayerId pid : PlayerId.ALL) {
            this.announces.put(pid, FXCollections.observableArrayList());
            this.points.put(pid, new SimpleIntegerProperty(0));
        }
    }

    /**
     * This bean indicates whether or not the announces should be shown.
     * <p>
     * This exists because we only want to show the announces once we've arrived
     * at the end of the first turn.
     *
     * @return a bean telling us whether or not to show the announces
     */
    public ObservableBooleanValue announcesVisible() {
        return this.announcesVisible;
    }

    /**
     * This bean holds an observable list of cards for each player.
     * <p>
     * This is useful in order to visualize the announces different players might have made.
     *
     * @param player the player to look up the list for
     * @return an observable list holding all the cards the player has announced.
     */
    public ObservableList<Card> announces(PlayerId player) {
        return FXCollections.unmodifiableObservableList(this.announces.get(player));
    }

    /**
     * This beans holds the number of points a player has made with their announce
     * <p>
     * This is useful because we want to be able to display the points a player
     * has made with a good announce in the GUI.
     *
     * @param player the player to check
     * @return a read only property holding the number of points for that player
     */
    public ReadOnlyIntegerProperty points(PlayerId player) {
        return points.get(player);
    }

    /**
     * This is a bean that holds the team that has won this round of announces.
     *
     * @return an object property containing the team with the best announce this turn
     */
    public ReadOnlyObjectProperty<TeamId> winningTeam() {
        return this.winningTeam;
    }

    /**
     * Change the visibility of the announces in a given direction.
     * <p>
     * This should be set to true after setting the announces, and to false
     * when we've moved on to the start of the next trick.
     *
     * @param value when true, the announces become visible, otherwise they're hidden
     */
    public void setAnnouncesVisible(boolean value) {
        this.announcesVisible.setValue(value);
    }

    /**
     * Update this bean with the announces players have made.
     * <p>
     * This is the central method for this class, and sets all the appropriate things.
     *
     * @param announces the announces each player has made
     */
    public void setAnnounces(Map<PlayerId, CardSet> announces) {
        for (PlayerId player : PlayerId.ALL) {
            ObservableList<Card> list = this.announces.get(player);
            list.clear();
            CardSet cards = announces.get(player);
            for (int i = 0; i < cards.size(); ++i) {
                list.add(cards.get(i));
            }
            int points = cards.announceValue().points();
            this.points.get(player).setValue(points);
        }
    }

    /**
     * Set the winning team for this bean
     *
     * @param team the team that won this round of announces
     */
    public void setWinningTeam(TeamId team) {
        this.winningTeam.setValue(team);
    }
}
