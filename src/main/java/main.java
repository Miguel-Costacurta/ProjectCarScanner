import javafx.application.Application;
import obd.connection.IObdConnection;
import obd.connection.ObdConnection;
import obd.core.pidscanner.PidScanner;
import obd.ui.MainWindow;

public class main {

    public static void main(String[] args) throws Exception {
        Application.launch(MainWindow.class, args);

    }

}
