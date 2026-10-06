package mercador.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import mercador.controller.NotificadorDados;

/** Faz uma janela recarregar seus dados sempre que outra tela alterar algo, e para de ouvir quando ela fecha. */
final class AtualizacaoAutomatica {

    private AtualizacaoAutomatica() {
    }

    static void ligar(JFrame janela, Runnable recarregar) {
        NotificadorDados.adicionarOuvinte(recarregar);
        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                NotificadorDados.removerOuvinte(recarregar);
            }
        });
    }
}
