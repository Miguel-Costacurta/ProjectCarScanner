package obd.connection;

import com.fazecast.jSerialComm.SerialPort;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ObdConnection implements IObdConnection{
    private SerialPort porta;

    private static final int[] BAUDS_TESTE = {38400, 115200, 9600};

    private volatile int timeoutObdMs = 180;

    private volatile boolean logTempo = false;

    private long totalComandos = 0;
    private long somaTempoNs = 0;

    @Override
    public boolean openConnection(){
        if (porta == null) return false;
        if (porta.isOpen()) return true;

        porta.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING,0,0);
        return porta.openPort();
    }
    @Override
    public boolean closeConnection(){
        if (porta == null) return false;
        return porta.closePort();
    }

    @Override
    public String enviarComando(String comando) throws Exception {
        return enviar(comando, timeoutObdMs, true);
    }

    private String enviar(String comando, int timeoutObdMs, boolean medir) throws Exception{
        if (porta == null || !porta.isOpen()){
            throw new IllegalStateException("Porta serial não está aberta");
        }

        if(!comando.endsWith("\r")){
            comando += "\r";
        }

        limparEntrada();

        byte[] out = comando.getBytes(StandardCharsets.US_ASCII);

        long inicio = System.nanoTime();

        int written = porta.writeBytes(out, out.length);

        if(written != out.length){
            throw  new RuntimeException("Falha ao escrever comando na Serial");
        }

        StringBuilder sb = new StringBuilder(96);
        byte[] buffer = new byte[128];

        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutObdMs);

        while(System.nanoTime() < deadline){
            int available = porta.bytesAvailable();

            if(available > 0){
                int paraLer = Math.min(buffer.length, available);
                int lidos = porta.readBytes(buffer, paraLer);

                if(lidos > 0) {
                    for(int i = 0; i < lidos; i++){
                        char c = (char) (buffer[i] & 0xFF);
                        sb.append(c);

                        if(c== '>'){
                            registrarTempo(inicio, comando, medir, false);
                            return sb.toString();
                        }
                    }
                }
            }else {
                Thread.sleep(200_00L);
            }
        }

        registrarTempo(inicio,comando,medir,true);
        return sb.toString();
    }

    private void registrarTempo(long inicioNs, String comando, boolean medir, boolean timeout){
        if (!medir) return;

        long duracao = System.nanoTime() - inicioNs;

        totalComandos++;
        somaTempoNs+= duracao;

        if(!logTempo) return;

        double ms = duracao / 1_000_000.0;

        if(timeout){
            System.out.printf("ELM TIMEOUT %.1f ms | %s%n", ms, comando.trim());
        }else {
            System.out.printf("ELM %.1f ms | %s%n", ms, comando.trim());
        }
        if(totalComandos % 50 == 0){
            double media = (somaTempoNs / (double) totalComandos) / 1_000_000.0;
            System.out.printf("Média ELM: %.1f ms em %d comandos%n", media, totalComandos);
        }

    }

    private void limparEntrada(){
        if(porta == null || !porta.isOpen()) return;

        byte[] lixo = new byte[256];

        while(porta.bytesAvailable() > 0){
            int n = porta.readBytes(lixo, Math.min(lixo.length, porta.bytesAvailable()));
            if (n <= 0) break;
        }
    }
    @Override
    public boolean detectarEConectar() {
        SerialPort[] portas = SerialPort.getCommPorts();

        for (SerialPort p : portas) {
            for (int baud : BAUDS_TESTE) {
                porta = p;

                if (porta.isOpen()) {
                    porta.closePort();
                }

                porta.setBaudRate(baud);
                porta.setNumDataBits(8);
                porta.setNumStopBits(SerialPort.ONE_STOP_BIT);
                porta.setParity(SerialPort.NO_PARITY);
                porta.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

                if (!porta.openPort()) {
                    continue;
                }

                try {
                    Thread.sleep(150);

                    String resposta = enviar("ATZ", 1500, false).toUpperCase();

                    if (!resposta.contains("ELM") && !resposta.contains("OBD")) {
                        resposta = enviar("ATI", 800, false).toUpperCase();
                    }

                    if (resposta.contains("ELM") || resposta.contains("OBD")) {
                        System.out.println("ELM encontrado: "
                                + p.getDescriptivePortName()
                                + " @ "
                                + baud
                                + " baud");

                        inicializarElmRapido();

                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("Erro testando "
                            + p.getSystemPortName()
                            + " @ "
                            + baud
                            + ": "
                            + e.getMessage());
                }
                porta.closePort();
            }
        }
        return false;
    }

    private void inicializarElmRapido() throws Exception {
        enviar("ATE0", 500, false);   // Echo off
        enviar("ATL0", 500, false);   // Line feed off
        enviar("ATS0", 500, false);   // Spaces off
        enviar("ATH0", 500, false);   // Headers off
        enviar("ATAT2", 500, false);  // Adaptive timing agressivo

        boolean protocoloOk = testarProtocolosKLine();

        if (!protocoloOk) {
            System.out.println("Protocolos K-Line fixos falharam. Tentando automático...");
            enviar("ATSP0", 1000, false);
            enviar("0100", 2500, false);
        }

        // Timeout interno do ELM: 0x14 * 4 ms = ~80 ms.
        // Se cortar resposta no teste real, troque para ATST20.
        enviar("ATST14", 500, false);

        String protocolo = enviar("ATDP", 800, false);
        System.out.println("Protocolo ELM: " + limparTexto(protocolo));
    }

    private boolean testarProtocolosKLine() {
        String[] protocolos = {
                "ATSP5", // ISO 14230-4 KWP fast init
                "ATSP4", // ISO 14230-4 KWP 5 baud init
                "ATSP3"  // ISO 9141-2
        };

        for (String protocolo : protocolos) {
            try {
                enviar(protocolo, 1000, false);

                String resposta = enviar("0100", 3000, false);
                String limpa = limparTexto(resposta).replace(" ", "");

                if (limpa.contains("4100")) {
                    System.out.println("Protocolo funcionando: " + protocolo);
                    return true;
                }

            } catch (Exception e) {
                System.out.println("Falhou " + protocolo + ": " + e.getMessage());
            }
        }

        return false;
    }

    private String limparTexto(String texto) {
        if (texto == null) return "";

        return texto
                .replace(">", "")
                .replace("\r", "")
                .replace("\n", "")
                .trim()
                .toUpperCase();
    }


    @Override
    public String getPortName() {
        return porta != null ? porta.getDescriptivePortName() : "";
    }

    public void setTimeoutObdMs(int timeoutObdMs) {
        this.timeoutObdMs = timeoutObdMs;
    }

    public void setLogTempo(boolean logTempo) {
        this.logTempo = logTempo;
    }

    public double getMediaTempoMs() {
        if (totalComandos == 0) return 0.0;
        return (somaTempoNs / (double) totalComandos) / 1_000_000.0;
    }
}