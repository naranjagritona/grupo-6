package main;

import paneles.Login_panel;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    Login_panel login = new Login_panel();
                    login.setVisible(true);
                });
    }
}
