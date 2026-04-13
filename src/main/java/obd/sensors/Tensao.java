package obd.sensors;

import obd.connection.ObdConnection;

import java.io.IOException;

public class Tensao extends Sensor{
    private double tensao;
    public Tensao(ObdConnection obdConnection) {
        super("0142\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws IOException, InterruptedException {
        String[] parts = respostaObd().split(" ");

        tensao = (double) (Integer.parseInt(parts[2].trim(), 16) * 256 + Integer.parseInt(parts[3].trim(), 16)) / 1000.0;

        return tensao;
    }
}
