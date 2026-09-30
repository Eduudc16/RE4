package mercador.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import mercador.model.Arma;

public class ArmaDAO {

    public List<Arma> listarTodas() {
        List<Arma> armas = new ArrayList<>();
        String sql = """
            SELECT a.id, a.item_id, i.nome AS item_nome, a.dano, a.capacidade,
                   a.velocidade_recarga, a.poder_tiro
            FROM arma a
            JOIN item i ON i.id = a.item_id
            ORDER BY i.nome
        """;

        try (Connection conn = ConexaoSQLite.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Arma arma = new Arma(
                        rs.getInt("id"),
                        rs.getInt("item_id"),
                        rs.getInt("dano"),
                        rs.getInt("capacidade"),
                        rs.getInt("velocidade_recarga"),
                        rs.getInt("poder_tiro")
                );
                arma.setItemNome(rs.getString("item_nome"));
                armas.add(arma);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return armas;
    }
}
