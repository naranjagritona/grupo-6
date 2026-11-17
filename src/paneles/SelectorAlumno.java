package paneles;

import dto.ItemCombo;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;

import javax.swing.*;
import javax.swing.event.*;

// filtramos las opciones cargadas sin consultar la base por cada letra y conservamos el ID elegido
public class SelectorAlumno extends JComboBox<ItemCombo> {
    private static final long serialVersionUID = 1L;
    private List<ItemCombo> opciones = List.of();
    private boolean actualizando;
    private final JTextField entrada;
    private final Timer espera;
    private ItemCombo elegido;

    public SelectorAlumno() {
        setEditable(true);
        setEditor(
                new javax.swing.plaf.basic.BasicComboBoxEditor() {
                    @Override
                    public void setItem(Object item) {
                        super.setItem(
                                item instanceof ItemCombo alumno ? nombreVisible(alumno) : item);
                    }
                });
        entrada = (JTextField) getEditor().getEditorComponent();
        entrada.getAccessibleContext()
                .setAccessibleName("Alumno: buscar por nombre, DNI o matrícula");
        entrada.setToolTipText("Escribí y elegí un alumno de las sugerencias.");
        JTextArea sugerencia = new JTextArea();
        sugerencia.setLineWrap(true);
        sugerencia.setWrapStyleWord(true);
        sugerencia.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        setRenderer(
                (lista, alumno, indice, seleccionado, foco) -> {
                    String texto = alumno == null ? "" : alumno.toString();
                    int separador = texto.indexOf(" · ");
                    sugerencia.setText(
                            separador < 0
                                    ? texto
                                    : texto.substring(0, separador)
                                            + "\n"
                                            + texto.substring(separador + 3));
                    sugerencia.setFont(lista.getFont());
                    sugerencia.setBackground(
                            seleccionado ? lista.getSelectionBackground() : lista.getBackground());
                    sugerencia.setForeground(
                            seleccionado ? lista.getSelectionForeground() : lista.getForeground());
                    sugerencia.setSize(Math.max(240, getWidth() - 24), Short.MAX_VALUE);
                    return sugerencia;
                });
        espera = new Timer(180, e -> filtrar());
        espera.setRepeats(false);
        entrada.getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            private void cambiar() {
                                if (!actualizando) {
                                    elegido = null;
                                    espera.restart();
                                    SwingUtilities.invokeLater(
                                            () -> {
                                                if (!actualizando && elegido == null)
                                                    fireActionEvent();
                                            });
                                }
                            }

                            public void insertUpdate(DocumentEvent e) {
                                cambiar();
                            }

                            public void removeUpdate(DocumentEvent e) {
                                cambiar();
                            }

                            public void changedUpdate(DocumentEvent e) {
                                cambiar();
                            }
                        });
        setSelectedIndex(-1);
    }

    // normalizamos tildes y mayúsculas para buscar nombres y documentos con el mismo criterio
    public static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT)
                .trim();
    }

    public static List<ItemCombo> coincidencias(List<ItemCombo> opciones, String texto) {
        String filtro = normalizar(texto);
        String[] palabras = filtro.split("\\s+");
        return opciones.stream()
                .filter(
                        i -> {
                            String alumno = normalizar(i.toString());
                            return java.util.Arrays.stream(palabras).allMatch(alumno::contains);
                        })
                .limit(30)
                .toList();
    }

    public void setOpciones(List<ItemCombo> opciones) {
        this.opciones = List.copyOf(opciones);
        limpiarSeleccion();
    }

    // cambiar el texto nunca selecciona por sí solo el primer resultado de la búsqueda
    private void filtrar() {
        if (!isEnabled()) return;
        String texto = entrada.getText();
        int cursor = entrada.getCaretPosition();
        actualizando = true;
        try {
            setModel(
                    new DefaultComboBoxModel<>(
                            coincidencias(opciones, texto).toArray(ItemCombo[]::new)));
            super.setSelectedItem(null);
            entrada.setText(texto);
            entrada.setCaretPosition(Math.min(cursor, texto.length()));
        } finally {
            actualizando = false;
        }
        if (isShowing() && entrada.hasFocus() && getItemCount() > 0) showPopup();
    }

    // una ficha puede mostrar un alumno inactivo sin ofrecerlo como opción para una inscripción
    // nueva
    public void seleccionar(ItemCombo alumno) {
        espera.stop();
        actualizando = true;
        try {
            elegido = alumno;
            super.setSelectedItem(alumno);
            getEditor().setItem(alumno);
        } finally {
            actualizando = false;
        }
        mostrarSeleccion();
        fireActionEvent();
    }

    public void limpiarSeleccion() {
        espera.stop();
        actualizando = true;
        try {
            elegido = null;
            super.setSelectedItem(null);
            entrada.setText("");
        } finally {
            actualizando = false;
        }
        mostrarSeleccion();
        fireActionEvent();
    }

    // sincronizamos la opción elegida antes de que Swing actualice el editor y sus eventos
    @Override
    public void setSelectedItem(Object item) {
        if (actualizando || entrada == null) {
            super.setSelectedItem(item);
            return;
        }
        if (item instanceof String texto && !texto.isBlank()) {
            List<ItemCombo> encontrados = coincidencias(opciones, texto);
            item = encontrados.size() == 1 ? encontrados.getFirst() : null;
        }
        actualizando = true;
        try {
            elegido = item instanceof ItemCombo opcion ? opcion : null;
            super.setSelectedItem(item);
            getEditor().setItem(item);
            if (espera != null) espera.stop();
        } finally {
            actualizando = false;
        }
        mostrarSeleccion();
        fireActionEvent();
    }

    // Los formularios reciben el evento cuando el ID y el texto ya coinciden.
    @Override
    protected void fireActionEvent() {
        if (!actualizando) super.fireActionEvent();
    }

    private void mostrarSeleccion() {
        entrada.setToolTipText(
                elegido == null
                        ? "Escribí y elegí un alumno de las sugerencias."
                        : elegido.toString());
        entrada.setCaretPosition(0);
    }

    private static String nombreVisible(ItemCombo alumno) {
        String texto = alumno.toString();
        int separador = texto.indexOf(" · ");
        return separador < 0 ? texto : texto.substring(0, separador);
    }

    @Override
    public ItemCombo getSelectedItem() {
        return elegido != null
                        && (entrada == null
                                || Objects.equals(entrada.getText(), nombreVisible(elegido)))
                ? elegido
                : null;
    }
}
