package paneles;

import servicios.Inscripcion_servicio;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// mostramos el alumno y los datos de su inscripción sin modificar el historial
public class Ficha_Inscripcion_dialog extends JDialog {
    public Ficha_Inscripcion_dialog(JFrame padre, Inscripcion_servicio.Ficha ficha) {
        super(padre, "Información de inscripción", true);
        setLayout(new BorderLayout(10, 10));
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("Tahoma", Font.PLAIN, 13));
        JPanel datos = new JPanel(new GridLayout(0, 2, 10, 10));
        datos.setBackground(Color.WHITE);
        datos.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        var registro = ficha.datos();
        Object[][] campos = {
            {"Alumno", registro.get("apellido") + ", " + registro.get("nombre")},
            {"DNI", registro.get("dni")}, {"Matrícula", registro.get("matricula")},
            {"Año y división", registro.get("año") + "° " + registro.get("division")},
            {"Período", registro.get("periodo")}, {"Turno", registro.get("turno")},
            {"Estado", registro.get("estado")}, {"Fecha de inscripción", registro.get("fecha_inscripcion")},
            {"Inscripción anterior", registro.get("id_inscripcion_origen")},
            {"Motivo de baja o traslado", registro.get("motivo_baja")}
        };
        for (Object[] campo : campos) {
            JLabel etiqueta = new JLabel(campo[0].toString());
            etiqueta.setFont(new Font("Tahoma", Font.PLAIN, 13));
            etiqueta.setForeground(new Color(24, 61, 110));
            datos.add(etiqueta);
            JTextArea valor = new JTextArea(Objects.toString(campo[1], ""));
            valor.setEditable(false);
            valor.setLineWrap(true);
            valor.setWrapStyleWord(true);
            valor.setFont(new Font("Tahoma", Font.PLAIN, 13));
            valor.setBackground(new Color(245, 247, 250));
            datos.add(valor);
        }
        pestañas.addTab("Inscripción", new JScrollPane(datos));
        pestañas.addTab("Notas", new JScrollPane(tabla(ficha.notas(), "materia", "titulo", "tipo", "fecha_evaluacion", "nota", "observaciones")));
        pestañas.addTab("Materias", new JScrollPane(tabla(ficha.resultados(), "nombre_materia", "promedio", "resultado")));
        pestañas.addTab("Historial", new JScrollPane(tabla(ficha.movimientos(), "estado_anterior", "estado_nuevo", "motivo", "usuario", "fecha")));
        add(pestañas, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setBackground(Color.WHITE);
        JButton cerrar = boton("Cerrar");
        cerrar.setBackground(new Color(100, 116, 139));
        cerrar.addActionListener(e -> dispose());
        acciones.add(cerrar);
        add(acciones, BorderLayout.SOUTH);
        setSize(870, 530);
        setLocationRelativeTo(padre);
    }

    // preparamos los botones con los colores y las fuentes de los formularios de gestión
    public static JButton boton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(37, 99, 235));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    // completamos la tabla y mantenemos sus celdas de solo lectura
    public static JTable tabla(List<Map<String, Object>> filas, String... columnas) {
        String[] etiquetas = Arrays.stream(columnas).map(c -> switch (c) {
            case "nombre_materia" -> "Materia";
            case "titulo" -> "Título";
            case "id_solicitud" -> "Solicitud";
            case "observacion", "observaciones" -> "Observaciones";
            default -> c.substring(0, 1).toUpperCase(Locale.ROOT) + c.substring(1).replace('_', ' ');
        }).toArray(String[]::new);
        DefaultTableModel modelo = new DefaultTableModel(etiquetas, 0) {
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
        for (var fila : filas) modelo.addRow(Arrays.stream(columnas).map(fila::get).toArray());
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tabla.setRowHeight(28);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionBackground(new Color(220, 232, 248));
        tabla.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(235, 240, 247));
        tabla.getTableHeader().setForeground(new Color(24, 61, 110));
        return tabla;
    }

    // ejecutamos las consultas fuera del hilo de la interfaz y mostramos el motivo si fallan
    public static <T> void ejecutar(Component padre, Callable<T> tarea, Consumer<T> terminado) {
        padre.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception { return tarea.call(); }
            protected void done() {
                padre.setCursor(Cursor.getDefaultCursor());
                try { terminado.accept(get()); }
                catch (Exception error) {
                    Throwable motivo = error.getCause() == null ? error : error.getCause();
                    JOptionPane.showMessageDialog(padre, motivo.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
                }
            }
        }.execute();
    }
}
