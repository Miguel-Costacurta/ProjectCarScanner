package obd.sensors;

import obd.connection.ObdConnection;

import java.io.IOException;

public abstract class Sensor{
    protected String resposta;
    private String codigo;
    private ObdConnection obdConnection;

    public Sensor(String codigo, ObdConnection obdConnection){
        this.codigo = codigo;
        this.obdConnection = obdConnection;
    }

    public abstract double traduzirResposta() throws IOException, InterruptedException;

    public String respostaObd() throws IOException, InterruptedException {
        boolean valid = false;
        while(!valid){
            resposta = obdConnection.enviarComando(codigo);
            if(!resposta.contains("SEARCHING") && !resposta.contains("ERROR") && !resposta.contains("NO DATA")){
                valid = true;
            } else {
                Thread.sleep(550);
            }
        }

        System.out.println("Resposta OBDII: " + resposta);

        return resposta;
    }


}
