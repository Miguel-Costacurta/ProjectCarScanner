package obd.ui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import obd.connection.IObdConnection;
import obd.core.dtcs.DtcDescription;
import obd.core.dtcs.DtcReader;

import java.util.List;

public class DtcTab {

    private final IObdConnection obdConnection;
    private final VBox resultados;

    // ─────────────────────────────────────────────────────────
    public DtcTab(IObdConnection obdConnection) {
        this.obdConnection = obdConnection;
        this.resultados = new VBox(6);
    }

    // ── monta e retorna o VBox pronto para exibição ───────────
    public VBox build() {
        Label titulo = new Label("CÓDIGOS DE FALHA");
        titulo.getStyleClass().add("config-title");
        titulo.setPadding(new Insets(0, 0, 10, 0));

        Button btnLer    = new Button("LER DTCs");
        Button btnApagar = new Button("APAGAR DTCs");
        btnLer.getStyleClass().addAll("btn", "btn-green");
        btnApagar.getStyleClass().addAll("btn", "btn-red");

        btnLer.setOnAction(e    -> lerDtcs(btnLer, btnApagar));
        btnApagar.setOnAction(e -> apagarDtcs(btnLer, btnApagar));

        HBox toolbar = new HBox(8, btnLer, btnApagar);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        // estado inicial
        Label vazio = new Label("PRESSIONE 'LER DTCs' PARA VERIFICAR");
        vazio.getStyleClass().add("dtc-empty");
        vazio.setPadding(new Insets(20));
        resultados.getChildren().add(vazio);

        ScrollPane scroll = new ScrollPane(resultados);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox aba = new VBox(titulo, toolbar, scroll);
        aba.setPadding(new Insets(12));
        aba.setSpacing(0);
        return aba;
    }

    // ── lê DTCs em background ─────────────────────────────────
    private void lerDtcs(Button btnLer, Button btnApagar) {
        setBotoes(btnLer, btnApagar, false);
        setStatus("LENDO...", "#1D9E75");

        Thread t = new Thread(() -> {
            try {
                DtcReader reader = new DtcReader(obdConnection);
                List<String> codigos = reader.lerDtcs();

                Platform.runLater(() -> {
                    resultados.getChildren().clear();
                    if (codigos.isEmpty()) {
                        Label ok = new Label("✓ NENHUM DTC ENCONTRADO");
                        ok.setStyle("-fx-text-fill: #1D9E75; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                        resultados.getChildren().add(ok);
                    } else {
                        for (String codigo : codigos) {
                            resultados.getChildren().add(buildDtcRow(codigo));
                        }
                    }
                    setBotoes(btnLer, btnApagar, true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setStatus("ERRO: " + ex.getMessage(), "#E24B4A");
                    setBotoes(btnLer, btnApagar, true);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── apaga DTCs em background ──────────────────────────────
    private void apagarDtcs(Button btnLer, Button btnApagar) {
        setBotoes(btnLer, btnApagar, false);

        Thread t = new Thread(() -> {
            try {
                obdConnection.enviarComando("04\r");
                Platform.runLater(() -> {
                    resultados.getChildren().clear();
                    Label ok = new Label("✓ DTCs APAGADOS COM SUCESSO");
                    ok.setStyle("-fx-text-fill: #1D9E75; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                    resultados.getChildren().add(ok);
                    setBotoes(btnLer, btnApagar, true);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setStatus("ERRO AO APAGAR: " + ex.getMessage(), "#E24B4A");
                    setBotoes(btnLer, btnApagar, true);
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── linha de DTC individual ───────────────────────────────
    private HBox buildDtcRow(String codigo) {
        Label codLabel = new Label(codigo);
        codLabel.getStyleClass().add("dtc-code");

        Label descLabel = new Label(DtcDescription.getDescricao(codigo));
        descLabel.getStyleClass().add("dtc-desc");
        HBox.setHgrow(descLabel, Priority.ALWAYS);

        Label badge = new Label("ATIVO");
        badge.getStyleClass().add("dtc-badge");

        HBox row = new HBox(12, codLabel, descLabel, badge);
        row.getStyleClass().add("dtc-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── helpers ───────────────────────────────────────────────
    private void setStatus(String texto, String cor) {
        resultados.getChildren().clear();
        Label label = new Label(texto);
        label.setStyle("-fx-text-fill: " + cor + "; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
        resultados.getChildren().add(label);
    }

    private void setBotoes(Button btnLer, Button btnApagar, boolean habilitado) {
        btnLer.setDisable(!habilitado);
        btnApagar.setDisable(!habilitado);
    }
}