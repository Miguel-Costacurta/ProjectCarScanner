package obd.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import obd.connection.IObdConnection;
import obd.ui.MainWindow;

public class Topbar {
    private final Runnable onDesconectar;
    private final IObdConnection obdConnection;

    private Label liveDot;
    private Label statusVeiculo;

    public Topbar(IObdConnection obdConnection, Runnable onDesconectar){
        this.obdConnection = obdConnection;
        this.onDesconectar = onDesconectar;
    }

    // ── barra superior: logo, veículo, badge, desconectar ─────
    public HBox buildTopbar() {
        // logo
        Label logo = new Label("PROJECTCARSCANNER");
        logo.getStyleClass().add("topbar-logo");

        // status do veículo no centro
        liveDot = new Label("● ");
        liveDot.setStyle("-fx-text-fill: #1D9E75; -fx-font-size: 9px;");
        statusVeiculo = new Label("CONECTADO — " + obdConnection.getPortName());
        statusVeiculo.getStyleClass().add("topbar-veiculo");

        HBox centro = new HBox(4, liveDot, statusVeiculo);
        centro.setAlignment(Pos.CENTER);
        HBox.setHgrow(centro, Priority.ALWAYS);

        // badge de conexão
        Label badge = new Label("📡 " + obdConnection.getPortName());
        badge.getStyleClass().add("topbar-badge");

        // botão desconectar
        Button btnDesc =
                new Button("DESCONECTAR");
        btnDesc.getStyleClass().add("btn-desconectar");
        btnDesc.setOnAction(e -> onDesconectar.run());

        HBox topbar = new HBox(12, logo, centro, badge, btnDesc);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(7, 14, 7, 14));
        topbar.setStyle(
                "-fx-background-color: #0f0f0f;" +
                        "-fx-border-color: transparent transparent #1D9E75 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        return topbar;
    }

    // ── pisca o ponto LIVE na topbar ──────────────────────────
    public void iniciarAnimacaoLive() {
        Timeline blink = new Timeline(
                new KeyFrame(Duration.millis(0), e -> liveDot.setOpacity(1.0)),
                new KeyFrame(Duration.millis(750),  e -> liveDot.setOpacity(0.2)),
                new KeyFrame(Duration.millis(1500), e -> liveDot.setOpacity(1.0))
        );
        blink.setCycleCount(Timeline.INDEFINITE);
        blink.play();
    }

}
