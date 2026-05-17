import javafx.application.Application;
import javafx.stage.Stage;
import obd.database.DatabaseManager;
import obd.ui.windows.ConnectWindow;

public class main extends Application {
    @Override
    public void start(Stage stage) {
        DatabaseManager.inicializar();
        new ConnectWindow(stage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}