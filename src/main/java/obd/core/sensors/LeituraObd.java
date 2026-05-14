package obd.core.sensors;

import javafx.application.Platform;
import obd.connection.IObdConnection;
import obd.ui.tabs.SensorsTab;

import java.util.Map;

public class LeituraObd {

    private final IObdConnection obdConnection;
    private final SensorsTab sensorsTab;

    private volatile boolean rodando   = true;
    private volatile int     intervalo = 50;

    // ─────────────────────────────────────────────────────────
    public LeituraObd(IObdConnection obdConnection, SensorsTab sensorsTab) {
        this.obdConnection = obdConnection;
        this.sensorsTab    = sensorsTab;
    }

    // ── loop principal de leitura ─────────────────────────────
    public void getResponse() {
        while (rodando) {
            Map<String, SensorAtivo> ativos = sensorsTab.getSensoresAtivos();

            if (ativos.isEmpty()) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                continue;
            }

            for (SensorAtivo sensor : ativos.values()) {
                if (!rodando) break;

                try {
                    String resposta = obdConnection.enviarComando("01" + sensor.pid + "\r");
                    resposta = resposta.replace(">", "").trim();

                    if (resposta.startsWith("7F") || resposta.contains("NO DATA")
                            || resposta.isBlank()) continue;

                    String[] partes = resposta.split(" ");
                    if (partes.length < 3) continue;

                    double valor = sensor.conversor.apply(partes);

                    Platform.runLater(() ->
                            sensorsTab.atualizarSensor(sensor.pid, valor)
                    );

                } catch (Exception e) {
                    System.out.println("Erro lendo PID " + sensor.pid + ": " + e.getMessage());
                }
            }

            try { Thread.sleep(intervalo); } catch (InterruptedException ignored) {}
        }
    }

    public void stop()                  { rodando = false; }
    public void setIntervalo(int ms)    { intervalo = ms; }
}