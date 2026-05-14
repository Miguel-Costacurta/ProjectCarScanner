package obd.core.sensors;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import java.util.function.Function;

public class ActiveSensor {
    public final String pid;
    public final String nome;
    public final String unidade;
    public final double valorMax;

    // conversor: recebe os bytes da resposta (ex: ["41","0C","1A","F8"])
    // e retorna o valor final como double
    public final Function<String[], Double> conversor;

    // referências ao card na SensorsTab
    public Label valorLabel;
    public Pane  barra;
    public double larguraCard;

    public ActiveSensor(String pid, String nome, String unidade,
                        double valorMax, Function<String[], Double> conversor) {
        this.pid       = pid;
        this.nome      = nome;
        this.unidade   = unidade;
        this.valorMax  = valorMax;
        this.conversor = conversor;
    }
}