package obd.ui;

import obd.connection.IObdConnection;
import obd.core.sensors.*;

public class LeituraObd {
    private IObdConnection obdConnection;
    private TPS mostrarTps;
    private RPM mostrarRpm;
    private FuelTrim mostrarFuelTrim;
    private Tensao mostrarTensao;
    private SparkAdvance mostrarAvanco;
    private Velocity mostrarVelocidade;

    private volatile boolean rodando = true;

    private IDataUpdate listener;

    LeituraObd(IObdConnection obdConnection, TPS tps, RPM rpm, FuelTrim fuelTrim, Tensao tensao,
                SparkAdvance avanco,Velocity velocity ,IDataUpdate listener) {
        this.listener = listener;
        this.obdConnection = obdConnection;
        this.mostrarRpm = rpm;
        this.mostrarTps = tps;
        this.mostrarFuelTrim = fuelTrim;
        this.mostrarTensao = tensao;
        this.mostrarVelocidade = velocity;
        this.mostrarAvanco = avanco;
    }

    public void getResponse() {
        double rpm, tps, tensao, fuelTrim, spark, velocity;
        while (rodando) {
            try {
                rpm = mostrarRpm.traduzirResposta();
                System.out.println("RAW: " + mostrarRpm.respostaObd());
                tps = mostrarTps.traduzirResposta();
                tensao = mostrarTensao.traduzirResposta();
                fuelTrim = mostrarFuelTrim.traduzirResposta();
                spark = mostrarAvanco.traduzirResposta();
                velocity = mostrarVelocidade.traduzirResposta();
                System.out.println("rpm lido: " + rpm);
                System.out.printf("tps lido: %.2f\n", tps);
                System.out.println("tensao lido: " + tensao);
                System.out.println("lambda lido: " + fuelTrim);
                listener.onDataUpdate(rpm,tps,tensao,fuelTrim,spark,velocity);

                Thread.sleep(50);

            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        rodando = false;
    }
}
