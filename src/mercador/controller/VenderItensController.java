package mercador.controller;

import java.sql.SQLException;
import java.time.LocalDate;

import mercador.database.ClienteDAO;
import mercador.database.ConexaoSQLite;
import mercador.database.InventarioClienteDAO;
import mercador.database.ItemDAO;
import mercador.database.TransacaoDAO;
import mercador.model.Cliente;
import mercador.model.Item;
import mercador.model.Transacao;

public class VenderItensController {

    // O mercador paga a metade do preço de catálogo ao comprar itens de volta do cliente.
    private static final double FATOR_VENDA = 0.5;

    private final ItemDAO itemDAO = new ItemDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private final InventarioClienteDAO inventarioDAO = new InventarioClienteDAO();

    public ResultadoOperacao venderItem(int itemId, int quantidade) {
        if (quantidade <= 0) {
            return ResultadoOperacao.erro("Quantidade deve ser maior que zero.");
        }

        Item item = itemDAO.buscarPorId(itemId);
        if (item == null) {
            return ResultadoOperacao.erro("Item não encontrado.");
        }

        int clienteId = ComprarItensController.CLIENTE_ATUAL_ID;
        Cliente cliente = clienteDAO.buscarPorId(clienteId);
        if (cliente == null) {
            return ResultadoOperacao.erro("Cliente não encontrado.");
        }

        int quantidadePossuida = inventarioDAO.buscarQuantidade(clienteId, itemId);
        if (quantidadePossuida <= 0) {
            return ResultadoOperacao.erro("Você não possui esse item.");
        }
        if (quantidade > quantidadePossuida) {
            return ResultadoOperacao.erro("Quantidade maior do que você tem.");
        }

        double valorVenda = item.getPreco() * FATOR_VENDA * quantidade;
        Transacao transacao = new Transacao(0, clienteId, itemId, "venda", quantidade, valorVenda, LocalDate.now().toString());

        try {
            ResultadoOperacao resultado = ConexaoSQLite.executarEmTransacao(conn -> {
                clienteDAO.atualizarSaldo(conn, clienteId, cliente.getDinheiro() + valorVenda);
                transacaoDAO.inserir(conn, transacao);
                inventarioDAO.subtrairQuantidade(conn, clienteId, itemId, quantidade);
                return ResultadoOperacao.sucesso("Venda realizada com sucesso.");
            });
            NotificadorDados.notificar();
            return resultado;
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacao.erro("Erro ao processar a venda. Nenhuma alteração foi salva.");
        }
    }
}
