package obd.connection;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class HomoObdConnection implements IObdConnection {
    @Override

    public String enviarComando(String comando) throws IOException, InterruptedException {
        String resposta = "";
        switch(comando){
            case ("010C\r"):
                resposta = "41 0C 1A F8 >";
                break;
            case ("0111\r"):
                resposta = "41 11 23 >";
                break;
            case ("0106\r"):
                resposta = "41 06 86 >";
                break;
            default:
                resposta = "7F 01 12 >";
                break;
        }
        return resposta;
    }

    @Override
    public boolean openConnection() { return true; }

    @Override
    public boolean detectarEConectar() { return true; }

    @Override
    public String getPortName() { return "HOMOLOGAÇÃO"; }

    @Override
    public boolean closeConnection() { return true; }
}
