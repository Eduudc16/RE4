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
import mercador.controller.VenderItensController;
import mercador.database.InventarioClienteDAO;
import mercador.model.ItemInventario;

public class VenderItensView extends JFrame {

    private final VenderItensController controller = new VenderItensController();
    private final InventarioClienteDAO inventarioDAO = new InventarioClienteDAO();
    private JTable tabelaItens;
    private DefaultTableModel modeloTabela;
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
        String[] colunas = {"ID", "Nome", "Categoria", "Preço", "Quantidade"};
        modeloTabela = new DefaultTableModel(colunas, 0);
        atualizarTabela();

        tabelaItens = new JTable(modeloTabela);
        return new JScrollPane(tabelaItens);
    }

    private void atualizarTabela() {
        modeloTabela.setRowCount(0);
        List<ItemInventario> itens = inventarioDAO.listarPorCliente(ComprarItensController.CLIENTE_ATUAL_ID);
        for (ItemInventario itemInv : itens) {
            modeloTabela.addRow(new Object[]{
                    itemInv.getItem().getId(),
                    itemInv.getItem().getNome(),
                    itemInv.getItem().getCategoriaNome(),
                    itemInv.getItem().getPreco(),
                    itemInv.getQuantidade()
            });
        }
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
