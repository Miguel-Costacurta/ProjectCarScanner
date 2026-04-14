import javafx.application.Application;
import obd.connection.ObdConnection;
import obd.sensors.FuelTrim;
import obd.sensors.RPM;
import obd.sensors.TPS;
import obd.sensors.Tensao;
import obd.ui.MainWindow;

import java.io.IOException;

public class main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Application.launch(MainWindow.class, args);

        /*
        ObdConnection obdConnection = new ObdConnection();
        RPM mostrarRPM = new RPM(obdConnection);
        TPS mostrarTPS = new TPS(obdConnection);
        Tensao mostrarTensao = new Tensao(obdConnection);
        FuelTrim fuelTrimBCatalyst = new FuelTrim(obdConnection,1);
        FuelTrim fuelTrimACatalyst = new FuelTrim(obdConnection,2);



        int contador = 0;
        obdConnection.detectarEConectar();
        obdConnection.openConnection();

        double lambdaBCatalyst = 1 + (fuelTrimBCatalyst.traduzirResposta() / 100);
        double lambdaACatalyst = 1 + (fuelTrimACatalyst.traduzirResposta() / 100);

        while(contador < 9){
            System.out.printf("RPM: %.0f\n",mostrarRPM.traduzirResposta());
            System.out.printf("TPS: %.2f\n",mostrarTPS.traduzirResposta());
            System.out.printf("Tension: %.1f V\n", mostrarTensao.traduzirResposta());

            System.out.printf("λ Before: %.2f\n", lambdaBCatalyst);
            System.out.printf("λ After: %.2f\n", lambdaACatalyst);

            lambdaBCatalyst = 1 + (fuelTrimBCatalyst.traduzirResposta() / 100);
            lambdaACatalyst = 1 + (fuelTrimACatalyst.traduzirResposta() / 100);

            contador++;
        }

        obdConnection.closeConnection();
    */
    }

}
