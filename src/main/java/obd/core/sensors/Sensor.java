package obd.core.sensors;

import obd.connection.IObdConnection;

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
            resposta = obdConnection.enviarComando(codigo);
            if (resposta.startsWith("7F")){
                System.out.println("PID nao suportado: " + codigo);
                return "UNSUPPORTED";
            }
            if(!resposta.contains("SEARCHING") &&
                    !resposta.contains("ERROR") &&
                    !resposta.contains("NO DATA") &&
                    !resposta.isBlank()){
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
