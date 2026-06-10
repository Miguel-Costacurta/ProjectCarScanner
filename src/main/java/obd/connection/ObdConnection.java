package obd.connection;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;

public class ObdConnection implements IObdConnection {

    private static final int[] BAUDS = {38400, 115200, 9600};

    // Para seu caso K-Line/KWP/ISO no Astra.
    // Ordem proposital: tenta KWP fast, KWP slow, ISO9141.
    private static final String[] PROTOCOLOS_KLINE = {"ATSP5", "ATSP4", "ATSP3"};

    // Primeiro tenta agressivo. Se não responder 4100, tenta mais tolerante.
    private static final String[] TIMEOUTS_INICIAIS_ELM = {"ATST14", "ATST20"};

    private static final long PARK_NANOS = 200_000L; // 0,2 ms
    private static final int WRITE_TIMEOUT_MS = 150;

    private SerialPort porta;

    private volatile int timeoutObdMs = 180;
    private volatile boolean logTempo = false;

    private volatile String protocoloAtual = "";
    private volatile String timeoutElmAtual = "";

    private long totalTempoNs = 0;
    private long totalComandos = 0;
    private long ultimoTempoNs = 0;

    @Override
    public synchronized boolean openConnection() {
        if (porta == null) return false;
        if (porta.isOpen()) return true;

        int baud = porta.getBaudRate() > 0 ? porta.getBaudRate() : 38400;
        configurarPorta(porta, baud);

        boolean ok = porta.openPort();

        if (ok) {
            configurarPorta(porta, baud);
        }

        return ok;
    }

    @Override
    public synchronized boolean closeConnection() {
        if (porta == null) return true;

        boolean ok = !porta.isOpen() || porta.closePort();

        porta = null;
        protocoloAtual = "";
        timeoutElmAtual = "";

        return ok;
    }

    @Override
    public synchronized String enviarComando(String comando) throws Exception {
        return enviar(comando, timeoutObdMs, true);
    }

    @Override
    public synchronized boolean detectarEConectar() {
        closeConnection();

        SerialPort[] portas = SerialPort.getCommPorts();

        for (SerialPort candidata : portas) {
            for (int baud : BAUDS) {
                porta = candidata;

                configurarPorta(porta, baud);

                // BUG corrigido: antes precisa abrir a porta.
                if (!porta.openPort()) {
                    porta = null;
                    continue;
                }

                // Reaplica após abrir. Em alguns drivers isso evita configuração ignorada.
                configurarPorta(porta, baud);

                boolean manterAberta = false;

                try {
                    limparEntrada();

                    String id = enviar("ATZ", 1500, false);

                    if (!pareceElm(id)) {
                        id = enviar("ATI", 700, false);
                    }

                    // Alguns clones são ruins para identificação, mas respondem OK em AT.
                    if (!pareceElm(id)) {
                        String ate0 = enviar("ATE0", 300, false);
                        if (!respostaOk(ate0)) {
                            continue;
                        }
                    }

                    if (inicializarElmRapido()) {
                        manterAberta = true;

                        if (logTempo) {
                            System.out.printf(
                                    Locale.ROOT,
                                    "ELM conectado: porta=%s baud=%d protocolo=%s timeout=%s%n",
                                    porta.getDescriptivePortName(),
                                    baud,
                                    protocoloAtual,
                                    timeoutElmAtual
                            );
                        }

                        return true;
                    }

                } catch (Exception e) {
                    if (logTempo) {
                        System.out.println(
                                "Falha na porta " + candidata.getSystemPortName()
                                        + " baud " + baud + ": " + e.getMessage()
                        );
                    }
                } finally {
                    if (!manterAberta) {
                        if (porta != null && porta.isOpen()) {
                            porta.closePort();
                        }
                        porta = null;
                    }
                }
            }
        }

        return false;
    }

