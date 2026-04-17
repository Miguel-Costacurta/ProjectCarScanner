package obd.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import obd.connection.HomoObdConnection;
import obd.connection.IObdConnection;
import obd.core.sensors.*;
import obd.core.pidscanner.PidScanner;

public class MainWindow extends Application {

    @Override
    public void start(Stage stage) {

        IObdConnection obdConnection = new ObdConnection();

        PidScanner scanner = new PidScanner(obdConnection);
        RPM mostrarRPM = new RPM(obdConnection);
        Tensao mostrarTensao = new Tensao(obdConnection);
        TPS mostrarTPS = new TPS(obdConnection);
        FuelTrim mostrarLambda = new FuelTrim(obdConnection, 1);
        SparkAdvance mostrarAvanco = new SparkAdvance(obdConnection);
        Velocity mostrarVelocidade = new Velocity(obdConnection);



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
        SensorCard cardSpark = new SensorCard("Avanço Ign.", "°",40);
        SensorCard cardVelocity = new SensorCard("Velocidade", "Km/h", 250);

        cardRpm.valor.setText("1500");
        cardLambda.valor.setText("1.00");
        cardTensao.valor.setText("13.8v");
        cardTps.valor.setText("11.8%");
        cardSpark.valor.setText("0");
        cardVelocity.valor.setText("0");

        HBox cardsRow = new HBox(12); // 12 = espaço entre cards

        cardsRow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cardRpm.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardTps.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardTensao.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardLambda.card, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cardSpark.card, Priority.ALWAYS);
        HBox.setHgrow(cardVelocity.card, Priority.ALWAYS);

        cardsRow.getChildren().addAll(cardRpm.card, cardTps.card, cardTensao.card, cardLambda.card, cardSpark.card, cardVelocity.card);

        layout.getChildren().addAll(statusConexao, titulo, divisor, cardsRow);

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("ProjectCarScanner");
        stage.show();

        Thread thread = new Thread(() -> {

            if (!obdConnection.detectarEConectar()) {
                Platform.runLater(() -> {
                    statusConexao.setText("● nenhum dispositivo encontrado");
                    statusConexao.setStyle("-fx-font-size: 12px; -fx-text-fill: #E24B4A;");
                });
                return;
            }

            Platform.runLater(() ->
                    statusConexao.setText("● conectado — " + obdConnection.getPortName())
            );

            try {
                Thread.sleep(500); // ← deixa o ELM estabilizar
            } catch (Exception e) {
                System.out.println("Erro ");;
            }

            try {
                scanner.scanear();
            } catch (Exception e) {
                System.out.println("Erro no scanner: " + e.getMessage());
            }
            LeituraObd leituraObd = new LeituraObd(obdConnection,mostrarTPS, mostrarRPM,mostrarLambda,mostrarTensao,mostrarAvanco,mostrarVelocidade, (rpm, tps, tensao, fuelTrim,spark,velocity) ->{

                Platform.runLater(()->{
                    cardRpm.valor.setText(String.format("%.0f", rpm));
                    cardLambda.valor.setText(String.format("%.2f", fuelTrim));
                    cardTps.valor.setText(String.format("%.2f", tps));
                    cardTensao.valor.setText(String.format("%.1f", tensao));
                    cardSpark.valor.setText(String.format("%d", spark));
                    cardVelocity.valor.setText(String.format("%.0f",velocity));
                });
            });
            stage.setOnCloseRequest(e -> leituraObd.stop());
            leituraObd.getResponse();
        });
        thread.setDaemon(true);
        thread.start();
    }
}
