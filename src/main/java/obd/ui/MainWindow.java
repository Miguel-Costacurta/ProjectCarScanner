package obd.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import obd.connection.ObdConnection;
import obd.sensors.FuelTrim;
import obd.sensors.RPM;
import obd.sensors.TPS;
import obd.sensors.Tensao;

import java.io.IOException;

public class MainWindow extends Application {


    @Override
    public void start(Stage stage) {

         ObdConnection obdConnection = new ObdConnection();

         RPM mostrarRPM = new RPM(obdConnection);
         Tensao mostrarTensao = new Tensao(obdConnection);
         TPS mostrarTPS = new TPS(obdConnection);
         FuelTrim mostrarLambda = new FuelTrim(obdConnection,1);

        obdConnection.openConnection();
        Label titulo = new Label("ProjectCarScanner");

        titulo.setStyle("-fx-font-size: 20px;");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f4f4f4;");
        //layout.getChildren().addAll(titulo);

        VBox cardRpm = new VBox(4);

        Label labelRpm = new Label("RPM");
        labelRpm.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label valorRpm = new Label("--");
        valorRpm.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        cardRpm.getChildren().addAll(labelRpm, valorRpm);

        VBox cardTps = new VBox(4);

        Label labelTps = new Label("TPS");
        labelTps.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label valorTps = new Label("--");
        valorTps.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        cardTps.getChildren().addAll(labelTps, valorTps);

        VBox cardTensao = new VBox(4);

        Label labelTensao = new Label("TENSÃO");
        labelTensao.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label valorTensao = new Label("--");
        valorTensao.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        cardTensao.getChildren().addAll(labelTensao, valorTensao);

        VBox cardLambda = new VBox(4);

        Label labelLamba = new Label("Lambda");
        labelLamba.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label valorLambda = new Label("--");
        valorLambda.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        cardLambda.getChildren().addAll(labelLamba, valorLambda);

        String estiloCard = "-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8;";
        cardRpm.setStyle(estiloCard);
        cardTps.setStyle(estiloCard);
        cardTensao.setStyle(estiloCard);
        cardLambda.setStyle(estiloCard);

        HBox cardsRow = new HBox(12); // 12 = espaço entre cards
        cardsRow.getChildren().addAll(cardRpm, cardTps, cardTensao, cardLambda);

        layout.getChildren().addAll(titulo, cardsRow); // adiciona tudo junto

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("ProjectCarScanner");
        stage.show();

        new Thread(() ->{
            while(true){
                try {
                    double rpm = mostrarRPM.traduzirResposta();
                    double tensao = mostrarTensao.traduzirResposta();
                    double tps = mostrarTPS.traduzirResposta();
                    double lambda =  mostrarLambda.traduzirResposta();

                    Platform.runLater(() -> {
                        valorRpm.setText(String.format("%.0f", rpm));
                        valorTensao.setText(String.format("%.2f", tensao));
                        valorTps.setText(String.format("%.1f", tps));
                        valorLambda.setText(String.format("%.2f", lambda));
                        }
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        ).start();

    }
}
