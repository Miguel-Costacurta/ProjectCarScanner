package obd.sensors;

import obd.connection.IObdConnection;
import obd.connection.ObdConnection;

import java.io.IOException;

public abstract class Sensor{
    protected String resposta;
    private String codigo;
    private IObdConnection obdConnection;

    public Sensor(String codigo, IObdConnection obdConnection){
        this.codigo = codigo;
        this.obdConnection = obdConnection;
    }

    public abstract double traduzirResposta() throws Exception;

    public String respostaObd() throws Exception {
        boolean valid = false;

        while(!valid){
            /*if(resposta == null || resposta.trim().isEmpty() || resposta.trim().equals(">")){
                Thread.sleep(100);
                continue;
            }*/
            resposta = obdConnection.enviarComando(codigo);
            if (resposta.startsWith("7F")){
                System.out.println("PID nao suportado: " + codigo);
                return "UNSUPPORTED";
            }
            if(!resposta.contains("SEARCHING") &&
                    !resposta.contains("ERROR") &&
                    !resposta.contains("NO DATA")){
                valid = true;
            } else {
                Thread.sleep(100);
            }
        }

        System.out.println("Resposta OBDII: " + resposta);
        resposta = resposta.replace(">", "").trim();
        return resposta;
    }


}
