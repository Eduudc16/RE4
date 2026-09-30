package mercador.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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

    public AdicionarItensView() {
        setTitle("Adicionar Itens");
        setSize(650, 450);
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

        JButton btnAdicionar = new JButton("Adicionar Item");
        btnAdicionar.addActionListener(e -> {
            String nome = campoNome.getText();
            String descricao = campoDescricao.getText();
            Categoria categoria = (Categoria) comboCategoria.getSelectedItem();

            if (nome.isBlank() || campoPreco.getText().isBlank() || categoria == null) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                return;
            }

            double preco = Double.parseDouble(campoPreco.getText());
            controller.adicionarItem(nome, descricao, preco, categoria.getId());
            JOptionPane.showMessageDialog(this, "Funcionalidade de cadastro ainda será implementada.");
        });

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
        painelExterno.add(btnAdicionar, BorderLayout.SOUTH);
        return painelExterno;
    }

    private JScrollPane criarTabela() {
        String[] colunas = {"ID", "Nome", "Categoria", "Preço"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);

        List<Item> itens = itemDAO.listarTodos();
        for (Item item : itens) {
            modelo.addRow(new Object[]{item.getId(), item.getNome(), item.getCategoriaNome(), item.getPreco()});
        }

        return new JScrollPane(new JTable(modelo));
    }
}
