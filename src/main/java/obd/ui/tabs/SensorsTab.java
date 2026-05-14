package obd.ui.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import obd.core.sensors.SensorAtivo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorsTab {

    // ── sensores ativos: PID → SensorAtivo ───────────────────
    private final Map<String, SensorAtivo> sensoresAtivos = new ConcurrentHashMap<>();

    // ── grid dinâmico onde os cards aparecem ──────────────────
    private GridPane grid;

    // ─────────────────────────────────────────────────────────
    public SensorsTab() {}

    // ── monta e retorna o VBox pronto para exibição ───────────
    public VBox build() {
        grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        // se já tem sensores ativos (voltou pra aba), reconstrói os cards
        redesenharGrid();

        Label vazio = new Label("ATIVE SENSORES NA ABA 'PIDs ATIVOS'");
        vazio.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: #333333; -fx-letter-spacing: 0.15em;"
        );

        StackPane overlay = new StackPane(grid, vazio);
        vazio.visibleProperty().bind(
                javafx.beans.binding.Bindings.isEmpty(
                        javafx.collections.FXCollections.observableArrayList(sensoresAtivos.values())
                )
        );

        VBox aba = new VBox(overlay);
        VBox.setVgrow(overlay, Priority.ALWAYS);
        return aba;
    }

    // ── chamado pela PidsTab ao ativar um sensor ──────────────
    public void adicionarSensor(SensorAtivo sensor) {
        if (sensoresAtivos.containsKey(sensor.pid)) return;
        sensoresAtivos.put(sensor.pid, sensor);
        if (grid != null) redesenharGrid();
    }

    // ── chamado pela PidsTab ao desativar um sensor ───────────
    public void removerSensor(String pid) {
        sensoresAtivos.remove(pid);
        if (grid != null) redesenharGrid();
    }

    // ── reconstrói o grid com os sensores atuais ──────────────
    private void redesenharGrid() {
        grid.getChildren().clear();
        int i = 0;
        for (SensorAtivo s : sensoresAtivos.values()) {
            VBox card = buildCard(s);
            grid.add(card, i % 3, i / 3);
            i++;
        }
    }

    // ── constrói o card visual de um sensor ───────────────────
    private VBox buildCard(SensorAtivo sensor) {
        Label nomeLabel = new Label(sensor.nome.toUpperCase());
        nomeLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 8px; " +
                        "-fx-text-fill: #444444; -fx-letter-spacing: 0.2em;"
        );

        Label pidLabel = new Label(sensor.pid);
        pidLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 7px; " +
                        "-fx-text-fill: #1D9E75;"
        );

        HBox header = new HBox(nomeLabel, pidLabel);
        HBox.setHgrow(nomeLabel, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        // valor — referência guardada para atualização
        sensor.valorLabel = new Label("--");
        sensor.valorLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 28px; " +
                        "-fx-font-weight: bold; -fx-text-fill: #f0f0f0;"
        );

        Label unidadeLabel = new Label(sensor.unidade);
        unidadeLabel.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; " +
                        "-fx-text-fill: #444444;"
        );

        HBox valorRow = new HBox(5, sensor.valorLabel, unidadeLabel);
        valorRow.setAlignment(Pos.BASELINE_LEFT);

        // barra de progresso
        Pane barraFundo = new Pane();
        barraFundo.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 2;");
        barraFundo.setPrefHeight(2);
        barraFundo.setMaxWidth(Double.MAX_VALUE);

        sensor.barra = new Pane();
        sensor.barra.setStyle("-fx-background-color: #1D9E75; -fx-background-radius: 2;");
        sensor.barra.setPrefHeight(2);
        sensor.barra.setPrefWidth(0);

        StackPane barraContainer = new StackPane(barraFundo, sensor.barra);
        StackPane.setAlignment(sensor.barra, Pos.CENTER_LEFT);

        VBox card = new VBox(6, header, valorRow, barraContainer);
        card.setStyle(
                "-fx-background-color: #161616; -fx-padding: 16; " +
                        "-fx-background-radius: 12; -fx-border-color: #2a2a2a; " +
                        "-fx-border-radius: 12; -fx-border-width: 0.5;"
        );

        // guarda largura para calcular barra depois
        card.widthProperty().addListener((obs, old, nv) ->
                sensor.larguraCard = nv.doubleValue()
        );

        return card;
    }

    // ── atualiza o valor de um sensor específico ──────────────
    public void atualizarSensor(String pid, double valor) {
        SensorAtivo s = sensoresAtivos.get(pid);
        if (s == null || s.valorLabel == null) return;

        s.valorLabel.setText(String.format("%.1f", valor));

        if (s.larguraCard > 0 && s.valorMax > 0) {
            double proporcao = Math.min(valor / s.valorMax, 1.0);
            s.barra.setPrefWidth(s.larguraCard * proporcao);
        }
    }

    // ── retorna os sensores ativos para o LeituraObd ──────────
    public Map<String, SensorAtivo> getSensoresAtivos() {
        return sensoresAtivos;
    }
}