    private boolean inicializarElmRapido() throws Exception {
        // Setup básico para reduzir bytes e trabalho do parser.
        enviar("ATE0", 300, false);   // Echo off
        enviar("ATL0", 300, false);   // Linefeeds off
        enviar("ATS0", 300, false);   // Spaces off
        enviar("ATH0", 300, false);   // Headers off

        // Se o clone não suportar, normalmente responde "?", mas não precisa abortar.
        enviar("ATAT2", 300, false);  // Adaptive timing aggressive

        for (String st : TIMEOUTS_INICIAIS_ELM) {
            enviar(st, 300, false);

            for (String protocolo : PROTOCOLOS_KLINE) {
                // Fecha sessão/protocolo anterior antes de trocar.
                enviar("ATPC", 300, false);

                String rProto = enviar(protocolo, 1000, false);

                if (!respostaOk(rProto)) {
                    continue;
                }

                enviar(st, 300, false);

                String r0100 = enviar("0100", 2200, false);

                if (contemResposta4100(r0100)) {
                    protocoloAtual = protocolo;
                    timeoutElmAtual = st;

                    String dp = enviar("ATDP", 500, false);

                    if (logTempo) {
                        System.out.println("Protocolo detectado: " + limparLinha(dp));
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private String enviar(String comando, int timeoutMs, boolean medirTempo) throws Exception {
        if (porta == null || !porta.isOpen()) {
            throw new IllegalStateException("Porta OBD não está aberta.");
        }

        limparEntrada();

        String comandoFinal = comando.endsWith("\r") ? comando : comando + "\r";
        byte[] dados = comandoFinal.getBytes(StandardCharsets.US_ASCII);

        long inicio = System.nanoTime();
        long limite = inicio + timeoutMs * 1_000_000L;

        escreverTudo(dados, WRITE_TIMEOUT_MS);

        StringBuilder resposta = new StringBuilder(128);
        byte[] buffer = new byte[256];

        boolean recebeuPrompt = false;

        while (System.nanoTime() < limite) {
            int disponiveis = porta.bytesAvailable();

            if (disponiveis > 0) {
                int paraLer = Math.min(disponiveis, buffer.length);
                int lidos = porta.readBytes(buffer, paraLer);

                if (lidos < 0) {
                    throw new RuntimeException("Erro lendo da serial.");
                }

                if (lidos == 0) {
                    LockSupport.parkNanos(PARK_NANOS);
                    continue;
                }

                for (int i = 0; i < lidos; i++) {
                    char c = (char) (buffer[i] & 0xFF);
                    resposta.append(c);

                    if (c == '>') {
                        recebeuPrompt = true;
                        break;
                    }
                }

                if (recebeuPrompt) {
                    break;
                }

                continue;
            }

            LockSupport.parkNanos(PARK_NANOS);
        }

        long duracaoNs = System.nanoTime() - inicio;

        if (medirTempo) {
            registrarTempo(duracaoNs, comando, resposta.toString(), !recebeuPrompt);
        }

        return resposta.toString();
    }

    private void escreverTudo(byte[] dados, int timeoutMs) throws Exception {
        long limite = System.nanoTime() + timeoutMs * 1_000_000L;
        int offset = 0;

        while (offset < dados.length && System.nanoTime() < limite) {
            int escritos = porta.writeBytes(dados, dados.length - offset, offset);

            if (escritos < 0) {
                throw new RuntimeException("Erro escrevendo na serial.");
            }

            if (escritos > 0) {
                offset += escritos;
                continue;
            }

            LockSupport.parkNanos(PARK_NANOS);
        }

        if (offset < dados.length) {
            throw new RuntimeException("Timeout escrevendo comando na serial.");
        }
    }

    private void limparEntrada() {
        if (porta == null || !porta.isOpen()) return;

        byte[] buffer = new byte[512];

        for (int i = 0; i < 8; i++) {
            int disponiveis = porta.bytesAvailable();

            if (disponiveis <= 0) {
                break;
            }

            int lidos = porta.readBytes(buffer, Math.min(disponiveis, buffer.length));

            if (lidos <= 0) {
                break;
            }

            LockSupport.parkNanos(100_000L);
        }
    }

    private void configurarPorta(SerialPort p, int baud) {
        p.setComPortParameters(
                baud,
                8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY
        );

        p.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

        p.setComPortTimeouts(
                SerialPort.TIMEOUT_NONBLOCKING,
                0,
                0
        );
    }

    private void registrarTempo(long duracaoNs, String comando, String resposta, boolean timeout) {
        totalTempoNs += duracaoNs;
        totalComandos++;
        ultimoTempoNs = duracaoNs;

        if (!logTempo) return;

        double ms = duracaoNs / 1_000_000.0;
        String compacta = limparLinha(resposta);

        if (compacta.length() > 80) {
            compacta = compacta.substring(0, 80) + "...";
        }

        System.out.printf(
                Locale.ROOT,
                "OBD %-6s %7.2f ms %s %s%n",
                comando.replace("\r", "").trim(),
                ms,
                timeout ? "TIMEOUT" : "OK     ",
                compacta
        );
    }

    private static boolean pareceElm(String resposta) {
        if (resposta == null) return false;

        String u = resposta.toUpperCase(Locale.ROOT);

        return u.contains("ELM")
                || u.contains("OBD")
                || u.contains("SCANTOOL")
                || u.contains("VGATE");
    }

    private static boolean respostaOk(String resposta) {
        if (resposta == null) return false;

        String u = resposta.toUpperCase(Locale.ROOT);

        return u.contains("OK")
                || u.contains("ELM")
                || u.contains("OBD");
    }

    private static boolean contemResposta4100(String resposta) {
        return somenteHex(resposta).contains("4100");
    }

    private static String somenteHex(String raw) {
        if (raw == null) return "";

        String u = raw.toUpperCase(Locale.ROOT);
        StringBuilder hex = new StringBuilder(u.length());

        for (int i = 0; i < u.length(); i++) {
            char c = u.charAt(i);

            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                hex.append(c);
            }
        }

        return hex.toString();
    }

    private static String limparLinha(String s) {
        if (s == null) return "";

        return s
                .replace("\r", "")
                .replace("\n", "")
                .replace(">", "")
                .trim();
    }

    public void setTimeoutObdMs(int timeoutObdMs) {
        if (timeoutObdMs < 50) {
            throw new IllegalArgumentException("timeoutObdMs muito baixo: " + timeoutObdMs);
        }

        this.timeoutObdMs = timeoutObdMs;
    }

    public int getTimeoutObdMs() {
        return timeoutObdMs;
    }

    public void setLogTempo(boolean logTempo) {
        this.logTempo = logTempo;
    }

    public boolean isLogTempo() {
        return logTempo;
    }

    public synchronized double getMediaTempoMs() {
        if (totalComandos == 0) return 0.0;

        return (totalTempoNs / 1_000_000.0) / totalComandos;
    }

    public synchronized double getUltimoTempoMs() {
        return ultimoTempoNs / 1_000_000.0;
    }

    public synchronized long getTotalComandosMedidos() {
        return totalComandos;
    }

    public synchronized void resetMediaTempo() {
        totalTempoNs = 0;
        totalComandos = 0;
        ultimoTempoNs = 0;
    }

    public String getProtocoloAtual() {
        return protocoloAtual;
    }

    public String getTimeoutElmAtual() {
        return timeoutElmAtual;
    }

    @Override
    public String getPortName() {
        if (porta == null) return "";

        return porta.getDescriptivePortName();
    }
}