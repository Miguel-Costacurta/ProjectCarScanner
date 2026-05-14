package obd.ui.windows;

import com.fazecast.jSerialComm.SerialPort;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import obd.connection.HomoObdConnection;
import obd.connection.IObdConnection;
import obd.connection.ObdConnection;

public class ConnectWindow {

    // ── estado interno ────────────────────────────────────────
    private String tipoConexao = "bluetooth"; // "bluetooth" ou "usb"
    private Stage stage;

    // ── referências para atualizar durante a conexão ──────────
    private Label statusLabel;
    private VBox cardBluetooth;
    private VBox cardUsb;

    // ────────────────────────────────────────────────────────
    public ConnectWindow(Stage stage) {
        this.stage = stage;
    }

    // ── monta e exibe a tela de conexão ──────────────────────
    public void show() {
        VBox root = buildLayout();

        Scene scene = new Scene(root, 800, 560);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("ProjectCarScanner");
        stage.setResizable(false);
        stage.show();
    }

    // ── constrói o layout completo ────────────────────────────
    private VBox buildLayout() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(32);
        root.setStyle("-fx-background-color: #0a0a0a;");
        root.setPadding(new Insets(48));

        root.getChildren().addAll(
                buildHeader(),
                buildCards(),
                buildPortaRow(),
                buildStatusLabel(),
                buildBotaoConectar()
        );

        // animação de entrada — fade in suave
        FadeTransition ft = new FadeTransition(Duration.millis(600), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        return root;
    }

    // ── cabeçalho com logo e subtítulo ────────────────────────
    private VBox buildHeader() {
        Label logo = new Label("CAR SCANNER");
        logo.getStyleClass().add("conn-logo");

        Label sub = new Label("DIAGNÓSTICO AUTOMOTIVO OBD-II");
        sub.getStyleClass().add("conn-sub");

        VBox header = new VBox(6, logo, sub);
        header.setAlignment(Pos.CENTER);
        return header;
    }

    // ── dois cards: Bluetooth e USB ───────────────────────────
    private HBox buildCards() {
        cardBluetooth = buildConnCard(
                "📡",
                "BLUETOOTH",
                "ELM327 via\nBluetooth / BLE",
                true   // selecionado por padrão
        );

        cardUsb = buildConnCard(
                "🔌",
                "CABO USB",
                "ELM327 via\nPorta Serial / USB",
                false
        );

        // clique no card Bluetooth
        cardBluetooth.setOnMouseClicked(e -> selecionarTipo("bluetooth"));

        // clique no card USB
        cardUsb.setOnMouseClicked(e -> selecionarTipo("usb"));

        HBox cards = new HBox(16, cardBluetooth, cardUsb);
        cards.setAlignment(Pos.CENTER);
        return cards;
    }

    // ── factory de card de conexão ────────────────────────────
    private VBox buildConnCard(String icone, String titulo, String descricao, boolean selecionado) {
        Label iconeLabel = new Label(icone);
        iconeLabel.setStyle("-fx-font-size: 28px;");

        Label tituloLabel = new Label(titulo);
        tituloLabel.getStyleClass().add("conn-card-label");

        Label descLabel = new Label(descricao);
        descLabel.getStyleClass().add("conn-card-desc");
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox card = new VBox(12, iconeLabel, tituloLabel, descLabel);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(160);
        card.setPadding(new Insets(28, 36, 28, 36));

        // aplica estilo inicial
        aplicarEstiloCard(card, selecionado);

        // hover
        card.setOnMouseEntered(e -> {
            if (!card.getStyleClass().contains("conn-card-selected")) {
                card.setStyle(card.getStyle() +
                        "-fx-border-color: #1D9E75; -fx-background-color: #0a1a12;");
            }
        });
        card.setOnMouseExited(e -> {
            boolean estaSelecionado = card.getStyleClass().contains("conn-card-selected");
            aplicarEstiloCard(card, estaSelecionado);
        });

        card.getStyleClass().add("conn-card");
        if (selecionado) card.getStyleClass().add("conn-card-selected");

        return card;
    }

    // ── aplica estilo visual ao card ──────────────────────────
    private void aplicarEstiloCard(VBox card, boolean selecionado) {
        card.getStyleClass().removeAll("conn-card-selected");
        if (selecionado) {
            card.getStyleClass().add("conn-card-selected");
        }
    }

