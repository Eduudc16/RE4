package mercador.controller;

import java.sql.SQLException;
import java.time.LocalDate;

import mercador.database.ArmaDAO;
import mercador.database.ClienteDAO;
import mercador.database.ConexaoSQLite;
import mercador.database.InventarioClienteDAO;
import mercador.database.TransacaoDAO;
import mercador.database.UpgradeDAO;
import mercador.model.Arma;
import mercador.model.Cliente;
import mercador.model.Transacao;
import mercador.model.Upgrade;

public class AprimorarItensController {

    private final ArmaDAO armaDAO = new ArmaDAO();
    private final UpgradeDAO upgradeDAO = new UpgradeDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private final InventarioClienteDAO inventarioDAO = new InventarioClienteDAO();

    public ResultadoOperacao aplicarUpgrade(int armaId, int upgradeId) {
        Arma arma = armaDAO.buscarPorId(armaId);
        if (arma == null) {
            return ResultadoOperacao.erro("Arma não encontrada.");
        }

        Upgrade upgrade = upgradeDAO.buscarPorId(upgradeId);
        if (upgrade == null) {
            return ResultadoOperacao.erro("Upgrade não encontrado.");
        }

        if (upgrade.getArmaId() != armaId) {
            return ResultadoOperacao.erro("Esse upgrade não pertence a essa arma.");
        }

        if (upgrade.isAplicado()) {
            return ResultadoOperacao.erro("Esse upgrade já foi aplicado.");
        }

        int clienteId = ComprarItensController.CLIENTE_ATUAL_ID;

        int quantidadePossuida = inventarioDAO.buscarQuantidade(clienteId, arma.getItemId());
        if (quantidadePossuida <= 0) {
            return ResultadoOperacao.erro("Você não possui essa arma.");
        }

        Cliente cliente = clienteDAO.buscarPorId(clienteId);
        if (cliente == null) {
            return ResultadoOperacao.erro("Cliente não encontrado.");
        }

        if (cliente.getDinheiro() < upgrade.getCusto()) {
            return ResultadoOperacao.erro("Saldo insuficiente.");
        }

        if (!aplicarBonus(arma, upgrade)) {
            return ResultadoOperacao.erro("Tipo de upgrade desconhecido: " + upgrade.getTipo());
        }

        Transacao transacao = new Transacao(0, clienteId, arma.getItemId(), "upgrade", 1, upgrade.getCusto(), LocalDate.now().toString());

        try {
            ResultadoOperacao resultado = ConexaoSQLite.executarEmTransacao(conn -> {
                clienteDAO.atualizarSaldo(conn, clienteId, cliente.getDinheiro() - upgrade.getCusto());
                armaDAO.atualizar(conn, arma);
                upgradeDAO.marcarComoAplicado(conn, upgradeId);
                transacaoDAO.inserir(conn, transacao);
                return ResultadoOperacao.sucesso("Upgrade aplicado com sucesso.");
            });
            NotificadorDados.notificar();
            return resultado;
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacao.erro("Erro ao aplicar o upgrade. Nenhuma alteração foi salva.");
        }
    }

    /**
     * Soma o `nivel` do upgrade ao atributo da arma correspondente ao
     * `tipo`. Retorna false se o tipo não for reconhecido (nenhum atributo
     * é alterado nesse caso).
     */
    private boolean aplicarBonus(Arma arma, Upgrade upgrade) {
        switch (upgrade.getTipo()) {
            case "Dano":
                arma.setDano(arma.getDano() + upgrade.getNivel());
                return true;
            case "Capacidade":
                arma.setCapacidade(arma.getCapacidade() + upgrade.getNivel());
                return true;
            case "Velocidade de Recarga":
                arma.setVelocidadeRecarga(arma.getVelocidadeRecarga() + upgrade.getNivel());
                return true;
            case "Poder de Tiro":
                arma.setPoderTiro(arma.getPoderTiro() + upgrade.getNivel());
                return true;
            default:
                return false;
        }
    }
}
