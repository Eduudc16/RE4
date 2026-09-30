package mercador.model;

/** Um item que o cliente possui, com a quantidade no inventário. */
public class ItemInventario {

    private final Item item;
    private final int quantidade;

    public ItemInventario(Item item, int quantidade) {
        this.item = item;
        this.quantidade = quantidade;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantidade() {
        return quantidade;
    }
}
