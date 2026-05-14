package obd.ui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import obd.connection.IObdConnection;
import obd.core.sensors.SensorAtivo;
import obd.core.pids.PidConverter;
import obd.core.pids.PidDescription;
import obd.core.pids.PidScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PidsTab {

    private final IObdConnection obdConnection;
    private final SensorsTab sensorsTab;

    private final VBox listaPids;
    private Label contadorLabel;

    // ── mapa de configurações por PID (unidade e valorMax) ────
    private static final Map<String, Object[]> CONFIG_PIDS = new HashMap<>();
    static {
        // formato: PID -> {unidade, valorMax, nome amigável}
        CONFIG_PIDS.put("0C", new Object[]{"rpm",  6800, "RPM",         PidConverter.RPM});
        CONFIG_PIDS.put("0D", new Object[]{"km/h", 300,  "Velocidade", PidConverter.VELOCIDADE});
        CONFIG_PIDS.put("05", new Object[]{"°C",   120,  "Temp. Motor", PidConverter.TEMPERATURA});
        CONFIG_PIDS.put("0F", new Object[]{"°C",   80,   "Temp. Ar (IAT)", PidConverter.TEMPERATURA});
        CONFIG_PIDS.put("0B", new Object[]{"kPa",  255,  "Pressão MAP",PidConverter.PRESSAO});
        CONFIG_PIDS.put("11", new Object[]{"%",    100,  "TPS", PidConverter.PERCENTUAL});
        CONFIG_PIDS.put("04", new Object[]{"%",    100,  "Carga Motor", PidConverter.PERCENTUAL});
        CONFIG_PIDS.put("06", new Object[]{"%",    100,  "STFT Banco 1", PidConverter.PERCENTUAL});
        CONFIG_PIDS.put("07", new Object[]{"%",    100,  "LTFT Banco 1", PidConverter.PERCENTUAL});
        CONFIG_PIDS.put("0E", new Object[]{"°",    64,   "Avanço Ign.", PidConverter.AVANCO});
        CONFIG_PIDS.put("10", new Object[]{"g/s",  655,  "MAF", PidConverter.MAF});
        CONFIG_PIDS.put("2F", new Object[]{"%",    100,  "Nível Comb.", PidConverter.PERCENTUAL});
        CONFIG_PIDS.put("33", new Object[]{"kPa",  255,  "Pressão Baro.", PidConverter.PRESSAO});
        CONFIG_PIDS.put("46", new Object[]{"°C",   80,   "Temp. Ambiente", PidConverter.TEMPERATURA});
        CONFIG_PIDS.put("5C", new Object[]{"°C",   150,  "Temp. Óleo", PidConverter.TEMPERATURA});
        CONFIG_PIDS.put("52", new Object[]{"%",    100,  "Etanol", PidConverter.PERCENTUAL});
    }

    // ─────────────────────────────────────────────────────────
    public PidsTab(IObdConnection obdConnection, SensorsTab sensorsTab) {
        this.obdConnection = obdConnection;
        this.sensorsTab    = sensorsTab;
        this.listaPids     = new VBox(4);
    }

    // ── monta e retorna o VBox pronto para exibição ───────────
    public VBox build() {
        Label titulo = new Label("PIDs SUPORTADOS");
        titulo.getStyleClass().add("config-title");

        contadorLabel = new Label("—");
        contadorLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #333333; -fx-letter-spacing: 0.1em;"
        );

        Button btnScanear = new Button("ESCANEAR PIDs");
        btnScanear.getStyleClass().addAll("btn", "btn-green");
        btnScanear.setOnAction(e -> escanear(btnScanear));

        HBox toolbar = new HBox(12, btnScanear, contadorLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        Label vazio = new Label("PRESSIONE 'ESCANEAR PIDs' PARA INICIAR");
        vazio.getStyleClass().add("dtc-empty");
        vazio.setPadding(new Insets(20));
        listaPids.getChildren().add(vazio);

        ScrollPane scroll = new ScrollPane(listaPids);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox aba = new VBox(titulo, toolbar, scroll);
        aba.setPadding(new Insets(12));
        aba.setSpacing(0);
        return aba;
    }

    // ── escaneia os PIDs em background ────────────────────────
    private void escanear(Button btnScanear) {
        btnScanear.setDisable(true);
        listaPids.getChildren().clear();

        Label lendo = new Label("ESCANEANDO...");
        lendo.setStyle(
                "-fx-text-fill: #1D9E75; -fx-font-family: 'Courier New'; -fx-font-size: 9px;"
        );
        listaPids.getChildren().add(lendo);
        contadorLabel.setText("—");

        Thread t = new Thread(() -> {
            try {
                PidScanner scanner = new PidScanner(obdConnection);
                List<String> pids  = scanner.scanearPids();

                Platform.runLater(() -> {
                    listaPids.getChildren().clear();
                    for (String pid : pids) {
                        listaPids.getChildren().add(buildPidRow(pid));
                    }
                    contadorLabel.setText(pids.size() + " PIDs ENCONTRADOS");
                    btnScanear.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    listaPids.getChildren().clear();
                    Label erro = new Label("ERRO: " + ex.getMessage());
                    erro.setStyle(
                            "-fx-text-fill: #E24B4A; -fx-font-family: 'Courier New'; " +
                                    "-fx-font-size: 9px;"
                    );
                    listaPids.getChildren().add(erro);
                    btnScanear.setDisable(false);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── linha de PID com toggle ───────────────────────────────
    private HBox buildPidRow(String pid) {
        Label pidLabel = new Label(pid);
        pidLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: #1D9E75; -fx-min-width: 32px;"
        );

        Label descLabel = new Label(PidDescription.getDescricao(pid));
        descLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; -fx-text-fill: #888888;"
        );
        HBox.setHgrow(descLabel, Priority.ALWAYS);

        // toggle — começa OFF
        Label toggle = new Label("OFF");
        toggle.setStyle(estiloToggle(false));
        toggle.setCursor(javafx.scene.Cursor.HAND);

        final boolean[] ativo = {false};
        toggle.setOnMouseClicked(e -> {
            ativo[0] = !ativo[0];
            toggle.setText(ativo[0] ? "ON" : "OFF");
            toggle.setStyle(estiloToggle(ativo[0]));

            if (ativo[0]) {
                // monta o SensorAtivo e envia para SensorsTab
                Object[] config = CONFIG_PIDS.getOrDefault(pid,
                        new Object[]{"", 100, PidDescription.getDescricao(pid), PidConverter.PERCENTUAL});
                SensorAtivo sensor = new SensorAtivo(
                        pid,
                        (String) config[2],
                        (String) config[0],
                        ((Number) config[1]).doubleValue(),
                        (Function<String[], Double>) config[3]

                );
                sensorsTab.adicionarSensor(sensor);
            } else {
                sensorsTab.removerSensor(pid);
            }
        });

        HBox row = new HBox(12, pidLabel, descLabel, toggle);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: #111111; -fx-border-color: #1a1a1a; " +
                        "-fx-border-width: 1px; -fx-border-radius: 3px; " +
                        "-fx-background-radius: 3px; -fx-padding: 8 12 8 12;"
        );
        return row;
    }

    // ── estilo do toggle ──────────────────────────────────────
    private String estiloToggle(boolean ativo) {
        String cor   = ativo ? "#1D9E75" : "#555555";
        String fundo = ativo ? "#0a1a12" : "transparent";
        return "-fx-font-family: 'Courier New'; -fx-font-size: 7px; " +
                "-fx-text-fill: " + cor + "; -fx-background-color: " + fundo + "; " +
                "-fx-border-color: " + cor + "; -fx-border-width: 1px; " +
                "-fx-border-radius: 2px; -fx-background-radius: 2px; " +
                "-fx-padding: 2 8 2 8; -fx-letter-spacing: 0.1em; " +
                "-fx-min-width: 36px; -fx-alignment: center;";
    }
}