package obd.ui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import obd.connection.IObdConnection;
import obd.database.daos.VeiculoDao;
import obd.database.models.Veiculo;

import java.util.List;

public class VeiculoTab {

    private final IObdConnection obdConnection;
    private final VeiculoDao veiculoDao = new VeiculoDao();

    // ── campos da seção ECU ───────────────────────────────────
    private Label valorVin;
    private Label valorEcu;
    private Label valorCalib;

    // ── campos do formulário ──────────────────────────────────
    private TextField tfMarca;
    private TextField tfModelo;
    private TextField tfAno;
    private TextField tfMotor;
    private TextField tfApelido;
    private Label statusSalvar;

    // ── lista de veículos cadastrados ─────────────────────────
    private VBox listaVeiculos;

    // ─────────────────────────────────────────────────────────
    public VeiculoTab(IObdConnection obdConnection) {
        this.obdConnection = obdConnection;
    }

    public VBox build() {
        VBox aba = new VBox(12);
        aba.setPadding(new Insets(12));

        aba.getChildren().addAll(
                buildSecaoEcu(),
                buildSecaoFormulario(),
                buildSecaoVeiculosCadastrados()
        );

        carregarVeiculos();
        return aba;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 1 — dados da ECU
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoEcu() {
        Label titulo = new Label("DADOS DA ECU");
        titulo.getStyleClass().add("config-title");

        Button btnLer = new Button("LER DA ECU");
        btnLer.getStyleClass().addAll("btn", "btn-green");
        btnLer.setOnAction(e -> lerDadosEcu(btnLer));

        valorVin   = buildValorLabel("—");
        valorEcu   = buildValorLabel("—");
        valorCalib = buildValorLabel("—");

        VBox campos = new VBox(4,
                buildInfoRow("VIN",        valorVin),
                buildInfoRow("ECU",        valorEcu),
                buildInfoRow("CALIBRADOR", valorCalib)
        );

        VBox secao = new VBox(8, titulo, new HBox(btnLer), campos);
        aplicarEstiloSecao(secao);
        return secao;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 2 — formulário de cadastro
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoFormulario() {
        Label titulo = new Label("CADASTRAR VEÍCULO");
        titulo.getStyleClass().add("config-title");

        tfMarca   = buildCampo("Ex: Chevrolet");
        tfModelo  = buildCampo("Ex: Astra");
        tfAno     = buildCampo("Ex: 2011");
        tfMotor   = buildCampo("Ex: 2.0 SOHC");
        tfApelido = buildCampo("Ex: Meu Astra");

        VBox campos = new VBox(4,
                buildCampoRow("APELIDO", tfApelido),
                buildCampoRow("MARCA",   tfMarca),
                buildCampoRow("MODELO",  tfModelo),
                buildCampoRow("ANO",     tfAno),
                buildCampoRow("MOTOR",   tfMotor)
        );

        statusSalvar = new Label("");
        statusSalvar.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #1D9E75;"
        );

        Button btnSalvar = new Button("SALVAR VEÍCULO");
        btnSalvar.getStyleClass().addAll("btn", "btn-green");
        btnSalvar.setOnAction(e -> salvarVeiculo(btnSalvar));

        Button btnLimpar = new Button("LIMPAR");
        btnLimpar.getStyleClass().add("btn");
        btnLimpar.setOnAction(e -> limparFormulario());

        HBox toolbar = new HBox(8, btnSalvar, btnLimpar, statusSalvar);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 0, 0, 0));

