import javafx.application.Application;
import javafx.stage.Stage;
import obd.ui.windows.ConnectWindow;

public class main extends Application{
    @Override
    public void start(Stage stage){
        new ConnectWindow(stage).show();
    }
}
