import javafx.application.Application;
import javafx.stage.Stage;
import obd.connection.IObdConnection;
import obd.connection.ObdConnection;
import obd.core.pidscanner.PidScanner;
import obd.ui.ConnectWindow;
import obd.ui.MainWindow;

public class main extends Application{
    @Override
    public void start(Stage stage){
        new ConnectWindow(stage).show();
    }
}
