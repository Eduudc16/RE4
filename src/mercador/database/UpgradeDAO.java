package mercador.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mercador.model.Upgrade;

public class UpgradeDAO {

    public Upgrade buscarPorId(int id) {
        String sql = "SELECT id, arma_id, tipo, nivel, custo, aplicado FROM upgrade WHERE id = ?";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Upgrade upgrade = new Upgrade(
                            rs.getInt("id"),
                            rs.getInt("arma_id"),
                            rs.getString("tipo"),
                            rs.getInt("nivel"),
                            rs.getDouble("custo")
                    );
                    upgrade.setAplicado(rs.getInt("aplicado") != 0);
                    return upgrade;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean marcarComoAplicado(int upgradeId) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return marcarComoAplicado(conn, upgradeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mesma operação, mas usando uma conexão já aberta pelo chamador — usada
     * dentro da transação de aplicarUpgrade, junto com o débito de saldo e
     * a atualização da arma, para permitir commit/rollback conjunto.
     */
    public boolean marcarComoAplicado(Connection conn, int upgradeId) throws SQLException {
        String sql = "UPDATE upgrade SET aplicado = 1 WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, upgradeId);
            return ps.executeUpdate() > 0;
        }
    }
}
