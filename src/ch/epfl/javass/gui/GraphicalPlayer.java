package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Shadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * This contains the GUI for the game.
 * <p>
 * The gui can be used to let the player see a representation of the events
 * happening in the game, as well as advance the game by choosing the next card to play.
 * Which card the player clicked on is communicated via a {@link BlockingQueue}, that is
 * passed to the gui. Each time the player selects a new card to player, a card is pushed
 * onto that queue.
 * </p>
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public class GraphicalPlayer {
    private static final int BIG_IMAGE_SIZE_W = 240;
    private static final int BIG_IMAGE_SIZE_H = 360;
    private static final int SMALL_IMAGE_SIZE_W = 160;
    private static final int SMALL_IMAGE_SIZE_H = 240;
    private static final int TRUMP_IMAGE_SIZE = 202;
    private static final int TRICK_PANE_GAP = 10;
    private static final int TRICK_PANE_SPACING = 4;
    private static final double PLAYABLE_OPACITY = 1.0;
    private static final double UNPLAYABLE_OPACITY = 0.2;
    private final Scene mainScene;
    private final BlockingQueue<Card> cardQ;
    private final BlockingQueue<Integer> trumpQ;
    private final BlockingQueue<CardSet> announceQ;
    private final String ownName;
    private CardSet announceSet;

    /**
     * Create a new GUI given all the information it needs.
     * <p>
     * We need static information in order to display it correctly, such as
     * information about the names of each player. We also need dynamic information,
     * passed to us via beans, about the current state of the scores, the on-going trick,
     * and the hands of each player. Finally, we need queues to communicate player selection.
     *
     * @param player      the id of the player this gui is for
     * @param names       a map of names for each playerID
     * @param cardQ       the queue to communicate card selection on
     * @param trumpQ      the queue to communicate the trump on
     * @param canDelegate a bean telling us whether or not the player can delegate
     * @param score       the score bean to keep track of the current state of the scores
     * @param trick       the trick bean to keep track of the current trick state
     * @param hand        the hand bean to keep track of the current state of the hand
     */
    public GraphicalPlayer(PlayerId player, Map<PlayerId, String> names, BlockingQueue<Card> cardQ, BlockingQueue<Integer> trumpQ, BlockingQueue<CardSet> announceQ, ObservableBooleanValue mustChooseTrump, ObservableBooleanValue canDelegate, ObservableBooleanValue canAnnounce, ScoreBean score, TrickBean trick, HandBean hand, AnnounceBean announceBean) {
        this.announceSet = CardSet.EMPTY;
        this.cardQ = cardQ;
        this.trumpQ = trumpQ;
        this.announceQ = announceQ;
        this.ownName = names.get(player);
        BorderPane mainView = new BorderPane();
        mainView.setTop(createScorePane(names, score));
        StackPane center = new StackPane();
        BooleanProperty announceChoicePaneProprety = new SimpleBooleanProperty(false);
        Pane trickPane = createTrickPane(player, names, trick, canAnnounce, announceChoicePaneProprety);
        trickPane.visibleProperty().bind(Bindings.not(mustChooseTrump));
        Pane trumpPane = createTrumpPane(canDelegate);
        trumpPane.visibleProperty().bind(mustChooseTrump);
        trumpPane.disableProperty().bind(Bindings.not(mustChooseTrump));
        center.getChildren().addAll(trickPane, trumpPane);
        mainView.setCenter(center);
        mainView.setBottom(createHandPane(hand, canAnnounce));
        Pane victory = createVictoryPane(names, score);
        victory.visibleProperty().bind(Bindings.isNotNull(score.winningTeamProperty()));
        Pane announceChoicePane = createChoiceAnnouncePane(hand, announceChoicePaneProprety);
        announceChoicePane.visibleProperty().bind(announceChoicePaneProprety);
        Pane announceResult = createAnnounceResult(announceBean, names);
        announceResult.visibleProperty().bind(announceBean.announcesVisible());
        //announceResult.visibleProperty().setValue(true);
        StackPane view = new StackPane();
        view.getChildren().addAll(mainView, victory, announceChoicePane, announceResult);
        this.mainScene = new Scene(view);
    }

    /**
     * In order to be able to see this GUI, we need to add its elements to a stage
     *
     * @param stage the stage to add this to
     * @return the stage this graphics have been added to
     */
    public Stage addToStage(Stage stage) {
        stage.setScene(this.mainScene);
        stage.setFullScreen(true);
        stage.titleProperty().setValue("Javass - " + this.ownName);
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
            diff.setValue(d <= 0 ? "" : " (+" + d + ")");
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
        int size = big ? BIG_IMAGE_SIZE_W : SMALL_IMAGE_SIZE_W;
        String s = String.format("/card_%d_%d_%d.png", card.color().ordinal(), card.rank().ordinal(), size);
        return new Image(s);
    }

    private static ObservableMap<Card, Image> makeCardImages(boolean big) {
        ObservableMap<Card, Image> cardImages = FXCollections.observableHashMap();
        for (Card.Rank r : Card.Rank.ALL) {
            for (Card.Color c : Card.Color.ALL) {
                Card card = Card.of(c, r);
                cardImages.put(card, getCardImage(card, big));
            }
        }
        return cardImages;
    }

    private static ObservableMap<Card, Image> bigCardImages = makeCardImages(true);
    private static ObservableMap<Card, Image> smallCardImages = makeCardImages(false);

    private static Pane createTrickPane(PlayerId me, Map<PlayerId, String> names, TrickBean trick, ObservableBooleanValue canAnnounce, BooleanProperty announceChoicePaneProprety) {
        List<PlayerId> players = new ArrayList<>(PlayerId.ALL);
        Collections.rotate(players, -me.ordinal());
        GridPane trickPane = new GridPane();
        trickPane.setHgap(TRICK_PANE_GAP);
        trickPane.setVgap(TRICK_PANE_GAP);
        int[] cols = {1, 2, 1, 0};
        int[] rows = {2, 0, 0, 0};
        int[] rowSpans = {1, 3, 1, 3};


        for (int i = 0; i < PlayerId.COUNT; ++i) {
            PlayerId player = players.get(i);
            VBox pane = new VBox();
            pane.setSpacing(TRICK_PANE_SPACING);
            StackPane imageLayers = new StackPane();
            ImageView v = new ImageView();
            v.setFitHeight(BIG_IMAGE_SIZE_H / 2);
            v.setFitWidth(BIG_IMAGE_SIZE_W / 2);
            ObjectBinding<Image> image = Bindings.valueAt(bigCardImages, Bindings.valueAt(trick.trick(), player));
            v.imageProperty().bind(image);
            FadeTransition fadeTransition = new FadeTransition();
            fadeTransition.setNode(v);
            fadeTransition.setDuration(Duration.millis(500));
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            image.addListener((obs, old, nw) -> {
                if (old == null && nw != null) {
                    fadeTransition.play();
                }
            });
            Rectangle r = new Rectangle();
            r.setHeight(BIG_IMAGE_SIZE_H / 2);
            r.setWidth(BIG_IMAGE_SIZE_W / 2);
            r.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent; -fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5;");
            r.setEffect(new GaussianBlur(4));
            r.setVisible(true);
            r.visibleProperty().bind(trick.winningPlayerProperty().isEqualTo(player));
            imageLayers.getChildren().addAll(r, v);
            Text txt = new Text(names.get(player));
            txt.setStyle("-fx-font: 14 Optima;");
            if (i == 0) {

                Button announceButton = new Button("Faire une annonce");
                announceButton.visibleProperty().bind(canAnnounce);
                announceButton.setOnAction((actionEvent) -> announceChoicePaneProprety.setValue(true));

                pane.getChildren().addAll(announceButton, imageLayers, txt);
            } else {
                pane.getChildren().addAll(txt, imageLayers);
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
        trumpView.setFitHeight(TRUMP_IMAGE_SIZE / 2);
        trumpView.setFitWidth(TRUMP_IMAGE_SIZE / 2);
        trumpView.imageProperty().bind(trumpImage);
        trickPane.add(trumpView, 1, 1, 1, 1);
        trickPane.setStyle("-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px; -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;");
        return trickPane;
    }

    private Pane createChoiceAnnouncePane(HandBean hand, BooleanProperty announceChoicePaneProprety) {
        HBox cards = new HBox();
        cards.setStyle("-fx-background-color: lightgray");
        cards.setAlignment(Pos.CENTER);
        for (int i = 0; i < 9; ++i) {
            ImageView view = new ImageView();
            ObjectBinding<Card> thisCard = Bindings.valueAt(hand.hand(), i);
            view.imageProperty().bind(Bindings.valueAt(smallCardImages, thisCard));
            view.setFitWidth(SMALL_IMAGE_SIZE_W / 2);
            view.setFitHeight(SMALL_IMAGE_SIZE_H / 2);
            view.setOpacity(UNPLAYABLE_OPACITY);

            final int thisI = i;
            view.setOnMouseClicked(e -> {
                Card card = hand.hand().get(thisI);
                if (announceSet.contains(card)) {
                    announceSet = announceSet.remove(card);
                    view.opacityProperty().setValue(UNPLAYABLE_OPACITY);
                } else {
                    announceSet = announceSet.add(card);
                    view.opacityProperty().setValue(PLAYABLE_OPACITY);
                }
            });
            view.setOnMouseEntered((e) -> {
                view.setFitWidth(SMALL_IMAGE_SIZE_W / 1.5);
                view.setFitHeight(SMALL_IMAGE_SIZE_H / 1.5);
            });
            view.setOnMouseExited((e) -> {
                view.setFitWidth(SMALL_IMAGE_SIZE_W / 2);
                view.setFitHeight(SMALL_IMAGE_SIZE_H / 2);
            });

            HBox card = new HBox(view);
            card.setMinHeight(SMALL_IMAGE_SIZE_H / 1.5);
            card.setMinWidth(SMALL_IMAGE_SIZE_W / 1.5);
            card.setAlignment(Pos.CENTER);
            cards.getChildren().add(card);
        }

        Button done = new Button("Valider le choix");
        done.setOnAction((o) -> announceChoicePaneProprety.setValue(false));

        VBox pane = new VBox(cards, done);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color: lightgray; -fx-spacing: 10px; -fx-padding: 5px");

        return pane;
    }


    private Pane createTrumpPane(ObservableBooleanValue canDelegate) {
        HBox trumps = new HBox();
        trumps.setAlignment(Pos.CENTER);
        trumps.setSpacing(30);
        for (Card.Color c : Card.Color.ALL) {
            ImageView trumpView = new ImageView();
            trumpView.setImage(new Image("/trump_" + c.ordinal() + ".png"));
            trumpView.setFitHeight(101);
            trumpView.setFitWidth(101);
            trumpView.setOnMouseClicked(e -> {
                try {
                    this.trumpQ.put(c.ordinal());
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
            trumps.getChildren().add(trumpView);
        }
        Button delegate = new Button();
        delegate.textProperty().set("Chibrer");
        delegate.visibleProperty().bind(canDelegate);
        delegate.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        delegate.setOnAction(e -> {
            try {
                this.trumpQ.put(100);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(50);
        box.getChildren().addAll(trumps, delegate);
        return box;
    }

    private Pane createHandPane(HandBean hand, ObservableBooleanValue canAnnounce) {
        HBox pane = new HBox();
        pane.setStyle("-fx-background-color: lightgray; -fx-spacing: 5px; -fx-padding: 5px");
        pane.setAlignment(Pos.CENTER);
        for (int i = 0; i < 9; ++i) {
            ImageView view = new ImageView();
            ObjectBinding<Card> thisCard = Bindings.valueAt(hand.hand(), i);
            view.imageProperty().bind(Bindings.valueAt(smallCardImages, thisCard));
            view.setFitWidth(SMALL_IMAGE_SIZE_W / 2);
            view.setFitHeight(SMALL_IMAGE_SIZE_H / 2);
            final int thisI = i;
            view.setOnMouseClicked(e -> {
                Card card = hand.hand().get(thisI);
                try {
                    this.cardQ.put(card);
                    if (canAnnounce.get()) {
                        this.announceQ.put(announceSet);
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
            BooleanBinding isPlayable = Bindings.createBooleanBinding(() -> hand.playableCards().contains(thisCard.get()), hand.playableCards(), hand.hand());
            view.opacityProperty().bind(Bindings.when(isPlayable).then(PLAYABLE_OPACITY).otherwise(UNPLAYABLE_OPACITY));
            view.disableProperty().bind(Bindings.not(isPlayable));
            pane.getChildren().add(view);
        }
        return pane;
    }

    private Pane createVictoryPane(Map<PlayerId, String> names, ScoreBean score) {
        ObservableMap<TeamId, String> teamPlayerNames = FXCollections.observableHashMap();
        teamPlayerNames.put(TeamId.TEAM_1, names.get(PlayerId.PLAYER_1) + " et " + names.get(PlayerId.PLAYER_3));
        teamPlayerNames.put(TeamId.TEAM_2, names.get(PlayerId.PLAYER_2) + " et " + names.get(PlayerId.PLAYER_4));

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-font: 16 Optima; -fx-background-color: white");
        Text txt = new Text();
        ObservableValue<String> players = Bindings.valueAt(teamPlayerNames, score.winningTeamProperty());
        ReadOnlyIntegerProperty score1 = score.totalPointsProperty(TeamId.TEAM_1);
        ReadOnlyIntegerProperty score2 = score.totalPointsProperty(TeamId.TEAM_2);
        txt.textProperty().bind(Bindings.format(
                "%s ont gagné avec %d contre %d",
                players,
                Bindings.max(score1, score2),
                Bindings.min(score1, score2)
        ));
        pane.setCenter(txt);
        return pane;
    }

    private Pane createAnnounceResult(AnnounceBean announceBean, Map<PlayerId, String> names) {
        VBox pane = new VBox();
        pane.setAlignment(Pos.CENTER);
        pane.setSpacing(10);

        for (PlayerId playerId : PlayerId.ALL) {
            announceBean.announces(playerId);

            HBox announce = new HBox();
            announce.setSpacing(10);
            announceBean.announcesVisible().addListener(v -> {

                for (Card card : announceBean.announces(playerId)) {
                    ImageView view = new ImageView(smallCardImages.get(card));
                    view.setFitWidth(SMALL_IMAGE_SIZE_W / 4);
                    view.setFitHeight(SMALL_IMAGE_SIZE_H / 4);
                    if (playerId.team() != announceBean.winningTeam().get()){
                        view.setEffect(new Shadow(0, Color.GREY));
                    }
                    announce.getChildren().add(view);
                }
            });

            VBox playerInfo = new VBox();

            Text txt = new Text(names.get(playerId) + " (+0)");
            txt.setStyle("-fx-font: 12 Optima;");
            txt.setFill(Color.RED);

            announceBean.points(playerId).addListener(o -> {
                int points = announceBean.points(playerId).get();
                String name = names.get(playerId);
                txt.textProperty().setValue(name + " (+" + points + ")");
                if (playerId.team() == announceBean.winningTeam().get()) {
                    txt.setFill(Color.GREEN);
                } else {
                    txt.setFill(Color.RED);
                }


            });

            playerInfo.getChildren().addAll(txt, announce);
            playerInfo.setSpacing(5);

            pane.getChildren().add(playerInfo);
        }
        return pane;
    }
}
