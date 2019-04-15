package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public class GraphicalPlayer {
    private final Scene mainScene;

    public GraphicalPlayer(PlayerId player, Map<PlayerId, String> names, ScoreBean score, TrickBean trick) {
        Pane scorePane = createScorePane(score, names);
        mainScene = new Scene(scorePane);
    }

    public Stage createStage() {
        Stage stage = new Stage();
        stage.setScene(mainScene);
        return stage;
    }

    private static String getTeamName(TeamId team, Map<PlayerId, String> names) {
        StringJoiner j = new StringJoiner(" et ");
        for (PlayerId p : PlayerId.ALL) {
            if (p.team() == team) {
                j.add(names.get(p));
            }
        }
        return j.toString() + " : ";
    }

    private static Text[] getTeamScores(ScoreBean score, TeamId team, Map<PlayerId, String> names) {
        Text[] texts = new Text[5];
        texts[0] = new Text(getTeamName(team, names));
        texts[0].setTextAlignment(TextAlignment.RIGHT);
        texts[1] = new Text();
        texts[1].textProperty().bind(Bindings.convert(score.turnPointsProperty(team)));
        texts[1].setTextAlignment(TextAlignment.RIGHT);
        StringProperty diff = new SimpleStringProperty();
        score.turnPointsProperty(team).addListener((o, old, nw) -> {
            int d = nw.intValue() - old.intValue();
            diff.setValue(d == 0 ? "" : " (+" + d + ")");
        });
        texts[2] = new Text();
        texts[2].textProperty().bind(diff);
        texts[2].setTextAlignment(TextAlignment.LEFT);
        texts[3] = new Text(" / Total : ");
        texts[3].setTextAlignment(TextAlignment.LEFT);
        texts[4] = new Text();
        texts[4] = new Text();
        texts[4].textProperty().bind(Bindings.convert(score.totalPointsProperty(team)));
        texts[4].setTextAlignment(TextAlignment.RIGHT);
        return texts;
    }

    private static Pane createScorePane(ScoreBean score, Map<PlayerId, String> names) {
        GridPane scorePane = new GridPane();
        scorePane.addRow(0, getTeamScores(score, TeamId.TEAM_1, names));
        scorePane.addRow(1, getTeamScores(score, TeamId.TEAM_2, names));
        scorePane.setStyle("-fx-font: 16 Optima; -fxbackground-color: lightgray; -fx-padding: 5px; -fx-alignment: center;");
        return scorePane;
    }

    private static Pane createTrickPane(TrickBean trick) {
        GridPane trickPane = new GridPane();
        return trickPane;
    }

    private void createVictoryPane() {
    }
}
