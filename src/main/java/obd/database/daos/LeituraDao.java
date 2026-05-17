package obd.database.daos;

import obd.database.DatabaseManager;
import obd.database.models.Leitura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeituraDao {

    public void salvarLeitura (Leitura leitura) throws SQLException{
        String sql = """
                INSERT INTO leituras (sessao_id, timestamp, pid, valor)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)){
            pstmt.setInt(1, leitura.getSessaoId());
            pstmt.setString(2, leitura.getTimestamp().toString());
            pstmt.setString(3,leitura.getPid());
            pstmt.setDouble(4, leitura.getValor());
            pstmt.executeUpdate();
        }
    }

    public void salvarLote(List<Leitura> leituras) throws SQLException{
        String sql = """
                INSERT INTO leituras (sessao_id, timestamp, pid, valor)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)){
            for (Leitura l : leituras){
                pstmt.setInt(1,l.getSessaoId());
                pstmt.setString(2, l.getTimestamp().toString());
                pstmt.setString(3, l.getPid());
                pstmt.setDouble(4, l.getValor());

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }catch (SQLException e){
            conn.rollback();
            throw e;
        }finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Leitura> buscarPorSessao(int sessaoId) throws SQLException {
        List<Leitura> lista = new ArrayList<>();
        String sql = "SELECT * FROM leituras WHERE sessao_id = ? ORDER BY timestamp";

        try (PreparedStatement stmt = DatabaseManager.getConnection()
                .prepareStatement(sql)) {
            stmt.setInt(1, sessaoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── busca leituras de um PID específico em uma sessão ─────
    public List<Leitura> buscarPorPid(int sessaoId, String pid) throws SQLException {
        List<Leitura> lista = new ArrayList<>();
        String sql = "SELECT * FROM leituras WHERE sessao_id = ? AND pid = ? ORDER BY timestamp";

        try (PreparedStatement stmt = DatabaseManager.getConnection()
                .prepareStatement(sql)) {
            stmt.setInt(1,    sessaoId);
            stmt.setString(2, pid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Leitura mapear(ResultSet rs) throws SQLException {
        String ts = rs.getString("timestamp");
        Leitura l = new Leitura();
        l.setId(rs.getInt("id"));
        l.setSessaoId(rs.getInt("sessao_id"));
        if (ts != null) l.setTimestamp(LocalDateTime.parse(ts.replace(" ", "T")));
        l.setPid(rs.getString("pid"));
        l.setValor(rs.getDouble("valor"));
        return l;
    }
}
