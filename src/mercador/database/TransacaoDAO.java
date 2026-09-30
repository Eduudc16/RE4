package mercador.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import mercador.model.Transacao;

public class TransacaoDAO {

    public int inserir(Transacao transacao) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return inserir(conn, transacao);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Mesma operação, mas usando uma conexão já aberta pelo chamador — usada
     * quando o registro precisa entrar na mesma transação de outras DAOs
     * (ex.: compra/venda), para permitir commit/rollback conjunto.
     */
    public int inserir(Connection conn, Transacao transacao) throws SQLException {
        String sql = "INSERT INTO transacao (cliente_id, item_id, tipo, quantidade, valor_total, data) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transacao.getClienteId());
            if (transacao.getItemId() != null) {
                ps.setInt(2, transacao.getItemId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, transacao.getTipo());
            ps.setInt(4, transacao.getQuantidade());
            ps.setDouble(5, transacao.getValorTotal());
            ps.setString(6, transacao.getData());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    public List<Transacao> listarTodas() {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT id, cliente_id, item_id, tipo, quantidade, valor_total, data FROM transacao ORDER BY id DESC";

        try (Connection conn = ConexaoSQLite.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transacoes.add(mapearTransacao(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transacoes;
    }

    public List<Transacao> listarPorCliente(int clienteId) {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT id, cliente_id, item_id, tipo, quantidade, valor_total, data FROM transacao WHERE cliente_id = ? ORDER BY id DESC";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(mapearTransacao(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transacoes;
    }

    private Transacao mapearTransacao(ResultSet rs) throws SQLException {
        int itemIdColuna = rs.getInt("item_id");
        Integer itemId = rs.wasNull() ? null : itemIdColuna;

        return new Transacao(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                itemId,
                rs.getString("tipo"),
                rs.getInt("quantidade"),
                rs.getDouble("valor_total"),
                rs.getString("data")
        );
    }
}
