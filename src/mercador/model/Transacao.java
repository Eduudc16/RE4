package mercador.model;

public class Transacao {
    private int id;
    private int clienteId;
    private Integer itemId;
    private String tipo;
    private int quantidade;
    private double valorTotal;
    private String data;

    public Transacao() {}

    public Transacao(int id, int clienteId, Integer itemId, String tipo, int quantidade, double valorTotal, String data) {
        this.id = id;
        this.clienteId = clienteId;
        this.itemId = itemId;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
        this.data = data;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
