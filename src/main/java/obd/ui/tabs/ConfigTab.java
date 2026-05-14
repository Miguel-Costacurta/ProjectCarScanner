package obd.ui.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import obd.connection.IObdConnection;
import obd.core.sensors.ObdReader;

public class ConfigTab {

    private final IObdConnection obdConnection;
    private volatile ObdReader leituraObd;

    // ── referências para leitura/escrita dos valores ──────────
    private Label valorPorta;
    private TextField tfIntervalo;
    private CheckBox cbRpm;
    private CheckBox cbTps;
    private CheckBox cbTensao;
    private CheckBox cbLambda;
    private CheckBox cbAvanco;
    private CheckBox cbVelocidade;
    private Label statusElm;

    // ─────────────────────────────────────────────────────────
    public ConfigTab(IObdConnection obdConnection, ObdReader leituraObd) {
        this.obdConnection = obdConnection;
        this.leituraObd    = leituraObd;
    }

    // ── monta e retorna o VBox pronto para exibição ───────────
    public VBox build() {
        VBox aba = new VBox(12);
        aba.setPadding(new Insets(12));

        aba.getChildren().addAll(
                buildSecaoConexao(),
                buildSecaoLeitura(),
                buildSecaoElm()
        );

        return aba;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 1 — informações de conexão (somente leitura)
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoConexao() {
        Label titulo = new Label("CONEXÃO");
        titulo.getStyleClass().add("config-title");

        valorPorta = new Label(obdConnection.getPortName());
        valorPorta.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px; " +
                        "-fx-text-fill: #1D9E75;"
        );

        Label baudLabel = new Label("38400 baud");
        baudLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px; " +
                        "-fx-text-fill: #1D9E75;"
        );

        VBox campos = new VBox(4,
                buildInfoRow("PORTA",    valorPorta),
                buildInfoRow("BAUD RATE", baudLabel)
        );

        return buildSecao(titulo, campos);
    }

    public void setLeituraObd(ObdReader leituraObd){
        this.leituraObd = leituraObd;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 2 — intervalo de leitura
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoLeitura() {
        Label titulo = new Label("LEITURA");
        titulo.getStyleClass().add("config-title");

        tfIntervalo = new TextField("50");
        tfIntervalo.setStyle(
                "-fx-background-color: #0a0a0a; -fx-border-color: #222222; " +
                        "-fx-border-width: 1px; -fx-border-radius: 2px; " +
                        "-fx-background-radius: 2px; -fx-text-fill: #888888; " +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-padding: 4 8 4 8; -fx-pref-width: 60px;"
        );

        Label msLabel = new Label("ms");
        msLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 9px; -fx-text-fill: #444;");

        Button btnAplicar = new Button("APLICAR");
        btnAplicar.getStyleClass().addAll("btn", "btn-green");
        btnAplicar.setOnAction(e -> aplicarIntervalo());

        HBox row = new HBox(8, tfIntervalo, msLabel, btnAplicar);
        row.setAlignment(Pos.CENTER_LEFT);

        Label dica = new Label("VALORES MENORES = LEITURA MAIS RÁPIDA / MAIOR USO DE CPU");
        dica.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 7px; " +
                        "-fx-text-fill: #333333; -fx-letter-spacing: 0.08em;"
        );

        VBox campos = new VBox(8, buildInfoRow("INTERVALO", row), dica);

        return buildSecao(titulo, campos);
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 4 — comandos ELM327
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoElm() {
        Label titulo = new Label("ELM327");
        titulo.getStyleClass().add("config-title");

        statusElm = new Label("");
        statusElm.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #1D9E75;"
        );

        Button btnReinit = new Button("REENVIAR CONFIGURAÇÕES AT");
        btnReinit.getStyleClass().addAll("btn");
        btnReinit.setOnAction(e -> reinicializarElm(btnReinit));

        HBox row = new HBox(12, btnReinit, statusElm);
        row.setAlignment(Pos.CENTER_LEFT);

        Label dica = new Label("REENVIA ATE0, ATL0, ATST0 PARA O ADAPTADOR");
        dica.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 7px; " +
                        "-fx-text-fill: #333333; -fx-letter-spacing: 0.08em;"
        );

        VBox campos = new VBox(8, row, dica);
        return buildSecao(titulo, campos);
    }

    // ════════════════════════════════════════════════════════
    //  AÇÕES
    // ════════════════════════════════════════════════════════
    private void aplicarIntervalo() {
        try {
            int valor = Integer.parseInt(tfIntervalo.getText().trim());
            if (valor < 0) valor = 0;
            if (leituraObd != null) leituraObd.setIntervalo(valor);
        } catch (NumberFormatException e) {
            tfIntervalo.setText("50");
        }
    }

    private void reinicializarElm(Button btn) {
        btn.setDisable(true);
        statusElm.setText("ENVIANDO...");

        Thread t = new Thread(() -> {
            try {
                obdConnection.enviarComando("ATE0\r");
                obdConnection.enviarComando("ATL0\r");
                obdConnection.enviarComando("ATST0\r");
                javafx.application.Platform.runLater(() -> {
                    statusElm.setText("✓ OK");
                    btn.setDisable(false);
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    statusElm.setText("ERRO: " + ex.getMessage());
                    statusElm.setStyle(
                            "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                                    "-fx-text-fill: #E24B4A;"
                    );
                    btn.setDisable(false);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }
    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private VBox buildSecao(Label titulo, VBox campos) {
        VBox secao = new VBox(8, titulo, campos);
        secao.setStyle(
                "-fx-background-color: #111111; -fx-border-color: #1a1a1a; " +
                        "-fx-border-width: 1px; -fx-border-radius: 3px; " +
                        "-fx-background-radius: 3px; -fx-padding: 12;"
        );
        return secao;
    }

    private HBox buildInfoRow(String chave, javafx.scene.Node valor) {
        Label chaveLabel = new Label(chave);
        chaveLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.15em; " +
                        "-fx-min-width: 90px;"
        );
        HBox row = new HBox(12, chaveLabel, valor);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-padding: 5 0 5 0; " +
                        "-fx-border-color: transparent transparent #141414 transparent; " +
                        "-fx-border-width: 0 0 1 0;"
        );
        return row;
    }
/*
    private CheckBox buildCheckbox(String texto, boolean selecionado) {
        CheckBox cb = new CheckBox(texto);
        cb.setSelected(selecionado);
        cb.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: #888888;"
        );
        return cb;
    } */

}