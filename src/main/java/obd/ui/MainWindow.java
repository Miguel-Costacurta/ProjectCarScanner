package obd.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.Label;

public class MainWindow extends Application {
    @Override
    public void start(Stage stage) {
        Label titulo = new Label("ProjectCarScanner");

        titulo.setStyle("-fx-font-size: 20px;");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f4f4f4;");
        //layout.getChildren().addAll(titulo);

        VBox cardRpm = new VBox(4);
        cardRpm.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8;");

        Label labelRpm = new Label("RPM");
        labelRpm.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label valorRpm = new Label("--");
        valorRpm.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        cardRpm.getChildren().addAll(labelRpm, valorRpm);


        layout.getChildren().addAll(titulo, cardRpm); // adiciona tudo junto

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("ProjectCarScanner");
        stage.show();
    }
}
