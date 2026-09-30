package mercador.model;

public class Arma {
    private int id;
    private int itemId;
    private String itemNome;
    private int dano;
    private int capacidade;
    private int velocidadeRecarga;
    private int poderTiro;

    public Arma() {}

    public Arma(int id, int itemId, int dano, int capacidade, int velocidadeRecarga, int poderTiro) {
        this.id = id;
        this.itemId = itemId;
        this.dano = dano;
        this.capacidade = capacidade;
        this.velocidadeRecarga = velocidadeRecarga;
        this.poderTiro = poderTiro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemNome() { return itemNome; }
    public void setItemNome(String itemNome) { this.itemNome = itemNome; }

    public int getDano() { return dano; }
    public void setDano(int dano) { this.dano = dano; }

    public int getCapacidade() { return capacidade; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }

    public int getVelocidadeRecarga() { return velocidadeRecarga; }
    public void setVelocidadeRecarga(int velocidadeRecarga) { this.velocidadeRecarga = velocidadeRecarga; }

    public int getPoderTiro() { return poderTiro; }
    public void setPoderTiro(int poderTiro) { this.poderTiro = poderTiro; }
}
