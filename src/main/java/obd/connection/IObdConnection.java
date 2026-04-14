package obd.connection;

public interface IObdConnectionTeste {
    boolean openConnection();
    boolean closeConnection();
    String enviarComando(String comando) throws Exception;
    boolean detectarEConectar();
    String getPortName();
}
