package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.EnumMap;
import java.util.Map;

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

    public ObservableBooleanValue announcesVisible() {
        return this.announcesVisible;
    }

    public ObservableList<Card> announces(PlayerId player) {
        return FXCollections.unmodifiableObservableList(this.announces.get(player));
    }

    public ReadOnlyIntegerProperty points(PlayerId player) {
        return points.get(player);
    }

    public ReadOnlyObjectProperty<TeamId> winningTeam() {
        return this.winningTeam;
    }


    public void setAnnouncesVisible(boolean value) {
        this.announcesVisible.setValue(value);
    }

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

    public void setWinningTeam(TeamId team) {
        this.winningTeam.setValue(team);
    }
}
