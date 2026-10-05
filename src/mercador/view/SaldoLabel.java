package mercador.view;

import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import mercador.controller.ComprarItensController;
import mercador.database.ClienteDAO;
import mercador.model.Cliente;

/**
 * Mostra o saldo atual do cliente no topo das telas de Comprar, Vender e
 * Aprimorar. A tela chama atualizar() depois de cada operação que mexe no
 * dinheiro do cliente.
 */
public class SaldoLabel extends JLabel {

    private static final NumberFormat FORMATO_MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public SaldoLabel() {
        setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        atualizar();
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
