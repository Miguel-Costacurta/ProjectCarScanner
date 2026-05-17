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

public class Topbar {
    private final Runnable onDesconectar;
    private final IObdConnection obdConnection;
    private final Runnable onIniciarGravacao;
    private final Runnable onPararGravacao;

    private Label liveDot;
    private Timeline blinkGravacao;
    private Button btnRec;
    private boolean gravando = false;


    public Topbar(IObdConnection obdConnection, Runnable onDesconectar, Runnable onIniciarGravacao, Runnable onPararGravacao){
        this.obdConnection = obdConnection;
        this.onDesconectar = onDesconectar;
        this.onIniciarGravacao = onIniciarGravacao;
        this.onPararGravacao = onPararGravacao;
    }

    // ── barra superior: logo, veículo, badge, desconectar ─────
    public HBox buildTopbar() {
        // logo
        Label logo = new Label("CAR SCANNER");
        logo.getStyleClass().add("topbar-logo");

        // status do veículo no centro
        liveDot = new Label("● ");
        liveDot.setStyle("-fx-text-fill: #1D9E75; -fx-font-size: 9px;");

        Label statusVeiculo = new Label("CONECTADO — " + obdConnection.getPortName());
        statusVeiculo.getStyleClass().add("topbar-veiculo");

        HBox centro = new HBox(4, liveDot, statusVeiculo);
        centro.setAlignment(Pos.CENTER);
        HBox.setHgrow(centro, Priority.ALWAYS);

        btnRec = new Button("● GRAVAR");
        btnRec.setStyle(estiloRec(false));
        btnRec.setOnAction(e-> toggleGravacao());

        // badge de conexão
        Label badge = new Label("📡 " + obdConnection.getPortName());
        badge.getStyleClass().add("topbar-badge");

        // botão desconectar
        Button btnDesc =
                new Button("DESCONECTAR");
        btnDesc.getStyleClass().add("btn-desconectar");
        btnDesc.setOnAction(e -> onDesconectar.run());

        HBox topbar = new HBox(12, logo, centro, btnRec, badge, btnDesc);
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

    private void toggleGravacao() {
        gravando = !gravando;

        if (gravando) {
            btnRec.setStyle(estiloRec(true));
            btnRec.setText("■ PARAR");
            onIniciarGravacao.run();
            iniciarBlinkRec();
        } else {
            btnRec.setStyle(estiloRec(false));
            btnRec.setText("● GRAVAR");
            onPararGravacao.run();
            pararBlinkRec();
        }
    }

    private void iniciarBlinkRec() {
        blinkGravacao = new Timeline(
                new KeyFrame(Duration.millis(0),    e -> btnRec.setOpacity(1.0)),
                new KeyFrame(Duration.millis(500),  e -> btnRec.setOpacity(0.3)),
                new KeyFrame(Duration.millis(1000), e -> btnRec.setOpacity(1.0))
        );
        blinkGravacao.setCycleCount(Timeline.INDEFINITE);
        blinkGravacao.play();
    }

    private void pararBlinkRec() {
        if (blinkGravacao != null) {
            blinkGravacao.stop();
            btnRec.setOpacity(1.0);
        }
    }

    private String estiloRec(boolean gravando) {
        String cor   = gravando ? "#E24B4A" : "#1D9E75";
        String fundo = gravando ? "#1a0a0a" : "transparent";
        return "-fx-background-color: " + fundo + "; " +
                "-fx-border-color: " + cor + "; -fx-border-width: 1px; " +
                "-fx-border-radius: 2px; -fx-background-radius: 2px; " +
                "-fx-text-fill: " + cor + "; -fx-font-family: 'Courier New'; " +
                "-fx-font-size: 8px; -fx-padding: 4 10 4 10; " +
                "-fx-letter-spacing: 0.12em; -fx-cursor: hand;";
    }

}
