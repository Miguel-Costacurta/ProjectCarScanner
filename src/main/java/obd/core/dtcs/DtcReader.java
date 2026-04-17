package obd.core.dtcs;

import obd.connection.IObdConnection;
import java.util.ArrayList;
import java.util.List;

public class DtcReader {
    private final IObdConnection obdConnection;

    public DtcReader(IObdConnection obdConnection) {
        this.obdConnection = obdConnection;
    }

    public List<String> lerDtcs() throws Exception {
        List<String> codigos = new ArrayList<>();

        String resposta = obdConnection.enviarComando("03\r");
        resposta = resposta.replace(">", "").trim();

        if (resposta.equals("UNSUPPORTED") || resposta.contains("NO DATA") || resposta.isBlank()) {
            return codigos; // lista vazia = sem erros
        }

        String[] bytes = resposta.split(" ");

        for (int i = 1; i + 1 < bytes.length; i += 2) {
            int byteA = Integer.parseInt(bytes[i].trim(), 16);
            int byteB = Integer.parseInt(bytes[i + 1].trim(), 16);

            if (byteA == 0 && byteB == 0) continue; // padding, ignora

            String codigo = parsearDtc(byteA, byteB);
            codigos.add(codigo);
        }

        return codigos;
    }

    private String parsearDtc(int a, int b) {

        char[] prefixos = {'P', 'C', 'B', 'U'};
        char prefixo = prefixos[(a >> 6) & 0x03];


        int digito1 = (a >> 4) & 0x03;


        int digito2 = a & 0x0F;


        int digito3 = (b >> 4) & 0x0F;
        int digito4 = b & 0x0F;

        return String.format("%c%d%X%X%X", prefixo, digito1, digito2, digito3, digito4);
    }
}
