package obd.sensors.pidscanner;

import obd.connection.IObdConnection;

import java.util.ArrayList;
import java.util.List;

public class PidScanner {
    private IObdConnection iObdConnection;

    public PidScanner(IObdConnection iObdConnection){
        this.iObdConnection = iObdConnection;
    }

    public List<String> scanear()throws Exception{
        List<String> pids = new ArrayList<>();

        String[] ranges = {"0100\r", "0120\r", "0140\r"};
        int[] baseOffset = {1,33,65};

        for (int r= 0; r < ranges.length; r++){
            String resposta = iObdConnection.enviarComando(ranges[r]);
            resposta = resposta.replace(">","").trim();

            if(resposta.startsWith("7F")) break;

            String[] partes = resposta.split(" ");
            if (partes.length <6) break;

            long bits = 0;
            for(int i = 2; i<= 5; i++){
                bits = (bits << 8) | Integer.parseInt(partes[i].trim(),16);
            }

            for (int i = 0; i < 32; i++){
                if((bits &(1L << (31-i))) !=0){
                    String pid = String.format("%02X", baseOffset[r] + i);
                    pids.add(pid);
                    System.out.println("PID suportado: " + pid);
                }
            }
        }
        return pids;
    }

}
