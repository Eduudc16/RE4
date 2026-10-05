package mercador.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import mercador.controller.AdicionarItensController;
import mercador.database.CategoriaDAO;
import mercador.database.ItemDAO;
import mercador.model.Categoria;
import mercador.model.Item;

public class AdicionarItensView extends JFrame {

    private final AdicionarItensController controller = new AdicionarItensController();
    private final ItemDAO itemDAO = new ItemDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private JTextField campoNome;
    private JTextField campoDescricao;
    private JTextField campoPreco;
    private JComboBox<Categoria> comboCategoria;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JTable tabelaItens;
    private DefaultTableModel modeloTabela;

    public AdicionarItensView() {
        setTitle("Adicionar Itens");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(criarFormulario(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);
    }

    private JPanel criarFormulario() {
        JPanel painel = new JPanel(new GridLayout(4, 2, 5, 5));

        campoNome = new JTextField();
        campoDescricao = new JTextField();
        campoPreco = new JTextField();
        comboCategoria = new JComboBox<>();
        for (Categoria categoria : categoriaDAO.listarTodas()) {
            comboCategoria.addItem(categoria);
        }

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(new JLabel("Descrição:"));
        painel.add(campoDescricao);
        painel.add(new JLabel("Preço:"));
        painel.add(campoPreco);
        painel.add(new JLabel("Categoria:"));
        painel.add(comboCategoria);

        JPanel painelExterno = new JPanel(new BorderLayout());
        painelExterno.add(painel, BorderLayout.CENTER);
        painelExterno.add(criarBotoes(), BorderLayout.SOUTH);
        return painelExterno;
    }

    private JPanel criarBotoes() {
        JButton btnAdicionar = new JButton("Adicionar Item");
        btnSalvar = new JButton("Salvar Alterações");
        btnExcluir = new JButton("Excluir Item");
        JButton btnLimpar = new JButton("Limpar");

        // Editar e excluir só ficam disponíveis com um item selecionado na tabela.
        btnSalvar.setEnabled(false);
        btnExcluir.setEnabled(false);

        btnAdicionar.addActionListener(e -> adicionarItem());
        btnSalvar.addActionListener(e -> salvarAlteracoes());
        btnExcluir.addActionListener(e -> excluirItem());
        btnLimpar.addActionListener(e -> limparFormulario());

        JPanel painel = new JPanel(new FlowLayout());
        painel.add(btnAdicionar);
        painel.add(btnSalvar);
        painel.add(btnExcluir);
        painel.add(btnLimpar);
        return painel;
    }

    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome", "Categoria", "Preço"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        atualizarTabela();

        tabelaItens = new JTable(modeloTabela);
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaItens.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });
        return new JScrollPane(tabelaItens);
    }

    private void atualizarTabela() {
        modeloTabela.setRowCount(0);
        List<Item> itens = itemDAO.listarTodos();
        for (Item item : itens) {
            modeloTabela.addRow(new Object[]{item.getId(), item.getNome(), item.getCategoriaNome(), item.getPreco()});
        }
    }

    /** Ao selecionar uma linha da tabela, carrega o item no formulário para editar ou excluir. */
    private void preencherFormularioComSelecao() {
        int id = itemSelecionadoId();
        Item item = id == -1 ? null : itemDAO.buscarPorId(id);
        btnSalvar.setEnabled(item != null);
        btnExcluir.setEnabled(item != null);
        if (item == null) {
            return;
        }

        campoNome.setText(item.getNome());
        campoDescricao.setText(item.getDescricao());
        campoPreco.setText(BigDecimal.valueOf(item.getPreco()).stripTrailingZeros().toPlainString());
        for (int i = 0; i < comboCategoria.getItemCount(); i++) {
            if (comboCategoria.getItemAt(i).getId() == item.getCategoriaId()) {
                comboCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    private int itemSelecionadoId() {
        int linha = tabelaItens.getSelectedRow();
        return linha == -1 ? -1 : (int) tabelaItens.getValueAt(linha, 0);
    }

    private void adicionarItem() {
        Item dados = lerFormulario();
        if (dados == null) {
            return;
        }

        int id = controller.adicionarItem(dados.getNome(), dados.getDescricao(), dados.getPreco(), dados.getCategoriaId());
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Não foi possível cadastrar o item.", getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        atualizarTabela();
        limparFormulario();
        JOptionPane.showMessageDialog(this, "Item cadastrado com sucesso.", getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void salvarAlteracoes() {
        int id = itemSelecionadoId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Selecione na tabela o item que deseja editar.");
            return;
        }
        Item dados = lerFormulario();
        if (dados == null) {
            return;
        }

        if (!controller.editarItem(id, dados.getNome(), dados.getDescricao(), dados.getPreco(), dados.getCategoriaId())) {
            JOptionPane.showMessageDialog(this, "Não foi possível salvar as alterações do item.", getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        atualizarTabela();
        limparFormulario();
        JOptionPane.showMessageDialog(this, "Item atualizado com sucesso.", getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void excluirItem() {
        int id = itemSelecionadoId();
        if (id == -1) {
            JOptionPane.showMessageDialog(this, "Selecione na tabela o item que deseja excluir.");
            return;
        }

        String nome = (String) tabelaItens.getValueAt(tabelaItens.getSelectedRow(), 1);
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Excluir o item \"" + nome + "\"? Essa ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        if (!controller.removerItem(id)) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível excluir o item.\n"
                            + "Itens ligados a uma arma, a transações ou ao inventário do cliente não podem ser excluídos.",
                    getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        atualizarTabela();
        limparFormulario();
        JOptionPane.showMessageDialog(this, "Item excluído com sucesso.", getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lê e valida os campos do formulário. Se algum estiver inválido, avisa o
     * usuário e devolve null.
     */
    private Item lerFormulario() {
        String nome = campoNome.getText().trim();
        Categoria categoria = (Categoria) comboCategoria.getSelectedItem();
        if (nome.isEmpty() || campoPreco.getText().isBlank() || categoria == null) {
            JOptionPane.showMessageDialog(this, "Preencha nome, preço e categoria.");
            return null;
        }

        double preco;
        try {
            preco = Double.parseDouble(campoPreco.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Use só números, ex.: 1500 ou 1500,50.");
            return null;
        }
        if (preco <= 0 || !Double.isFinite(preco)) {
            JOptionPane.showMessageDialog(this, "O preço deve ser um número maior que zero.");
            return null;
        }

        return new Item(0, nome, campoDescricao.getText().trim(), preco, categoria.getId());
    }

    private void limparFormulario() {
        tabelaItens.clearSelection();
        campoNome.setText("");
        campoDescricao.setText("");
        campoPreco.setText("");
        if (comboCategoria.getItemCount() > 0) {
            comboCategoria.setSelectedIndex(0);
        }
    }
}
