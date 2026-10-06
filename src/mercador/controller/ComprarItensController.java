package mercador.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import mercador.database.ClienteDAO;
import mercador.database.ConexaoSQLite;
import mercador.database.InventarioClienteDAO;
import mercador.database.ItemDAO;
import mercador.database.TransacaoDAO;
import mercador.model.Cliente;
import mercador.model.Item;
import mercador.model.ItemInventario;
import mercador.model.Maleta;
import mercador.model.Transacao;

public class ComprarItensController {

    // Não existe login/sessão no projeto ainda. Enquanto isso não existir,
    // todas as operações são feitas em nome deste cliente fixo.
    public static final int CLIENTE_ATUAL_ID = 1;

    private final ItemDAO itemDAO = new ItemDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private final InventarioClienteDAO inventarioDAO = new InventarioClienteDAO();

    public ResultadoOperacao comprarItem(int itemId, int quantidade) {
        if (quantidade <= 0) {
            return ResultadoOperacao.erro("Quantidade deve ser maior que zero.");
        }

        Item item = itemDAO.buscarPorId(itemId);
        if (item == null) {
            return ResultadoOperacao.erro("Item não encontrado.");
        }

        if (!item.isConsumivel()) {
            if (quantidade > 1) {
                return ResultadoOperacao.erro("Só é possível comprar uma unidade de " + item.getNome() + " por vez.");
            }
            if (inventarioDAO.buscarQuantidade(CLIENTE_ATUAL_ID, itemId) > 0) {
                return ResultadoOperacao.erro("Você já possui " + item.getNome() + ". Apenas consumíveis podem ser comprados mais de uma vez.");
            }
        }

        // Mesma conta que a tela da maleta usa para desenhar o inventário (ver Maleta.organizar).
        List<ItemInventario> inventarioAtual = inventarioDAO.listarPorCliente(CLIENTE_ATUAL_ID);
        if (!Maleta.cabe(inventarioAtual, item, quantidade)) {
            int cabem = Maleta.quantasCabem(inventarioAtual, item, quantidade);
            if (cabem == 0) {
                return ResultadoOperacao.erro("Maleta cheia: não há espaço para " + item.getNome() + ". Venda itens para liberar espaço.");
            }
            return ResultadoOperacao.erro("Não há espaço na maleta para " + quantidade + " unidades de " + item.getNome()
                    + ". Cabem apenas " + cabem + ".");
        }

        Cliente cliente = clienteDAO.buscarPorId(CLIENTE_ATUAL_ID);
        if (cliente == null) {
            return ResultadoOperacao.erro("Cliente não encontrado.");
        }

        double valorTotal = item.getPreco() * quantidade;
        if (cliente.getDinheiro() < valorTotal) {
            return ResultadoOperacao.erro("Saldo insuficiente.");
        }

        Transacao transacao = new Transacao(0, CLIENTE_ATUAL_ID, itemId, "compra", quantidade, valorTotal, LocalDate.now().toString());

        try {
            ResultadoOperacao resultado = ConexaoSQLite.executarEmTransacao(conn -> {
                clienteDAO.atualizarSaldo(conn, CLIENTE_ATUAL_ID, cliente.getDinheiro() - valorTotal);
                transacaoDAO.inserir(conn, transacao);
                inventarioDAO.adicionarQuantidade(conn, CLIENTE_ATUAL_ID, itemId, quantidade);
                return ResultadoOperacao.sucesso("Compra realizada com sucesso.");
            });
            NotificadorDados.notificar();
            return resultado;
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacao.erro("Erro ao processar a compra. Nenhuma alteração foi salva.");
        }
    }
}
