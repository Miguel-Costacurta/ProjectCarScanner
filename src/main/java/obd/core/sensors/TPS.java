package obd.core.sensors;

import obd.connection.IObdConnection;

public class TPS extends Sensor {
    private double tps;

    public TPS(IObdConnection obdConnection) {
        super("0111\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws Exception {
        resposta = respostaObd();
        if(resposta.equals("UNSUPPORTED")) return 0.0;

        String[] part = resposta.split(" ");

       tps = ((double) (Integer.parseInt(part[2].trim(), 16) * 100) / 255);

       return tps;
    }
}
