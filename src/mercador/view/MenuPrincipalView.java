package mercador.view;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;

public class MenuPrincipalView extends JFrame {

    public MenuPrincipalView() {
        setTitle("O Mercador");
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JButton btnComprar = new JButton("Comprar Itens");
        JButton btnVender = new JButton("Vender Itens");
        JButton btnAprimorar = new JButton("Aprimorar Itens");
        JButton btnAdicionar = new JButton("Adicionar Itens");

        btnComprar.addActionListener(e -> new ComprarItensView().setVisible(true));
        btnVender.addActionListener(e -> new VenderItensView().setVisible(true));
        btnAprimorar.addActionListener(e -> new AprimorarItensView().setVisible(true));
        btnAdicionar.addActionListener(e -> new AdicionarItensView().setVisible(true));

        add(btnComprar);
        add(btnVender);
        add(btnAprimorar);
        add(btnAdicionar);
    }
}
