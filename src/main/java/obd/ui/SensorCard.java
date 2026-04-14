package obd.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SensorCard {
    public VBox card;
    public Label valor;
    public Pane barra;
    public double valorMax;

    public SensorCard(String nome, String unidade, double valorMax) {
        this.valorMax = valorMax;

        card = new VBox(8);
        card.setStyle("-fx-background-color: #161616; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #2a2a2a; -fx-border-radius: 12; -fx-border-width: 0.5;");

        Label label = new Label(nome.toUpperCase());
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-letter-spacing: 0.08em;");

        valor = new Label("--");
        valor.setStyle("-fx-font-size: 32px; -fx-font-weight: 500; -fx-text-fill: #f0f0f0;");

        Label unidadeLabel = new Label(unidade);
        unidadeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;");

        HBox valorRow = new HBox(5);
        valorRow.setAlignment(Pos.BASELINE_LEFT);
        valorRow.getChildren().addAll(valor, unidadeLabel);

        Pane barraFundo = new Pane();
        barraFundo.setStyle("-fx-background-color: #222222; -fx-background-radius: 2;");
        barraFundo.setPrefHeight(2);
        barraFundo.setMaxWidth(Double.MAX_VALUE);

        barra = new Pane();
        barra.setStyle("-fx-background-color: #1D9E75; -fx-background-radius: 2;");
        barra.setPrefHeight(2);
        barra.setPrefWidth(0);

        StackPane barraContainer = new StackPane(barraFundo, barra);
        StackPane.setAlignment(barra, Pos.CENTER_LEFT);

        card.getChildren().addAll(label, valorRow, barraContainer);
    }
}