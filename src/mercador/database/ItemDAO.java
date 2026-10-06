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
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return inserir(conn, item);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Mesma operação, mas usando uma conexão já aberta pelo chamador — usada
     * no cadastro de uma arma, que grava `item` e `arma` na mesma transação.
     */
    public int inserir(Connection conn, Item item) throws SQLException {
        String sql = "INSERT INTO item (nome, descricao, preco, categoria_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        }

        return -1;
    }

    public boolean atualizar(Item item) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return atualizar(conn, item);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Mesma operação, mas usando uma conexão já aberta pelo chamador (ver inserir(Connection, Item)). */
    public boolean atualizar(Connection conn, Item item) throws SQLException {
        String sql = "UPDATE item SET nome = ?, descricao = ?, preco = ?, categoria_id = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getNome());
            ps.setString(2, item.getDescricao());
            ps.setDouble(3, item.getPreco());
            ps.setInt(4, item.getCategoriaId());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Remove um item. Recusa a remoção (retorna false) se houver transacao,
     * linha de inventario_cliente ou upgrade da arma do item dependente, em
     * vez de aplicar CASCADE automaticamente. Se o item for uma arma sem
     * upgrades, a linha em `arma` é removida junto, na mesma transação.
     */
    public boolean deletar(int id) {
        try {
            return ConexaoSQLite.executarEmTransacao(conn -> {
                if (possuiDependencias(conn, id)) {
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM arma WHERE item_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM item WHERE id = ?")) {
                    ps.setInt(1, id);
                    return ps.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean possuiDependencias(Connection conn, int itemId) throws SQLException {
        String[] consultas = {
            "SELECT COUNT(*) FROM transacao WHERE item_id = ?",
            "SELECT COUNT(*) FROM inventario_cliente WHERE item_id = ?",
            "SELECT COUNT(*) FROM upgrade WHERE arma_id IN (SELECT id FROM arma WHERE item_id = ?)"
        };

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

        return false;
    }
}
