import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.Scanner;

public class main {
    public static void main(String[] args) throws IOException, InterruptedException {
        SerialPort ports[] = SerialPort.getCommPorts();

        for (SerialPort port :ports){
            System.out.println(port.getSystemPortName() + " -> " + port.getDescriptivePortName());
        }

        SerialPort porta = SerialPort.getCommPort("COM6");
        porta.setBaudRate(38400);

        if (porta.openPort()) {
            System.out.println("Porta aberta com sucesso!");

            String comando = "010C\r";
            int contador = 0;
            int rpm = 0;

            Scanner scanner = new Scanner(System.in);

            while(contador <= 9){

                porta.getOutputStream().write(comando.getBytes());
                porta.getOutputStream().flush();
                byte[] buffer = new byte[256];
                int bytes = porta.getInputStream().read(buffer);
                String resposta = new String(buffer, 0, bytes);
                System.out.println("RPM: " + resposta);

                String[] partes = resposta.split(" ");


                rpm = ((Integer.parseInt(partes[2].trim(),16) * 256) + Integer.parseInt(partes[3].trim(),16))/4;

                System.out.println("RPM calculado: " + rpm);

                contador++;
            }



            Thread.sleep(1500);


            byte[] buffer = new byte[256];
            int bytes = porta.getInputStream().read(buffer);
            String resposta = new String(buffer, 0, bytes);
            System.out.println("Resposta: " + resposta);

            porta.closePort();
        } else {
            System.out.println("Falha ao abrir a porta. Tente COM4.");
        }
    }
}
