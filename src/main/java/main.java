import javafx.application.Application;
import obd.connection.HomoObdConnection;
import obd.connection.IObdConnection;
import obd.connection.ObdConnection;
import obd.sensors.pidscanner.PidScanner;
import obd.ui.MainWindow;

import java.io.IOException;

public class main {

    public static void main(String[] args) throws Exception {
        IObdConnection iObdConnection = new ObdConnection();
        PidScanner scanner = new PidScanner(iObdConnection);
        scanner.scanear();
        //Application.launch(MainWindow.class, args);

    }

}
