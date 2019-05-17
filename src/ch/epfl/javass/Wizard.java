package ch.epfl.javass;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.StringJoiner;

public class Wizard extends Application {
    private enum View {
        CHOICE,
        LOCAL,
        REMOTE;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private ObjectProperty<View> currentView = new SimpleObjectProperty<>(View.CHOICE);
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setFullScreen(true);

        Pane choicePane = createChoicePane();
        choicePane.visibleProperty().bind(Bindings.equal(currentView, View.CHOICE));
        Pane localPane = createLocalPane();
        localPane.visibleProperty().bind(Bindings.equal(currentView, View.LOCAL));
        Pane remotePane = createRemotePane();
        remotePane.visibleProperty().bind(Bindings.equal(currentView, View.REMOTE));

        StackPane view = new StackPane();
        view.getChildren().addAll(choicePane, remotePane, localPane);
        primaryStage.setScene(new Scene(view));
        primaryStage.show();
    }

    private Pane createChoicePane() {
        Button localButton = new Button();
        Button remoteButton = new Button();

        localButton.textProperty().set("Créer une nouvelle partie");
        localButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        localButton.setOnAction(e -> currentView.setValue(View.LOCAL));

        remoteButton.textProperty().set("Rejoindre une partie");
        remoteButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        remoteButton.setOnAction(e -> currentView.setValue(View.REMOTE));

        VBox vBox = new VBox(50);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(localButton, remoteButton);

        return vBox;
    }

    private Pane createLocalPane() {
        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        SimpleObjectProperty<String> arg1 = new SimpleObjectProperty<>("");
        SimpleObjectProperty<String> arg2 = new SimpleObjectProperty<>("");
        SimpleObjectProperty<String> arg3 = new SimpleObjectProperty<>("");
        SimpleObjectProperty<String> arg4 = new SimpleObjectProperty<>("");


        Pane player1 = selectPlayer(
                arg1,
                new SimpleObjectProperty<>(false),
                "Aline"
        );
        Pane player2 = selectPlayer(
                arg2,
                new SimpleObjectProperty<>(false),
                "Bastien"
        );

        Pane player3 = selectPlayer(
                arg3,
                new SimpleObjectProperty<>(false),
                "Colette"
        );
        Pane player4 = selectPlayer(
                arg4,
                new SimpleObjectProperty<>(false),
                "David"
        );
        Button okButton = new Button();

        okButton.textProperty().set("Lancer la partie");
        okButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        okButton.setOnAction(e -> {
            LocalMain lm = new LocalMain(Arrays.asList(arg1.getValue(), arg2.getValue(), arg3.getValue(), arg4.getValue()));
            lm.start(primaryStage);
        });

        Button backButton = new Button();
        backButton.textProperty().set("retour");
        backButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        backButton.setOnAction(e -> currentView.setValue(View.CHOICE));

        vBox.getChildren().addAll(player1, player2, player3, player4, okButton, backButton);
        return vBox;
    }

    private Pane selectPlayer(ObjectProperty<String> arg, ObjectProperty<Boolean> isHuman, String defaultName) {
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        ComboBox<String> type = new ComboBox<>();
        type.getItems().add("humain");
        type.getItems().add("distant");
        type.getItems().add("simulé");

        isHuman.bind(Bindings.equal(type.valueProperty(), "humain"));
        hBox.getChildren().add(new Text("type de joueur:"));
        hBox.getChildren().add(type);


        TextField nameField = new TextField(defaultName);
        nameField.setMinWidth(80);
        hBox.getChildren().add(new Text("nom:"));
        hBox.getChildren().add(nameField);

        HBox iterations = new HBox(10);
        TextField iterationsField = new TextField("10000");
        iterationsField.setMaxWidth(80);
        nameField.setPrefWidth(60);
        iterations.getChildren().add(new Text("itérations:"));
        iterations.getChildren().add(iterationsField);

        HBox ip = new HBox(10);
        TextField ipField = new TextField("XXX.XXX.XXX.XXX");
        ipField.setPrefWidth(140);
        nameField.setPrefWidth(60);
        ip.getChildren().add(new Text("ip:"));
        ip.getChildren().add(ipField);

        ip.visibleProperty().bind(Bindings.equal(type.valueProperty(), "distant"));
        iterations.visibleProperty().bind(Bindings.equal(type.valueProperty(), "simulé"));
        StackPane optionalFields = new StackPane();
        optionalFields.getChildren().addAll(ip, iterations);
        hBox.getChildren().add(optionalFields);

        Runnable runnable = () -> {
            StringJoiner sb = new StringJoiner(":");
            String typeS = type.getValue();
            if (typeS.equals("humain")) {
                sb.add("h");
            } else if (typeS.equals("simulé")) {
                sb.add("s");
            } else if (typeS.equals("distant")) {
                sb.add("r");
            } else {
                arg.setValue("");
                return;
            }

            String nameS = nameField.getText();
            if (nameS.isEmpty()) {
                arg.setValue("");
                return;
            }
            sb.add(nameS);

            if (typeS.equals("simulé")) {
                String iterS = iterationsField.getText();
                try {
                    Integer.parseInt(iterS);
                } catch (NumberFormatException e) {
                    arg.setValue("");
                    return;
                }
                sb.add(iterS);
            } else if (typeS.equals("distant")) {
                String ipS = ipField.getText();
                if (!ipS.matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}")) {
                    arg.setValue("");
                    return;
                }
                sb.add(ipS);
            }
            arg.setValue(sb.toString());
        };

        type.valueProperty().addListener(c -> runnable.run());
        nameField.textProperty().addListener(c -> runnable.run());
        iterationsField.textProperty().addListener(c -> runnable.run());
        ipField.textProperty().addListener(c -> runnable.run());
        return hBox;
    }

    private Pane createRemotePane() {
        RemoteMain remoteMain = new RemoteMain();
        remoteMain.start(primaryStage);

        Text t = new Text();
        t.setFont(new Font(20));
        t.setTextAlignment(TextAlignment.CENTER);
        try {
            t.setText("Transmettez votre addresse ip à l'hôte de la partie:\n" + InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            t.setText("Adresse ip inconnue");
        }
        Button backButton = new Button();
        backButton.textProperty().set("retour");
        backButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        backButton.setOnAction(e -> {
            currentView.setValue(View.CHOICE);
            try {
                remoteMain.stop();
            } catch (Exception exception){
                throw new IllegalStateException("Can't go back");
            }
        });

        VBox vBox = new VBox(30);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(t, backButton);
        return vBox;
    }
}
