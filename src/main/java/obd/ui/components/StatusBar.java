package obd.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import obd.connection.IObdConnection;

public class StatusBar {
    private final IObdConnection obdConnection;

    public StatusBar(IObdConnection obdConnection){
        this.obdConnection = obdConnection;
    }

    public HBox buildStatusBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(4, 14, 4, 14));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
                "-fx-background-color: #0d0d0d;" +
                        "-fx-border-color: #161616 transparent transparent transparent;" +
                        "-fx-border-width: 1 0 0 0;"
        );

        bar.getChildren().addAll(
                buildStatusItem("CONEXÃO", obdConnection.getPortName()),
                buildStatusItem("PIDs ATIVOS", "4"),
                buildStatusItem("CICLO", "~500ms")
        );

        return bar;
    }

    // ── par chave/valor da status bar ────────────────────────
    private HBox buildStatusItem(String chave, String valor) {
        Label k = new Label(chave + " ");
        k.getStyleClass().add("status-key");

        Label v = new Label(valor);
        v.getStyleClass().add("status-val");

        return new HBox(k, v);
    }
}
