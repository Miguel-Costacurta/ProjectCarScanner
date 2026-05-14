package obd.ui.windows;

import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import obd.connection.IObdConnection;

import obd.core.sensors.*;
import obd.ui.components.Navbar;
import obd.ui.components.StatusBar;
import obd.ui.components.Topbar;
import obd.ui.tabs.*;


public class MainWindow {

    // ── dependências recebidas da ConnectWindow ───────────────
    private final Stage stage;
    private final IObdConnection obdConnection;

    // ── leitura em background ─────────────────────────────────
    private LeituraObd leituraObd;

    // ── cards de sensores ─────────────────────────────────────
    private final SensorsTab sensorsTab;
    private final DtcTab dtcTab;
    private final PidsTab pidsTab;
    private final VeiculoTab veiculoTab;
    private final ConfigTab configTab;

    // ── área de conteúdo das abas ─────────────────────────────
    private StackPane contentArea;

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
    public void showMainWindow() {
        BorderPane root = new BorderPane();
        Topbar topbar = new Topbar(obdConnection, this::desconectar);
        Navbar navbar = new Navbar(
                aba -> contentArea.getChildren().setAll(aba),
                sensorsTab, dtcTab, pidsTab, veiculoTab, configTab
        );
        root.setStyle("-fx-background-color: #0a0a0a;");

        root.setTop(new VBox(topbar.buildTopbar(), navbar.buildNavbar()));
        root.setCenter(buildContentArea());
        root.setBottom(new StatusBar(obdConnection).buildStatusBar());

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

        iniciarLeituraObd();
        topbar.iniciarAnimacaoLive();
    }

    // ════════════════════════════════════════════════════════
    //  LEITURA OBD EM BACKGROUND
    // ════════════════════════════════════════════════════════
    private void iniciarLeituraObd(){
        leituraObd = new LeituraObd(obdConnection,sensorsTab);
        Thread thread = new Thread(leituraObd::getResponse);
        configTab.setLeituraObd(leituraObd);
        thread.setDaemon(true);
        thread.start();
    }


    // ── volta para a tela de conexão ─────────────────────────
    private void desconectar() {
        if (leituraObd != null) leituraObd.stop();
        obdConnection.closeConnection();
        new ConnectWindow(stage).show();
    }

    // ── área central que troca de conteúdo ───────────────────
    private StackPane buildContentArea() {
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #0a0a0a;");
        contentArea.getChildren().add(sensorsTab.build());
        return contentArea;
    }
}