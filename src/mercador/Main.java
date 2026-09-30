package mercador;

import javax.swing.SwingUtilities;

import mercador.database.ConexaoSQLite;
import mercador.view.MenuPrincipalView;

public class Main {
    public static void main(String[] args) {
        ConexaoSQLite.inicializarBanco();
        SwingUtilities.invokeLater(() -> new MenuPrincipalView().setVisible(true));
    }
}
