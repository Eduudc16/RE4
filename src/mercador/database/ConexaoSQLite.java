package mercador.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoSQLite {

    private static final String DB_FOLDER = "database";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FOLDER + "/mercador.db";

    public static Connection getConnection() throws SQLException {
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void inicializarBanco() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            criarTabelas(stmt);
            if (bancoVazio(stmt)) {
                inserirDadosIniciais(stmt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void criarTabelas(Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS categoria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS cliente (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                dinheiro REAL NOT NULL DEFAULT 0
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS item (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                descricao TEXT,
                preco REAL NOT NULL,
                categoria_id INTEGER NOT NULL,
                FOREIGN KEY (categoria_id) REFERENCES categoria(id)
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS arma (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_id INTEGER NOT NULL,
                dano INTEGER NOT NULL,
                capacidade INTEGER NOT NULL,
                velocidade_recarga INTEGER NOT NULL,
                poder_tiro INTEGER NOT NULL,
                FOREIGN KEY (item_id) REFERENCES item(id)
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS upgrade (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                arma_id INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                nivel INTEGER NOT NULL,
                custo REAL NOT NULL,
                FOREIGN KEY (arma_id) REFERENCES arma(id)
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS transacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                item_id INTEGER,
                tipo TEXT NOT NULL,
                quantidade INTEGER NOT NULL,
                valor_total REAL NOT NULL,
                data TEXT NOT NULL,
                FOREIGN KEY (cliente_id) REFERENCES cliente(id),
                FOREIGN KEY (item_id) REFERENCES item(id)
            )
        """);
    }

    private static boolean bancoVazio(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM categoria")) {
            return rs.next() && rs.getInt("total") == 0;
        }
    }

    private static void inserirDadosIniciais(Statement stmt) throws SQLException {
        stmt.execute("""
            INSERT INTO categoria (id, nome) VALUES
                (1, 'Arma'),
                (2, 'Munição'),
                (3, 'Cura'),
                (4, 'Tesouro'),
                (5, 'Colete')
        """);

        stmt.execute("""
            INSERT INTO cliente (id, nome, dinheiro) VALUES
                (1, 'Leon S. Kennedy', 5000)
        """);

        stmt.execute("""
            INSERT INTO item (id, nome, descricao, preco, categoria_id) VALUES
                (1, 'Punisher', 'Pistola inicial, precisa e confiável', 2000, 1),
                (2, 'Riot Gun', 'Espingarda de disparo largo', 5000, 1),
                (3, 'Red9', 'Pistola pesada com grande poder de fogo', 8000, 1),
                (4, 'Rifle Semi-Automático', 'Rifle de precisão com mira telescópica', 15000, 1),
                (5, 'Munição de Pistola', 'Caixa com 30 balas de pistola', 200, 2),
                (6, 'Munição de Espingarda', 'Caixa com 10 cartuchos', 400, 2),
                (7, 'Erva Verde', 'Recupera parte da vida', 100, 3),
                (8, 'Spray de Primeiros Socorros', 'Recupera toda a vida', 600, 3),
                (9, 'Rubi Lapidado', 'Uma joia rara e valiosa', 3000, 4),
                (10, 'Olho Elegante', 'Tesouro raro encontrado em uma estátua', 10000, 4),
                (11, 'Colete à Prova de Balas', 'Reduz o dano recebido', 4000, 5)
        """);

        stmt.execute("""
            INSERT INTO arma (id, item_id, dano, capacidade, velocidade_recarga, poder_tiro) VALUES
                (1, 1, 30, 8, 40, 50),
                (2, 2, 60, 5, 30, 70),
                (3, 3, 70, 8, 35, 80),
                (4, 4, 90, 5, 50, 95)
        """);

        stmt.execute("""
            INSERT INTO upgrade (id, arma_id, tipo, nivel, custo) VALUES
                (1, 1, 'Poder de Tiro', 2, 5000),
                (2, 1, 'Capacidade', 2, 3000),
                (3, 2, 'Poder de Tiro', 2, 6000),
                (4, 3, 'Velocidade de Recarga', 2, 4000)
        """);
    }
}
