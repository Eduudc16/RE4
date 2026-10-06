package mercador.controller;

import java.sql.SQLException;

import mercador.database.ArmaDAO;
import mercador.database.CategoriaDAO;
import mercador.database.ConexaoSQLite;
import mercador.database.InventarioClienteDAO;
import mercador.database.ItemDAO;
import mercador.model.Arma;
import mercador.model.Categoria;
import mercador.model.Item;

public class AdicionarItensController {

    private final ItemDAO itemDAO = new ItemDAO();
    private final ArmaDAO armaDAO = new ArmaDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final InventarioClienteDAO inventarioDAO = new InventarioClienteDAO();

    /**
     * Cadastra um novo item. Se a categoria for "Arma", `atributos` (dano,
     * capacidade, recarga e poder de tiro) é obrigatório e a linha em `arma` é
     * gravada junto com o item, na mesma transação — sem ela a arma não
     * apareceria na tela Aprimorar. Para as outras categorias, `atributos` é
     * ignorado.
     */
    public ResultadoOperacao adicionarItem(String nome, String descricao, double preco, int categoriaId, Arma atributos) {
        Categoria categoria = categoriaDAO.buscarPorId(categoriaId);
        String erro = validar(nome, preco, categoria, atributos);
        if (erro != null) {
            return ResultadoOperacao.erro(erro);
        }

        Item item = new Item(0, nome.trim(), descricao == null ? "" : descricao.trim(), preco + 0.0, categoriaId);
        boolean ehArma = Categoria.ARMA.equals(categoria.getNome());

        try {
            ConexaoSQLite.executarEmTransacao(conn -> {
                int itemId = itemDAO.inserir(conn, item);
                if (itemId == -1) {
                    throw new SQLException("O item não foi inserido.");
                }
                if (ehArma) {
                    armaDAO.inserir(conn, comItemId(atributos, 0, itemId));
                }
                return itemId;
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacao.erro("Não foi possível cadastrar o item. Nenhuma alteração foi salva.");
        }

        NotificadorDados.notificar();
        return ResultadoOperacao.sucesso("Item cadastrado com sucesso.");
    }

    /**
     * Edita um item existente. Para uma arma, os atributos também são
     * atualizados (ou criados, se a arma ainda não tinha a linha em `arma`).
     *
     * Regras que protegem o inventário:
     * - um item que algum cliente possui não pode mudar de categoria, porque
     *   as regras da nova categoria (consumível, tamanho na maleta) passariam a
     *   valer para as unidades que ele já tem;
     * - uma arma não pode sair da categoria "Arma", porque a linha dela em
     *   `arma` (e seus upgrades) ficaria sem sentido.
     */
    public ResultadoOperacao editarItem(int id, String nome, String descricao, double preco, int categoriaId, Arma atributos) {
        if (id <= 0) {
            return ResultadoOperacao.erro("Item inválido.");
        }

        Item atual = itemDAO.buscarPorId(id);
        if (atual == null) {
            return ResultadoOperacao.erro("Item não encontrado.");
        }

        Categoria categoria = categoriaDAO.buscarPorId(categoriaId);
        String erro = validar(nome, preco, categoria, atributos);
        if (erro != null) {
            return ResultadoOperacao.erro(erro);
        }

        boolean ehArma = Categoria.ARMA.equals(categoria.getNome());
        Arma armaAtual = armaDAO.buscarPorItemId(id);

        if (atual.getCategoriaId() != categoriaId && inventarioDAO.algumClientePossui(id)) {
            return ResultadoOperacao.erro("Esse item está no inventário do cliente e não pode mudar de categoria. "
                    + "Venda-o antes de alterar a categoria.");
        }
        if (armaAtual != null && !ehArma) {
            return ResultadoOperacao.erro("Uma arma não pode sair da categoria " + Categoria.ARMA + ".");
        }

        Item item = new Item(id, nome.trim(), descricao == null ? "" : descricao.trim(), preco + 0.0, categoriaId);

        try {
            ConexaoSQLite.executarEmTransacao(conn -> {
                if (!itemDAO.atualizar(conn, item)) {
                    throw new SQLException("O item não foi atualizado.");
                }
                if (ehArma) {
                    if (armaAtual == null) {
                        armaDAO.inserir(conn, comItemId(atributos, 0, id));
                    } else {
                        armaDAO.atualizar(conn, comItemId(atributos, armaAtual.getId(), id));
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacao.erro("Não foi possível salvar as alterações do item. Nenhuma alteração foi salva.");
        }

        NotificadorDados.notificar();
        return ResultadoOperacao.sucesso("Item atualizado com sucesso.");
    }

    /**
     * Remove um item existente. Falha se o item já tiver transação ou estiver
     * no inventário, ou se for uma arma com upgrades (ver ItemDAO.deletar).
     * Uma arma sem upgrades é removida junto com a sua linha em `arma`.
     */
    public ResultadoOperacao removerItem(int id) {
        if (id <= 0) {
            return ResultadoOperacao.erro("Item inválido.");
        }
        if (!itemDAO.deletar(id)) {
            return ResultadoOperacao.erro("Não foi possível excluir o item.\n"
                    + "Itens que já tiveram transações, que estão no inventário do cliente "
                    + "ou que são armas com upgrades não podem ser excluídos.");
        }

        NotificadorDados.notificar();
        return ResultadoOperacao.sucesso("Item excluído com sucesso.");
    }

    /** Devolve a mensagem de erro dos dados do item, ou null se estiverem válidos. */
    private String validar(String nome, double preco, Categoria categoria, Arma atributos) {
        if (nome == null || nome.isBlank()) {
            return "Informe o nome do item.";
        }
        if (preco < 0 || !Double.isFinite(preco)) {
            return "O preço não pode ser negativo (use 0 para item grátis).";
        }
        if (categoria == null) {
            return "Categoria não encontrada.";
        }
        if (Categoria.ARMA.equals(categoria.getNome()) && !atributosValidos(atributos)) {
            return "Uma arma precisa de dano, capacidade, velocidade de recarga e poder de tiro maiores que zero.";
        }
        return null;
    }

    private boolean atributosValidos(Arma atributos) {
        return atributos != null
                && atributos.getDano() > 0
                && atributos.getCapacidade() > 0
                && atributos.getVelocidadeRecarga() > 0
                && atributos.getPoderTiro() > 0;
    }

    /** Copia os atributos para uma Arma ligada ao item, sem alterar o objeto recebido do chamador. */
    private Arma comItemId(Arma atributos, int armaId, int itemId) {
        return new Arma(armaId, itemId, atributos.getDano(), atributos.getCapacidade(),
                atributos.getVelocidadeRecarga(), atributos.getPoderTiro());
    }
}
