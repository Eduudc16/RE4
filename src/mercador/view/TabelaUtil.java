package mercador.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Estilo comum das JTable das telas: altura de linha, cabeçalho em negrito,
 * linhas zebradas, larguras de coluna e formatação de valores em R$.
 * Só muda a apresentação — os valores no modelo continuam numéricos.
 */
public final class TabelaUtil {

    private static final NumberFormat FORMATO_MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private static final Color COR_LINHA_ALTERNADA = new Color(245, 245, 245);

    private TabelaUtil() {
    }

    /**
     * @param larguras  largura preferida de cada coluna, na ordem (0 = deixa o padrão)
     * @param colunasMoeda índices das colunas exibidas como R$
     * @param colunasCentro índices das colunas centralizadas (ex.: ID, quantidade)
     */
    public static void estilizar(JTable tabela, int[] larguras, int[] colunasMoeda, int[] colunasCentro) {
        tabela.setRowHeight(26);
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(new Color(224, 224, 224));
        tabela.setIntercellSpacing(new java.awt.Dimension(0, 1));
        tabela.setFillsViewportHeight(true);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabela.getTableHeader().setReorderingAllowed(false);

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setFont(cabecalho.getFont().deriveFont(Font.BOLD));
        cabecalho.setPreferredSize(new java.awt.Dimension(cabecalho.getPreferredSize().width, 28));

        tabela.setDefaultRenderer(Object.class, new RendererZebrado(SwingConstants.LEFT, false));

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            TableColumn coluna = tabela.getColumnModel().getColumn(i);
            if (i < larguras.length && larguras[i] > 0) {
                coluna.setPreferredWidth(larguras[i]);
            }
            if (contem(colunasMoeda, i)) {
                coluna.setCellRenderer(new RendererZebrado(SwingConstants.RIGHT, true));
            } else if (contem(colunasCentro, i)) {
                coluna.setCellRenderer(new RendererZebrado(SwingConstants.CENTER, false));
            }
        }
    }

    public static String formatarMoeda(double valor) {
        return FORMATO_MOEDA.format(valor);
    }

    private static boolean contem(int[] indices, int valor) {
        for (int i : indices) {
            if (i == valor) {
                return true;
            }
        }
        return false;
    }

    private static class RendererZebrado extends DefaultTableCellRenderer {

        private final int alinhamento;
        private final boolean moeda;

        RendererZebrado(int alinhamento, boolean moeda) {
            this.alinhamento = alinhamento;
            this.moeda = moeda;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            Object exibido = valor;
            if (moeda && valor instanceof Number numero) {
                exibido = formatarMoeda(numero.doubleValue());
            }
            JLabel label = (JLabel) super.getTableCellRendererComponent(tabela, exibido, selecionada, foco, linha, coluna);
            label.setHorizontalAlignment(alinhamento);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!selecionada) {
                label.setBackground(linha % 2 == 0 ? tabela.getBackground() : COR_LINHA_ALTERNADA);
            }
            return label;
        }
    }
}
