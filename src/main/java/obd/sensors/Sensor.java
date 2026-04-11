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
        resposta = obdConnection.enviarComando(codigo);
        System.out.println("Resposta OBDII: " + resposta);

        return resposta;
    }


}
