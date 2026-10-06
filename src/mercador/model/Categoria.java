package mercador.model;

public class Categoria {

    // Categorias que as regras do sistema reconhecem (criadas em ConexaoSQLite.inserirDadosIniciais).
    public static final String ARMA = "Arma";
    public static final String MUNICAO = "Munição";
    public static final String CURA = "Cura";

    private int id;
    private String nome;

    public Categoria() {}

    public Categoria(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String toString() { return nome; }
}
