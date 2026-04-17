package obd.core.sensors;

import obd.connection.IObdConnection;

public class SparkAdvance extends Sensor{
    private int sparkAdvance;

    public SparkAdvance(IObdConnection iObdConnection){
        super("010E\r",iObdConnection);
    }
    @Override
    public double traduzirResposta() throws Exception {
        String resposta = respostaObd();

        sparkAdvance = (int) (Integer.parseInt(resposta.trim(),16)/2.0) - 64 ;

        return sparkAdvance;
    }
}
