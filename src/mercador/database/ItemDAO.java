package mercador.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public Item buscarPorId(int id) {
        String sql = """
            SELECT i.id, i.nome, i.descricao, i.preco, i.categoria_id, c.nome AS categoria_nome
            FROM item i
            JOIN categoria c ON c.id = i.categoria_id
            WHERE i.id = ?
        """;

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Item item = new Item(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getDouble("preco"),
                            rs.getInt("categoria_id")
                    );
                    item.setCategoriaNome(rs.getString("categoria_nome"));
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int inserir(Item item) {
        String sql = "INSERT INTO item (nome, descricao, preco, categoria_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getNome());
            ps.setString(2, item.getDescricao());
            ps.setDouble(3, item.getPreco());
            ps.setInt(4, item.getCategoriaId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean atualizar(Item item) {
        String sql = "UPDATE item SET nome = ?, descricao = ?, preco = ?, categoria_id = ? WHERE id = ?";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getNome());
            ps.setString(2, item.getDescricao());
            ps.setDouble(3, item.getPreco());
            ps.setInt(4, item.getCategoriaId());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove um item. Recusa a remoção (retorna false) se houver arma,
     * transacao ou linha de inventario_cliente dependente, em vez de
     * aplicar CASCADE automaticamente.
     */
    public boolean deletar(int id) {
        if (possuiDependencias(id)) {
            return false;
        }

        String sql = "DELETE FROM item WHERE id = ?";
        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean possuiDependencias(int itemId) {
        String[] consultas = {
            "SELECT COUNT(*) FROM arma WHERE item_id = ?",
            "SELECT COUNT(*) FROM transacao WHERE item_id = ?",
            "SELECT COUNT(*) FROM inventario_cliente WHERE item_id = ?"
        };

        try (Connection conn = ConexaoSQLite.getConnection()) {
            for (String sql : consultas) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, itemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }

        return false;
    }
}
