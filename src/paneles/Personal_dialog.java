package paneles;

import clases.Personal;
import clases.Usuario;
import servicios.Personal_servicio;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import static paneles.Ficha_Inscripcion_dialog.*;

// registramos o modificamos los datos del personal desde el módulo de administración
public class Personal_dialog extends JDialog {
    public Personal_dialog(Window padre, Usuario usuario, Personal.Tipo seleccionado, Map<String, Object> original, Runnable actualizar) {
        super(padre, original == null ? "Registrar personal" : "Modificar personal", ModalityType.APPLICATION_MODAL);
        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JComboBox<Personal.Tipo> tipo = new JComboBox<>(Personal.Tipo.values());
        if (seleccionado != null) tipo.setSelectedItem(seleccionado);
        tipo.setEnabled(original == null);
        JTextField nombre = new JTextField(), apellido = new JTextField(), dni = new JTextField(), mail = new JTextField(), cuenta = new JTextField();
        JPasswordField clave = new JPasswordField();
        JTextField legajo = new JTextField("Se genera al guardar"); legajo.setEditable(false);
        JComboBox<String> estado = new JComboBox<>(new String[] {"Activo", "Inactivo"});
        if (original != null) {
            nombre.setText(original.get("nombre").toString()); apellido.setText(original.get("apellido").toString());
            dni.setText(original.get("dni").toString()); mail.setText(original.get("mail").toString());
            cuenta.setText(original.get("nombre_usuario").toString()); cuenta.setEditable(false);
            legajo.setText(original.get("legajo").toString()); estado.setSelectedItem(original.get("estado"));
        }
        Object[][] campos = {{"Perfil", tipo}, {"Nombre", nombre}, {"Apellido", apellido}, {"DNI", dni}, {"Mail", mail}, {"Legajo", legajo}, {"Usuario", cuenta}, {"Estado", estado}};
        for (Object[] campo : campos) {
            JLabel etiqueta = new JLabel(campo[0].toString()); etiqueta.setFont(new Font("Tahoma", Font.PLAIN, 13));
            formulario.add(etiqueta); JComponent entrada = (JComponent) campo[1]; entrada.setFont(new Font("Tahoma", Font.PLAIN, 13)); formulario.add(entrada);
        }
        if (original == null) { formulario.add(new JLabel("Contraseña")); formulario.add(clave); }
        add(formulario, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); acciones.setBackground(Color.WHITE);
        JButton guardar = boton("Guardar"), cerrar = boton("Cerrar");
        guardar.addActionListener(e -> {
            Personal persona = new Personal();
            persona.setTipo((Personal.Tipo) tipo.getSelectedItem());
            persona.setNombre(nombre.getText()); persona.setApellido(apellido.getText()); persona.setDni(dni.getText()); persona.setMail(mail.getText());
            persona.setUsuario(cuenta.getText()); persona.setContraseña(new String(clave.getPassword())); persona.setEstado(estado.getSelectedItem().toString());
            if (original != null) persona.setId(((Number) original.get("id")).intValue());
            guardar.setEnabled(false);
            new SwingWorker<String, Void>() {
                protected String doInBackground() throws Exception {
                    Personal_servicio servicio = new Personal_servicio();
                    if (original == null) return servicio.registrar(persona, usuario.getId_usuario());
                    servicio.modificar(persona, usuario.getId_usuario()); return legajo.getText();
                }
                protected void done() {
                    guardar.setEnabled(true);
                    try {
                        String generado = get(); clave.setText("");
                        JOptionPane.showMessageDialog(Personal_dialog.this, "Datos guardados. Legajo: " + generado);
                        actualizar.run(); dispose();
                    } catch (Exception error) {
                        Throwable motivo = error.getCause() == null ? error : error.getCause();
                        JOptionPane.showMessageDialog(Personal_dialog.this, motivo.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }.execute();
        });
        cerrar.addActionListener(e -> dispose()); acciones.add(guardar); acciones.add(cerrar); add(acciones, BorderLayout.SOUTH);
        setSize(560, original == null ? 470 : 430); setLocationRelativeTo(padre);
    }
}
