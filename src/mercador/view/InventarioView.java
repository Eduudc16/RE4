package mercador.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import mercador.controller.ComprarItensController;
import mercador.database.InventarioClienteDAO;
import mercador.model.Item;
import mercador.model.ItemInventario;

/**
 * Tela de inventário no estilo "maleta" do RE4: cada item ocupa um bloco
 * de tamanho diferente na grade, preenchido automaticamente (sem
 * drag-and-drop). Mostra apenas os itens que o cliente realmente possui,
 * com a quantidade de cada um.
 */
public class InventarioView extends JFrame {

    public InventarioView() {
        setTitle("Inventário (Maleta)");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        List<ItemInventario> itens = new InventarioClienteDAO().listarPorCliente(ComprarItensController.CLIENTE_ATUAL_ID);

        add(new JScrollPane(new MaletaPanel(itens)), BorderLayout.CENTER);
    }

    /** Grade estilo maleta: cada item ocupa um bloco de LxA células, encaixado por um algoritmo simples de preenchimento. */
    private static class MaletaPanel extends JPanel {

        private static final int COLUNAS = 8;
        private static final int LINHAS = 8;
        private static final int CELULA = 60;
        private static final int MARGEM = 10;
        private static final int GAP = 4;

        MaletaPanel(List<ItemInventario> itens) {
            setLayout(null);
            int largura = MARGEM * 2 + COLUNAS * CELULA;
            int altura = MARGEM * 2 + LINHAS * CELULA;
            setPreferredSize(new Dimension(largura, altura));

            posicionarItens(itens);
        }

        private void posicionarItens(List<ItemInventario> itens) {
            boolean[][] ocupado = new boolean[LINHAS][COLUNAS];

            List<ItemInventario> ordenados = new ArrayList<>(itens);
            ordenados.sort((a, b) -> areaDoItem(b.getItem()) - areaDoItem(a.getItem()));

            for (ItemInventario itemInv : ordenados) {
                int[] tamanho = tamanhoDoItem(itemInv.getItem());
                int larguraCel = tamanho[0];
                int alturaCel = tamanho[1];

                int[] posicao = encontrarEspaco(ocupado, larguraCel, alturaCel);
                if (posicao == null) {
                    continue;
                }

                marcarOcupado(ocupado, posicao[0], posicao[1], larguraCel, alturaCel);
                add(criarBloco(itemInv, posicao[0], posicao[1], larguraCel, alturaCel));
            }
        }

        private int[] encontrarEspaco(boolean[][] ocupado, int largura, int altura) {
            for (int linha = 0; linha <= LINHAS - altura; linha++) {
                for (int coluna = 0; coluna <= COLUNAS - largura; coluna++) {
                    if (cabeAqui(ocupado, linha, coluna, largura, altura)) {
                        return new int[]{linha, coluna};
                    }
                }
            }
            return null;
        }

        private boolean cabeAqui(boolean[][] ocupado, int linha, int coluna, int largura, int altura) {
            for (int l = linha; l < linha + altura; l++) {
                for (int c = coluna; c < coluna + largura; c++) {
                    if (ocupado[l][c]) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void marcarOcupado(boolean[][] ocupado, int linha, int coluna, int largura, int altura) {
            for (int l = linha; l < linha + altura; l++) {
                for (int c = coluna; c < coluna + largura; c++) {
                    ocupado[l][c] = true;
                }
            }
        }

        private BlocoItem criarBloco(ItemInventario itemInv, int linha, int coluna, int largura, int altura) {
            BlocoItem bloco = new BlocoItem(itemInv);
            int x = MARGEM + coluna * CELULA + GAP / 2;
            int y = MARGEM + linha * CELULA + GAP / 2;
            int w = largura * CELULA - GAP;
            int h = altura * CELULA - GAP;
            bloco.setBounds(x, y, w, h);
            return bloco;
        }

        private int areaDoItem(Item item) {
            int[] tamanho = tamanhoDoItem(item);
            return tamanho[0] * tamanho[1];
        }

        /** Tamanho (colunas x linhas) de cada item na maleta, como no jogo: armas maiores, munição/cura pequenas. */
        private int[] tamanhoDoItem(Item item) {
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

        private int[] tamanhoPorCategoria(String categoria) {
            if (categoria == null) {
                return new int[]{1, 1};
            }
            switch (categoria) {
                case "Arma": return new int[]{2, 2};
                case "Colete": return new int[]{2, 2};
                case "Tesouro": return new int[]{1, 1};
                default: return new int[]{1, 1};
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.LIGHT_GRAY);
            for (int l = 0; l <= LINHAS; l++) {
                int y = MARGEM + l * CELULA;
                g2.drawLine(MARGEM, y, MARGEM + COLUNAS * CELULA, y);
            }
            for (int c = 0; c <= COLUNAS; c++) {
                int x = MARGEM + c * CELULA;
                g2.drawLine(x, MARGEM, x, MARGEM + LINHAS * CELULA);
            }
        }
    }

    /** Bloco visual de um item dentro da maleta, no mesmo estilo simples das outras telas. Mostra a quantidade. */
    private static class BlocoItem extends JPanel {

        BlocoItem(ItemInventario itemInv) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.GRAY));

            String texto = itemInv.getItem().getNome() + " (x" + itemInv.getQuantidade() + ")";
            JLabel label = new JLabel("<html><div style='text-align:center;'>" + texto + "</div></html>", SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }
}
