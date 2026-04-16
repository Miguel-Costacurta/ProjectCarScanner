package obd.connection;

import com.fazecast.jSerialComm.SerialPort;
import obd.sensors.dtcs.DtcDescription;
import obd.sensors.dtcs.DtcReader;

import java.io.IOException;
import java.util.List;

public class HomoObdConnection implements IObdConnection {

    private double rpm = 1726.0;
    private double tps = 13.0;
    private double tensao = 12.8;
    private double lambda = 128.0;

    private double dtc = 128.0;

    private final java.util.Random random = new java.util.Random();

    private double oscilar(double valor, double amplitude, double min, double max) {
        double novo = valor + (random.nextDouble() * amplitude * 2 - amplitude);
        return Math.max(min, Math.min(max, novo));
    }

    @Override
    public String enviarComando(String comando) throws IOException, InterruptedException {
        String resposta = "";
        switch(comando) {
            case ("010C\r"): {
                rpm = oscilar(rpm, 80, 700, 6800);
                int raw = (int) (rpm * 4);
                int a = (raw >> 8) & 0xFF;
                int b = raw & 0xFF;
                return String.format("41 0C %02X %02X >", a, b);
            }
            case "0111\r": {
                tps = oscilar(tps, 2, 0, 100);
                int raw = (int) (tps * 255 / 100);
                return String.format("41 11 %02X >", raw);
            }
            case "ATRV\r": {
                tensao = oscilar(tensao, 0.1, 11.0, 14.8);
                tensao = tensao/10;
                return String.format("%.1fV >", tensao);
            }
            case "0106\r": {
                lambda = oscilar(lambda, 3, 110, 150);
                int raw = (int) lambda;
                return String.format("41 06 %02X >", raw);
            }
            case "03\r": {
                dtc = oscilar(dtc, 2, 0, 10);
                int raw = (int) (dtc * 2);
                int a = (raw >> 8) & 0xFF;
                int b = raw & 0xFF;
                return String.format("43 %02X %02X >", a, b);
            }
            case "0100\r":
                return "41 00 BE 3E B8 11 >";
            default:
                return "7F 01 12 >";
        }
    }

    @Override
    public boolean openConnection() { return true; }

    @Override
    public boolean detectarEConectar() { return true; }

    @Override
    public String getPortName() { return "HOMOLOGAÇÃO"; }

    @Override
    public boolean closeConnection() { return true; }
}
