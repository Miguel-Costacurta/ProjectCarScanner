package obd.connection;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.sql.Time;

public class ObdConnection implements IObdConnection{
    private SerialPort porta;

     public boolean openConnection(){
         porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 2000, 2000);
         return porta.openPort();
     }

     public boolean closeConnection(){
         return porta.closePort();
     }

    public String enviarComando(String comando) throws IOException, InterruptedException {
        long inicio = System.currentTimeMillis();
         porta.getOutputStream().write(comando.getBytes());
        porta.getOutputStream().flush();

        StringBuilder sb = new StringBuilder();
        int b;
        while((b = porta.getInputStream().read()) != -1){
            char c = (char) b;
            sb.append(c);
            if(c == '>') break; // ELM sempre termina com >
        }
        long fim = System.currentTimeMillis();
        System.out.println("Media de tempo: "+ (fim - inicio));
        return sb.toString();
    }

     public boolean detectarEConectar(){
         SerialPort[] portas = SerialPort.getCommPorts();

         for(SerialPort p : portas){
             porta = p;
             porta.setBaudRate(38400);
             porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 2000, 2000);

             if(porta.openPort()){
                 try {
                     porta.getOutputStream().write("ATZ\r".getBytes());
                     Thread.sleep(900); // ← ATZ precisa de mais tempo
                     byte[] buffer = new byte[256];
                     int bytes = porta.getInputStream().read(buffer);
                     String resposta = new String(buffer, 0, bytes);
                     System.out.println("Resposta ATZ: " + resposta); // ver o que chega
                     if(resposta.contains("ELM327")){
                         System.out.println("ELM327 encontrado em: " + p.getDescriptivePortName());
                         enviarComando("ATE0\r");
                         enviarComando("ATL0\r");
                         enviarComando("ATST0\r");
                         return true;
                     }
                 } catch (Exception e){
                     System.out.println("Erro na porta " + p.getSystemPortName() + ": " + e.getMessage());
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
