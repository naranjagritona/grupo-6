package paneles;

import clases.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtContraseña;
    private JButton btnIngresar;

    //constructor vacio necesario para que el diseñador visual windowbuilder cargue la vista sin lanzar excepciones
    public LoginFrame() {
        setTitle("Academia Integral XXI - Iniciar Sesión");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponentes();
    }

    private void initComponentes() {

        //el gridbaglayout organiza los elementos en una cuadricula centrada dentro del panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 244, 248));

        JLabel lblTitulo = new JLabel("ACCESO AL SISTEMA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(30, 58, 138));
        
        GridBagConstraints gbc_lblTitulo = new GridBagConstraints();
        gbc_lblTitulo.insets = new Insets(8, 8, 8, 8);
        gbc_lblTitulo.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblTitulo.gridx = 0; 
        gbc_lblTitulo.gridy = 0; 
        gbc_lblTitulo.gridwidth = 2;
        panel.add(lblTitulo, gbc_lblTitulo);

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setForeground(new Color(51, 65, 85));
        
        GridBagConstraints gbc_lblUser = new GridBagConstraints();
        gbc_lblUser.insets = new Insets(8, 8, 8, 8);
        gbc_lblUser.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblUser.gridx = 0; 
        gbc_lblUser.gridy = 1;
        panel.add(lblUser, gbc_lblUser);

        txtUsuario = new JTextField(15);
        
        GridBagConstraints gbc_txtUsuario = new GridBagConstraints();
        gbc_txtUsuario.insets = new Insets(8, 8, 8, 8);
        gbc_txtUsuario.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtUsuario.gridx = 1; 
        gbc_txtUsuario.gridy = 1;
        panel.add(txtUsuario, gbc_txtUsuario);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(new Color(51, 65, 85));
        
        GridBagConstraints gbc_lblPass = new GridBagConstraints();
        gbc_lblPass.insets = new Insets(8, 8, 8, 8);
        gbc_lblPass.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblPass.gridx = 0; 
        gbc_lblPass.gridy = 2;
        panel.add(lblPass, gbc_lblPass);

        txtContraseña = new JPasswordField(15);
        
        GridBagConstraints gbc_txtContraseña = new GridBagConstraints();
        gbc_txtContraseña.insets = new Insets(8, 8, 8, 8);
        gbc_txtContraseña.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtContraseña.gridx = 1; 
        gbc_txtContraseña.gridy = 2;
        panel.add(txtContraseña, gbc_txtContraseña);

        btnIngresar = new JButton("Ingresar");
        btnIngresar.setBackground(new Color(37, 99, 235));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        
        GridBagConstraints gbc_btnIngresar = new GridBagConstraints();
        gbc_btnIngresar.insets = new Insets(8, 8, 8, 8);
        gbc_btnIngresar.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnIngresar.gridx = 0; 
        gbc_btnIngresar.gridy = 3; 
        gbc_btnIngresar.gridwidth = 2;
        panel.add(btnIngresar, gbc_btnIngresar);

        add(panel);

        //configuracion del evento de autenticacion al hacer clic en el boton
        btnIngresar.addActionListener(e -> realizarLogin());
    }

    private void realizarLogin() {
        String user = txtUsuario.getText().trim();
        String pass = new String(txtContraseña.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuarioLogueado = Usuario.autenticar(user, pass);

        if (usuarioLogueado != null) {
            JOptionPane.showMessageDialog(this, "¡Bienvenido, " + usuarioLogueado.getNombre_usuario() + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            AdminFrame adminFrame = new AdminFrame(usuarioLogueado);
            adminFrame.setVisible(true);

            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de autenticación", JOptionPane.ERROR_MESSAGE);
            txtContraseña.setText("");
        }
    }
}