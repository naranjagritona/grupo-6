package main;

import javax.swing.SwingUtilities;

import paneles.LoginFrame;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}