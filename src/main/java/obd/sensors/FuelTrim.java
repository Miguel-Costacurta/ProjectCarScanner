package obd.sensors;

import obd.connection.ObdConnection;

import java.io.IOException;

public class FuelTrim extends Sensor{
    private double lambda;
    public FuelTrim(ObdConnection obdConnection, int band){
        super(band == 1 ? "0106\r" : "0109\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws IOException, InterruptedException {
        String[] parts = respostaObd().split(" ");

        lambda = ((Integer.parseInt(parts[2].trim(),16) / 128.0 * 100) - 100);

        return lambda;
    }
}
