package obd.ui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import obd.connection.IObdConnection;

public class VeiculoTab {

    private final IObdConnection obdConnection;

    // ── labels atualizados após leitura ───────────────────────
    private Label valorVin;
    private Label valorEcu;
    private Label valorCalib;

    // ─────────────────────────────────────────────────────────
    public VeiculoTab(IObdConnection obdConnection) {
        this.obdConnection = obdConnection;
    }

    // ── monta e retorna o VBox pronto para exibição ───────────
    public VBox build() {
        VBox aba = new VBox(16);
        aba.setPadding(new Insets(12));

        aba.getChildren().addAll(
                buildSecaoEcu(),
                buildSecaoManual()
        );

        return aba;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 1 — dados da ECU via OBD2
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoEcu() {
        Label titulo = new Label("DADOS DA ECU");
        titulo.getStyleClass().add("config-title");

        Button btnLer = new Button("LER DA ECU");
        btnLer.getStyleClass().addAll("btn", "btn-green");
        btnLer.setOnAction(e -> lerDadosEcu(btnLer));

        HBox toolbar = new HBox(btnLer);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        valorVin   = buildValorLabel("—");
        valorEcu   = buildValorLabel("—");
        valorCalib = buildValorLabel("—");

        VBox campos = new VBox(4,
                buildInfoRow("VIN",             valorVin),
                buildInfoRow("ECU",             valorEcu),
                buildInfoRow("CALIBRADOR",      valorCalib)
        );

        VBox secao = new VBox(8, titulo, toolbar, campos);
        secao.setStyle(
                "-fx-background-color: #111111; -fx-border-color: #1a1a1a; " +
                        "-fx-border-width: 1px; -fx-border-radius: 3px; " +
                        "-fx-background-radius: 3px; -fx-padding: 12;"
        );
        return secao;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 2 — dados manuais do veículo
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoManual() {
        Label titulo = new Label("IDENTIFICAÇÃO DO VEÍCULO");
        titulo.getStyleClass().add("config-title");

        Label aviso = new Label("CADASTRO COMPLETO EM BREVE");
        aviso.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 7px; " +
                        "-fx-text-fill: #333333; -fx-letter-spacing: 0.12em;"
        );

        HBox cabecalho = new HBox(titulo);
        HBox.setHgrow(titulo, Priority.ALWAYS);
        cabecalho.getChildren().add(aviso);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        TextField tfMarca    = buildCampo("Ex: Chevrolet");
        TextField tfModelo   = buildCampo("Ex: Astra");
        TextField tfAno      = buildCampo("Ex: 2011");
        TextField tfMotor    = buildCampo("Ex: 2.0 SOHC");
        TextField tfApelido  = buildCampo("Ex: Meu Astra");

        VBox campos = new VBox(4,
                buildCampoRow("MARCA",   tfMarca),
                buildCampoRow("MODELO",  tfModelo),
                buildCampoRow("ANO",     tfAno),
                buildCampoRow("MOTOR",   tfMotor),
                buildCampoRow("APELIDO", tfApelido)
        );

        VBox secao = new VBox(8, cabecalho, campos);
        secao.setStyle(
                "-fx-background-color: #111111; -fx-border-color: #1a1a1a; " +
                        "-fx-border-width: 1px; -fx-border-radius: 3px; " +
                        "-fx-background-radius: 3px; -fx-padding: 12;"
        );
        return secao;
    }

    // ════════════════════════════════════════════════════════
    //  LEITURA OBD2 — serviço 09
    // ════════════════════════════════════════════════════════
    private void lerDadosEcu(Button btnLer) {
        btnLer.setDisable(true);
        setValores("LENDO...", "LENDO...", "LENDO...");

        Thread t = new Thread(() -> {
            try {
                String vin   = lerCampo("0902\r");
                String ecu   = lerCampo("090A\r");
                String calib = lerCampo("0904\r");

                Platform.runLater(() -> {
                    setValores(vin, ecu, calib);
                    btnLer.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setValores("ERRO", "ERRO", "ERRO");
                    btnLer.setDisable(false);
                    System.out.println("Erro ao ler ECU: " + ex.getMessage());
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── envia comando e trata a resposta como texto legível ───
    private String lerCampo(String comando) throws Exception {
        String resposta = obdConnection.enviarComando(comando);
        resposta = resposta.replace(">", "").trim();

        if (resposta.startsWith("7F") || resposta.contains("NO DATA")
                || resposta.isBlank()) {
            return "NÃO DISPONÍVEL";
        }

        // converte bytes hex para caracteres ASCII
        StringBuilder sb = new StringBuilder();
        String[] partes = resposta.split(" ");
        for (int i = 2; i < partes.length; i++) {
            String hex = partes[i].trim();
            if (hex.isEmpty()) continue;
            int valor = Integer.parseInt(hex, 16);
            if (valor > 0) sb.append((char) valor); // ignora bytes nulos
        }

        String resultado = sb.toString().trim();
        return resultado.isEmpty() ? "NÃO DISPONÍVEL" : resultado;
    }

    // ── helpers ───────────────────────────────────────────────

    private void setValores(String vin, String ecu, String calib) {
        valorVin.setText(vin);
        valorEcu.setText(ecu);
        valorCalib.setText(calib);
    }

    private HBox buildInfoRow(String chave, Label valor) {
        Label chaveLabel = new Label(chave);
        chaveLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.15em; " +
                        "-fx-min-width: 90px;"
        );
        HBox row = new HBox(12, chaveLabel, valor);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5 0 5 0; -fx-border-color: transparent transparent #141414 transparent; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private Label buildValorLabel(String texto) {
        Label label = new Label(texto);
        label.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px; " +
                        "-fx-text-fill: #1D9E75; -fx-letter-spacing: 0.05em;"
        );
        return label;
    }

    private HBox buildCampoRow(String chave, TextField campo) {
        Label chaveLabel = new Label(chave);
        chaveLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.15em; " +
                        "-fx-min-width: 90px;"
        );
        HBox.setHgrow(campo, Priority.ALWAYS);
        HBox row = new HBox(12, chaveLabel, campo);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 4 0 4 0;");
        return row;
    }

    private TextField buildCampo(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setStyle(
                "-fx-background-color: #0a0a0a; -fx-border-color: #222222; " +
                        "-fx-border-width: 1px; -fx-border-radius: 2px; " +
                        "-fx-background-radius: 2px; -fx-text-fill: #888888; " +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-padding: 5 8 5 8; -fx-prompt-text-fill: #333333;"
        );
        return tf;
    }
}