package obd.core.sensors;

import obd.connection.IObdConnection;

public class SparkAdvance extends Sensor{
    private double sparkAdvance;

    public SparkAdvance(IObdConnection iObdConnection){
        super("010E\r",iObdConnection);
    }
    @Override
    public double traduzirResposta() throws Exception {
        String parts[] = respostaObd().split(" ");

        sparkAdvance = (Integer.parseInt(parts[2].trim(),16)/2.0) - 64 ;

        return sparkAdvance;
    }
}
