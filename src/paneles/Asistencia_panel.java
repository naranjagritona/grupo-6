package paneles;

import clases.*;
import servicios.Asistencia_servicio;
import servicios.Permisos_servicio;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;
import static paneles.Ficha_Inscripcion_dialog.*;

// mostramos las asistencias con las acciones que corresponden al rol de cada usuario
public class Asistencia_panel extends JPanel {
    private final Usuario usuario;
    private final Asistencia_servicio servicio;
    private final JComboBox<ItemCombo> cursos = new JComboBox<>();
    private final JTextField fecha = new JTextField(LocalDate.now().toString(), 10);
    private final JPanel listado = new JPanel(new BorderLayout());
    private JTable tabla;
    private List<Map<String, Object>> filas = List.of();

    public Asistencia_panel(Usuario usuario) {
        this.usuario = usuario; servicio = new Asistencia_servicio(usuario);
        setLayout(new BorderLayout(10, 10)); setBackground(Color.WHITE); setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel titulo = new JLabel("Asistencia / Preceptoría"); titulo.setFont(new Font("Tahoma", Font.BOLD, 24)); titulo.setForeground(new Color(24, 61, 110));
        JPanel cabecera = new JPanel(new BorderLayout(8, 12)); cabecera.setBackground(Color.WHITE); cabecera.add(titulo, BorderLayout.NORTH);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT)); filtros.setBackground(Color.WHITE);
        filtros.add(new JLabel("Año / División:")); filtros.add(cursos); filtros.add(new JLabel("Fecha:")); filtros.add(fecha);
        JButton consultar = boton("Consultar"); consultar.addActionListener(e -> cargar()); filtros.add(consultar);
        cabecera.add(filtros, BorderLayout.CENTER); add(cabecera, BorderLayout.NORTH); add(listado, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); acciones.setBackground(Color.WHITE);
        if (usuario.getId_rol() == 4) { JButton guardar = boton("Registrar asistencia"); guardar.addActionListener(e -> guardar()); acciones.add(guardar); }
        if (Permisos_servicio.secretario(usuario)) { JButton justificar = boton("Justificar falta"); justificar.addActionListener(e -> justificar()); acciones.add(justificar); }
        if (Permisos_servicio.directivo(usuario)) { JButton asignar = boton("Asignar preceptor"); asignar.addActionListener(e -> asignar()); acciones.add(asignar); }
        add(acciones, BorderLayout.SOUTH);
        ejecutar(this, () -> new dao.Curso_dao().listar(usuario), lista -> {
            for (Curso curso : lista) cursos.addItem(new ItemCombo(curso.getId_curso(), curso.getAño() + "° " + curso.getDivision() + " · " + curso.getNombre_periodo()));
            cargar();
        });
    }

    // consultamos las asistencias para el curso y la fecha seleccionados
    private void cargar() {
        if (!(cursos.getSelectedItem() instanceof ItemCombo curso)) return;
        ejecutar(this, () -> servicio.listar(curso.getId(), LocalDate.parse(fecha.getText())), resultado -> {
            filas = resultado; tabla = tabla(filas, "alumno", "inscripcion", "estado", "observacion", "justificacion");
            listado.removeAll(); listado.add(new JScrollPane(tabla)); listado.revalidate(); listado.repaint();
        });
    }

    // buscamos la inscripción de la fila seleccionada para registrar o justificar su asistencia
    private Map<String, Object> seleccion() {
        if (tabla == null || tabla.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Seleccioná un alumno de la tabla."); return null; }
        return filas.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
    }

    // pedimos el estado y la observación antes de enviar los datos al servicio
    private void guardar() {
        var alumno = seleccion(); if (alumno == null) return;
        JComboBox<String> estado = new JComboBox<>(new String[] {"Presente", "Ausente", "Tarde"});
        if (alumno.get("estado") != null) estado.setSelectedItem(alumno.get("estado"));
        JTextField observacion = new JTextField(Objects.toString(alumno.get("observacion"), ""));
        Object[] campos = {alumno.get("alumno"), "Estado", estado, "Observación o motivo de corrección", observacion};
        if (JOptionPane.showConfirmDialog(this, campos, "Asistencia", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) ejecutar(this, () -> {
            servicio.guardar(((Number) alumno.get("id_inscripcion")).intValue(), LocalDate.parse(fecha.getText()), estado.getSelectedItem().toString(), observacion.getText()); return true;
        }, resultado -> cargar());
    }

    // pedimos el motivo que presenta secretaría para justificar la falta seleccionada
    private void justificar() {
        var alumno = seleccion(); if (alumno == null || alumno.get("id_asistencia") == null) return;
        String motivo = JOptionPane.showInputDialog(this, "Motivo de justificación:");
        if (motivo != null) ejecutar(this, () -> { servicio.justificar(((Number) alumno.get("id_asistencia")).intValue(), motivo); return true; }, resultado -> cargar());
    }

    // mostramos los preceptores activos y guardamos la asignación del curso
    private void asignar() {
        if (!(cursos.getSelectedItem() instanceof ItemCombo curso)) return;
        ejecutar(this, servicio::preceptores, lista -> {
            JComboBox<ItemCombo> preceptores = new JComboBox<>();
            for (var preceptor : lista) preceptores.addItem(new ItemCombo(((Number) preceptor.get("id_usuario")).intValue(), preceptor.get("nombre").toString()));
            if (preceptores.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "Registrá un preceptor desde Admin antes de asignarlo."); return; }
            if (JOptionPane.showConfirmDialog(this, preceptores, "Asignar preceptor al curso", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) ejecutar(this, () -> { servicio.asignar(((ItemCombo) preceptores.getSelectedItem()).getId(), curso.getId()); return true; }, resultado -> JOptionPane.showMessageDialog(this, "Preceptor asignado."));
        });
    }
}
