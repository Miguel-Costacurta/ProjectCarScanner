package obd.ui;

import javafx.application.Platform;
import obd.connection.ObdConnection;
import obd.sensors.*;

import java.io.IOException;

public class LeituraObd {
    private ObdConnection obdConnection;
    private TPS tps;
    private RPM mostrarRpm;
    private FuelTrim fuelTrim;
    private Tensao tensao;

    private IDataUpdate listener;

    LeituraObd(ObdConnection obdConnection, TPS tps, RPM rpm, FuelTrim fuelTrim, Tensao tensao
                ,IDataUpdate listener) {
        this.listener = listener;
        this.obdConnection = obdConnection;
        this.mostrarRpm = rpm;
        this.tps = tps;
        this.fuelTrim = fuelTrim;
        this.tensao = tensao;
    }

    public void getResponse() {
        double rpm;
        while (true) {
            System.out.println("=== inicio da iteracao ===");
            try {

                System.out.println("=== mostrando os dados ===");
                rpm = mostrarRpm.traduzirResposta();
                System.out.println("rpm lido: " + rpm);
                listener.onDataUpdate(rpm);
                //double tensao = mostrarTensao.traduzirResposta();
                //double tps = mostrarTPS.traduzirResposta();
                //double lambda = mostrarLambda.traduzirResposta();

                Thread.sleep(500);


            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
