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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

public class Wizard extends Application {
    private final int MENU_IMAGE_DEFAULT_H = 100;
    private final int MENU_IMAGE_DEFAULT_W = 309;

    private RemoteMain remoteMain = null;

    private enum View {
        CHOICE("Menu", 2.5),
        LOCAL("Créer une partie local", 1.5),
        REMOTE("Se connecter à une partie", 1.5);

        private String string;
        private double menuImageScaleFactor;

        View(String string, double menuImageScaleFactor) {
            this.string = string;
            this.menuImageScaleFactor = menuImageScaleFactor;
        }

        public double getMenuImageScaleFactor() {
            return menuImageScaleFactor;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private ObjectProperty<View> currentView = new SimpleObjectProperty<>();
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setFullScreen(true);

        currentView.addListener(observable -> primaryStage.setTitle("Javass - " + currentView.get().toString()));
        currentView.setValue(View.CHOICE);

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
        vBox.getChildren().addAll(
                menuImageCreator((int) (MENU_IMAGE_DEFAULT_H * View.CHOICE.getMenuImageScaleFactor()), (int) (MENU_IMAGE_DEFAULT_W * View.CHOICE.getMenuImageScaleFactor())),
                localButton,
                remoteButton);

        return vBox;
    }

    private Pane createLocalPane() {
        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        List<SimpleObjectProperty<String>> args = Arrays.asList(
                new SimpleObjectProperty<>(""),
                new SimpleObjectProperty<>(""),
                new SimpleObjectProperty<>(""),
                new SimpleObjectProperty<>("")
        );
        List<SimpleObjectProperty<Config.Item>> items = new ArrayList<>();
        for (Config.Item item : Config.fromDefaultPath().getItems()) {
            items.add(new SimpleObjectProperty<>(item));
        }

        Pane player1 = selectPlayer(
                args.get(0),
                items.get(0),
                new SimpleObjectProperty<>(false)
        );
        Pane player2 = selectPlayer(
                args.get(1),
                items.get(1),
                new SimpleObjectProperty<>(false)
        );

        Pane player3 = selectPlayer(
                args.get(2),
                items.get(2),
                new SimpleObjectProperty<>(false)
        );
        Pane player4 = selectPlayer(
                args.get(3),
                items.get(3),
                new SimpleObjectProperty<>(false)
        );

        Text errorText = new Text();
        errorText.setFill(Color.RED);

        Button okButton = new Button();
        okButton.textProperty().set("Lancer la partie");
        okButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        okButton.setOnAction(e -> {
            int humanCount = 0;
            for (int i = 0; i < args.size(); ++i) {
                String argValue = args.get(i).getValue();
                String error = PlayerBuilder.validatePlayer(argValue);
                if (error != null) {
                    errorText.setText("Joueur " + (i + 1) + " : " + error);
                    return;
                }
                if (argValue.startsWith("h")) {
                    if (++humanCount >= 2) {
                        errorText.setText("Il ne peut y avoir qu'un joueur humain");
                        return;
                    }
                }
            }
            List<Config.Item> itemValues = new ArrayList<>(items.size());
            for (SimpleObjectProperty<Config.Item> item : items) {
                itemValues.add(item.getValue());
            }
            Config config = Config.fromItems(itemValues);
            config.save();
            List<String> stringArgs = new ArrayList<>(args.size());
            for (SimpleObjectProperty<String> arg : args) {
                stringArgs.add(arg.getValue());
            }
            LocalMain lm = new LocalMain(stringArgs);
            lm.start(primaryStage);
        });

        Button backButton = new Button();
        backButton.textProperty().set("retour");
        backButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        backButton.setOnAction(e -> currentView.setValue(View.CHOICE));

        vBox.getChildren().add(menuImageCreator((int) (MENU_IMAGE_DEFAULT_H * View.LOCAL.getMenuImageScaleFactor()), (int) (MENU_IMAGE_DEFAULT_W * View.LOCAL.getMenuImageScaleFactor())));
        vBox.getChildren().addAll(player1, player2, player3, player4, errorText, okButton, backButton);
        return vBox;
    }

