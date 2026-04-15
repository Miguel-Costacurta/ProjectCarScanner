package obd.connection;

public interface IObdConnection {
    boolean openConnection();
    boolean closeConnection();
    String enviarComando(String comando) throws Exception;
    boolean detectarEConectar();
    String getPortName();
}
