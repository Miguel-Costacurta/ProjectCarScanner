package obd.core.sensors;

import javafx.application.Platform;
import obd.connection.IObdConnection;
import obd.core.pids.ElmParser;
import obd.database.daos.LeituraDao;
import obd.database.models.Leitura;
import obd.ui.tabs.SensorsTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class ObdReader {

    private final IObdConnection obdConnection;
    private final SensorsTab sensorsTab;
    private final LeituraDao leituraDao = new LeituraDao();

    private final Map<String, Double> ultimosValores = new ConcurrentHashMap<>();

    private final Map<String, Long> proximaLeituraNs = new ConcurrentHashMap<>();

    private final Map<String, String> comandosPorPid = new ConcurrentHashMap<>();

    private final Map<String, Long> ultimoLogErroNs = new ConcurrentHashMap<>();

    private volatile long ultimaAtualizacaoUi = 0;
    private static final long UI_INTERVAL_NS = 50_000_000L; // 50 ms

    private static final long NS_POR_MS = 1_000_000L;
    private static final long LOOP_IDLE_NS = 1_000_000L; // 1 ms

    private volatile boolean rodando = true;

    private volatile int intervalo = 0;

    private volatile boolean gravando = false;

    private volatile int sessaoAtiva = -1;

    private static final int DB_QUEUE_SIZE = 10_000;
    private static final int DB_BATCH_SIZE = 100;
    private static final long DB_POLL_MS = 500;

    private final BlockingQueue<Leitura> filaBanco = new LinkedBlockingQueue<>(DB_QUEUE_SIZE);
    private final Object dbLock = new Object();

    private final AtomicLong leiturasDescartadasBanco = new AtomicLong(0);

    private volatile boolean writerRodando = true;
    private Thread writerThread;

    public ObdReader(IObdConnection obdConnection, SensorsTab sensorsTab) {
        this.obdConnection = obdConnection;
        this.sensorsTab = sensorsTab;
        iniciarWriterBanco();
    }

    public void getResponse() {
        while (rodando) {
            Map<String, ActiveSensor> ativos = sensorsTab.getSensoresAtivos();

            if (ativos.isEmpty()) {
                dormir(200);
                continue;
            }

            List<ActiveSensor> snapshotSensores = new ArrayList<>(ativos.values());

            boolean leuAlgumPid = false;

            for (ActiveSensor sensor : snapshotSensores) {
                if (!rodando) break;
                if (sensor == null || sensor.pid == null) continue;

                long agora = System.nanoTime();

                if (!deveLerAgora(sensor.pid, agora)) {
                    continue;
                }

                try {
                    String comando = comandosPorPid.computeIfAbsent(
                            sensor.pid,
                            pid -> "01" + pid.toUpperCase(Locale.ROOT)
                    );

                    String resposta = obdConnection.enviarComando(comando);

                    leuAlgumPid = true;

                    if (respostaInvalidaRapida(resposta)) {
                        agendarProximaLeitura(sensor.pid);
                        continue;
                    }

                    String[] partes = ElmParser.extrairResposta41(resposta, sensor.pid);

                    if (partes.length < 3) {
                        agendarProximaLeitura(sensor.pid);
                        continue;
                    }

                    double valor = converterValor(sensor, partes);

                    publicarValor(sensor.pid, valor);
                    gravarSeNecessario(sensor.pid, valor);

                    agendarProximaLeitura(sensor.pid);

                } catch (Exception e) {
                    agendarProximaLeitura(sensor.pid);
                    logErroLimitado(sensor.pid, e);
                }
            }

            if (intervalo > 0) {
                dormir(intervalo);
            } else if (!leuAlgumPid) {
                LockSupport.parkNanos(LOOP_IDLE_NS);
            }
        }

        flushBuffer();
    }

    private boolean deveLerAgora(String pid, long agoraNs) {
        int periodoMs = periodoPidMs(pid);

        if (periodoMs <= 0) {
            return true;
        }

        long proximo = proximaLeituraNs.getOrDefault(pid, 0L);

        return agoraNs >= proximo;
    }

    private void agendarProximaLeitura(String pid) {
        int periodoMs = periodoPidMs(pid);

        if (periodoMs <= 0) {
            proximaLeituraNs.remove(pid);
            return;
        }

        proximaLeituraNs.put(pid, System.nanoTime() + periodoMs * NS_POR_MS);
    }

    private int periodoPidMs(String pid) {
        if (pid == null) return 500;

        return switch (pid.toUpperCase(Locale.ROOT)) {
            // Loop rápido: lê sempre que o ELM liberar.
            case "0C" -> 0;    // RPM
            case "11" -> 0;    // TPS
            case "0B" -> 0;    // MAP

            // Loop médio.
            case "0D" -> 150;  // velocidade
            case "0E" -> 150;  // avanço ignição

            // Loop lento.
            case "05" -> 800;  // temperatura água
            case "0F" -> 800;  // IAT
            case "04" -> 500;  // carga calculada
            case "06" -> 800;  // STFT banco 1
            case "07" -> 800;  // LTFT banco 1
            case "10" -> 500;  // MAF
            case "2F" -> 1000; // combustível
            case "33" -> 1000; // pressão barométrica
            case "46" -> 1000; // temperatura ambiente
            case "5C" -> 1000; // temperatura óleo
            case "52" -> 1000; // etanol

            default -> 500;
        };
    }

    private boolean respostaInvalidaRapida(String resposta) {
        if (resposta == null) return true;

        String u = resposta.toUpperCase(Locale.ROOT);

        return u.isBlank()
                || u.contains("NO DATA")
                || u.contains("UNABLE")
                || u.contains("STOPPED")
                || u.contains("CAN ERROR")
                || u.contains("BUS ERROR")
                || u.contains("BUFFER FULL")
                || u.contains("SEARCHING")
                || u.contains("?");
    }

    private double converterValor(ActiveSensor sensor, String[] partes) {
        Object convertido = sensor.conversor.apply(partes);

        if (convertido instanceof Number n) {
            return n.doubleValue();
        }

        throw new IllegalStateException(
                "Conversor do PID " + sensor.pid + " não retornou Number: " + convertido
        );
    }

    private void publicarValor(String pid, double valor) {
        ultimosValores.put(pid, valor);

        long agora = System.nanoTime();

        if (agora - ultimaAtualizacaoUi < UI_INTERVAL_NS) {
            return;
        }

        ultimaAtualizacaoUi = agora;

        Map<String, Double> snapshot = new HashMap<>(ultimosValores);

        Platform.runLater(() -> {
            for (Map.Entry<String, Double> e : snapshot.entrySet()) {
                sensorsTab.atualizarSensor(e.getKey(), e.getValue());
            }
        });
    }

    private void iniciarWriterBanco() {
        if (writerThread != null && writerThread.isAlive()) {
            return;
        }

        writerRodando = true;

        writerThread = new Thread(this::loopWriterBanco, "obd-db-writer");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    private void loopWriterBanco() {
        List<Leitura> lote = new ArrayList<>(DB_BATCH_SIZE);

        while (writerRodando || !filaBanco.isEmpty()) {
            try {
                Leitura primeira = filaBanco.poll(DB_POLL_MS, TimeUnit.MILLISECONDS);

                if (primeira != null) {
                    lote.add(primeira);
                    filaBanco.drainTo(lote, DB_BATCH_SIZE - 1);
                }

                if (!lote.isEmpty()) {
                    salvarLoteSeguro(lote);
                    lote.clear();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                if (!writerRodando) {
                    break;
                }

            } catch (Exception e) {
                System.out.println("Erro no writer do banco: " + e.getMessage());
                lote.clear();
            }
        }

        flushFilaBanco();
    }

    private void gravarSeNecessario(String pid, double valor) {
        if (!gravando || sessaoAtiva <= 0) {
            return;
        }

        Leitura leitura = new Leitura(sessaoAtiva, pid, valor);

        boolean adicionou = filaBanco.offer(leitura);

        if (!adicionou) {
            leiturasDescartadasBanco.incrementAndGet();
        }
    }

    private void salvarLoteSeguro(List<Leitura> lote) throws Exception {
        if (lote == null || lote.isEmpty()) return;

        synchronized (dbLock) {
            leituraDao.salvarLote(new ArrayList<>(lote));
        }
    }

    private void flushFilaBanco() {
        List<Leitura> lote = new ArrayList<>(DB_BATCH_SIZE);

        try {
            while (!filaBanco.isEmpty()) {
                filaBanco.drainTo(lote, DB_BATCH_SIZE);

                if (!lote.isEmpty()) {
                    salvarLoteSeguro(lote);
                    lote.clear();
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao salvar leituras pendentes: " + e.getMessage());
        }
    }

    public void iniciarGravacao(int sessaoId) {
        if (sessaoId <= 0) {
            throw new IllegalArgumentException("sessaoId inválido: " + sessaoId);
        }

        iniciarWriterBanco();

        filaBanco.clear();
        leiturasDescartadasBanco.set(0);

        this.sessaoAtiva = sessaoId;
        this.gravando = true;

        System.out.println("Gravação iniciada - sessão " + sessaoId);
    }

    public void pararGravacao() {
        this.gravando = false;

        flushFilaBanco();

        long descartadas = leiturasDescartadasBanco.get();

        if (descartadas > 0) {
            System.out.println("Gravação finalizada. Leituras descartadas por fila cheia: " + descartadas);
        } else {
            System.out.println("Gravação finalizada.");
        }

        this.sessaoAtiva = -1;
    }

    public void flushBuffer() {
        flushFilaBanco();
    }

    public void stop() {
        rodando = false;
        gravando = false;

        flushBuffer();

        writerRodando = false;

        if (writerThread != null) {
            writerThread.interrupt();
        }
    }

    public void setIntervalo(int ms) {
        if (ms < 0) {
            ms = 0;
        }

        intervalo = ms;
    }

    public int getIntervalo() {
        return intervalo;
    }

    public boolean isGravando() {
        return gravando;
    }

    public long getLeiturasDescartadasBanco() {
        return leiturasDescartadasBanco.get();
    }

    private void dormir(int ms) {
        if (ms <= 0) return;

        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void logErroLimitado(String pid, Exception e) {
        long agora = System.nanoTime();
        long ultimo = ultimoLogErroNs.getOrDefault(pid, 0L);

        if (agora - ultimo < 2_000_000_000L) {
            return;
        }

        ultimoLogErroNs.put(pid, agora);

        System.out.println("Erro lendo PID " + pid + ": " + e.getMessage());
    }
}