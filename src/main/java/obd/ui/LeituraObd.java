package obd.ui;

import javafx.application.Platform;
import obd.connection.IObdConnection;
import obd.connection.ObdConnection;
import obd.sensors.*;

import java.io.IOException;

public class LeituraObd {
    private IObdConnection obdConnection;
    private TPS mostrarTps;
    private RPM mostrarRpm;
    private FuelTrim mostrarFuelTrim;
    private Tensao mostrarTensao;

    private IDataUpdate listener;

    LeituraObd(IObdConnection obdConnection, TPS tps, RPM rpm, FuelTrim fuelTrim, Tensao tensao
                , IDataUpdate listener) {
        this.listener = listener;
        this.obdConnection = obdConnection;
        this.mostrarRpm = rpm;
        this.mostrarTps = tps;
        this.mostrarFuelTrim = fuelTrim;
        this.mostrarTensao = tensao;
    }

    public void getResponse() {
        double rpm, tps, tensao, fuelTrim;
        while (true) {
            System.out.println("=== inicio da iteracao ===");
            try {

                System.out.println("=== mostrando os dados ===");
                rpm = mostrarRpm.traduzirResposta();
                tps = mostrarTps.traduzirResposta();
                tensao = mostrarTensao.traduzirResposta();
                fuelTrim = mostrarFuelTrim.traduzirResposta();
                System.out.println("rpm lido: " + rpm);
                System.out.println("tps lido: " + tps);
                System.out.println("tensao lido: " + tensao);
                System.out.println("lambda lido: " + fuelTrim);
                listener.onDataUpdate(rpm,tps,tensao,fuelTrim);

                Thread.sleep(500);


            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
