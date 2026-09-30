package mercador.controller;

import mercador.database.ItemDAO;
import mercador.model.Item;

public class AdicionarItensController {

    private final ItemDAO itemDAO = new ItemDAO();

    /**
     * Cadastra um novo item. Retorna o id gerado em caso de sucesso,
     * ou -1 se algum campo for inválido ou a inserção falhar.
     *
     * Observação: a tela atual (AdicionarItensView) não coleta atributos
     * de arma (dano, capacidade, velocidade de recarga, poder de tiro),
     * então este método insere apenas em `item`. A criação da linha em
     * `arma` para itens da categoria "Arma" depende de a tela passar a
     * coletar esses campos (fora do escopo deste card).
     */
    public int adicionarItem(String nome, String descricao, double preco, int categoriaId) {
        if (nome == null || nome.isBlank()) {
            return -1;
        }
        if (preco <= 0) {
            return -1;
        }
        if (categoriaId <= 0) {
            return -1;
        }

        Item item = new Item(0, nome.trim(), descricao == null ? "" : descricao.trim(), preco, categoriaId);
        return itemDAO.inserir(item);
    }

    /**
     * Edita um item existente. Retorna true em caso de sucesso, false se
     * algum campo for inválido ou o item não existir.
     */
    public boolean editarItem(int id, String nome, String descricao, double preco, int categoriaId) {
        if (id <= 0) {
            return false;
        }
        if (nome == null || nome.isBlank()) {
            return false;
        }
        if (preco <= 0) {
            return false;
        }
        if (categoriaId <= 0) {
            return false;
        }

        Item item = new Item(id, nome.trim(), descricao == null ? "" : descricao.trim(), preco, categoriaId);
        return itemDAO.atualizar(item);
    }

    /**
     * Remove um item existente. Retorna false se o item não existir ou se
     * houver arma, transação ou inventário dependente (ver ItemDAO.deletar).
     */
    public boolean removerItem(int id) {
        if (id <= 0) {
            return false;
        }
        return itemDAO.deletar(id);
    }
}
