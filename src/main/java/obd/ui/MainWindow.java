package obd.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import obd.connection.IObdConnection;

import obd.core.sensors.*;
import obd.ui.tabs.*;


public class MainWindow {

    // ── dependências recebidas da ConnectWindow ───────────────
    private final Stage stage;
    private final IObdConnection obdConnection;

    // ── leitura em background ─────────────────────────────────
    private LeituraObd leituraObd;

    // ── componentes de UI que precisam ser atualizados ────────
    private Label liveDot;
    private Label statusVeiculo;

    // ── cards de sensores ─────────────────────────────────────
    private final SensorsTab sensorsTab;
    private final DtcTab dtcTab;
    private final PidsTab pidsTab;
    private final VeiculoTab veiculoTab;
    private final ConfigTab configTab;

    // ── área de conteúdo das abas ─────────────────────────────
    private StackPane contentArea;

    // ── aba ativa atualmente ──────────────────────────────────
    private Label navBtnAtivo;

    // ─────────────────────────────────────────────────────────
    public MainWindow(Stage stage, IObdConnection obdConnection) {
        this.stage = stage;
        this.obdConnection = obdConnection;

        this.sensorsTab = new SensorsTab();
        this.dtcTab = new DtcTab(obdConnection);
        this.pidsTab = new PidsTab(obdConnection, sensorsTab);
        this.veiculoTab = new VeiculoTab(obdConnection);
        this.configTab = new ConfigTab(obdConnection, leituraObd);
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
        Button btnDesc =
                new Button("DESCONECTAR");
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
        btnSensores.setOnMouseClicked(e -> mostrarAba(sensorsTab.build(), btnSensores));
        btnDtc.setOnMouseClicked(     e -> mostrarAba(dtcTab.build(),      btnDtc));
        btnPids.setOnMouseClicked(    e -> mostrarAba(pidsTab.build(),      btnPids));
        btnVeiculo.setOnMouseClicked( e -> mostrarAba(veiculoTab.build(),   btnVeiculo));
        btnConfig.setOnMouseClicked(  e -> mostrarAba(configTab.build(),    btnConfig));

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
        btn.setCursor(Cursor.HAND);
        return btn;
    }

    // ── troca a aba visível ───────────────────────────────────
    private void mostrarAba(VBox novaAba, Label navBtn) {
        contentArea.getChildren().setAll(novaAba);

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
        contentArea.getChildren().add(sensorsTab.build());
        return contentArea;
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
    private void iniciarLeitura(){
        leituraObd = new LeituraObd(obdConnection,sensorsTab);
        Thread thread = new Thread(leituraObd::getResponse);
        configTab.setLeituraObd(leituraObd);
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