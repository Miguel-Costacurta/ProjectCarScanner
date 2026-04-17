package obd.core.sensors;

import obd.connection.IObdConnection;

public class Velocity extends Sensor{
    private int velocity;

    public Velocity(IObdConnection iObdConnection){
        super("010D\r",iObdConnection);
    }
    @Override
    public double traduzirResposta() throws Exception {
        String resposta = respostaObd();

        velocity = Integer.parseInt(resposta.trim(),16);

        return velocity;
    }
}
