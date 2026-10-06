package mercador.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mercador.model.Item;
import mercador.model.ItemInventario;

public class InventarioClienteDAO {

    public List<ItemInventario> listarPorCliente(int clienteId) {
        List<ItemInventario> itens = new ArrayList<>();
        String sql = """
            SELECT i.id, i.nome, i.descricao, i.preco, i.categoria_id, c.nome AS categoria_nome, ic.quantidade
            FROM inventario_cliente ic
            JOIN item i ON i.id = ic.item_id
            JOIN categoria c ON c.id = i.categoria_id
            WHERE ic.cliente_id = ? AND ic.quantidade > 0
            ORDER BY i.nome
        """;

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item item = new Item(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getDouble("preco"),
                            rs.getInt("categoria_id")
                    );
                    item.setCategoriaNome(rs.getString("categoria_nome"));
                    itens.add(new ItemInventario(item, rs.getInt("quantidade")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }

    /** Se algum cliente tem pelo menos uma unidade do item. Na dúvida (erro de banco), considera que tem. */
    public boolean algumClientePossui(int itemId) {
        String sql = "SELECT COUNT(*) FROM inventario_cliente WHERE item_id = ? AND quantidade > 0";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean adicionarQuantidade(int clienteId, int itemId, int quantidade) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return adicionarQuantidade(conn, clienteId, itemId, quantidade);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Soma `quantidade` ao que o cliente já tem daquele item (ou cria a
     * linha, se ainda não existir), usando uma conexão já aberta pelo
     * chamador para poder entrar na mesma transação de outras DAOs
     * (ex.: compra), com commit/rollback conjunto.
     */
    public boolean adicionarQuantidade(Connection conn, int clienteId, int itemId, int quantidade) throws SQLException {
        String sqlUpdate = "UPDATE inventario_cliente SET quantidade = quantidade + ? WHERE cliente_id = ? AND item_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, clienteId);
            ps.setInt(3, itemId);
            if (ps.executeUpdate() > 0) {
                return true;
            }
        }

        String sqlInsert = "INSERT INTO inventario_cliente (cliente_id, item_id, quantidade) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
            ps.setInt(1, clienteId);
            ps.setInt(2, itemId);
            ps.setInt(3, quantidade);
            return ps.executeUpdate() > 0;
        }
    }

    public int buscarQuantidade(int clienteId, int itemId) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return buscarQuantidade(conn, clienteId, itemId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int buscarQuantidade(Connection conn, int clienteId, int itemId) throws SQLException {
        String sql = "SELECT quantidade FROM inventario_cliente WHERE cliente_id = ? AND item_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("quantidade") : 0;
            }
        }
    }

    public boolean subtrairQuantidade(int clienteId, int itemId, int quantidade) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return subtrairQuantidade(conn, clienteId, itemId, quantidade);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Subtrai `quantidade` do que o cliente tem daquele item. Se a
     * quantidade resultante for zero (ou menos), remove a linha em vez de
     * deixar zerada. Usa uma conexão já aberta pelo chamador para entrar na
     * mesma transação de outras DAOs (ex.: venda), com commit/rollback
     * conjunto.
     */
    public boolean subtrairQuantidade(Connection conn, int clienteId, int itemId, int quantidade) throws SQLException {
        int atual = buscarQuantidade(conn, clienteId, itemId);
        int novaQuantidade = atual - quantidade;

        if (novaQuantidade <= 0) {
            String sql = "DELETE FROM inventario_cliente WHERE cliente_id = ? AND item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, clienteId);
                ps.setInt(2, itemId);
                return ps.executeUpdate() > 0;
            }
        }

        String sql = "UPDATE inventario_cliente SET quantidade = ? WHERE cliente_id = ? AND item_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, novaQuantidade);
            ps.setInt(2, clienteId);
            ps.setInt(3, itemId);
            return ps.executeUpdate() > 0;
        }
    }
}
