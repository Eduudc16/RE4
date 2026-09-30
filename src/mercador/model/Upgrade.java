package mercador.model;

public class Upgrade {
    private int id;
    private int armaId;
    private String tipo;
    private int nivel;
    private double custo;

    public Upgrade() {}

    public Upgrade(int id, int armaId, String tipo, int nivel, double custo) {
        this.id = id;
        this.armaId = armaId;
        this.tipo = tipo;
        this.nivel = nivel;
        this.custo = custo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getArmaId() { return armaId; }
    public void setArmaId(int armaId) { this.armaId = armaId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public double getCusto() { return custo; }
    public void setCusto(double custo) { this.custo = custo; }
}