    private Pane selectPlayer(ObjectProperty<String> arg, ObjectProperty<Config.Item> itemProp, ObjectProperty<Boolean> isHuman) {
        Config.Item item = itemProp.getValue();
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        ComboBox<String> type = new ComboBox<>();
        type.getItems().add("humain");
        type.getItems().add("distant");
        type.getItems().add("simulé");
        type.setValue(item.type);

        isHuman.bind(Bindings.equal(type.valueProperty(), "humain"));
        hBox.getChildren().add(new Text("type de joueur:"));
        hBox.getChildren().add(type);


        TextField nameField = new TextField(item.name);
        nameField.setMinWidth(80);
        hBox.getChildren().add(new Text("nom:"));
        hBox.getChildren().add(nameField);

        HBox iterations = new HBox(10);
        TextField iterationsField = new TextField(item.iterations);
        iterationsField.setMaxWidth(80);
        nameField.setPrefWidth(60);
        iterations.getChildren().add(new Text("itérations:"));
        iterations.getChildren().add(iterationsField);

        HBox ip = new HBox(10);
        TextField ipField = new TextField(item.ip);
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
            switch (typeS) {
                case "humain":
                    sb.add("h");
                    break;
                case "simulé":
                    sb.add("s");
                    break;
                case "distant":
                    sb.add("r");
                    break;
            }

            String nameS = nameField.getText();
            if (nameS.isEmpty()) {
                arg.setValue("");
                return;
            }
            sb.add(nameS);

            if (typeS.equals("simulé")) {
                String iterS = iterationsField.getText();
                sb.add(iterS);
            } else if (typeS.equals("distant")) {
                String ipS = ipField.getText();
                sb.add(ipS);
            }
            arg.setValue(sb.toString());

            itemProp.setValue(new Config.Item(typeS, nameS, iterationsField.getText(), ipField.getText()));
        };
        runnable.run();

        type.valueProperty().addListener(c -> runnable.run());
        nameField.textProperty().addListener(c -> runnable.run());
        iterationsField.textProperty().addListener(c -> runnable.run());
        ipField.textProperty().addListener(c -> runnable.run());
        return hBox;
    }

    private Pane createRemotePane() {

        currentView.addListener(o -> {
            if (currentView.get().equals(View.REMOTE)) {
                remoteMain = new RemoteMain();
                remoteMain.start(primaryStage);
            } else if (remoteMain != null) {
                try {
                    remoteMain.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Text t = new Text();
        t.setFont(new Font(20));
        t.setTextAlignment(TextAlignment.CENTER);
        try {
            t.setText("Transmettez votre addresse ip à l'hôte de la partie:\n" + getIP());
        } catch (Exception e) {
            t.setText("Adresse ip inconnue");
        }
        Button backButton = new Button();
        backButton.textProperty().set("retour");
        backButton.setStyle("-fx-font-size: 20px; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        backButton.setOnAction(e -> {
            currentView.setValue(View.CHOICE);
        });

        VBox vBox = new VBox(30);
        vBox.getChildren().add(menuImageCreator((int) (MENU_IMAGE_DEFAULT_H * View.REMOTE.getMenuImageScaleFactor()), (int) (MENU_IMAGE_DEFAULT_W * View.REMOTE.getMenuImageScaleFactor())));
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(t, backButton);
        return vBox;
    }

    private ImageView menuImageCreator(int height, int width) {
        ImageView imageMenu = new ImageView("/menu.png");
        imageMenu.setFitHeight(height);
        imageMenu.setFitWidth(width);
        return imageMenu;
    }

    private String getIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress current_addr = addresses.nextElement();
                    if (!current_addr.isLoopbackAddress() && current_addr instanceof Inet4Address) {
                        return current_addr.getHostAddress();
                    }
                }
            }
        } catch (IOException e) {
            // We want to return anyways
        }
        return "Addresse ip inconnue";
    }
}
