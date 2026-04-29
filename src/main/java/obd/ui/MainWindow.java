package obd.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import obd.connection.IObdConnection;
import obd.core.sensors.*;

public class MainWindow {

    // ── dependências recebidas da ConnectWindow ───────────────
    private final Stage stage;
    private final IObdConnection obdConnection;

    // ── sensores ──────────────────────────────────────────────
    private RPM mostrarRPM;
    private Tensao mostrarTensao;
    private TPS mostrarTPS;
    private FuelTrim mostrarLambda;
    private SparkAdvance mostrarAvanco;
    private Velocity mostrarVelocidade;

    // ── leitura em background ─────────────────────────────────
    private LeituraObd leituraObd;

    // ── componentes de UI que precisam ser atualizados ────────
    private Label liveDot;
    private Label statusVeiculo;

    // ── cards de sensores ─────────────────────────────────────
    private SensorCard cardRpm;
    private SensorCard cardTps;
    private SensorCard cardTensao;
    private SensorCard cardLambda;
    private SensorCard cardAvanco;
    private SensorCard cardVelocidade;

    // ── área de conteúdo das abas ─────────────────────────────
    private StackPane contentArea;

    // ── aba ativa atualmente ──────────────────────────────────
    private VBox abaAtiva;
    private Label navBtnAtivo;

    // ─────────────────────────────────────────────────────────
    public MainWindow(Stage stage, IObdConnection obdConnection) {
        this.stage = stage;
        this.obdConnection = obdConnection;

        // instancia sensores com a conexão recebida
        this.mostrarRPM    = new RPM(obdConnection);
        this.mostrarTensao = new Tensao(obdConnection);
        this.mostrarTPS    = new TPS(obdConnection);
        this.mostrarLambda = new FuelTrim(obdConnection, 1);
        this.mostrarAvanco = new SparkAdvance(obdConnection);
        this.mostrarVelocidade = new Velocity(obdConnection);
    }

    // ── monta e exibe a janela principal ──────────────────────
    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        root.setTop(buildTopSection());
        root.setCenter(buildContentArea());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("ProjectCarScanner");
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(500);

        stage.setOnCloseRequest(e -> {
            if (leituraObd != null) leituraObd.stop();
        });

        stage.show();

