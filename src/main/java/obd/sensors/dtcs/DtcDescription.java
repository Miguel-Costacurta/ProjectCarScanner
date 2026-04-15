package obd.sensors.dtcs;

import java.util.HashMap;
import java.util.Map;

public class DtcDescription {
    private static final Map<String, String> mapa;

    static{
        mapa = new HashMap<>();
        mapa.put("P0101", "Sensor MAF fora do range");
        mapa.put("P0113", "Sensor IAT — sinal alto");
        mapa.put("P0131","Sensor O2 banco 1 — sinal baixo");
        mapa.put("P0171", "Mistura pobre — banco 1");
        mapa.put("P0172", "Mistura rica — banco 1");
        mapa.put("P0261","Injetor cilindro 1 — sinal baixo");
        mapa.put("P0300", "Falha de ignição aleatória");
        mapa.put("P0301", "Falha de ignição — cilindro 1");
        mapa.put("P0302","Falha de ignição — cilindro 2");
        mapa.put("P0303", "Falha de ignição — cilindro 3");
        mapa.put("P0304", "Falha de ignição — cilindro 4");
        mapa.put("P0335","Sensor CKP — sem sinal");
        mapa.put("P0401", "EGR — fluxo insuficiente");
        mapa.put("P0420", "Catalisador banco 1 — eficiência baixa");
        mapa.put("P0505","Sistema de marcha lenta — falha");


    }
    public static String getDescricao(String codigo) {
        return mapa.getOrDefault(codigo, "DTC nao encontrado");
    }

}
