package obd.connection;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class ObdConnection {
    private SerialPort porta = SerialPort.getCommPort("COM6");

     public boolean openConnection(){
         return porta.openPort();
     }

     public boolean closeConnection(){
         return porta.closePort();
     }

     public String enviarComando(String comando) throws IOException, InterruptedException {
         porta.getOutputStream().write(comando.getBytes());
         porta.getOutputStream().flush();
         Thread.sleep(300);
         byte[] buffer = new byte[256];
         int bytes = porta.getInputStream().read(buffer);
         String resposta = new String(buffer, 0, bytes);

         return resposta;
     }
}