        iniciarLeitura();
        iniciarAnimacaoLive();
    }

    // ── topbar + navbar juntos no topo ────────────────────────
    private VBox buildTopSection() {
        return new VBox(buildTopbar(), buildNavbar());
    }

    // ── barra superior: logo, veículo, badge, desconectar ─────
    private HBox buildTopbar() {
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
        javafx.scene.control.Button btnDesc =
                new javafx.scene.control.Button("DESCONECTAR");
        btnDesc.getStyleClass().add("btn-desconectar");
        btnDesc.setOnAction(e -> desconectar());

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

    // ── navbar: botões de abas ────────────────────────────────
    private HBox buildNavbar() {
        HBox navbar = new HBox();
        navbar.setStyle(
                "-fx-background-color: #0f0f0f;" +
                        "-fx-border-color: transparent transparent #1a1a1a transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label btnSensores = buildNavBtn("SENSORES");
        Label btnDtc      = buildNavBtn("DTC");
        Label btnPids     = buildNavBtn("PIDs ATIVOS");
        Label btnVeiculo  = buildNavBtn("VEÍCULO");
        Label btnConfig   = buildNavBtn("CONFIGURAÇÕES");

        // ação de cada botão
        btnSensores.setOnMouseClicked(e -> mostrarAba(buildAbaSensores(), btnSensores));
        btnDtc.setOnMouseClicked(     e -> mostrarAba(buildAbaDtc(),      btnDtc));
        btnPids.setOnMouseClicked(    e -> mostrarAba(buildAbaPids(),      btnPids));
        btnVeiculo.setOnMouseClicked( e -> mostrarAba(buildAbaVeiculo(),   btnVeiculo));
        btnConfig.setOnMouseClicked(  e -> mostrarAba(buildAbaConfig(),    btnConfig));

        navbar.getChildren().addAll(btnSensores, btnDtc, btnPids, btnVeiculo, btnConfig);

        // abre aba de sensores por padrão
        ativarNavBtn(btnSensores);

        return navbar;
    }

    // ── factory de botão de navegação ────────────────────────
    private Label buildNavBtn(String texto) {
        Label btn = new Label(texto);
        btn.getStyleClass().add("nav-btn");
        btn.setPadding(new Insets(10, 14, 10, 14));
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    // ── troca a aba visível ───────────────────────────────────
    private void mostrarAba(VBox novaAba, Label navBtn) {
        contentArea.getChildren().setAll(novaAba);
        abaAtiva = novaAba;

        // desativa todos os botões de nav
        if (navBtnAtivo != null) {
            navBtnAtivo.getStyleClass().remove("nav-btn-active");
        }
        ativarNavBtn(navBtn);
    }

    private void ativarNavBtn(Label navBtn) {
        navBtn.getStyleClass().add("nav-btn-active");
        navBtnAtivo = navBtn;
    }

    // ── área central que troca de conteúdo ───────────────────
    private StackPane buildContentArea() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #0a0a0a;");
        contentArea.getChildren().add(buildAbaSensores());
        return contentArea;
    }

    // ════════════════════════════════════════════════════════
    //  ABA SENSORES
    // ════════════════════════════════════════════════════════
    private VBox buildAbaSensores() {
        cardRpm    = new SensorCard("RPM",    "rpm", 6800);
        cardTps    = new SensorCard("TPS",    "%",   100);
        cardTensao = new SensorCard("TENSÃO", "V",   14.5);
        cardLambda = new SensorCard("LAMBDA", "λ",   1.99);
        cardAvanco = new SensorCard("Ponto Ign","º",40);
        cardVelocidade = new SensorCard("Velocidade", "km/h", 300);

        // valores iniciais
        cardRpm.valor.setText("--");
        cardTps.valor.setText("--");
        cardTensao.valor.setText("--");
        cardLambda.valor.setText("--");
        cardVelocidade.valor.setText("--");
        cardAvanco.valor.setText("--");

        // grid de cards
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        // cada card cresce igualmente
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        grid.add(cardRpm.card,    0, 0);
        grid.add(cardTps.card,    1, 0);
        grid.add(cardTensao.card, 2, 0);
        grid.add(cardLambda.card, 3, 0);
        grid.add(cardAvanco.card, 3, 0);
        grid.add(cardVelocidade.card, 3, 0);

        VBox aba = new VBox(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
        return aba;
    }

    // ════════════════════════════════════════════════════════
    //  ABA DTC
    // ════════════════════════════════════════════════════════
    private VBox buildAbaDtc() {
        Label titulo = new Label("CÓDIGOS DE FALHA");
        titulo.getStyleClass().add("config-title");
        titulo.setPadding(new Insets(0, 0, 10, 0));

        // botões
        javafx.scene.control.Button btnLer =
                new javafx.scene.control.Button("LER DTCs");
        btnLer.getStyleClass().addAll("btn", "btn-green");

        javafx.scene.control.Button btnApagar =
                new javafx.scene.control.Button("APAGAR DTCs");
        btnApagar.getStyleClass().addAll("btn", "btn-red");

        HBox toolbar = new HBox(8, btnLer, btnApagar);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        // área de resultados
        VBox resultados = new VBox(6);

        Label vazio = new Label("PRESSIONE 'LER DTCs' PARA VERIFICAR");
        vazio.getStyleClass().add("dtc-empty");
        vazio.setPadding(new Insets(20));
        resultados.getChildren().add(vazio);

        // ação de ler
        btnLer.setOnAction(e -> {
            resultados.getChildren().clear();
            Label lendo = new Label("LENDO...");
            lendo.setStyle("-fx-text-fill: #1D9E75; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            resultados.getChildren().add(lendo);

            Thread t = new Thread(() -> {
                try {
                    obd.core.dtcs.DtcReader reader =
                            new obd.core.dtcs.DtcReader(obdConnection);
                    java.util.List<String> codigos = reader.lerDtcs();

                    Platform.runLater(() -> {
                        resultados.getChildren().clear();
                        if (codigos.isEmpty()) {
                            Label ok = new Label("✓ NENHUM DTC ENCONTRADO");
                            ok.getStyleClass().add("dtc-empty");
                            ok.setStyle("-fx-text-fill: #1D9E75; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                            resultados.getChildren().add(ok);
                        } else {
                            for (String codigo : codigos) {
                                resultados.getChildren().add(buildDtcRow(codigo));
                            }
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        resultados.getChildren().clear();
                        Label erro = new Label("ERRO: " + ex.getMessage());
                        erro.setStyle("-fx-text-fill: #E24B4A; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                        resultados.getChildren().add(erro);
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        });

        // ação de apagar
        btnApagar.setOnAction(e -> {
            Thread t = new Thread(() -> {
                try {
                    obdConnection.enviarComando("04\r");
                    Platform.runLater(() -> {
                        resultados.getChildren().clear();
                        Label ok = new Label("✓ DTCs APAGADOS COM SUCESSO");
                        ok.setStyle("-fx-text-fill: #1D9E75; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                        resultados.getChildren().add(ok);
                    });
                } catch (Exception ex) {
                    System.out.println("Erro ao apagar DTCs: " + ex.getMessage());
                }
            });
            t.setDaemon(true);
            t.start();
        });

        VBox aba = new VBox(titulo, toolbar, resultados);
        aba.setPadding(new Insets(12));
        aba.setSpacing(0);
        return aba;
    }

    // ── linha de DTC individual ───────────────────────────────
    private HBox buildDtcRow(String codigo) {
        Label codLabel = new Label(codigo);
        codLabel.getStyleClass().add("dtc-code");

        String descricao = obd.core.dtcs.DtcDescription.getDescricao(codigo);
        Label descLabel = new Label(descricao);
        descLabel.getStyleClass().add("dtc-desc");

        Label badge = new Label("ATIVO");
        badge.getStyleClass().add("dtc-badge");

        HBox row = new HBox(12, codLabel, descLabel, badge);
        row.getStyleClass().add("dtc-row");
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(descLabel, Priority.ALWAYS);
        return row;
    }

    // ════════════════════════════════════════════════════════
    //  ABA PIDs ATIVOS (placeholder — será a PidsTab.java)
    // ════════════════════════════════════════════════════════
    private VBox buildAbaPids() {
        Label titulo = new Label("PIDs ATIVOS");
        titulo.getStyleClass().add("config-title");

        Label info = new Label("IMPLEMENTE A PidsTab.java AQUI");
        info.setStyle("-fx-text-fill: #444; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        VBox aba = new VBox(12, titulo, info);
        aba.setPadding(new Insets(12));
        return aba;
    }

    // ════════════════════════════════════════════════════════
    //  ABA VEÍCULO (placeholder — será a VeiculoTab.java)
    // ════════════════════════════════════════════════════════
    private VBox buildAbaVeiculo() {
        Label titulo = new Label("IDENTIFICAÇÃO DO VEÍCULO");
        titulo.getStyleClass().add("config-title");

        Label info = new Label("IMPLEMENTE A VeiculoTab.java AQUI");
        info.setStyle("-fx-text-fill: #444; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        VBox aba = new VBox(12, titulo, info);
        aba.setPadding(new Insets(12));
        return aba;
    }

    // ════════════════════════════════════════════════════════
    //  ABA CONFIGURAÇÕES (placeholder — será a ConfigTab.java)
    // ════════════════════════════════════════════════════════
    private VBox buildAbaConfig() {
        Label titulo = new Label("CONFIGURAÇÕES");
        titulo.getStyleClass().add("config-title");

        Label info = new Label("IMPLEMENTE A ConfigTab.java AQUI");
        info.setStyle("-fx-text-fill: #444; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        VBox aba = new VBox(12, titulo, info);
        aba.setPadding(new Insets(12));
        return aba;
    }

    // ── barra de status inferior ──────────────────────────────
    private HBox buildStatusBar() {
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

    // ════════════════════════════════════════════════════════
    //  LEITURA OBD EM BACKGROUND
    // ════════════════════════════════════════════════════════
    private void iniciarLeitura() {
        leituraObd = new LeituraObd(
                obdConnection,
                mostrarTPS,
                mostrarRPM,
                mostrarLambda,
                mostrarTensao,
                mostrarAvanco,
                mostrarVelocidade,
                (rpm, tps, tensao, fuelTrim, spark, velocity) -> Platform.runLater(() -> {
                    // atualiza cards
                    cardRpm.valor.setText(String.format("%.0f", rpm));
                    cardRpm.barra.setPrefWidth(
                            cardRpm.card.getWidth() * (rpm / cardRpm.valorMax));

                    cardTps.valor.setText(String.format("%.1f", tps));
                    cardTps.barra.setPrefWidth(
                            cardTps.card.getWidth() * (tps / cardTps.valorMax));

                    cardTensao.valor.setText(String.format("%.1f", tensao));
                    cardTensao.barra.setPrefWidth(
                            cardTensao.card.getWidth() * (tensao / cardTensao.valorMax));

                    cardLambda.valor.setText(String.format("%.2f", fuelTrim));
                    cardLambda.barra.setPrefWidth(
                            cardLambda.card.getWidth() * (fuelTrim / cardLambda.valorMax));

                    cardLambda.valor.setText(String.format("%.0f", velocity));
                    cardLambda.barra.setPrefWidth(
                            cardLambda.card.getWidth() * (velocity / cardLambda.valorMax));

                    cardLambda.valor.setText(String.format("%.0f", spark));
                    cardLambda.barra.setPrefWidth(
                            cardLambda.card.getWidth() * (spark / cardLambda.valorMax)
                    );
                    // alerta visual no card lambda (fora de 0.90 ~ 1.10)
                    boolean lambdaAlerta = fuelTrim < 0.90 || fuelTrim > 1.10;
                    cardLambda.card.getStyleClass().removeAll("sensor-card-warn");
                    if (lambdaAlerta) cardLambda.card.getStyleClass().add("sensor-card-warn");
                })
        );

        Thread thread = new Thread(leituraObd::getResponse);
        thread.setDaemon(true);
        thread.start();
    }

    // ── pisca o ponto LIVE na topbar ──────────────────────────
    private void iniciarAnimacaoLive() {
        Timeline blink = new Timeline(
                new KeyFrame(Duration.millis(0),    e -> liveDot.setOpacity(1.0)),
                new KeyFrame(Duration.millis(750),  e -> liveDot.setOpacity(0.2)),
                new KeyFrame(Duration.millis(1500), e -> liveDot.setOpacity(1.0))
        );
        blink.setCycleCount(Timeline.INDEFINITE);
        blink.play();
    }

    // ── volta para a tela de conexão ─────────────────────────
    private void desconectar() {
        if (leituraObd != null) leituraObd.stop();
        obdConnection.closeConnection();
        new ConnectWindow(stage).show();
    }
}