package obd.sensors;

import obd.connection.ObdConnection;

import java.io.IOException;

public class RPM extends Sensor{
    private double rpm;

    public RPM(ObdConnection obdConnection){
        super("010C\r", obdConnection);
    }

    @Override
    public double traduzirResposta() throws InterruptedException,IOException {
        resposta = respostaObd();
        String[] partes = resposta.split(" ");

        rpm = ((Integer.parseInt(partes[2].trim(),16) * 256) + Integer.parseInt(partes[3].trim(),16))/4;

        return rpm;
    }

}
