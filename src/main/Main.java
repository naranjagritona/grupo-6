package main;

import javax.swing.SwingUtilities;

import paneles.Login_panel;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Login_panel login = new Login_panel();
            login.setVisible(true);
        });
    }
}