package mercador.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import mercador.model.Categoria;

public class CategoriaDAO {

    public List<Categoria> listarTodas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nome FROM categoria ORDER BY nome";

        try (Connection conn = ConexaoSQLite.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categorias;
    }
}
