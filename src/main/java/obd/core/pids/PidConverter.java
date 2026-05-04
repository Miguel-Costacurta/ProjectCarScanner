package obd.core.pids;

import java.util.function.Function;

/**
 * Fórmulas de conversão para cada PID OBD2.
 * Recebe o array de partes da resposta (ex: ["41","0C","1A","F8"])
 * e retorna o valor final como double.
 */
public class PidConverter {

    // RPM: ((A*256)+B)/4
    public static final Function<String[], Double> RPM =
            p -> ((parse(p[2]) * 256) + parse(p[3])) / 4.0;

    // Velocidade: A
    public static final Function<String[], Double> VELOCIDADE =
            p -> (double) parse(p[2]);

    // Temperatura (motor, ar, ambiente, óleo): A - 40
    public static final Function<String[], Double> TEMPERATURA =
            p -> (double) parse(p[2]) - 40;

    // Percentual simples (carga, TPS, etanol, nível comb.): A*100/255
    public static final Function<String[], Double> PERCENTUAL =
            p -> parse(p[2]) * 100.0 / 255.0;

    // Fuel trim STFT/LTFT: (A/128*100)-100
    public static final Function<String[], Double> FUEL_TRIM =
            p -> (parse(p[2]) / 128.0 * 100) - 100;

    // Avanço de ignição: (A/2)-64
    public static final Function<String[], Double> AVANCO =
            p -> (parse(p[2]) / 2.0) - 64;

    // Pressão MAP: A (kPa)
    public static final Function<String[], Double> PRESSAO =
            p -> (double) parse(p[2]);

    // MAF: ((A*256)+B)/100
    public static final Function<String[], Double> MAF =
            p -> ((parse(p[2]) * 256) + parse(p[3])) / 100.0;

    // helper
    private static int parse(String hex) {
        return Integer.parseInt(hex.trim(), 16);
    }
}