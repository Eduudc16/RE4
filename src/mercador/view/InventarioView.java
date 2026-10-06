package mercador.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import mercador.model.Maleta;

/**
 * Tela de inventário no estilo "maleta" do RE4: cada unidade de item ocupa um
 * bloco de tamanho diferente na grade, preenchido automaticamente (sem
 * drag-and-drop) — quanto mais unidades de um consumível, mais espaço ele usa.
 * Mostra apenas os itens que o cliente realmente possui e se atualiza sozinha
 * quando uma compra ou venda é feita em outra janela.
 */
public class InventarioView extends JFrame {

    private final InventarioClienteDAO inventarioDAO = new InventarioClienteDAO();
    private final MaletaPanel maleta = new MaletaPanel();
    private final JLabel avisoMaletaCheia = new JLabel("", SwingConstants.CENTER);

    public InventarioView() {
        setTitle("Inventário (Maleta)");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        avisoMaletaCheia.setForeground(Color.RED);
        add(new JScrollPane(maleta), BorderLayout.CENTER);
        add(avisoMaletaCheia, BorderLayout.SOUTH);

        recarregar();
        AtualizacaoAutomatica.ligar(this, this::recarregar);
    }

    private void recarregar() {
        List<ItemInventario> itens = inventarioDAO.listarPorCliente(ComprarItensController.CLIENTE_ATUAL_ID);
        Maleta.Disposicao disposicao = Maleta.organizar(itens);
        maleta.exibir(disposicao);

        int naoCouberam = disposicao.getNaoCouberam();
        avisoMaletaCheia.setVisible(naoCouberam > 0);
        avisoMaletaCheia.setText("Maleta cheia: " + naoCouberam
                + " unidade(s) não couberam e não aparecem. Venda itens para liberar espaço.");
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    /** Desenha a grade da maleta e os blocos que a classe Maleta já posicionou. */
    private static class MaletaPanel extends JPanel {

        private static final int CELULA = 60;
        private static final int MARGEM = 10;
        private static final int GAP = 4;

        MaletaPanel() {
            setLayout(null);
            int largura = MARGEM * 2 + Maleta.COLUNAS * CELULA;
            int altura = MARGEM * 2 + Maleta.LINHAS * CELULA;
            setPreferredSize(new Dimension(largura, altura));
        }

        void exibir(Maleta.Disposicao disposicao) {
            removeAll();
            for (Maleta.Bloco bloco : disposicao.getBlocos()) {
                add(criarBloco(bloco));
            }
            revalidate();
            repaint();
        }

        private BlocoItem criarBloco(Maleta.Bloco bloco) {
            BlocoItem painel = new BlocoItem(bloco.getItem());
            int x = MARGEM + bloco.getColuna() * CELULA + GAP / 2;
            int y = MARGEM + bloco.getLinha() * CELULA + GAP / 2;
            int w = bloco.getLargura() * CELULA - GAP;
            int h = bloco.getAltura() * CELULA - GAP;
            painel.setBounds(x, y, w, h);
            return painel;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.LIGHT_GRAY);
            for (int l = 0; l <= Maleta.LINHAS; l++) {
                int y = MARGEM + l * CELULA;
                g2.drawLine(MARGEM, y, MARGEM + Maleta.COLUNAS * CELULA, y);
            }
            for (int c = 0; c <= Maleta.COLUNAS; c++) {
                int x = MARGEM + c * CELULA;
                g2.drawLine(x, MARGEM, x, MARGEM + Maleta.LINHAS * CELULA);
            }
        }
    }

    /** Bloco visual de uma unidade de item dentro da maleta, no mesmo estilo simples das outras telas. */
    private static class BlocoItem extends JPanel {

        BlocoItem(Item item) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            // Nos blocos pequenos o nome não cabe inteiro; o tooltip mostra o nome completo.
            setToolTipText(item.getNome());

            JLabel label = new JLabel("<html><div style='text-align:center;'>" + item.getNome() + "</div></html>", SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }
}
