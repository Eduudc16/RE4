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

import mercador.controller.ComprarItensController;
import mercador.controller.ResultadoOperacao;
import mercador.database.ItemDAO;
import mercador.model.Item;

public class ComprarItensView extends JFrame {

    private final ComprarItensController controller = new ComprarItensController();
    private final ItemDAO itemDAO = new ItemDAO();
    private final SaldoLabel labelSaldo = new SaldoLabel();
    private JTable tabelaItens;
    private DefaultTableModel modeloTabela;
    private JSpinner spinnerQuantidade;

    public ComprarItensView() {
        setTitle("Comprar Itens");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(labelSaldo, BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);
        add(criarPainelInferior(), BorderLayout.SOUTH);
    }

    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome", "Categoria", "Preço"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        atualizarTabela();

        tabelaItens = new JTable(modeloTabela);
        return new JScrollPane(tabelaItens);
    }

    private void atualizarTabela() {
        modeloTabela.setRowCount(0);
        List<Item> itens = itemDAO.listarTodos();
        for (Item item : itens) {
            modeloTabela.addRow(new Object[]{item.getId(), item.getNome(), item.getCategoriaNome(), item.getPreco()});
        }
    }

    private JPanel criarPainelInferior() {
        JPanel painel = new JPanel();

        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JButton btnComprar = new JButton("Comprar");

        btnComprar.addActionListener(e -> {
            int linha = tabelaItens.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione um item para comprar.");
                return;
            }
            int itemId = (int) tabelaItens.getValueAt(linha, 0);
            int quantidade = (int) spinnerQuantidade.getValue();
            ResultadoOperacao resultado = controller.comprarItem(itemId, quantidade);
            if (resultado.isSucesso()) {
                atualizarTabela();
                labelSaldo.atualizar();
            }
            int tipoMensagem = resultado.isSucesso() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), tipoMensagem);
        });

        painel.add(spinnerQuantidade);
        painel.add(btnComprar);
        return painel;
    }
}
