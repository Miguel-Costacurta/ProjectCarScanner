package obd.core.sensors;

import javafx.application.Platform;
import obd.connection.IObdConnection;
import obd.core.pids.ElmParser;
import obd.database.daos.LeituraDao;
import obd.database.models.Leitura;
import obd.ui.tabs.SensorsTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObdReader {

    private final IObdConnection obdConnection;
    private final SensorsTab sensorsTab;
    private final LeituraDao leituraDao = new LeituraDao();
    private final Map<String, Double> ultimosValores = new ConcurrentHashMap<>();
    private volatile long ultimaAtualizacaoUi = 0;

    private static final long UI_INTERVAL_NS = 50_000_000L;

    private volatile boolean rodando   = true;
    private volatile int     intervalo = 0;

    private volatile boolean gravando = false;
    private volatile int sessaoAtiva = -1;
    private final List<Leitura> buffer = new ArrayList<>();
    private static final int BUFFER_SIZE = 20;

    // ─────────────────────────────────────────────────────────
    public ObdReader(IObdConnection obdConnection, SensorsTab sensorsTab) {
        this.obdConnection = obdConnection;
        this.sensorsTab    = sensorsTab;
    }

    // ── loop principal de leitura ─────────────────────────────
    public void getResponse() {
        while (rodando) {
            Map<String, ActiveSensor> ativos = sensorsTab.getSensoresAtivos();

            if (ativos.isEmpty()) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                continue;
            }

            for (ActiveSensor sensor : new ArrayList<>(ativos.values())) {
                if (!rodando) break;

                try {
                    String resposta = obdConnection.enviarComando("01" + sensor.pid + "\r");
                    resposta = resposta.replace(">", "").trim();

                    if (resposta.startsWith("7F") || resposta.contains("NO DATA")
                            || resposta.isBlank()) continue;

                    String[] partes = ElmParser.extrairResposta41(resposta, sensor.pid);

                    if(partes.length < 3) continue;

                    double valor = sensor.conversor.apply(partes);

                    publicarValor(sensor.pid, valor);

                    if(gravando && sessaoAtiva > 0){
                        synchronized (buffer) {
                            buffer.add(new Leitura(sessaoAtiva, sensor.pid, valor));
                            if (buffer.size() >= BUFFER_SIZE){
                                flushBuffer();
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Erro lendo PID " + sensor.pid + ": " + e.getMessage());
                }
            }
            if(intervalo > 0){
                try {
                    Thread.sleep(intervalo);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void publicarValor(String pid, double valor) {
        ultimosValores.put(pid, valor);

        long agora = System.nanoTime();

        if (agora - ultimaAtualizacaoUi < UI_INTERVAL_NS) {
            return;
        }

        ultimaAtualizacaoUi = agora;

        Map<String, Double> snapshot = new ConcurrentHashMap<>(ultimosValores);

        Platform.runLater(() -> {
            for (Map.Entry<String, Double> e : snapshot.entrySet()) {
                sensorsTab.atualizarSensor(e.getKey(), e.getValue());
            }
        });
    }
    public void iniciarGravacao(int sessaoId){
        synchronized (buffer) {buffer.clear();}
        this.sessaoAtiva = sessaoId;
        this.gravando = true;
        System.out.println("Gravação iniciado - sessão " + sessaoId);
    }

    public void pararGravacao(){
        this.gravando = false;
        synchronized (buffer) {flushBuffer();}
        System.out.println("Gravação finalizada.");

    }

    public void flushBuffer() {
        if (buffer.isEmpty()) return;
         try {
             leituraDao.salvarLote(new ArrayList<>(buffer));
             buffer.clear();
         }catch ( Exception e){
             System.out.println("Erro ao salvar leituras: " + e.getMessage());
         }
    }

    public void stop()                  { rodando = false; }
    public void setIntervalo(int ms)    { intervalo = ms; }
    public boolean isGravando()            {return gravando;}
}