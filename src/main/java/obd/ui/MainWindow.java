package obd.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
        FuelTrim mostrarLambda = new FuelTrim(obdConnection, 1);

        Label statusConexao = new Label();
        statusConexao.setStyle("-fx-font-size: 12px; -fx-text-fill: #1D9E75;");

        Pane divisor = new Pane();
        divisor.setStyle("-fx-background-color: #1a1a1a;");
        divisor.setPrefHeight(1);
        divisor.setMaxWidth(Double.MAX_VALUE);

        Label titulo = new Label("ProjectCarScanner");

        titulo.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        VBox layout = new VBox(16);
        layout.setStyle("-fx-background-color: #121212; -fx-padding: 24;");

        SensorCard cardRpm = new SensorCard("RPM", "rpm", 6800);
        SensorCard cardTps = new SensorCard("TPS", "%", 100);
        SensorCard cardTensao = new SensorCard("Tensão", "V", 14.5);
        SensorCard cardLambda = new SensorCard("Lambda", "λ", 1.99);

        cardRpm.valor.setText("1500");
        cardLambda.valor.setText("1.00");
        cardTensao.valor.setText("13.8v");
        cardTps.valor.setText("11.8%");

        HBox cardsRow = new HBox(12); // 12 = espaço entre cards

        cardsRow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cardRpm.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardTps.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardTensao.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardLambda.card, javafx.scene.layout.Priority.ALWAYS);

        cardsRow.getChildren().addAll(cardRpm.card, cardTps.card, cardTensao.card, cardLambda.card);

        layout.getChildren().addAll(statusConexao, titulo, divisor, cardsRow); // adiciona tudo junto

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("ProjectCarScanner");
        stage.show();

        new Thread(() -> {
            // primeiro conecta
            if (!obdConnection.detectarEConectar()) {
                Platform.runLater(() -> {
                    statusConexao.setText("● nenhum dispositivo encontrado");
                    statusConexao.setStyle("-fx-font-size: 12px; -fx-text-fill: #E24B4A;");
                });
                return; // para aqui se não conectou
            }

            Platform.runLater(() ->
                    statusConexao.setText("● conectado — " + obdConnection.getPortName())
            );

            // só depois começa a ler
            while (true) {
                try {
                    double rpm = mostrarRPM.traduzirResposta();
                    double tensao = mostrarTensao.traduzirResposta();
                    double tps = mostrarTPS.traduzirResposta();
                    double lambda = mostrarLambda.traduzirResposta();

                    Platform.runLater(() -> {
                        try {
                            System.out.println("RPM bruto: " + mostrarRPM.respostaObd());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        cardRpm.valor.setText(String.format("%.0f", rpm));
                        cardRpm.barra.setPrefWidth(cardRpm.card.getWidth() * (rpm / cardRpm.valorMax));
                        cardTensao.valor.setText(String.format("%.2f", tensao));
                        cardTensao.barra.setPrefWidth(cardTensao.card.getWidth() * (tensao / cardTensao.valorMax));
                        cardTps.valor.setText(String.format("%.1f", tps));
                        cardTps.barra.setPrefWidth(cardTps.card.getWidth() * (tps / cardTps.valorMax));
                        cardLambda.valor.setText(String.format("%.2f", lambda));
                        cardLambda.barra.setPrefWidth(cardLambda.card.getWidth() * (lambda / cardLambda.valorMax));

                    });
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println("Erro: " + e.getMessage());
                }
            }
        }).start();
    }
}
