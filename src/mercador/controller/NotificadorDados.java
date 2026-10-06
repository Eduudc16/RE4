package mercador.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Avisa as telas abertas de que os dados mudaram (compra, venda, upgrade,
 * cadastro de itens). Cada tela que mostra dados se registra e recarrega o que
 * exibe quando é avisada, então uma janela aberta nunca fica com valores
 * antigos depois de uma operação feita em outra janela.
 *
 * Os controllers chamam notificar() só depois do commit da operação.
 */
public final class NotificadorDados {

    private static final List<Runnable> OUVINTES = new CopyOnWriteArrayList<>();

    private NotificadorDados() {
    }

    public static void adicionarOuvinte(Runnable ouvinte) {
        OUVINTES.add(ouvinte);
    }

    public static void removerOuvinte(Runnable ouvinte) {
        OUVINTES.remove(ouvinte);
    }

    public static void notificar() {
        for (Runnable ouvinte : OUVINTES) {
            try {
                ouvinte.run();
            } catch (RuntimeException e) {
                // Uma tela com problema não pode impedir as outras de atualizar nem esconder o resultado da operação.
                e.printStackTrace();
            }
        }
    }
}
