package mercador.view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import mercador.controller.AprimorarItensController;
import mercador.database.ArmaDAO;
import mercador.model.Arma;

public class AprimorarItensView extends JFrame {

    private final AprimorarItensController controller = new AprimorarItensController();
    private final ArmaDAO armaDAO = new ArmaDAO();
    private JTable tabelaArmas;

    public AprimorarItensView() {
        setTitle("Aprimorar Itens");
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(criarTabela(), BorderLayout.CENTER);
        add(criarPainelInferior(), BorderLayout.SOUTH);
    }

    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Arma", "Dano", "Capacidade", "Recarga", "Poder de Tiro"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);

        List<Arma> armas = armaDAO.listarTodas();
        for (Arma arma : armas) {
            modelo.addRow(new Object[]{
                    arma.getId(), arma.getItemNome(), arma.getDano(),
                    arma.getCapacidade(), arma.getVelocidadeRecarga(), arma.getPoderTiro()
            });
        }

        tabelaArmas = new JTable(modelo);
        return new JScrollPane(tabelaArmas);
    }

    private JPanel criarPainelInferior() {
        JPanel painel = new JPanel();
        JButton btnAprimorar = new JButton("Aprimorar");

        btnAprimorar.addActionListener(e -> {
            int linha = tabelaArmas.getSelectedRow();
            if (linha == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma arma para aprimorar.");
                return;
            }
            int armaId = (int) tabelaArmas.getValueAt(linha, 0);
            controller.aplicarUpgrade(armaId, 0);
            JOptionPane.showMessageDialog(this, "Funcionalidade de aprimoramento ainda será implementada.");
        });

        painel.add(btnAprimorar);
        return painel;
    }
}
