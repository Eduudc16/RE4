package mercador.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import mercador.database.ItemDAO;
import mercador.model.Item;

/**
 * Tela de inventário no estilo "maleta" do RE4: cada item ocupa um bloco
 * de tamanho diferente na grade, preenchido automaticamente (sem
 * drag-and-drop). Como ainda não existe uma tabela de inventário do
 * cliente, esta tela exibe o catálogo completo apenas como prévia visual.
 */
public class InventarioView extends JFrame {

    public InventarioView() {
        setTitle("Maleta - Inventário");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        List<Item> itens = new ItemDAO().listarTodos();

        add(criarCabecalho(itens.size()), BorderLayout.NORTH);
        add(new JScrollPane(new MaletaPanel(itens)), BorderLayout.CENTER);

        setSize(620, 650);
        setLocationRelativeTo(null);
    }

    private JPanel criarCabecalho(int totalItens) {
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout());
        painel.setBackground(new Color(40, 30, 20));

        JLabel titulo = new JLabel("Maleta do Mercador", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(new Color(230, 210, 170));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 4, 10));

        JLabel aviso = new JLabel(
                "Prévia visual com " + totalItens + " itens do catálogo — inventário real do cliente ainda será implementado",
                SwingConstants.CENTER);
        aviso.setFont(new Font("SansSerif", Font.ITALIC, 11));
        aviso.setForeground(new Color(180, 165, 140));
        aviso.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(aviso, BorderLayout.SOUTH);
        return painel;
    }

    /** Grade estilo maleta: cada item ocupa um bloco de LxA células, encaixado por um algoritmo simples de preenchimento. */
    private static class MaletaPanel extends JPanel {

        private static final int COLUNAS = 8;
        private static final int LINHAS = 8;
        private static final int CELULA = 60;
        private static final int MARGEM = 16;
        private static final int GAP = 4;

        MaletaPanel(List<Item> itens) {
            setLayout(null);
            setBackground(new Color(92, 62, 38));
            int largura = MARGEM * 2 + COLUNAS * CELULA;
            int altura = MARGEM * 2 + LINHAS * CELULA;
            setPreferredSize(new java.awt.Dimension(largura, altura));

            posicionarItens(itens);
        }

        private void posicionarItens(List<Item> itens) {
            boolean[][] ocupado = new boolean[LINHAS][COLUNAS];

            List<Item> ordenados = new ArrayList<>(itens);
            ordenados.sort((a, b) -> areaDoItem(b) - areaDoItem(a));

            for (Item item : ordenados) {
                int[] tamanho = tamanhoDoItem(item);
                int larguraCel = tamanho[0];
                int alturaCel = tamanho[1];

                int[] posicao = encontrarEspaco(ocupado, larguraCel, alturaCel);
                if (posicao == null) {
                    continue;
                }

                marcarOcupado(ocupado, posicao[0], posicao[1], larguraCel, alturaCel);
                add(criarBloco(item, posicao[0], posicao[1], larguraCel, alturaCel));
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

        private BlocoItem criarBloco(Item item, int linha, int coluna, int largura, int altura) {
            BlocoItem bloco = new BlocoItem(item);
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(76, 50, 30));
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

    /** Bloco visual de um item dentro da maleta, com cor por categoria. */
    private static class BlocoItem extends JPanel {

        private final Color cor;

        BlocoItem(Item item) {
            setOpaque(false);
            setLayout(new BorderLayout());
            this.cor = corPorCategoria(item.getCategoriaNome());

            JLabel label = new JLabel("<html><div style='text-align:center;'>" + item.getNome() + "</div></html>", SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            label.setForeground(Color.WHITE);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }

        private Color corPorCategoria(String categoria) {
            if (categoria == null) {
                return new Color(110, 110, 110);
            }
            switch (categoria) {
                case "Arma": return new Color(70, 70, 75);
                case "Munição": return new Color(160, 130, 40);
                case "Cura": return new Color(50, 130, 60);
                case "Tesouro": return new Color(180, 150, 40);
                case "Colete": return new Color(60, 80, 120);
                default: return new Color(110, 110, 110);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(cor.darker());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
