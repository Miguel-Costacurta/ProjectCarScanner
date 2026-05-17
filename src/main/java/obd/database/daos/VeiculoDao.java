package obd.database.daos;

import obd.database.DatabaseManager;
import obd.database.models.Veiculo;

import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDao {

    public Veiculo salvarVeiculo (Veiculo veiculo) throws SQLException {
        String sql = """
                INSERT INTO veiculos (apelido, marca, modelo, ano, motor, vin)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
        try(PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1, veiculo.getApelido());
            pstmt.setString(2, veiculo.getMarca());
            pstmt.setString(3, veiculo.getModelo());
            pstmt.setInt(4, veiculo.getAno());
            pstmt.setString(5, veiculo.getMotor());
            pstmt.setString(6, veiculo.getVin());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if(keys.next()) veiculo.setId(keys.getInt(1));
        }
        return veiculo;
    }

    public List<Veiculo> buscarTodosVeiculos() throws SQLException{
        List<Veiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM veiculos ORDER BY apelido";

        try (Statement stmt = DatabaseManager.getConnection().createStatement()){
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Veiculo buscarVeiculoId(int id) throws SQLException{
        String sql = """
                SELECT * FROM veiculos WHERE id = ?
                """;
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)){
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapear(rs);
        }
        return null;
    }

    public void deletarVeiculo(int id) throws SQLException{
        String sql = "DELETE FROM veiculos WHERE id = ?";

        try(PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)){
            pstmt.setInt(1,id);
            pstmt.executeUpdate();
        }
    }

    public Veiculo mapear(ResultSet rs) throws SQLException{
        Veiculo veiculo = new Veiculo();

        veiculo.setId(rs.getInt("id"));
        veiculo.setApelido(rs.getString("apelido"));
        veiculo.setMarca(rs.getString("marca"));
        veiculo.setModelo(rs.getString("modelo"));
        veiculo.setAno(rs.getInt("ano"));
        veiculo.setMotor(rs.getString("motor"));
        veiculo.setVin(rs.getString("vin"));
        String dataCriacao = rs.getString("data_criacao");
        if(dataCriacao != null) veiculo.setDataCriacao(LocalDateTime.parse(dataCriacao.replace(" ", "T")));
        return veiculo;
    }
}
