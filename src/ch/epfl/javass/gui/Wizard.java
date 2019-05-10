package ch.epfl.javass.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Wizard {
    private enum View{
        CHOICE,
        LOCAL,
        REMOTE;
    }

    private ObjectProperty<View> currentView = new SimpleObjectProperty<>(View.LOCAL);
    private Scene mainScene;


    public Wizard() {
        Pane choicePane = createChoicePane();
        choicePane.visibleProperty().bind(Bindings.equal(currentView, View.CHOICE));
        Pane localPane = createLocalPane();
        localPane.visibleProperty().bind(Bindings.equal(currentView, View.LOCAL));
        Pane remotePane = createRemotePane();
        remotePane.visibleProperty().bind(Bindings.equal(currentView, View.REMOTE));

        StackPane view = new StackPane();
        view.getChildren().addAll(choicePane, remotePane, localPane);
        this.mainScene = new Scene(view);
    }

    private Pane createChoicePane(){
        Text t = new Text();
        t.setText("choice");
        return new HBox(t);
    }

    private Pane createLocalPane(){
        Text t = new Text();
        t.setText("local");
        return new HBox(t);
    }

    private Pane createRemotePane(){
        Text t = new Text();
        t.setText("remote");
        return new HBox(t);
    }

    public void addToStage(Stage stage) {
        stage.setScene(mainScene);
    }
}
