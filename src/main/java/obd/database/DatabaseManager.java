package obd.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:projectcarscanner.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException{
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static void inicializar(){
        try(Statement stmt = getConnection().createStatement()){
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS veiculos (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    apelido    TEXT NOT NULL,
                    marca      TEXT,
                    modelo     TEXT,
                    ano        INTEGER,
                    motor      TEXT,
                    vin        TEXT,
                    data_criacao  TEXT DEFAULT (datetime('now'))
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sessoes (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    veiculo_id  INTEGER NOT NULL,
                    descricao   TEXT,
                    inicio      TEXT NOT NULL,
                    fim         TEXT,
                    FOREIGN KEY (veiculo_id) REFERENCES veiculos(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS leituras (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    sessao_id  INTEGER NOT NULL,
                    timestamp  TEXT NOT NULL,
                    pid        TEXT NOT NULL,
                    valor      REAL NOT NULL,
                    FOREIGN KEY (sessao_id) REFERENCES sessoes(id)
                )
            """);

            System.out.println("Banco de dados inicializado.");
        } catch (SQLException e){
            System.out.println("Erro ao inicializar banco de dados: " + e.getMessage());
        }
    }

    public static void fechar(){
        try{
            if (connection != null || !connection.isClosed()){
                connection.close();
            }
        }catch (SQLException e){
            System.out.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
