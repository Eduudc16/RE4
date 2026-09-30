package mercador.model;

public class Cliente {
    private int id;
    private String nome;
    private double dinheiro;

    public Cliente() {}

    public Cliente(int id, String nome, double dinheiro) {
        this.id = id;
        this.nome = nome;
        this.dinheiro = dinheiro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getDinheiro() { return dinheiro; }
    public void setDinheiro(double dinheiro) { this.dinheiro = dinheiro; }
}
