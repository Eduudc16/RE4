package mercador.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mercador.model.Cliente;

public class ClienteDAO {

    public Cliente buscarPorId(int id) {
        String sql = "SELECT id, nome, dinheiro FROM cliente WHERE id = ?";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getDouble("dinheiro"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean atualizarSaldo(int clienteId, double novoSaldo) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return atualizarSaldo(conn, clienteId, novoSaldo);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mesma operação, mas usando uma conexão já aberta pelo chamador — usada
     * quando o saldo precisa ser atualizado dentro de uma transação maior
     * (ex.: compra/venda), junto com outras DAOs, para permitir commit/rollback
     * conjunto.
     */
    public boolean atualizarSaldo(Connection conn, int clienteId, double novoSaldo) throws SQLException {
        String sql = "UPDATE cliente SET dinheiro = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, novoSaldo);
            ps.setInt(2, clienteId);
            return ps.executeUpdate() > 0;
        }
    }
}
