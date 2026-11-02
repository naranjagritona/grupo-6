package paneles;

import clases.*;
import dao.PlanCurso_dao;
import dao.DetalleCurso_dao;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// mostramos la información general, las materias y los alumnos del curso seleccionado
public class DetalleCurso_dialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // compartimos los accesos a datos y las opciones de docentes que utiliza la ventana
    private final PlanCurso_dao dao;
    private final DetalleCurso_dao detalleDao;
    private final java.util.function.Supplier<List<Docente>> obtenerDocentes;
    private Curso curso;
    private final Usuario usuario;
    private final Runnable alCambiar;
    private final DefaultTableModel modelo = soloLectura("Año", "Materia", "Docente");
    private final DefaultTableModel modeloAlumnos = soloLectura("Alumno", "Estado de inscripción", "Fecha de inscripción");
    private final JTable tabla = new JTable(modelo);
    private final JTable tablaAlumnos = new JTable(modeloAlumnos);
    private final JTabbedPane pestanias = new JTabbedPane();
    private final JPanel informacion = new JPanel(new GridLayout(0, 2, 20, 14));
    private final JPanel indicadores = new JPanel(new GridLayout(1, 4, 10, 10));
    private final JLabel titulo = new JLabel();
    private final JLabel estado = new JLabel(" ");
    private final JLabel notaAlumnos = new JLabel(" ");
    private final JButton asignar = new JButton("Asignar / cambiar docente");
    private final JButton actualizar = new JButton("Actualizar");
    private boolean cargando;
    private List<PlanCurso_dao.Detalle> filas = List.of();
    private List<Docente> docentes = List.of();

    // juntamos los resultados de la consulta antes de actualizar los componentes de la ventana
    private record Carga(DetalleCurso_dao.Datos datos, List<PlanCurso_dao.Detalle> materias, List<Docente> docentes) {}

    // creamos tablas sin edición para que los cambios se hagan desde sus formularios
    private static DefaultTableModel soloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    // preparamos los accesos a datos que utiliza el detalle del curso
    public DetalleCurso_dialog(Component origen, Curso curso, Usuario usuario, Runnable alCambiar) {
        this(origen, curso, usuario, alCambiar, new PlanCurso_dao(),
                () -> new dao.Docente_dao().obtenerTodos(), new DetalleCurso_dao());
    }

    // armamos las pestañas, los indicadores y los botones según los permisos del usuario
    DetalleCurso_dialog(Component origen, Curso curso, Usuario usuario, Runnable alCambiar,
            PlanCurso_dao dao, java.util.function.Supplier<List<Docente>> obtenerDocentes, DetalleCurso_dao detalleDao) {
        super(SwingUtilities.getWindowAncestor(origen), "Detalle del curso " + curso.getAño() + "° " + curso.getDivision(), ModalityType.APPLICATION_MODAL);
        this.curso = curso; this.usuario = usuario; this.alCambiar = alCambiar;
        this.dao = dao; this.obtenerDocentes = obtenerDocentes; this.detalleDao = detalleDao;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel cabecera = new JPanel(new BorderLayout(8, 12));
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setText("Curso " + curso.getAño() + "° " + curso.getDivision());
        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(indicadores, BorderLayout.CENTER);
        contenido.add(cabecera, BorderLayout.NORTH);

        informacion.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel general = new JPanel(new BorderLayout()); general.add(informacion, BorderLayout.NORTH);
        pestanias.addTab("Información general", new JScrollPane(general));
        tabla.setRowHeight(30); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setAutoCreateRowSorter(true);
        pestanias.addTab("Materias y docentes", new JScrollPane(tabla));
        tablaAlumnos.setRowHeight(30); tablaAlumnos.setAutoCreateRowSorter(true);
        tablaAlumnos.getColumnModel().getColumn(0).setPreferredWidth(300);
        JPanel alumnos = new JPanel(new BorderLayout(8, 10));
        alumnos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        alumnos.add(new JScrollPane(tablaAlumnos), BorderLayout.CENTER);
        alumnos.add(notaAlumnos, BorderLayout.SOUTH);
        pestanias.addTab("Alumnos", alumnos);
        pestanias.addChangeListener(e -> actualizarBotonDocente());
        contenido.add(pestanias, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(8, 8)); pie.add(estado, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        asignar.setEnabled(false);
        if (servicios.Permisos_servicio.gestion(usuario)) botones.add(asignar);
        asignar.addActionListener(e -> elegirDocente());
        actualizar.addActionListener(e -> cargar()); botones.add(actualizar);
        JButton cerrar = new JButton("Cerrar"); cerrar.addActionListener(e -> dispose()); botones.add(cerrar);
        pie.add(botones, BorderLayout.SOUTH); contenido.add(pie, BorderLayout.SOUTH);
        setContentPane(contenido);
        Rectangle pantalla = getGraphicsConfiguration().getBounds();
        Insets margenes = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        setSize(Math.min(1000, pantalla.width - margenes.left - margenes.right - 40),
                Math.min(690, pantalla.height - margenes.top - margenes.bottom - 40));
        setLocationRelativeTo(origen); cargar();
    }

    // habilitamos la asignación solamente cuando la pestaña de materias está disponible
    private void actualizarBotonDocente() {
        asignar.setEnabled(!cargando && !filas.isEmpty() && pestanias.getSelectedIndex() == 1);
    }

    // avisamos que hay una operación en curso y evitamos repetirla desde los botones
    private void ocupado(boolean valor) {
        cargando = valor; actualizarBotonDocente(); actualizar.setEnabled(!valor);
    }

    // consultamos el detalle en segundo plano y actualizamos las pestañas cuando termina
    private void cargar() {
        ocupado(true); estado.setText("Cargando información del curso…");
        new SwingWorker<Carga, Void>() {
            protected Carga doInBackground() throws Exception {
                var datos = detalleDao.consultar(curso.getId_curso(), usuario);
                var materias = dao.detalle(curso.getId_curso());
                var opciones = servicios.Permisos_servicio.gestion(usuario) ? obtenerDocentes.get() : List.<Docente>of();
                return new Carga(datos, materias, opciones);
            }
            protected void done() {
                if (!isDisplayable()) return;
                try {
                    Carga carga = get(); curso = carga.datos().curso();
                    filas = carga.materias(); docentes = carga.docentes();
                    mostrarInformacion(carga.datos());
                    modelo.setRowCount(0);
                    for (var fila : filas) modelo.addRow(new Object[]{curso.getAño() + "°", fila.nombre(), fila.docente() == null ? "Sin asignar" : fila.nombreDocente()});
                    modeloAlumnos.setRowCount(0);
                    for (var alumno : carga.datos().alumnos()) modeloAlumnos.addRow(new Object[]{alumno.nombre(), alumno.estado(), alumno.fecha() == null ? "—" : FECHA.format(alumno.fecha())});
                    pestanias.setTitleAt(1, "Materias y docentes (" + filas.size() + ")");
                    pestanias.setTitleAt(2, usuario != null && usuario.getId_rol() == 3 ? "Mi inscripción" : "Alumnos e inscripciones (" + carga.datos().alumnos().size() + ")");
                    notaAlumnos.setText(carga.datos().alumnos().isEmpty() ? "Todavía no hay alumnos inscriptos en este curso." : " ");
                    estado.setText("Información actualizada.");
                } catch (Exception e) { error(e); estado.setText("No se pudo cargar el detalle."); }
                finally { ocupado(false); }
            }
        }.execute();
    }

    // completamos el encabezado, los cupos y los datos generales con la consulta actualizada
    private void mostrarInformacion(DetalleCurso_dao.Datos datos) {
        setTitle("Detalle del curso " + curso.getAño() + "° " + curso.getDivision());
        titulo.setText("Curso " + curso.getAño() + "° " + curso.getDivision() + " · " + curso.getEstado());
        indicadores.removeAll();
        indicador("Cupo total", curso.getCupo_maximo());
        indicador("Alumnos con lugar", datos.ocupados());
        indicador("Lugares disponibles", datos.disponibles());
        indicador("Materias", filas.size());
        informacion.removeAll();
        dato("Curso", curso.getAño() + "° " + curso.getDivision());
        dato("Período lectivo", curso.getNombre_periodo());
        dato("Estado", curso.getEstado()); dato("Modalidad", curso.getModalidad());
        dato("Turno", curso.getTurno()); dato("Inicio", FECHA.format(curso.getFecha_inicio()));
        dato("Fin", FECHA.format(curso.getFecha_fin())); dato("Cupo máximo", curso.getCupo_maximo());
        indicadores.revalidate(); indicadores.repaint(); informacion.revalidate(); informacion.repaint();
    }

    // armamos cada recuadro que muestra el cupo, los alumnos o la cantidad de materias
    private void indicador(String etiqueta, Object valor) {
        JPanel panel = new JPanel(new BorderLayout(4, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        JLabel numero = new JLabel(String.valueOf(valor)); numero.setFont(numero.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(numero, BorderLayout.CENTER); indicadores.add(panel);
    }

    // mostramos el nombre del dato y avisamos si todavía no tiene un valor configurado
    private void dato(String etiqueta, Object valor) {
        JPanel panel = new JPanel(new BorderLayout(4, 5));
        JLabel nombre = new JLabel(etiqueta); nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
        panel.add(nombre, BorderLayout.NORTH);
        panel.add(new JLabel(valor == null || valor.toString().isBlank() ? "Sin configurar" : valor.toString()), BorderLayout.CENTER);
        informacion.add(panel);
    }

    // tomamos la materia seleccionada y guardamos el docente después de confirmar la elección
    private void elegirDocente() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Seleccioná una materia."); return; }
        var materia = filas.get(tabla.convertRowIndexToModel(fila));
        JComboBox<Docente> opciones = new JComboBox<>(); opciones.addItem(null);
        for (Docente d : docentes) if ("Activo".equals(d.getEstado()) || d.getId_docente().equals(materia.docente())) opciones.addItem(d);
        opciones.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l,v,i,s,f);
                setText(v instanceof Docente d ? d.getApellido() + ", " + d.getNombre() + ("Activo".equals(d.getEstado()) ? "" : " (inactivo)") : "Sin asignar"); return this;
            }
        });
        for (int i=1; i<opciones.getItemCount(); i++) if (opciones.getItemAt(i).getId_docente().equals(materia.docente())) opciones.setSelectedIndex(i);
        if (JOptionPane.showConfirmDialog(this, new Object[]{materia.nombre(), opciones}, "Docente de la materia", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Docente elegido = (Docente) opciones.getSelectedItem();
        Integer id = elegido == null ? null : elegido.getId_docente();
        if (java.util.Objects.equals(id, materia.docente())) return;
        ocupado(true); estado.setText("Guardando asignación…");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception { dao.asignar(curso.getId_curso(), materia.materia(), id, materia.docente()); return null; }
            protected void done() {
                try { get(); alCambiar.run(); cargar(); }
                catch (Exception e) { error(e); estado.setText("No se guardó la asignación."); ocupado(false); }
            }
        }.execute();
    }

    // mostramos la causa del error para que el usuario sepa por qué no se completó la operación
    private void error(Exception e) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        JOptionPane.showMessageDialog(this, causa.getMessage(), "Detalle del curso", JOptionPane.ERROR_MESSAGE);
    }
}
