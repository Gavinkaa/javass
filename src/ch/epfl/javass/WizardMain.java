package ch.epfl.javass;

import ch.epfl.javass.gui.Wizard;
import javafx.application.Application;
import javafx.stage.Stage;

public class WizardMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Wizard w = new Wizard();
        w.addToStage(primaryStage);
        primaryStage.show();
    }
}
