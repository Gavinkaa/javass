package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Represents a JavaFX bean allowing us to observe changes
 * to the score of game.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class ScoreBean {
    // We use 2 value arrays for each team property
    private final SimpleIntegerProperty[] turnPoints = {
            new SimpleIntegerProperty(0),
            new SimpleIntegerProperty(0)
    };
    private final SimpleIntegerProperty[] gamePoints = {
            new SimpleIntegerProperty(0),
            new SimpleIntegerProperty(0)
    };
    private final SimpleIntegerProperty[] totalPoints = {
            new SimpleIntegerProperty(0),
            new SimpleIntegerProperty(0)
    };
    // null indicates no winning team yet
    private final SimpleObjectProperty<TeamId> winningTeam = new SimpleObjectProperty<>();

    /**
     * Get the readonly property representing the turn points for a team
     *
     * @param team the team to look at
     * @return a property to observe changes in turn points for that team
     */
    public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
        return this.turnPoints[team.ordinal()];
    }

    /**
     * Set the turn points for a team in an observable way
     *
     * @param team          the team to set the turn points for
     * @param newTurnPoints the new value for their turn points
     */
    public void setTurnPoints(TeamId team, int newTurnPoints) {
        this.turnPoints[team.ordinal()].set(newTurnPoints);
    }

    /**
     * Get the gamePoints for a team in an observable way
     *
     * @param team the team to observer
     * @return the property we can listen to for changes in their game points
     */
    public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
        return this.gamePoints[team.ordinal()];
    }

    /**
     * Set the game points for a team in an observable way
     *
     * @param team          the team to set the game points of
     * @param newGamePoints the new value for the game points
     */
    public void setGamePoints(TeamId team, int newGamePoints) {
        this.gamePoints[team.ordinal()].set(newGamePoints);
    }

    /**
     * Get the observable property for the total points of a tea            new SimpleIntegerProperty(0),m
     *
     * @param team the team we care about accessing
     * @return a read-only property containing their total points
     */
    public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
        return this.totalPoints[team.ordinal()];
    }

    /**
     * Change the total points of a team in an observable way.
     *
     * @param team           the team to change the total points of
     * @param newTotalPoints the new value for the total points
     */
    public void setTotalPoints(TeamId team, int newTotalPoints) {
        this.totalPoints[team.ordinal()].set(newTotalPoints);
    }

    /**
     * Get the property containing the winning team.
     *
     * @return the winning team, with null representing no win yet
     */
    public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
        return this.winningTeam;
    }

    /**
     * Set the winning team in an observable way.
     *
     * @param winningTeam the team that has just won the game
     */
    public void setWinningTeam(TeamId winningTeam) {
        this.winningTeam.set(winningTeam);
    }
}
