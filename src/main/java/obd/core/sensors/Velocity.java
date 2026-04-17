package obd.core.sensors;

import obd.connection.IObdConnection;

public class Velocity extends Sensor{
    private int velocity;

    public Velocity(IObdConnection iObdConnection){
        super("010D\r",iObdConnection);
    }
    @Override
    public double traduzirResposta() throws Exception {
        String[] parts = respostaObd().split(" ");

        velocity = Integer.parseInt(parts[2].trim(),16);

        return velocity;
    }
}
