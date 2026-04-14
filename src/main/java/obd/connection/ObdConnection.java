package obd.connection;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class ObdConnection {
    private SerialPort porta;

     public boolean openConnection(){
         porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000,0);
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

     public boolean detectarEConectar(){
         SerialPort[] portas = SerialPort.getCommPorts();

         for(SerialPort p : portas){
             porta = p;
             porta.setBaudRate(38400);
             porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

             if(porta.openPort()){
                 try {
                     String resposta = enviarComando("ATZ \r");
                     if (resposta.contains("ELM327")){
                         System.out.println("ELM327 encontrado em: " + p.getDescriptivePortName());
                         return true;
                     }
                 } catch (Exception e){

                 }
                 porta.closePort();
             }
         }
         return false;
     }
     public String getPortName(){
         return porta.getDescriptivePortName();
     }
}
