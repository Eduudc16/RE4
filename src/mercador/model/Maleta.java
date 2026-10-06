package mercador.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras da maleta do inventário: uma grade de COLUNAS x LINHAS células em que
 * cada unidade de item ocupa um bloco de tamanho próprio, encaixado
 * automaticamente. A mesma conta serve para desenhar a maleta (InventarioView)
 * e para recusar uma compra que não caberia (ComprarItensController), então a
 * tela nunca mostra menos do que o inventário realmente tem.
 */
public final class Maleta {

    public static final int COLUNAS = 8;
    public static final int LINHAS = 8;

    private Maleta() {
    }

    /** Uma unidade de item já encaixada na grade, com posição e tamanho em células. */
    public static class Bloco {

        private final Item item;
        private final int linha;
        private final int coluna;
        private final int largura;
        private final int altura;

        private Bloco(Item item, int linha, int coluna, int largura, int altura) {
            this.item = item;
            this.linha = linha;
            this.coluna = coluna;
            this.largura = largura;
            this.altura = altura;
        }

        public Item getItem() {
            return item;
        }

        public int getLinha() {
            return linha;
        }

        public int getColuna() {
            return coluna;
        }

        public int getLargura() {
            return largura;
        }

        public int getAltura() {
            return altura;
        }
    }

    /** Resultado do encaixe: os blocos posicionados e quantas unidades não couberam na grade. */
    public static class Disposicao {

        private final List<Bloco> blocos;
        private final int naoCouberam;

        private Disposicao(List<Bloco> blocos, int naoCouberam) {
            this.blocos = blocos;
            this.naoCouberam = naoCouberam;
        }

        public List<Bloco> getBlocos() {
            return blocos;
        }

        public int getNaoCouberam() {
            return naoCouberam;
        }
    }

    /**
     * Encaixa uma unidade por bloco, das maiores para as menores, no primeiro
     * espaço livre da grade — quanto mais unidades de um item, mais espaço ele
     * ocupa. O resultado não depende da ordem da lista recebida.
     */
    public static Disposicao organizar(List<ItemInventario> itens) {
        List<Item> unidades = new ArrayList<>();
        for (ItemInventario itemInv : itens) {
            for (int i = 0; i < itemInv.getQuantidade(); i++) {
                unidades.add(itemInv.getItem());
            }
        }
        unidades.sort((a, b) -> {
            int porArea = areaDoItem(b) - areaDoItem(a);
            return porArea != 0 ? porArea : Integer.compare(a.getId(), b.getId());
        });

        boolean[][] ocupado = new boolean[LINHAS][COLUNAS];
        List<Bloco> blocos = new ArrayList<>();
        int naoCouberam = 0;

        for (Item item : unidades) {
            int[] tamanho = tamanhoDoItem(item);
            int[] posicao = encontrarEspaco(ocupado, tamanho[0], tamanho[1]);
            if (posicao == null) {
                naoCouberam++;
                continue;
            }
            marcarOcupado(ocupado, posicao[0], posicao[1], tamanho[0], tamanho[1]);
            blocos.add(new Bloco(item, posicao[0], posicao[1], tamanho[0], tamanho[1]));
        }

        return new Disposicao(blocos, naoCouberam);
    }

    /** Diz se o inventário atual, somado a `quantidade` unidades de `item`, ainda cabe inteiro na maleta. */
    public static boolean cabe(List<ItemInventario> atuais, Item item, int quantidade) {
        List<ItemInventario> depois = new ArrayList<>();
        boolean jaPossuia = false;
        for (ItemInventario itemInv : atuais) {
            if (itemInv.getItem().getId() == item.getId()) {
                depois.add(new ItemInventario(item, itemInv.getQuantidade() + quantidade));
                jaPossuia = true;
            } else {
                depois.add(itemInv);
            }
        }
        if (!jaPossuia) {
            depois.add(new ItemInventario(item, quantidade));
        }
        return organizar(depois).getNaoCouberam() == 0;
    }

    /** Quantas unidades de `item` (até `maximo`) ainda cabem na maleta, para explicar a recusa ao usuário. */
    public static int quantasCabem(List<ItemInventario> atuais, Item item, int maximo) {
        int cabem = 0;
        while (cabem < maximo && cabe(atuais, item, cabem + 1)) {
            cabem++;
        }
        return cabem;
    }

    private static int[] encontrarEspaco(boolean[][] ocupado, int largura, int altura) {
        for (int linha = 0; linha <= LINHAS - altura; linha++) {
            for (int coluna = 0; coluna <= COLUNAS - largura; coluna++) {
                if (cabeAqui(ocupado, linha, coluna, largura, altura)) {
                    return new int[]{linha, coluna};
                }
            }
        }
        return null;
    }

    private static boolean cabeAqui(boolean[][] ocupado, int linha, int coluna, int largura, int altura) {
        for (int l = linha; l < linha + altura; l++) {
            for (int c = coluna; c < coluna + largura; c++) {
                if (ocupado[l][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void marcarOcupado(boolean[][] ocupado, int linha, int coluna, int largura, int altura) {
        for (int l = linha; l < linha + altura; l++) {
            for (int c = coluna; c < coluna + largura; c++) {
                ocupado[l][c] = true;
            }
        }
    }

    private static int areaDoItem(Item item) {
        int[] tamanho = tamanhoDoItem(item);
        return tamanho[0] * tamanho[1];
    }

    /** Tamanho (colunas x linhas) de cada item na maleta, como no jogo: armas maiores, munição/cura pequenas. */
    public static int[] tamanhoDoItem(Item item) {
        switch (item.getNome()) {
            case "Punisher": return new int[]{2, 2};
            case "Riot Gun": return new int[]{3, 2};
            case "Red9": return new int[]{2, 2};
            case "Rifle Semi-Automático": return new int[]{1, 4};
            case "Munição de Pistola": return new int[]{1, 1};
            case "Munição de Espingarda": return new int[]{1, 2};
            case "Erva Verde": return new int[]{1, 1};
            case "Spray de Primeiros Socorros": return new int[]{1, 2};
            case "Rubi Lapidado": return new int[]{1, 1};
            case "Olho Elegante": return new int[]{2, 2};
            case "Colete à Prova de Balas": return new int[]{2, 3};
            default: return tamanhoPorCategoria(item.getCategoriaNome());
        }
    }

    private static int[] tamanhoPorCategoria(String categoria) {
        if (categoria == null) {
            return new int[]{1, 1};
        }
        switch (categoria) {
            case Categoria.ARMA: return new int[]{2, 2};
            case "Colete": return new int[]{2, 2};
            case "Tesouro": return new int[]{1, 1};
            default: return new int[]{1, 1};
        }
    }
}