        VBox secao = new VBox(8, titulo, campos, toolbar);
        aplicarEstiloSecao(secao);
        return secao;
    }

    // ════════════════════════════════════════════════════════
    //  SEÇÃO 3 — veículos cadastrados
    // ════════════════════════════════════════════════════════
    private VBox buildSecaoVeiculosCadastrados() {
        Label titulo = new Label("VEÍCULOS CADASTRADOS");
        titulo.getStyleClass().add("config-title");

        listaVeiculos = new VBox(4);

        ScrollPane scroll = new ScrollPane(listaVeiculos);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(200);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox secao = new VBox(8, titulo, scroll);
        aplicarEstiloSecao(secao);
        return secao;
    }

    // ════════════════════════════════════════════════════════
    //  AÇÕES
    // ════════════════════════════════════════════════════════
    private void salvarVeiculo(Button btnSalvar) {
        String apelido = tfApelido.getText().trim();
        if (apelido.isEmpty()) {
            setStatus("APELIDO É OBRIGATÓRIO", "#E24B4A");
            return;
        }

        btnSalvar.setDisable(true);

        Thread t = new Thread(() -> {
            try {
                int ano = 0;
                try { ano = Integer.parseInt(tfAno.getText().trim()); }
                catch (NumberFormatException ignored) {}

                Veiculo v = new Veiculo(
                        apelido,
                        tfMarca.getText().trim(),
                        tfModelo.getText().trim(),
                        ano,
                        tfMotor.getText().trim(),
                        valorVin.getText().equals("—") ? "" : valorVin.getText()
                );
                veiculoDao.salvarVeiculo(v);

                Platform.runLater(() -> {
                    setStatus("✓ SALVO", "#1D9E75");
                    limparFormulario();
                    carregarVeiculos();
                    btnSalvar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setStatus("ERRO: " + ex.getMessage(), "#E24B4A");
                    btnSalvar.setDisable(false);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void carregarVeiculos() {
        Thread t = new Thread(() -> {
            try {
                List<Veiculo> veiculos = veiculoDao.buscarTodosVeiculos();
                Platform.runLater(() -> {
                    listaVeiculos.getChildren().clear();
                    if (veiculos.isEmpty()) {
                        Label vazio = new Label("NENHUM VEÍCULO CADASTRADO");
                        vazio.setStyle(
                                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                                        "-fx-text-fill: #333333; -fx-padding: 8;"
                        );
                        listaVeiculos.getChildren().add(vazio);
                    } else {
                        for (Veiculo v : veiculos) {
                            listaVeiculos.getChildren().add(buildVeiculoRow(v));
                        }
                    }
                });
            } catch (Exception ex) {
                System.out.println("Erro ao carregar veículos: " + ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private HBox buildVeiculoRow(Veiculo v) {
        Label nomeLabel = new Label(v.toString());
        nomeLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: #888888;"
        );
        HBox.setHgrow(nomeLabel, Priority.ALWAYS);

        Button btnDeletar = new Button("✕");
        btnDeletar.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #E24B4A; " +
                        "-fx-border-width: 1px; -fx-border-radius: 2px; " +
                        "-fx-text-fill: #E24B4A; -fx-font-size: 8px; " +
                        "-fx-padding: 2 6 2 6; -fx-cursor: hand;"
        );
        btnDeletar.setOnAction(e -> deletarVeiculo(v.getId()));

        HBox row = new HBox(12, nomeLabel, btnDeletar);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: #0f0f0f; -fx-border-color: #1a1a1a; " +
                        "-fx-border-width: 1px; -fx-border-radius: 3px; " +
                        "-fx-background-radius: 3px; -fx-padding: 8 12 8 12;"
        );
        return row;
    }

    private void deletarVeiculo(int id) {
        Thread t = new Thread(() -> {
            try {
                veiculoDao.deletarVeiculo(id);
                Platform.runLater(this::carregarVeiculos);
            } catch (Exception ex) {
                System.out.println("Erro ao deletar: " + ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void limparFormulario() {
        tfApelido.clear();
        tfMarca.clear();
        tfModelo.clear();
        tfAno.clear();
        tfMotor.clear();
        statusSalvar.setText("");
    }

    // ════════════════════════════════════════════════════════
    //  LEITURA ECU
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
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private String lerCampo(String comando) throws Exception {
        String resposta = obdConnection.enviarComando(comando);
        resposta = resposta.replace(">", "").trim();
        if (resposta.startsWith("7F") || resposta.contains("NO DATA") || resposta.isBlank())
            return "NÃO DISPONÍVEL";

        StringBuilder sb = new StringBuilder();
        String[] partes = resposta.split(" ");
        for (int i = 2; i < partes.length; i++) {
            String hex = partes[i].trim();
            if (hex.isEmpty()) continue;
            int valor = Integer.parseInt(hex, 16);
            if (valor > 0) sb.append((char) valor);
        }
        String resultado = sb.toString().trim();
        return resultado.isEmpty() ? "NÃO DISPONÍVEL" : resultado;
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════
    private void setValores(String vin, String ecu, String calib) {
        valorVin.setText(vin);
        valorEcu.setText(ecu);
        valorCalib.setText(calib);
    }

    private void setStatus(String texto, String cor) {
        statusSalvar.setText(texto);
        statusSalvar.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: " + cor + ";"
        );
    }

    private void aplicarEstiloSecao(VBox secao) {
        secao.setStyle(
                "-fx-background-color: #111111; -fx-border-color: #1a1a1a; " +
                        "-fx-border-width: 1px; -fx-border-radius: 3px; " +
                        "-fx-background-radius: 3px; -fx-padding: 12;"
        );
    }

    private HBox buildInfoRow(String chave, Label valor) {
        Label chaveLabel = new Label(chave);
        chaveLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.15em; -fx-min-width: 90px;"
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
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.15em; -fx-min-width: 90px;"
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