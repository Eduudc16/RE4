package mercador.view;

import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import mercador.controller.ComprarItensController;
import mercador.controller.NotificadorDados;
import mercador.database.ClienteDAO;
import mercador.model.Cliente;

/**
 * Mostra o saldo atual do cliente no topo das telas de Comprar, Vender e
 * Aprimorar. Ele se atualiza sozinho (via NotificadorDados) depois de cada
 * operação que mexe no dinheiro do cliente, inclusive as feitas em outra janela.
 */
public class SaldoLabel extends JLabel {

    private static final NumberFormat FORMATO_MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private final ClienteDAO clienteDAO = new ClienteDAO();
    // Guardado num campo para remover da lista exatamente o mesmo objeto que foi registrado.
    private final Runnable aoAlterarDados = this::atualizar;

    public SaldoLabel() {
        setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        atualizar();
    }

    // Enquanto a janela está aberta, o saldo se atualiza sozinho quando outra tela mexer no dinheiro.
    @Override
    public void addNotify() {
        super.addNotify();
        NotificadorDados.adicionarOuvinte(aoAlterarDados);
    }

    @Override
    public void removeNotify() {
        NotificadorDados.removerOuvinte(aoAlterarDados);
        super.removeNotify();
    }

    public void atualizar() {
        Cliente cliente = clienteDAO.buscarPorId(ComprarItensController.CLIENTE_ATUAL_ID);
        if (cliente == null) {
            setText("Saldo: indisponível");
            return;
        }
        setText("Saldo: " + FORMATO_MOEDA.format(cliente.getDinheiro()));
    }
}
