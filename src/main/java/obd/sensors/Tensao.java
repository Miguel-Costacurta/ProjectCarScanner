package obd.sensors;

import obd.connection.IObdConnection;
import obd.connection.ObdConnection;

import java.io.IOException;

public class Tensao extends Sensor{
    private double tensao;
    public Tensao(IObdConnection obdConnection) {
        super("ATRV\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws Exception {
        resposta = respostaObd();

        if(resposta.equals("UNSUPPORTED")){
            return 0.0;
        }

        tensao = Double.parseDouble(resposta.replaceAll("[^0-9.]", ""));

        return tensao;
    }
}
