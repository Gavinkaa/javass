package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public class GraphicalPlayer {
    private final Scene mainScene;

    public GraphicalPlayer(PlayerId player, Map<PlayerId, String> names, ScoreBean score, TrickBean trick) {
        BorderPane border = new BorderPane();
        border.setTop(createScorePane(names, score));
        border.setCenter(createTrickPane(player, names, trick));
        mainScene = new Scene(border);
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

    private static Pane createScorePane(Map<PlayerId, String> names, ScoreBean score) {
        GridPane scorePane = new GridPane();
        scorePane.addRow(0, getTeamScores(score, TeamId.TEAM_1, names));
        scorePane.addRow(1, getTeamScores(score, TeamId.TEAM_2, names));
        scorePane.setStyle("-fx-font: 16 Optima; -fx-background-color: lightgrey; -fx-padding: 5px; -fx-alignment: center;");
        return scorePane;
    }

    private static Image getCardImage(Card card, boolean big) {
        int size = big ? 240 : 160;
        String s = String.format("/card_%d_%d_%d.png", card.color().ordinal(), card.rank().ordinal(), size);
        return new Image(s);
    }

    private static Pane createTrickPane(PlayerId me, Map<PlayerId, String> names, TrickBean trick) {
        List<PlayerId> players = new ArrayList<>(PlayerId.ALL);
        Collections.rotate(players, -me.ordinal());
        GridPane trickPane = new GridPane();
        trickPane.setHgap(10);
        trickPane.setVgap(10);
        int[] cols = {1, 2, 1, 0};
        int[] rows = {2, 0, 0, 0};
        int[] rowSpans = {1, 3, 1, 3};
        ObservableMap<Card, Image> cardImages = FXCollections.observableHashMap();
        for (Card.Rank r : Card.Rank.ALL) {
            for (Card.Color c : Card.Color.ALL) {
                Card card = Card.of(c, r);
                cardImages.put(card, getCardImage(card, true));
            }
        }
        for (int i = 0; i < PlayerId.COUNT; ++i) {
            PlayerId player = players.get(i);
            VBox pane = new VBox();
            pane.setSpacing(4);
            StackPane imageLayers = new StackPane();
            ImageView v = new ImageView();
            v.setFitHeight(180);
            v.setFitWidth(120);
            ObjectBinding<Image> image = Bindings.valueAt(cardImages, Bindings.valueAt(trick.trick(), player));
            v.imageProperty().bind(image);
            Rectangle r = new Rectangle();
            r.setHeight(180);
            r.setWidth(120);
            r.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent; -fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5;");
            r.setEffect(new GaussianBlur(4));
            r.setVisible(true);
            imageLayers.getChildren().addAll(r, v);

            Text txt = new Text(names.get(player));
            txt.setStyle("-fx-font: 14 Optima;");
            if (i == 0) {
                pane.getChildren().addAll(v, txt);
            } else {
                pane.getChildren().addAll(txt, v);
            }
            pane.setStyle("-fx-alignment: center;");
            trickPane.add(pane, cols[i], rows[i], 1, rowSpans[i]);
        }

        ObservableMap<Card.Color, Image> trumpImages = FXCollections.observableHashMap();
        for (Card.Color c : Card.Color.ALL) {
            Image image = new Image("/trump_" + c.ordinal() + ".png");
            trumpImages.put(c, image);
        }
        ObjectBinding<Image> trumpImage = Bindings.valueAt(trumpImages, trick.trumpProperty());
        ImageView trumpView = new ImageView();
        GridPane.setHalignment(trumpView, HPos.CENTER);
        trumpView.setFitHeight(101);
        trumpView.setFitWidth(101);
        trumpView.imageProperty().bind(trumpImage);
        trickPane.add(trumpView, 1, 1, 1, 1);
        trickPane.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px; -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");
        return trickPane;
    }

    private void createVictoryPane() {
    }
}
