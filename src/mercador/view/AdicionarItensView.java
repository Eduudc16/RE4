package mercador.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
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
import mercador.controller.ResultadoOperacao;
import mercador.database.ArmaDAO;
import mercador.database.CategoriaDAO;
import mercador.database.ItemDAO;
import mercador.model.Arma;
import mercador.model.Categoria;
import mercador.model.Item;

public class AdicionarItensView extends JFrame {

    private final AdicionarItensController controller = new AdicionarItensController();
    private final ItemDAO itemDAO = new ItemDAO();
    private final ArmaDAO armaDAO = new ArmaDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private JTextField campoNome;
    private JTextField campoDescricao;
    private JTextField campoPreco;
    private JComboBox<Categoria> comboCategoria;
    private JPanel painelArma;
    private JTextField campoDano;
    private JTextField campoCapacidade;
    private JTextField campoRecarga;
    private JTextField campoPoderTiro;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JTable tabelaItens;
    private DefaultTableModel modeloTabela;

    public AdicionarItensView() {
        setTitle("Adicionar Itens");
        setSize(650, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(criarFormulario(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);

        atualizarPainelArma();
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

        painelArma = criarPainelArma();
        comboCategoria.addActionListener(e -> atualizarPainelArma());

        JPanel painelExterno = new JPanel(new BorderLayout());
        painelExterno.add(painel, BorderLayout.NORTH);
        painelExterno.add(painelArma, BorderLayout.CENTER);
        painelExterno.add(criarBotoes(), BorderLayout.SOUTH);
        return painelExterno;
    }

    /**
     * Atributos da arma (tabela `arma`). Só aparecem quando a categoria é "Arma":
     * sem eles a arma não existiria na tela Aprimorar.
     */
    private JPanel criarPainelArma() {
        campoDano = new JTextField();
        campoCapacidade = new JTextField();
        campoRecarga = new JTextField();
        campoPoderTiro = new JTextField();

        JPanel painel = new JPanel(new GridLayout(4, 2, 5, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Atributos da arma"));
        painel.add(new JLabel("Dano:"));
        painel.add(campoDano);
        painel.add(new JLabel("Capacidade:"));
        painel.add(campoCapacidade);
        painel.add(new JLabel("Velocidade de recarga:"));
        painel.add(campoRecarga);
        painel.add(new JLabel("Poder de tiro:"));
        painel.add(campoPoderTiro);
        return painel;
    }

    private boolean categoriaArmaSelecionada() {
        Categoria categoria = (Categoria) comboCategoria.getSelectedItem();
        return categoria != null && Categoria.ARMA.equals(categoria.getNome());
    }

    private void atualizarPainelArma() {
        painelArma.setVisible(categoriaArmaSelecionada());
        getContentPane().revalidate();
        getContentPane().repaint();
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
        TabelaUtil.estilizar(tabelaItens, new int[]{50, 250, 120, 120}, new int[]{3}, new int[]{0});
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
        preencherAtributosArma(item.getId());
    }

    /** Carrega os atributos da arma do item; ficam em branco se o item não for uma arma cadastrada. */
    private void preencherAtributosArma(int itemId) {
        Arma arma = armaDAO.buscarPorItemId(itemId);
        campoDano.setText(arma == null ? "" : String.valueOf(arma.getDano()));
        campoCapacidade.setText(arma == null ? "" : String.valueOf(arma.getCapacidade()));
        campoRecarga.setText(arma == null ? "" : String.valueOf(arma.getVelocidadeRecarga()));
        campoPoderTiro.setText(arma == null ? "" : String.valueOf(arma.getPoderTiro()));
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
        Arma atributos = null;
        if (categoriaArmaSelecionada()) {
            atributos = lerAtributosArma();
            if (atributos == null) {
                return;
            }
        }

        ResultadoOperacao resultado = controller.adicionarItem(
                dados.getNome(), dados.getDescricao(), dados.getPreco(), dados.getCategoriaId(), atributos);
        if (!resultado.isSucesso()) {
            JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        atualizarTabela();
        limparFormulario();
        JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), JOptionPane.INFORMATION_MESSAGE);
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
        Arma atributos = null;
        if (categoriaArmaSelecionada()) {
            atributos = lerAtributosArma();
            if (atributos == null) {
                return;
            }
        }

        ResultadoOperacao resultado = controller.editarItem(
                id, dados.getNome(), dados.getDescricao(), dados.getPreco(), dados.getCategoriaId(), atributos);
        if (!resultado.isSucesso()) {
            JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        atualizarTabela();
        limparFormulario();
        JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), JOptionPane.INFORMATION_MESSAGE);
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

        ResultadoOperacao resultado = controller.removerItem(id);
        if (!resultado.isSucesso()) {
            JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        atualizarTabela();
        limparFormulario();
        JOptionPane.showMessageDialog(this, resultado.getMensagem(), getTitle(), JOptionPane.INFORMATION_MESSAGE);
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
        if (preco < 0 || !Double.isFinite(preco)) {
            JOptionPane.showMessageDialog(this, "O preço não pode ser negativo (use 0 para item grátis).");
            return null;
        }

        return new Item(0, nome, campoDescricao.getText().trim(), preco, categoria.getId());
    }

    /**
     * Lê e valida os atributos da arma (só usado quando a categoria é "Arma").
     * Se algum estiver inválido, avisa o usuário e devolve null.
     */
    private Arma lerAtributosArma() {
        int dano;
        int capacidade;
        int recarga;
        int poderTiro;
        try {
            dano = Integer.parseInt(campoDano.getText().trim());
            capacidade = Integer.parseInt(campoCapacidade.getText().trim());
            recarga = Integer.parseInt(campoRecarga.getText().trim());
            poderTiro = Integer.parseInt(campoPoderTiro.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Dano, capacidade, recarga e poder de tiro devem ser números inteiros.");
            return null;
        }
        if (dano <= 0 || capacidade <= 0 || recarga <= 0 || poderTiro <= 0) {
            JOptionPane.showMessageDialog(this, "Dano, capacidade, recarga e poder de tiro devem ser maiores que zero.");
            return null;
        }

        return new Arma(0, 0, dano, capacidade, recarga, poderTiro);
    }

    private void limparFormulario() {
        tabelaItens.clearSelection();
        campoNome.setText("");
        campoDescricao.setText("");
        campoPreco.setText("");
        campoDano.setText("");
        campoCapacidade.setText("");
        campoRecarga.setText("");
        campoPoderTiro.setText("");
        if (comboCategoria.getItemCount() > 0) {
            comboCategoria.setSelectedIndex(0);
        }
    }
}