    // ── linha de seleção de porta ─────────────────────────────
    private HBox buildPortaRow() {
        Label portaLabel = new Label("PORTA");
        portaLabel.getStyleClass().add("conn-port-label");

        ComboBox<String> portaCombo = new ComboBox<>();
        portaCombo.getStyleClass().add("conn-combo");
        portaCombo.setStyle("-fx-background-color: #111; -fx-border-color: #222; " +
                "-fx-border-width: 1px; -fx-font-family: 'Courier New'; " +
                "-fx-font-size: 9px;");

        // preenche com portas disponíveis
        SerialPort[] portas = SerialPort.getCommPorts();
        if (portas.length == 0) {
            portaCombo.getItems().add("NENHUMA PORTA ENCONTRADA");
        } else {
            for (SerialPort p : portas) {
                portaCombo.getItems().add(p.getSystemPortName()
                        + " — " + p.getDescriptivePortName());
            }
        }
        portaCombo.getItems().add("AUTO DETECTAR");
        portaCombo.getSelectionModel().selectLast(); // AUTO por padrão

        HBox row = new HBox(10, portaLabel, portaCombo);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // ── label de status (feedback ao usuário) ────────────────
    private Label buildStatusLabel() {
        statusLabel = new Label("");
        statusLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.1em;"
        );
        statusLabel.setMinHeight(20);
        return statusLabel;
    }

    // ── botão principal de conectar ───────────────────────────
    private HBox buildBotaoConectar() {
        // botão de homologação (testes sem carro)
        javafx.scene.control.Button btnHomo = new javafx.scene.control.Button("MODO TESTE");
        btnHomo.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #333; " +
                        "-fx-border-width: 1px; -fx-border-radius: 2px; " +
                        "-fx-text-fill: #555; -fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 8px; -fx-letter-spacing: 0.15em; " +
                        "-fx-padding: 10 20 10 20; -fx-cursor: hand;"
        );
        btnHomo.setOnAction(e -> iniciarApp(new HomoObdConnection()));

        // botão principal
        javafx.scene.control.Button btnConectar =
                new javafx.scene.control.Button("CONECTAR");
        btnConectar.getStyleClass().add("btn-conectar");
        btnConectar.setOnAction(e -> iniciarConexao(btnConectar));

        HBox row = new HBox(12, btnHomo, btnConectar);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // ── lógica de seleção de tipo de conexão ─────────────────
    private void selecionarTipo(String tipo) {
        tipoConexao = tipo;
        aplicarEstiloCard(cardBluetooth, tipo.equals("bluetooth"));
        aplicarEstiloCard(cardUsb, tipo.equals("usb"));
    }

    // ── inicia o processo de conexão em thread separada ───────
    private void iniciarConexao(javafx.scene.control.Button btnConectar) {
        btnConectar.setText("CONECTANDO...");
        btnConectar.setDisable(true);

        // animação de pontinhos no status
        Timeline pontos = new Timeline(
                new KeyFrame(Duration.millis(0),   e -> setStatus("CONECTANDO ·", "#1D9E75")),
                new KeyFrame(Duration.millis(500),  e -> setStatus("CONECTANDO · ·", "#1D9E75")),
                new KeyFrame(Duration.millis(1000), e -> setStatus("CONECTANDO · · ·", "#1D9E75"))
        );
        pontos.setCycleCount(Timeline.INDEFINITE);
        pontos.play();

        ObdConnection obdConnection = new ObdConnection();

        Thread thread = new Thread(() -> {
            boolean conectou = obdConnection.detectarEConectar();

            Platform.runLater(() -> {
                pontos.stop();
                btnConectar.setDisable(false);
                btnConectar.setText("CONECTAR");

                if (conectou) {
                    setStatus("● CONECTADO — " + obdConnection.getPortName(), "#1D9E75");
                    // pequena pausa para mostrar o status antes de abrir o app
                    Timeline delay = new Timeline(
                            new KeyFrame(Duration.millis(600),
                                    ev -> iniciarApp(obdConnection))
                    );
                    delay.play();
                } else {
                    setStatus("● NENHUM DISPOSITIVO ENCONTRADO", "#E24B4A");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── abre a janela principal depois de conectar ────────────
    private void iniciarApp(IObdConnection obdConnection) {
        MainWindow mainWindow = new MainWindow(stage,obdConnection);
        mainWindow.showMainWindow();
    }

    // ── utilitário para atualizar o label de status ───────────
    private void setStatus(String texto, String cor) {
        statusLabel.setText(texto);
        statusLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: " + cor + "; -fx-letter-spacing: 0.1em;"
        );
    }
}