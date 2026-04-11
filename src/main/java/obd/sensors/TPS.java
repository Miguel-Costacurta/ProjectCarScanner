package obd.sensors;

import obd.connection.ObdConnection;

import java.io.IOException;

public class TPS extends Sensor{
    private double tps;

    public TPS(ObdConnection obdConnection) {
        super("0111\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws IOException, InterruptedException {
       String[] part = respostaObd().split(" ");

       tps = ((double) (Integer.parseInt(part[2].trim(), 16) * 100) / 255);

       return tps;
    }
}
