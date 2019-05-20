package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a JavaFX bean allowing us to observe changes
 * to the trick in a game of Jass. The trick contains the cards on
 * the playing field in the gui.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class TrickBean {
    private final SimpleObjectProperty<Card.Color> trump = new SimpleObjectProperty<>();
    // null indicates no player winning this trick
    private final SimpleObjectProperty<PlayerId> winningPlayer = new SimpleObjectProperty<>(null);
    private final ObservableMap<PlayerId, Card> cards = FXCollections.observableHashMap();

    /**
     * @return a read-only property for the current trump in the trick
     */
    public ReadOnlyObjectProperty<Card.Color> trumpProperty() {
        return this.trump;
    }

    /**
     * Set the trump to a new value.
     *
     * @param newTrump the new trump color for this trick
     */
    public void setTrump(Card.Color newTrump) {
        this.trump.set(newTrump);
    }

    /**
     * @return the read-only property for the player winning this truck. null if no player is winning
     */
    public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
        return this.winningPlayer;
    }

    /**
     * Get an observable read-only map from each Player to the card they.
     *
     * @return a map from each Player to the card they have, or null if they don't
     */
    public ObservableMap<PlayerId, Card> trick() {
        return FXCollections.unmodifiableObservableMap(this.cards);
    }

    /**
     * Set the observable properties of this trick based on a Trick.
     * This will set the winning player if it exists, otherwise.
     *
     * @param newTrick the new state of the trick
     */
    public void setTrick(Trick newTrick) {
        if (newTrick.isEmpty()) {
            this.winningPlayer.set(null);
        } else {
            this.winningPlayer.set(newTrick.winningPlayer());
        }
        // We only want to commit one version of things
        Map<PlayerId, Card> tmp = new EnumMap<>(PlayerId.class);
        for (PlayerId p : PlayerId.ALL) {
            tmp.put(p, null);
        }
        for (int i = 0; i < newTrick.size(); ++i) {
            tmp.put(newTrick.player(i), newTrick.card(i));
        }
        this.cards.putAll(tmp);
    }
}
