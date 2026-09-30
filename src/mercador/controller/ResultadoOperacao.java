package mercador.controller;

/**
 * Resultado de uma operação de controller (compra, venda, upgrade): indica
 * se deu certo e traz uma mensagem clara para exibir ao usuário.
 */
public class ResultadoOperacao {

    private final boolean sucesso;
    private final String mensagem;

    private ResultadoOperacao(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public static ResultadoOperacao sucesso(String mensagem) {
        return new ResultadoOperacao(true, mensagem);
    }

    public static ResultadoOperacao erro(String mensagem) {
        return new ResultadoOperacao(false, mensagem);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }
}
