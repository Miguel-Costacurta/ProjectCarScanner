package obd.core.sensors;

import obd.connection.IObdConnection;

public class RPM extends Sensor {
    private double rpm;

    public RPM(IObdConnection obdConnection){
        super("010C\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws Exception {
        resposta = respostaObd();

        if(resposta.equals("UNSUPPORTED")) return 0.0;

        String[] partes = resposta.split(" ");

        rpm = ((Integer.parseInt(partes[2].trim(),16) * 256) + Integer.parseInt(partes[3].trim(),16))/4;

        return rpm;
    }

}
