package paneles;

import clases.Alumno;
import clases.Calificacion;
import clases.Columna_Notas;
import clases.Curso;
import clases.Inscripcion;
import clases.ItemCombo;
import clases.Usuario;
import clases.Docente;
import dao.Alumno_dao;
import dao.Calificacion_dao;
import dao.Columna_Notas_dao;
import dao.Curso_dao;
import dao.Inscripcion_dao;
import dao.Docente_dao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Gestión de evaluaciones y calificaciones de alumnos inscriptos en cada curso. */
public class Calificaciones_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Curso_dao cursoDao = new Curso_dao();
    private final Inscripcion_dao inscripcionDao = new Inscripcion_dao();
    private final Alumno_dao alumnoDao = new Alumno_dao();
    private final Columna_Notas_dao columnaDao = new Columna_Notas_dao();
    private final Calificacion_dao calificacionDao = new Calificacion_dao();
    private final Usuario usuarioLogueado;
    private final JComboBox<ItemCombo> cmbCurso = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[] {"Alumno", "DNI", "Evaluación", "Tipo", "Nota", "Máximo", "Estado", "Fecha", "Observaciones"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final Map<Integer, Calificacion> calificacionesPorFila = new HashMap<>();
    private final Map<Integer, Columna_Notas> columnasPorFila = new HashMap<>();

    public Calificaciones_panel() { this(null); }

    public Calificaciones_panel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(24, 61, 110));
        encabezado.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel titulo = new JLabel("Gestión de Calificaciones");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Tahoma", Font.BOLD, 22));
        JLabel subtitulo = new JLabel("Evaluaciones, notas e historial por curso");
        subtitulo.setForeground(new Color(220, 230, 245));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo); textos.add(Box.createVerticalStrut(4)); textos.add(subtitulo);
        encabezado.add(textos, BorderLayout.WEST);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filtros.setBackground(Color.WHITE);
        filtros.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        filtros.add(new JLabel("Curso:"));
        cmbCurso.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof ItemCombo) setText(((ItemCombo) value).getTexto());
                return this;
            }
        });
        cmbCurso.setPreferredSize(new java.awt.Dimension(330, 28));
        cmbCurso.addActionListener(e -> cargarTabla());
        filtros.add(cmbCurso);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargarCursos()); filtros.add(actualizar);
        JPanel superior = new JPanel(new BorderLayout());
        superior.setBackground(Color.WHITE);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(filtros, BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);

        tabla.setRowHeight(27);
        tabla.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tabla.setSelectionBackground(new Color(220, 232, 248));
        tabla.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(235, 240, 247));
        tabla.getTableHeader().setForeground(new Color(24, 61, 110));
        tabla.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        acciones.setBackground(Color.WHITE);
        JButton nuevaEvaluacion = new JButton("Nueva evaluación");
        JButton cargarNota = new JButton("Cargar nota");
        JButton modificar = new JButton("Modificar nota");
        JButton historial = new JButton("Historial");
        JButton borrar = new JButton("Eliminar nota");
        nuevaEvaluacion.addActionListener(e -> crearEvaluacion());
        cargarNota.addActionListener(e -> cargarNota());
        modificar.addActionListener(e -> modificarNota());
        historial.addActionListener(e -> mostrarHistorial());
        borrar.addActionListener(e -> eliminarNota());
        acciones.add(nuevaEvaluacion); acciones.add(cargarNota); acciones.add(modificar);
        acciones.add(historial); acciones.add(borrar);
        add(acciones, BorderLayout.SOUTH);
        cargarCursos();
    }

    private void cargarCursos() {
        Integer anterior = cmbCurso.getSelectedItem() instanceof ItemCombo ? ((ItemCombo)cmbCurso.getSelectedItem()).getId() : null;
        cmbCurso.removeAllItems();
        List<Curso> cursos = cursoDao.obtenerTodos();
        Integer idDocente = null;
        if (usuarioLogueado != null && usuarioLogueado.getId_rol() == 2) {
            for (Docente d : new Docente_dao().obtenerTodos()) {
                if (usuarioLogueado.getId_usuario().equals(d.getId_usuario())) { idDocente = d.getId_docente(); break; }
            }
        }
        for (Curso c : cursos) {
            if (usuarioLogueado != null && usuarioLogueado.getId_rol() == 2 && (idDocente == null || !idDocente.equals(c.getId_docente()))) continue;
            String nombre = "Curso " + c.getId_curso() + " · " + valor(c.getNombre_materia()) + " · " + valor(c.getNombre_periodo()) + " · " + c.getAño() + "° " + valor(c.getDivision());
            cmbCurso.addItem(new ItemCombo(c.getId_curso(), nombre));
            if (anterior != null && anterior.equals(c.getId_curso())) cmbCurso.setSelectedIndex(cmbCurso.getItemCount() - 1);
        }
        cargarTabla();
    }

    private void cargarTabla() {
        modelo.setRowCount(0); calificacionesPorFila.clear(); columnasPorFila.clear();
        ItemCombo cursoItem = (ItemCombo)cmbCurso.getSelectedItem();
        if (cursoItem == null) {
            modelo.addRow(new Object[] {"No hay cursos asignados o disponibles para consultar.", "", "", "", "", "", "", "", ""});
            return;
        }
        List<Columna_Notas> columnas = columnaDao.obtenerTodos();
        List<Calificacion> notas = calificacionDao.obtenerTodos();
        List<Inscripcion> inscripciones = inscripcionDao.obtenerTodos();
        Map<Integer, Alumno> alumnos = new HashMap<>();
        for (Alumno a : alumnoDao.obtenerTodos()) alumnos.put(a.getId_alumno(), a);
        Map<Integer, Inscripcion> inscripcionesCurso = new HashMap<>();
        for (Inscripcion i : inscripciones) {
            if (cursoItem.getId().equals(i.getId_curso()) && !"Cancelada".equalsIgnoreCase(i.getEstado()) && !"Finalizada".equalsIgnoreCase(i.getEstado()))
                inscripcionesCurso.put(i.getId_inscripcion(), i);
        }
        for (Columna_Notas col : columnas) {
            if (!cursoItem.getId().equals(col.getId_curso())) continue;
            for (Inscripcion ins : inscripcionesCurso.values()) {
                Alumno a = alumnos.get(ins.getId_alumno());
                if (a == null) continue;
                Calificacion nota = buscarNota(notas, ins.getId_inscripcion(), col.getId_columna_notas());
                String estado = nota == null ? "Pendiente" : (nota.getNota() >= col.getNota_aprobacion() ? "Aprobado" : "Desaprobado");
                int fila = modelo.getRowCount();
                modelo.addRow(new Object[] {a.getNombre() + " " + a.getApellido(), a.getDni(), col.getTitulo(), col.getTipo(),
                    nota == null ? "—" : nota.getNota(), col.getNota_maxima(), estado,
                    nota == null || nota.getFecha_carga() == null ? "—" : nota.getFecha_carga().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    nota == null ? "" : nota.getObservaciones()});
                if (nota != null) calificacionesPorFila.put(fila, nota);
                columnasPorFila.put(fila, col);
            }
        }
        if (modelo.getRowCount() == 0) modelo.addRow(new Object[] {"Sin inscripciones activas o evaluaciones para este curso", "", "", "", "", "", "", "", ""});
    }

    private Calificacion buscarNota(List<Calificacion> notas, Integer idInscripcion, Integer idColumna) {
        for (Calificacion n : notas) if (idInscripcion.equals(n.getId_inscripcion()) && idColumna.equals(n.getId_columna_notas())) return n;
        return null;
    }

    private void crearEvaluacion() {
        ItemCombo curso = (ItemCombo)cmbCurso.getSelectedItem();
        if (curso == null) { aviso("Primero registrá o seleccioná un curso."); return; }
        JTextField titulo = new JTextField();
        JComboBox<String> tipo = new JComboBox<>(new String[] {"Evaluación", "Trabajo práctico", "Participación", "Examen", "Otro"});
        JTextField maximo = new JTextField("10");
        JTextField aprobacion = new JTextField("6");
        JTextField fecha = new JTextField(LocalDate.now().toString());
        JPanel form = formulario(new String[] {"Título:", "Tipo:", "Nota máxima:", "Nota de aprobación:", "Fecha (AAAA-MM-DD):"}, new Component[] {titulo, tipo, maximo, aprobacion, fecha});
        if (JOptionPane.showConfirmDialog(this, form, "Nueva evaluación", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            double max = Double.parseDouble(maximo.getText().trim());
            double apr = Double.parseDouble(aprobacion.getText().trim());
            LocalDate dia = LocalDate.parse(fecha.getText().trim());
            if (titulo.getText().trim().isEmpty() || max <= 0 || apr < 0 || apr > max) throw new IllegalArgumentException();
            Columna_Notas col = new Columna_Notas(); col.setId_curso(curso.getId()); col.setTitulo(titulo.getText().trim());
            col.setTipo((String)tipo.getSelectedItem()); col.setNota_maxima(max); col.setNota_aprobacion(apr);
            col.setFecha_evaluacion(dia); col.setFecha_cierre(dia); col.setEstado("Abierta");
            columnaDao.insertarVerificando(col); cargarTabla();
            JOptionPane.showMessageDialog(this, "Evaluación registrada.");
        } catch (Exception ex) { aviso("No se pudo crear la evaluación. Revisá los datos y la conexión con la base de datos. " + ex.getMessage()); }
    }

    private void cargarNota() {
        ItemCombo curso = (ItemCombo)cmbCurso.getSelectedItem();
        if (curso == null) return;
        List<Columna_Notas> columnas = columnasDelCurso(curso.getId());
        List<Inscripcion> inscripciones = inscripcionesDelCurso(curso.getId());
        if (columnas.isEmpty() || inscripciones.isEmpty()) { aviso("Este curso necesita una evaluación y al menos un alumno inscripto."); return; }
        JComboBox<Columna_Notas> colBox = comboColumnas(columnas);
        JComboBox<ItemCombo> alumnoBox = comboInscripciones(inscripciones);
        JTextField nota = new JTextField(); JTextField obs = new JTextField();
        JPanel form = formulario(new String[] {"Evaluación:", "Alumno:", "Nota:", "Observaciones:"}, new Component[] {colBox, alumnoBox, nota, obs});
        if (JOptionPane.showConfirmDialog(this, form, "Cargar calificación", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Columna_Notas col = (Columna_Notas)colBox.getSelectedItem(); ItemCombo ins = (ItemCombo)alumnoBox.getSelectedItem();
        try {
            double valor = Double.parseDouble(nota.getText().trim());
            if (valor < 0 || valor > col.getNota_maxima()) throw new IllegalArgumentException();
            if (buscarNota(calificacionDao.obtenerTodos(), ins.getId(), col.getId_columna_notas()) != null) { aviso("Ese alumno ya tiene una nota para esta evaluación. Usá Modificar nota."); return; }
            Calificacion n = new Calificacion(); n.setId_inscripcion(ins.getId()); n.setId_columna_notas(col.getId_columna_notas()); n.setNota(valor); n.setFecha_carga(LocalDate.now()); n.setObservaciones(obs.getText().trim());
            calificacionDao.insertarVerificando(n); cargarTabla(); JOptionPane.showMessageDialog(this, "Calificación registrada.");
        } catch (Exception ex) { aviso("No se pudo guardar la calificación. Verificá el valor y la conexión con la base de datos. " + ex.getMessage()); }
    }

    private void modificarNota() {
        Calificacion n = notaSeleccionada(); if (n == null) { aviso("Seleccioná una calificación cargada."); return; }
        Columna_Notas col = columnasPorFila.get(tabla.getSelectedRow());
        JTextField valor = new JTextField(String.valueOf(n.getNota())); JTextField motivo = new JTextField();
        JPanel form = formulario(new String[] {"Nueva nota:", "Motivo del cambio:"}, new Component[] {valor, motivo});
        if (JOptionPane.showConfirmDialog(this, form, "Modificar calificación", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            double nueva = Double.parseDouble(valor.getText().trim());
            if (nueva < 0 || nueva > col.getNota_maxima()) throw new IllegalArgumentException();
            String motivoTexto = motivo.getText().trim();
            if (motivoTexto.isEmpty()) { aviso("Indicá el motivo para conservar la trazabilidad del cambio."); return; }
            double anterior = n.getNota();
            n.setNota(nueva); n.setFecha_carga(LocalDate.now()); n.setObservaciones(motivoTexto);
            calificacionDao.modificarConHistorial(n, anterior, motivoTexto);
            cargarTabla(); JOptionPane.showMessageDialog(this, "Calificación actualizada y cambio agregado al historial.");
        } catch (Exception ex) { aviso("No se pudo guardar el cambio: " + ex.getMessage()); }
    }

    private void mostrarHistorial() {
        Calificacion n = notaSeleccionada(); if (n == null) { aviso("Seleccioná una calificación cargada."); return; }
        StringBuilder texto = new StringBuilder();
        for (clases.Calificacion_Historial h : new dao.Calificacion_Historial_dao().obtenerTodos()) if (n.getId_calificacion().equals(h.getId_calificacion()))
            texto.append(h.getFecha_modificacion()).append(" | ").append(h.getNota_anterior()).append(" → ").append(h.getNota_nueva()).append(" | ").append(h.getMotivo()).append("\n");
        JOptionPane.showMessageDialog(this, texto.length() == 0 ? "Esta calificación todavía no tiene modificaciones." : texto.toString(), "Historial de calificación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void eliminarNota() {
        Calificacion n = notaSeleccionada(); if (n == null) { aviso("Seleccioná una calificación cargada."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar la calificación seleccionada?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            calificacionDao.eliminar(n.getId_calificacion()); cargarTabla();
        }
    }

    private Calificacion notaSeleccionada() { return calificacionesPorFila.get(tabla.getSelectedRow()); }
    private List<Columna_Notas> columnasDelCurso(Integer id) { java.util.ArrayList<Columna_Notas> r = new java.util.ArrayList<>(); for (Columna_Notas c : columnaDao.obtenerTodos()) if (id.equals(c.getId_curso())) r.add(c); return r; }
    private List<Inscripcion> inscripcionesDelCurso(Integer id) { java.util.ArrayList<Inscripcion> r = new java.util.ArrayList<>(); for (Inscripcion i : inscripcionDao.obtenerTodos()) if (id.equals(i.getId_curso()) && !"Cancelada".equalsIgnoreCase(i.getEstado()) && !"Finalizada".equalsIgnoreCase(i.getEstado())) r.add(i); return r; }
    private JComboBox<Columna_Notas> comboColumnas(List<Columna_Notas> opciones) { JComboBox<Columna_Notas> b = new JComboBox<>(); for (Columna_Notas c : opciones) b.addItem(c); b.setRenderer(new DefaultListCellRenderer() { private static final long serialVersionUID = 1L; @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f) { super.getListCellRendererComponent(l,v,i,s,f); if(v instanceof Columna_Notas) setText(((Columna_Notas)v).getTitulo()+" (máx. "+((Columna_Notas)v).getNota_maxima()+")"); return this; }}); return b; }
    private JComboBox<ItemCombo> comboInscripciones(List<Inscripcion> opciones) { JComboBox<ItemCombo> b = new JComboBox<>(); Map<Integer, Alumno> alumnos = new HashMap<>(); for(Alumno a:alumnoDao.obtenerTodos()) alumnos.put(a.getId_alumno(),a); for(Inscripcion i:opciones) { Alumno a=alumnos.get(i.getId_alumno()); if(a!=null) b.addItem(new ItemCombo(i.getId_inscripcion(),a.getNombre()+" "+a.getApellido()+" · DNI "+a.getDni())); } return b; }
    private JPanel formulario(String[] etiquetas, Component[] campos) { JPanel p=new JPanel(new GridLayout(etiquetas.length,2,8,8)); for(int i=0;i<etiquetas.length;i++){p.add(new JLabel(etiquetas[i]));p.add(campos[i]);} return p; }
    private String valor(String s) { return s == null || s.trim().isEmpty() ? "Sin dato" : s.trim(); }
    private void aviso(String s) { JOptionPane.showMessageDialog(this,s,"Calificaciones",JOptionPane.WARNING_MESSAGE); }
}
