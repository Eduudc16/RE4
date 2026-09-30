package mercador.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import mercador.model.Item;

public class ItemDAO {

    public List<Item> listarTodos() {
        List<Item> itens = new ArrayList<>();
        String sql = """
            SELECT i.id, i.nome, i.descricao, i.preco, i.categoria_id, c.nome AS categoria_nome
            FROM item i
            JOIN categoria c ON c.id = i.categoria_id
            ORDER BY i.nome
        """;

        try (Connection conn = ConexaoSQLite.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getDouble("preco"),
                        rs.getInt("categoria_id")
                );
                item.setCategoriaNome(rs.getString("categoria_nome"));
                itens.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }
}
