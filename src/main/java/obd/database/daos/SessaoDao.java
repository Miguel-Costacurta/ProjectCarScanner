package obd.database.daos;

import obd.database.DatabaseManager;
import obd.database.models.Sessao;

import javax.xml.crypto.Data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessaoDao {

    public Sessao salvar(Sessao sessao) throws SQLException {
        String sql = """
                INSERT INTO sessoes (veiculo_id, descricao, inicio)
                VALUES (? ,? ,?)
                """;
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setInt(1, sessao.getVeiculoId());
            pstmt.setString(2, sessao.getDescricao());
            pstmt.setString(3, sessao.getInicio().toString());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if(keys.next()) sessao.setId(keys.getInt(1));
        }
        return sessao;
    }

    public void finalizarSessao (int sessaoId) throws SQLException {
        String sql = "UPDATE sessoes SET fim = ? WHERE id = ?";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)){
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, sessaoId);
            pstmt.executeUpdate();
        }
    }

    public List<Sessao> buscarPorVeiculo(int veiculoId) throws SQLException{
        List<Sessao> sessoes = new ArrayList<>();
        String sql = "SELECT * FROM sessoes WHERE id_veiculo = ? ORDER BY inicio DESC";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)){
            pstmt.setInt(1, veiculoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) sessoes.add(mapear(rs));
        }
        return sessoes;
    }

    public Sessao mapear (ResultSet rs) throws SQLException{
        Sessao s = new Sessao();
        s.setId(rs.getInt("id"));
        s.setVeiculoId(rs.getInt("veiculo_id"));
        s.setDescricao(rs.getString("descricao"));
        String inicio = rs.getString("inicio");
        if(inicio != null) s.setInicio(LocalDateTime.parse(inicio));
        String fim = rs.getString("fim");
        if (fim != null) s.setFim(LocalDateTime.parse(fim));
        return s;
    }
}
