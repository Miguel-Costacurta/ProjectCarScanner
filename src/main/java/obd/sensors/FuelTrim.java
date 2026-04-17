package obd.sensors;

import obd.connection.IObdConnection;
import obd.connection.ObdConnection;

import java.io.IOException;

public class FuelTrim extends Sensor{
    private double lambda;
    public FuelTrim(IObdConnection obdConnection, int band){
        super(band == 1 ? "0106\r" : "0109\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws Exception {
        resposta = respostaObd();
        System.out.println("RAW: " + resposta);
        if(resposta.equals("UNSUPPORTED")) return 0.0;

        String[] parts = resposta.split(" ");

        lambda = Integer.parseInt(parts[2].trim(), 16) / 128.0;

        return lambda;
    }
}
