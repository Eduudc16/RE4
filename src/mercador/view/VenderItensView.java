package mercador.view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import mercador.controller.VenderItensController;
import mercador.database.ItemDAO;
import mercador.model.Item;

public class VenderItensView extends JFrame {

    private final VenderItensController controller = new VenderItensController();
    private final ItemDAO itemDAO = new ItemDAO();
    private JTable tabelaItens;
    private JSpinner spinnerQuantidade;

    public VenderItensView() {
        setTitle("Vender Itens");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(criarTabela(), BorderLayout.CENTER);
        add(criarPainelInferior(), BorderLayout.SOUTH);
    }

    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome", "Categoria", "Preço"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);

        // TODO: substituir pela listagem do inventário do Cliente quando o CRUD estiver pronto
        List<Item> itens = itemDAO.listarTodos();
        for (Item item : itens) {
            modelo.addRow(new Object[]{item.getId(), item.getNome(), item.getCategoriaNome(), item.getPreco()});
        }

        tabelaItens = new JTable(modelo);
        return new JScrollPane(tabelaItens);
    }

    private JPanel criarPainelInferior() {
        JPanel painel = new JPanel();

        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JButton btnVender = new JButton("Vender");

        btnVender.addActionListener(e -> {
            int linha = tabelaItens.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um item para vender.");
                return;
            }
            int itemId = (int) tabelaItens.getValueAt(linha, 0);
            int quantidade = (int) spinnerQuantidade.getValue();
            controller.venderItem(itemId, quantidade);
            JOptionPane.showMessageDialog(this, "Funcionalidade de venda ainda será implementada.");
        });

        painel.add(spinnerQuantidade);
        painel.add(btnVender);
        return painel;
    }
}
