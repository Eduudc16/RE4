package mercador.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import mercador.controller.AprimorarItensController;
import mercador.controller.ResultadoOperacao;
import mercador.database.ArmaDAO;
import mercador.database.UpgradeDAO;
import mercador.model.Arma;
import mercador.model.Upgrade;

public class AprimorarItensView extends JFrame {

    private final AprimorarItensController controller = new AprimorarItensController();
    private final ArmaDAO armaDAO = new ArmaDAO();
    private final UpgradeDAO upgradeDAO = new UpgradeDAO();
    private final SaldoLabel labelSaldo = new SaldoLabel();
    private JTable tabelaArmas;
    private DefaultTableModel modeloArmas;
    private JTable tabelaUpgrades;
    private DefaultTableModel modeloUpgrades;

    public AprimorarItensView() {
        setTitle("Aprimorar Itens");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(labelSaldo, BorderLayout.NORTH);
        add(criarTabelas(), BorderLayout.CENTER);
        add(criarPainelInferior(), BorderLayout.SOUTH);

        AtualizacaoAutomatica.ligar(this, this::atualizarTabelaArmas);
    }

    private JPanel criarTabelas() {
        String[] colunasArmas = {"ID", "Arma", "Dano", "Capacidade", "Recarga", "Poder de Tiro"};
        modeloArmas = new DefaultTableModel(colunasArmas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabelaArmas = new JTable(modeloArmas);
        tabelaArmas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaArmas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarTabelaUpgrades();
            }
        });

        String[] colunasUpgrades = {"ID", "Tipo", "Nível", "Custo"};
        modeloUpgrades = new DefaultTableModel(colunasUpgrades, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabelaUpgrades = new JTable(modeloUpgrades);
        tabelaUpgrades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TabelaUtil.estilizar(tabelaArmas, new int[]{50, 200, 70, 90, 80, 110}, new int[]{}, new int[]{0, 2, 3, 4, 5});
        TabelaUtil.estilizar(tabelaUpgrades, new int[]{50, 250, 80, 120}, new int[]{3}, new int[]{0, 2});

        atualizarTabelaArmas();

        JScrollPane rolagemArmas = new JScrollPane(tabelaArmas);
        rolagemArmas.setBorder(BorderFactory.createTitledBorder("Armas"));
        JScrollPane rolagemUpgrades = new JScrollPane(tabelaUpgrades);
        rolagemUpgrades.setBorder(BorderFactory.createTitledBorder("Upgrades disponíveis da arma selecionada"));

        JPanel painel = new JPanel(new GridLayout(2, 1, 0, 10));
        painel.add(rolagemArmas);
        painel.add(rolagemUpgrades);
        return painel;
    }

    /**
     * Recarrega as armas e seleciona de novo a arma que estava selecionada,
     * para a lista de upgrades continuar aberta depois de um aprimoramento.
     */
    private void atualizarTabelaArmas() {
        int armaSelecionadaId = armaSelecionadaId();

        modeloArmas.setRowCount(0);
        List<Arma> armas = armaDAO.listarTodas();
        for (Arma arma : armas) {
            modeloArmas.addRow(new Object[]{
                    arma.getId(), arma.getItemNome(), arma.getDano(),
                    arma.getCapacidade(), arma.getVelocidadeRecarga(), arma.getPoderTiro()
            });
        }

        for (int linha = 0; linha < modeloArmas.getRowCount(); linha++) {
            if ((int) modeloArmas.getValueAt(linha, 0) == armaSelecionadaId) {
                tabelaArmas.setRowSelectionInterval(linha, linha);
                break;
            }
        }
    }

    private void atualizarTabelaUpgrades() {
        modeloUpgrades.setRowCount(0);
        int armaId = armaSelecionadaId();
        if (armaId == -1) {
            return;
        }

        List<Upgrade> upgrades = upgradeDAO.listarDisponiveisPorArma(armaId);
        for (Upgrade upgrade : upgrades) {
            modeloUpgrades.addRow(new Object[]{upgrade.getId(), upgrade.getTipo(), upgrade.getNivel(), upgrade.getCusto()});
        }
    }

    private int armaSelecionadaId() {
        int linha = tabelaArmas.getSelectedRow();
        return linha == -1 ? -1 : (int) tabelaArmas.getValueAt(linha, 0);
    }

    private JPanel criarPainelInferior() {
        JPanel painel = new JPanel();
        JButton btnAprimorar = new JButton("Aprimorar");

        btnAprimorar.addActionListener(e -> {
            int armaId = armaSelecionadaId();
            if (armaId == -1) {
                JOptionPane.showMessageDialog(this, "Selecione uma arma para aprimorar.");
                return;
            }
            if (modeloUpgrades.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Essa arma não tem upgrades disponíveis.");
                return;
            }
            int linhaUpgrade = tabelaUpgrades.getSelectedRow();
            if (linhaUpgrade == -1) {
                JOptionPane.showMessageDialog(this, "Selecione o upgrade que deseja aplicar.");
                return;
            }

            int upgradeId = (int) tabelaUpgrades.getValueAt(linhaUpgrade, 0);
            // A tabela e o saldo se atualizam sozinhos: o controller avisa as telas abertas (NotificadorDados).
            ResultadoOperacao resultado = controller.aplicarUpgrade(armaId, upgradeId);
            int tipoMensagem = resultado.isSucesso() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), tipoMensagem);
        });

        painel.add(btnAprimorar);
        return painel;
    }
}
