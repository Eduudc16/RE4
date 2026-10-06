package mercador.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public Arma buscarPorId(int id) {
        return buscarPor("a.id", id);
    }

    /** A linha de `arma` do item, ou null se o item não for uma arma cadastrada. */
    public Arma buscarPorItemId(int itemId) {
        return buscarPor("a.item_id", itemId);
    }

    private Arma buscarPor(String coluna, int valor) {
        String sql = """
            SELECT a.id, a.item_id, i.nome AS item_nome, a.dano, a.capacidade,
                   a.velocidade_recarga, a.poder_tiro
            FROM arma a
            JOIN item i ON i.id = a.item_id
            WHERE\s""" + coluna + " = ?";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Arma arma = new Arma(
                            rs.getInt("id"),
                            rs.getInt("item_id"),
                            rs.getInt("dano"),
                            rs.getInt("capacidade"),
                            rs.getInt("velocidade_recarga"),
                            rs.getInt("poder_tiro")
                    );
                    arma.setItemNome(rs.getString("item_nome"));
                    return arma;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int inserir(Arma arma) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return inserir(conn, arma);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Mesma operação, mas usando uma conexão já aberta pelo chamador — usada
     * no cadastro de uma arma, que grava `item` e `arma` na mesma transação.
     */
    public int inserir(Connection conn, Arma arma) throws SQLException {
        String sql = "INSERT INTO arma (item_id, dano, capacidade, velocidade_recarga, poder_tiro) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, arma.getItemId());
            ps.setInt(2, arma.getDano());
            ps.setInt(3, arma.getCapacidade());
            ps.setInt(4, arma.getVelocidadeRecarga());
            ps.setInt(5, arma.getPoderTiro());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    public boolean atualizar(Arma arma) {
        try (Connection conn = ConexaoSQLite.getConnection()) {
            return atualizar(conn, arma);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mesma operação, mas usando uma conexão já aberta pelo chamador — usada
     * quando a atualização precisa entrar na mesma transação de outras DAOs
     * (ex.: aplicarUpgrade), para permitir commit/rollback conjunto.
     */
    public boolean atualizar(Connection conn, Arma arma) throws SQLException {
        String sql = "UPDATE arma SET item_id = ?, dano = ?, capacidade = ?, velocidade_recarga = ?, poder_tiro = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, arma.getItemId());
            ps.setInt(2, arma.getDano());
            ps.setInt(3, arma.getCapacidade());
            ps.setInt(4, arma.getVelocidadeRecarga());
            ps.setInt(5, arma.getPoderTiro());
            ps.setInt(6, arma.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Remove uma arma. Recusa a remoção (retorna false) se houver upgrade
     * dependente, em vez de aplicar CASCADE automaticamente.
     */
    public boolean deletar(int id) {
        if (possuiUpgrades(id)) {
            return false;
        }

        String sql = "DELETE FROM arma WHERE id = ?";
        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean possuiUpgrades(int armaId) {
        String sql = "SELECT COUNT(*) FROM upgrade WHERE arma_id = ?";

        try (Connection conn = ConexaoSQLite.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, armaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}
