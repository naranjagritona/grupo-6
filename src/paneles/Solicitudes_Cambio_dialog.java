package paneles;

import clases.Usuario;
import servicios.Permisos_servicio;
import servicios.Solicitud_Cambio_servicio;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import static paneles.Ficha_Inscripcion_dialog.*;

// mostramos las solicitudes para que dirección pueda aprobarlas o rechazarlas con un motivo
public class Solicitudes_Cambio_dialog extends JDialog {
    private final Solicitud_Cambio_servicio servicio = new Solicitud_Cambio_servicio();
    private final Usuario usuario;
    private final JPanel listado = new JPanel(new BorderLayout());
    private List<Map<String, Object>> solicitudes = List.of();
    private JTable tabla;
    private final Runnable actualizar;

    public Solicitudes_Cambio_dialog(JFrame padre, Usuario usuario, Runnable actualizar) {
        super(padre, "Solicitudes a Dirección", true);
        this.usuario = usuario;
        this.actualizar = actualizar;
        setLayout(new BorderLayout(10, 10)); add(listado);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); acciones.setBackground(Color.WHITE);
        if (Permisos_servicio.directivo(usuario)) for (boolean aprobar : new boolean[] {true, false}) {
            JButton resolver = boton(aprobar ? "Aprobar" : "Rechazar");
            resolver.addActionListener(e -> resolver(aprobar)); acciones.add(resolver);
        }
        JButton recargar = boton("Actualizar"); recargar.addActionListener(e -> cargar()); acciones.add(recargar);
        JButton cerrar = boton("Cerrar"); cerrar.addActionListener(e -> dispose()); acciones.add(cerrar);
        add(acciones, BorderLayout.SOUTH); setSize(1050, 500); setLocationRelativeTo(padre); cargar();
    }

    // cargamos las solicitudes con la explicación y los datos del alumno y su destino
    private void cargar() {
        ejecutar(this, () -> servicio.listar(usuario.getId_usuario()), filas -> {
            solicitudes = filas;
            tabla = tabla(filas, "id_solicitud", "alumno", "origen", "destino", "observacion", "estado", "solicitante", "motivo_resolucion");
            listado.removeAll(); listado.add(new JScrollPane(tabla)); listado.revalidate(); listado.repaint();
        });
    }

    // pedimos el motivo de la decisión antes de enviar la resolución al servicio
    private void resolver(boolean aprobar) {
        if (tabla == null || tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Seleccioná una solicitud."); return; }
        var solicitud = solicitudes.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
        String motivo = JOptionPane.showInputDialog(this, aprobar ? "Motivo de aprobación:" : "Motivo obligatorio del rechazo:");
        if (motivo != null) ejecutar(this, () -> {
            servicio.resolver(((Number) solicitud.get("id_solicitud")).intValue(), aprobar, motivo, usuario.getId_usuario());
            return true;
        }, resultado -> { actualizar.run(); cargar(); });
    }
}
