import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

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

            // Envia ATZ (reset do ELM327)
            String comando = "ATZ\r";
            porta.getOutputStream().write(comando.getBytes());
            porta.getOutputStream().flush();

            // Espera a resposta
            Thread.sleep(1500);

            // Lê o que veio
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
