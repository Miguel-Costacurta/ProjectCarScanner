import javafx.application.Application;
import javafx.stage.Stage;
import obd.ui.ConnectWindow;

public class main extends Application{
    @Override
    public void start(Stage stage){
        new ConnectWindow(stage).show();
    }
}
