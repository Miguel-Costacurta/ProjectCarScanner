package obd.sensors;

import obd.connection.IObdConnection;
import obd.connection.ObdConnection;

import java.io.IOException;

public class TPS extends Sensor{
    private double tps;

    public TPS(IObdConnection obdConnection) {
        super("0111\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws Exception {
        resposta = respostaObd();
        if(resposta.equals("UNSUPPORTED")) return 0.0;
        System.out.println("RAW: " + resposta);
        String[] part = resposta.split(" ");

       tps = ((double) (Integer.parseInt(part[2].trim(), 16) * 100) / 255);

       return tps;
    }
}
