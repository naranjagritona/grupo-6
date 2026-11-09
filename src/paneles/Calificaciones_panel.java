package paneles;

import clases.Calificacion;
import clases.Columna_Notas;
import clases.Curso;
import clases.ItemCombo;
import clases.Usuario;

import dao.Alumno_dao;
import dao.Calificacion_dao;
import dao.Columna_Notas_dao;
import dao.Curso_dao;
import dao.Inscripcion_dao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Gestión de evaluaciones y calificaciones de alumnos inscriptos en cada curso. */
// mostramos las evaluaciones, las notas y las opciones para consultar su historial
public class Calificaciones_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Curso_dao cursoDao = new Curso_dao();
    private final Inscripcion_dao inscripcionDao = new Inscripcion_dao();
    private final Alumno_dao alumnoDao = new Alumno_dao();
    private final Columna_Notas_dao columnaDao = new Columna_Notas_dao();
    private final Calificacion_dao calificacionDao = new Calificacion_dao();
    private final Usuario usuarioLogueado;
    private final servicios.Calificaciones_servicio servicio;
    private boolean ocupado;
    private List<dao.ConsultaCalificaciones_dao.Fila> filasCurso = List.of();
    private List<dao.ConsultaCalificaciones_dao.Fila> filasActuales = List.of();
    private boolean llenandoCursos;
    private boolean llenandoFiltros;
    private int versionCursos;
    private int versionTabla;
    private javax.swing.SwingWorker<?, ?> cargaTabla;
    private final JLabel estadoCarga = new JLabel(" ");
    private final JComboBox<ItemCombo> cmbCurso = new JComboBox<>();
    private final JComboBox<String> cmbMateria = new JComboBox<>();
    private final JComboBox<ItemCombo> cmbEvaluacion = new JComboBox<>();
    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[] {
                        "Alumno",
                        "DNI",
                        "Materia / Evaluación",
                        "Tipo",
                        "Nota",
                        "Máximo",
                        "Estado",
                        "Fecha",
                        "Observaciones"
                    },
                    0) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modelo);
    private final DefaultTableModel modeloPlanilla = new DefaultTableModel() {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) {
            return servicios.Permisos_servicio.editarCalificaciones(usuarioLogueado)
                    && column >= 2 && column - 2 < columnasPlanilla.size()
                    && "Abierta".equals(new ArrayList<>(columnasPlanilla.values()).get(column - 2).getEstado());
        }
        @Override public void setValueAt(Object value, int row, int column) {
            Object anterior = getValueAt(row, column);
            super.setValueAt(value, row, column);
            if (column >= 2 && row >= 0 && row < inscripcionesPlanilla.size()) {
                guardarCeldaPlanilla(row, column - 2, anterior, value);
            }
        }
    };
    private final JTable tablaPlanilla = new JTable(modeloPlanilla);
    private final Map<Integer, dao.ConsultaCalificaciones_dao.Fila> inscripcionesPlanilla = new LinkedHashMap<>();
    private final Map<Integer, Columna_Notas> columnasPlanilla = new LinkedHashMap<>();
    private final javax.swing.JTabbedPane vistas = new javax.swing.JTabbedPane();
    private final Map<Integer, Calificacion> calificacionesPorFila = new HashMap<>();
    private final Map<Integer, Columna_Notas> columnasPorFila = new HashMap<>();

    public Calificaciones_panel() {
        this(null);
    }

    public Calificaciones_panel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        this.servicio = new servicios.Calificaciones_servicio(usuario);
        servicios.Auditoria_servicio.registrar(
                "ABRIR MODULO", "Calificaciones", "Consulta del módulo");
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Color.WHITE);
        JLabel titulo = new JLabel("Calificaciones");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filtros.setBackground(Color.WHITE);
        filtros.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        filtros.add(new JLabel("Curso:"));
        cmbCurso.setRenderer(
                new DefaultListCellRenderer() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean selected,
                            boolean focus) {
                        super.getListCellRendererComponent(list, value, index, selected, focus);
                        if (value instanceof ItemCombo) setText(((ItemCombo) value).getTexto());
                        return this;
                    }
                });
        cmbCurso.setPreferredSize(new java.awt.Dimension(330, 28));
        cmbCurso.addActionListener(
                e -> {
                    if (!llenandoCursos) {
                        cargarTabla();
                    }
                });
        filtros.add(cmbCurso);
        filtros.add(new JLabel("Materia:"));
        cmbMateria.setPreferredSize(new java.awt.Dimension(180, 28));
        cmbMateria.addActionListener(e -> {
            if (!llenandoFiltros) {
                actualizarEvaluacionesFiltro();
                aplicarFiltros();
            }
        });
        filtros.add(cmbMateria);
        filtros.add(new JLabel("Evaluación:"));
        cmbEvaluacion.setRenderer(cmbCurso.getRenderer());
        cmbEvaluacion.setPreferredSize(new java.awt.Dimension(230, 28));
        cmbEvaluacion.addActionListener(e -> {
            if (!llenandoFiltros) aplicarFiltros();
        });
        filtros.add(cmbEvaluacion);
        filtros.add(estadoCarga);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargarCursos());
        filtros.add(actualizar);
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
        scroll.setColumnHeaderView(tabla.getTableHeader());
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        tablaPlanilla.setRowHeight(34);
        tablaPlanilla.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaPlanilla.setSelectionBackground(new Color(220, 232, 248));
        tablaPlanilla.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        tablaPlanilla.getTableHeader().setBackground(new Color(235, 240, 247));
        tablaPlanilla.getTableHeader().setForeground(new Color(24, 61, 110));
        tablaPlanilla.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPlanilla = new JScrollPane(tablaPlanilla);
        scrollPlanilla.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        JPanel vistaPlanilla = new JPanel(new BorderLayout(0, 6));
        vistaPlanilla.setBackground(Color.WHITE);
        JLabel ayudaPlanilla = new JLabel("Editá las notas directamente en las celdas abiertas. Las filas corresponden a alumnos inscriptos.");
        ayudaPlanilla.setForeground(new Color(90, 104, 122));
        ayudaPlanilla.setBorder(BorderFactory.createEmptyBorder(8, 16, 2, 8));
        vistaPlanilla.add(ayudaPlanilla, BorderLayout.NORTH);
        vistaPlanilla.add(scrollPlanilla, BorderLayout.CENTER);
        vistas.addTab("Planilla editable", vistaPlanilla);
        vistas.addTab("Detalle", scroll);
        add(vistas, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        acciones.setBackground(Color.WHITE);
        JButton nuevaEvaluacion = new JButton("Agregar tema / columna");
        JButton cargarNota = new JButton("Cargar nota");
        JButton modificar = new JButton("Modificar nota");
        JButton historial = new JButton("Historial");
        JButton borrar = new JButton("Eliminar nota");
        nuevaEvaluacion.addActionListener(e -> crearEvaluacion());
        cargarNota.addActionListener(e -> cargarNota());
        modificar.addActionListener(e -> modificarNota());
        historial.addActionListener(e -> mostrarHistorial());
        borrar.addActionListener(e -> eliminarNota());
        boolean edita = servicios.Permisos_servicio.editarCalificaciones(usuario);
        nuevaEvaluacion.setVisible(edita);
        cargarNota.setVisible(edita);
        modificar.setVisible(edita);
        borrar.setVisible(edita);
        JButton cerrarEvaluacion = new JButton("Cerrar evaluación"); cerrarEvaluacion.setVisible(edita);
        cerrarEvaluacion.addActionListener(e -> {
            JComboBox<ItemCombo> opciones = new JComboBox<>(); java.util.Set<Integer> ids = new java.util.HashSet<>();
            for (var fila : filasActuales) if ("Abierta".equals(fila.columna().getEstado()) && ids.add(fila.columna().getId_columna_notas())) opciones.addItem(new ItemCombo(fila.columna().getId_columna_notas(), fila.columna().getNombre_materia() + " · " + fila.columna().getTitulo()));
            if (opciones.getItemCount() == 0) { aviso("No hay evaluaciones abiertas."); return; }
            if (JOptionPane.showConfirmDialog(this, opciones, "Cerrar evaluación", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) ejecutar(() -> { servicio.cerrarEvaluacion(((ItemCombo) opciones.getSelectedItem()).getId()); return true; }, resultado -> cargarTabla());
        });
        acciones.add(cerrarEvaluacion);
        JButton resultadoMateria = new JButton("Resultado de materia"); resultadoMateria.setVisible(edita);
        resultadoMateria.addActionListener(e -> cerrarMateria()); acciones.add(resultadoMateria);
        acciones.add(nuevaEvaluacion);
        acciones.add(cargarNota);
        acciones.add(modificar);
        acciones.add(historial);
        acciones.add(borrar);
        add(acciones, BorderLayout.SOUTH);
        cargarCursos();
    }

    // llenamos el selector una sola vez y consultamos solo los cursos visibles.
    private void cargarCursos() {
        servicios.Auditoria_servicio.registrar("CONSULTAR", "Calificaciones", "Actualizar cursos");
        Integer anterior =
                cmbCurso.getSelectedItem() instanceof ItemCombo
                        ? ((ItemCombo) cmbCurso.getSelectedItem()).getId()
                        : null;
        int version = ++versionCursos;
        versionTabla++;
        if (cargaTabla != null) {
            cargaTabla.cancel(true);
        }
        cmbCurso.setEnabled(false);
        estadoCarga.setText("Cargando cursos…");
        new javax.swing.SwingWorker<List<Curso>, Void>() {
            protected List<Curso> doInBackground() {
                return cursoDao.listar(usuarioLogueado);
            }

            protected void done() {
                if (version != versionCursos) {
                    return;
                }
                try {
                    List<Curso> cursos = get();
                    llenandoCursos = true;
                    cmbCurso.removeAllItems();
                    for (Curso c : cursos) {
                        String nombre =
                                "Curso "
                                        + c.getId_curso()
                                        + " · "
                                        + c.getDivision() + " · " + c.getTurno()
                                        + " · "
                                        + valor(c.getNombre_periodo())
                                        + " · "
                                        + c.getAño()
                                        + "°";
                        cmbCurso.addItem(new ItemCombo(c.getId_curso(), nombre));
                        if (anterior != null && anterior.equals(c.getId_curso())) {
                            cmbCurso.setSelectedIndex(cmbCurso.getItemCount() - 1);
                        }
                    }
                    llenandoCursos = false;
                    cmbCurso.setEnabled(true);
                    cargarTabla();
                } catch (Exception e) {
                    estadoCarga.setText("No se pudieron cargar los cursos.");
                    aviso(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } finally {
                    llenandoCursos = false;
                    cmbCurso.setEnabled(true);
                }
            }
        }.execute();
    }

    // consultamos solamente el curso elegido sin detener la ventana.
    private void cargarTabla() {
        if (llenandoCursos) {
            return;
        }
        int version = ++versionTabla;
        if (cargaTabla != null) {
            cargaTabla.cancel(true);
        }
        modelo.setRowCount(0);
        filasCurso = List.of();
        filasActuales = List.of();
        calificacionesPorFila.clear();
        columnasPorFila.clear();
        actualizarPlanilla(List.of());
        ItemCombo cursoItem = (ItemCombo) cmbCurso.getSelectedItem();
        if (cursoItem == null) {
            limpiarFiltros();
            estadoCarga.setText("No hay cursos disponibles.");
            return;
        }
        estadoCarga.setText("Cargando calificaciones…");
        servicios.Auditoria_servicio.registrar(
                "CONSULTAR", "Calificaciones", "Curso " + cursoItem.getTexto());
        cargaTabla =
                new javax.swing.SwingWorker<List<dao.ConsultaCalificaciones_dao.Fila>, Void>() {
                    protected List<dao.ConsultaCalificaciones_dao.Fila> doInBackground() {
                        return new dao.ConsultaCalificaciones_dao()
                                .listar(cursoItem.getId(), usuarioLogueado);
                    }

                    protected void done() {
                        // una consulta anterior no debe reemplazar el curso que elegimos después.
                        if (isCancelled() || version != versionTabla) {
                            return;
                        }
                        try {
                            List<dao.ConsultaCalificaciones_dao.Fila> filas = get();
                            filasCurso = filas;
                            actualizarOpcionesFiltros(filas);
                            aplicarFiltros();
                        } catch (Exception e) {
                            estadoCarga.setText("No se pudieron cargar las calificaciones.");
                            aviso(
                                    e.getCause() == null
                                            ? e.getMessage()
                                            : e.getCause().getMessage());
                        }
                    }
                };
        cargaTabla.execute();
    }

    private void actualizarOpcionesFiltros(List<dao.ConsultaCalificaciones_dao.Fila> filas) {
        String materiaAnterior = (String) cmbMateria.getSelectedItem();
        Integer evaluacionAnterior = cmbEvaluacion.getSelectedItem() instanceof ItemCombo item
                ? item.getId() : 0;
        llenandoFiltros = true;
        cmbMateria.removeAllItems();
        cmbMateria.addItem("Todas las materias");
        filas.stream().map(f -> f.columna().getNombre_materia()).distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER).forEach(cmbMateria::addItem);
        if (materiaAnterior != null) cmbMateria.setSelectedItem(materiaAnterior);
        llenandoFiltros = false;
        actualizarEvaluacionesFiltro(evaluacionAnterior);
    }

    private void actualizarEvaluacionesFiltro() {
        Integer anterior = cmbEvaluacion.getSelectedItem() instanceof ItemCombo item ? item.getId() : 0;
        actualizarEvaluacionesFiltro(anterior);
    }

    private void actualizarEvaluacionesFiltro(Integer evaluacionAnterior) {
        String materia = (String) cmbMateria.getSelectedItem();
        llenandoFiltros = true;
        cmbEvaluacion.removeAllItems();
        cmbEvaluacion.addItem(new ItemCombo(0, "Todas las evaluaciones"));
        filasCurso.stream()
                .filter(f -> materia == null || "Todas las materias".equals(materia)
                        || materia.equals(f.columna().getNombre_materia()))
                .map(dao.ConsultaCalificaciones_dao.Fila::columna)
                .collect(java.util.stream.Collectors.toMap(
                        Columna_Notas::getId_columna_notas, c -> c,
                        (primera, ignorada) -> primera, LinkedHashMap::new))
                .values().forEach(c -> cmbEvaluacion.addItem(new ItemCombo(
                        c.getId_columna_notas(), c.getTitulo() + " · " + c.getTipo()
                                + (c.getFecha_evaluacion() == null ? "" : " · " + c.getFecha_evaluacion()))));
        if (evaluacionAnterior != null) {
            for (int i = 0; i < cmbEvaluacion.getItemCount(); i++) {
                if (evaluacionAnterior.equals(cmbEvaluacion.getItemAt(i).getId())) {
                    cmbEvaluacion.setSelectedIndex(i);
                    break;
                }
            }
        }
        llenandoFiltros = false;
    }

    private void limpiarFiltros() {
        llenandoFiltros = true;
        cmbMateria.removeAllItems();
        cmbMateria.addItem("Todas las materias");
        cmbEvaluacion.removeAllItems();
        cmbEvaluacion.addItem(new ItemCombo(0, "Todas las evaluaciones"));
        llenandoFiltros = false;
    }

    private void aplicarFiltros() {
        String materia = (String) cmbMateria.getSelectedItem();
        ItemCombo evaluacion = (ItemCombo) cmbEvaluacion.getSelectedItem();
        int idEvaluacion = evaluacion == null ? 0 : evaluacion.getId();
        filasActuales = filasCurso.stream()
                .filter(f -> materia == null || "Todas las materias".equals(materia)
                        || materia.equals(f.columna().getNombre_materia()))
                .filter(f -> idEvaluacion == 0 || idEvaluacion == f.columna().getId_columna_notas())
                .toList();
        modelo.setRowCount(0);
        calificacionesPorFila.clear();
        columnasPorFila.clear();
        actualizarPlanilla(filasActuales);
        for (dao.ConsultaCalificaciones_dao.Fila dato : filasActuales) {
            Columna_Notas columna = dato.columna();
            Calificacion nota = dato.nota();
            String resultado = nota == null ? "Pendiente"
                    : nota.getNota() >= columna.getNota_aprobacion() ? "Aprobado" : "Desaprobado";
            int fila = modelo.getRowCount();
            modelo.addRow(new Object[] {
                dato.alumno(), dato.dni(), columna.getNombre_materia() + " · " + columna.getTitulo(),
                columna.getTipo(), nota == null ? "—" : nota.getNota(), columna.getNota_maxima(), resultado,
                nota == null || nota.getFecha_carga() == null ? "—"
                        : nota.getFecha_carga().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                nota == null ? "" : nota.getObservaciones()
            });
            if (nota != null) calificacionesPorFila.put(fila, nota);
            columnasPorFila.put(fila, columna);
        }
        estadoCarga.setText(filasActuales.size() + " registros");
        if (filasActuales.isEmpty()) {
            modelo.addRow(new Object[] {"No hay calificaciones para estos filtros", "", "", "", "", "", "", "", ""});
        }
    }

    // armamos una fila por alumno y una columna por evaluación, como en una planilla.
    private void actualizarPlanilla(List<dao.ConsultaCalificaciones_dao.Fila> filas) {
        columnasPlanilla.clear();
        inscripcionesPlanilla.clear();
        for (var fila : filas) {
            columnasPlanilla.putIfAbsent(fila.columna().getId_columna_notas(), fila.columna());
            inscripcionesPlanilla.putIfAbsent(fila.idInscripcion(), fila);
        }
        List<Object> encabezados = new java.util.ArrayList<>(List.of("Alumno", "DNI"));
        for (Columna_Notas columna : columnasPlanilla.values()) {
            encabezados.add(columna.getNombre_materia() + " · " + columna.getTitulo()
                    + " (máx. " + columna.getNota_maxima() + ")");
        }
        modeloPlanilla.setDataVector(new Object[0][encabezados.size()], encabezados.toArray());
        Map<String, Object> notas = new HashMap<>();
        for (var fila : filas) {
            if (fila.nota() != null) {
                notas.put(fila.idInscripcion() + ":" + fila.columna().getId_columna_notas(), fila.nota().getNota());
            }
        }
        for (var entrada : inscripcionesPlanilla.entrySet()) {
            Object[] valores = new Object[encabezados.size()];
            valores[0] = entrada.getValue().alumno();
            valores[1] = entrada.getValue().dni();
            int indice = 2;
            for (Integer idColumna : columnasPlanilla.keySet()) {
                Object nota = notas.get(entrada.getKey() + ":" + idColumna);
                valores[indice++] = nota == null ? "" : nota;
            }
            modeloPlanilla.addRow(valores);
        }
        tablaPlanilla.getColumnModel().getColumn(0).setPreferredWidth(190);
        tablaPlanilla.getColumnModel().getColumn(1).setPreferredWidth(100);
        for (int i = 2; i < tablaPlanilla.getColumnCount(); i++) {
            tablaPlanilla.getColumnModel().getColumn(i).setPreferredWidth(150);
        }
    }

    // guardamos la celda mediante la misma validación e historial que los formularios.
    private void guardarCeldaPlanilla(int fila, int indiceColumna, Object anterior, Object nuevo) {
        if (ocupado) { cargarTabla(); return; }
        if (indiceColumna < 0 || indiceColumna >= columnasPlanilla.size()) return;
        String valor = nuevo == null ? "" : nuevo.toString().trim();
        if (valor.isEmpty()) { cargarTabla(); return; }
        var alumno = new ArrayList<>(inscripcionesPlanilla.values()).get(fila);
        Columna_Notas columna = new ArrayList<>(columnasPlanilla.values()).get(indiceColumna);
        Calificacion existente = filasActuales.stream()
                .filter(d -> d.idInscripcion() == alumno.idInscripcion()
                        && d.columna().getId_columna_notas().equals(columna.getId_columna_notas()))
                .map(dao.ConsultaCalificaciones_dao.Fila::nota).filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
        if (existente != null && String.valueOf(anterior).equals(valor)) return;
        String motivo = "";
        if (existente != null) {
            motivo = JOptionPane.showInputDialog(this, "Motivo de la corrección de la nota:", "Editar nota", JOptionPane.QUESTION_MESSAGE);
            if (motivo == null) { cargarTabla(); return; }
        }
        final String razon = motivo;
        ocupado = true;
        estadoCarga.setText("Guardando nota…");
        new javax.swing.SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                if (existente == null) servicio.registrarNota(alumno.idInscripcion(), columna.getId_columna_notas(), valor, "");
                else servicio.modificarNota(existente.getId_calificacion(), valor, razon);
                return null;
            }
            protected void done() {
                ocupado = false;
                try {
                    get();
                    estadoCarga.setText("Nota guardada");
                } catch (Exception e) {
                    estadoCarga.setText("No se pudo guardar la nota");
                    aviso(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                } finally {
                    cargarTabla();
                }
            }
        }.execute();
    }

    // ejecutamos las operaciones sin dejar la ventana esperando la conexión
    private <T> void ejecutar(
            java.util.concurrent.Callable<T> tarea, java.util.function.Consumer<T> listo) {
        if (ocupado) {
            return;
        }
        ocupado = true;
        estadoCarga.setText("Procesando…");
        new javax.swing.SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            protected void done() {
                ocupado = false;
                try {
                    T resultado = get();
                    estadoCarga.setText("Operación completada");
                    listo.accept(resultado);
                } catch (Exception e) {
                    estadoCarga.setText("No se pudo completar la operación");
                    aviso(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                }
            }
        }.execute();
    }

    // prepara los datos necesarios para registrar una evaluación
    private void crearEvaluacion() {
        if (ocupado) return;
        ItemCombo curso = (ItemCombo) cmbCurso.getSelectedItem();
        if (curso == null) {
            aviso("Primero seleccioná un curso.");
            return;
        }
        JComboBox<ItemCombo> materia = new JComboBox<>();
        try {
            for (var m : new servicios.Resultado_Academico_servicio().materias(curso.getId(), usuarioLogueado.getId_usuario())) materia.addItem(new ItemCombo(((Number) m.get("id_materia")).intValue(), m.get("nombre_materia").toString()));
        } catch (Exception error) { aviso(error.getMessage()); return; }
        if (materia.getItemCount() == 0) { aviso("El curso no tiene materias asignadas."); return; }
        JTextField titulo = new JTextField();
        JComboBox<String> tipo =
                new JComboBox<>(
                        new String[] {"Evaluación", "Trabajo Práctico", "Recuperatorio", "Otro"});
        JTextField maximo = new JTextField("10");
        JTextField aprobacion = new JTextField("6");
        JTextField fecha = new JTextField(LocalDate.now().toString());
        JPanel form =
                formulario(
                        new String[] {
                            "Materia:",
                            "Título:",
                            "Tipo:",
                            "Nota máxima:",
                            "Nota de aprobación:",
                            "Fecha (AAAA-MM-DD):"
                        },
                        new Component[] {materia, titulo, tipo, maximo, aprobacion, fecha});
        if (JOptionPane.showConfirmDialog(
                        this, form, "Nueva evaluación", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        String nombre = titulo.getText(), tipoElegido = (String) tipo.getSelectedItem();
        String max = maximo.getText(), apr = aprobacion.getText(), dia = fecha.getText();
        ejecutar(
                () -> {
                    servicio.crearEvaluacion(curso.getId(), ((ItemCombo) materia.getSelectedItem()).getId(), nombre, tipoElegido, max, apr, dia);
                    return true;
                },
                resultado -> {
                    cargarTabla();
                    JOptionPane.showMessageDialog(this, "Evaluación registrada.");
                });
    }

    // pedimos la materia y el alumno para calcular el resultado con las evaluaciones cerradas
    private void cerrarMateria() {
        if (ocupado || !(cmbCurso.getSelectedItem() instanceof ItemCombo curso)) return;
        JComboBox<ItemCombo> materias = new JComboBox<>(), alumnos = new JComboBox<>();
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (var fila : filasActuales) if (ids.add(fila.idInscripcion())) alumnos.addItem(new ItemCombo(fila.idInscripcion(), fila.alumno()));
        ejecutar(() -> new servicios.Resultado_Academico_servicio().materias(curso.getId(), usuarioLogueado.getId_usuario()), lista -> {
            for (var materia : lista) materias.addItem(new ItemCombo(((Number) materia.get("id_materia")).intValue(), materia.get("nombre_materia").toString()));
            if (materias.getItemCount() == 0 || alumnos.getItemCount() == 0) { aviso("El curso necesita materias y alumnos inscriptos."); return; }
            JTextField motivo = new JTextField();
            Object[] campos = {"Alumno", alumnos, "Materia", materias, "Observación del resultado", motivo};
            if (JOptionPane.showConfirmDialog(this, campos, "Resultado final por materia", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) ejecutar(() -> new servicios.Resultado_Academico_servicio().cerrar(((ItemCombo) alumnos.getSelectedItem()).getId(), ((ItemCombo) materias.getSelectedItem()).getId(), motivo.getText(), usuarioLogueado.getId_usuario()), resultado -> { JOptionPane.showMessageDialog(this, resultado); cargarTabla(); });
        });
    }

    // prepara la inscripción y la evaluación para registrar una nota
    private void cargarNota() {
        if (ocupado) return;
        java.util.Map<Integer, Columna_Notas> columnas = new java.util.LinkedHashMap<>();
        java.util.Map<Integer, String> alumnos = new java.util.LinkedHashMap<>();
        for (dao.ConsultaCalificaciones_dao.Fila fila : filasActuales) {
            if ("Abierta".equals(fila.columna().getEstado())) {
                columnas.put(fila.columna().getId_columna_notas(), fila.columna());
                alumnos.put(fila.idInscripcion(), fila.alumno() + " · DNI " + fila.dni());
            }
        }
        if (columnas.isEmpty() || alumnos.isEmpty()) {
            aviso(
                    "El curso necesita una evaluación abierta y un alumno inscripto. Esperá a que"
                            + " termine la carga.");
            return;
        }
        JComboBox<ItemCombo> evaluaciones = new JComboBox<>();
        columnas.forEach((id, col) -> evaluaciones.addItem(new ItemCombo(id, col.getTitulo())));
        JComboBox<ItemCombo> inscripciones = new JComboBox<>();
        alumnos.forEach((id, nombre) -> inscripciones.addItem(new ItemCombo(id, nombre)));
        JTextField nota = new JTextField();
        JTextField observaciones = new JTextField();
        JPanel form =
                formulario(
                        new String[] {"Evaluación:", "Alumno:", "Nota:", "Observaciones:"},
                        new Component[] {evaluaciones, inscripciones, nota, observaciones});
        if (JOptionPane.showConfirmDialog(
                        this, form, "Cargar calificación", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        int columna = ((ItemCombo) evaluaciones.getSelectedItem()).getId();
        int inscripcion = ((ItemCombo) inscripciones.getSelectedItem()).getId();
        String valor = nota.getText(), detalle = observaciones.getText();
        ejecutar(
                () -> {
                    servicio.registrarNota(inscripcion, columna, valor, detalle);
                    return true;
                },
                resultado -> {
                    cargarTabla();
                    JOptionPane.showMessageDialog(this, "Calificación registrada.");
                });
    }

    // guarda la corrección de la nota junto con el motivo del cambio
    private void modificarNota() {
        if (ocupado) return;
        Calificacion nota = notaSeleccionada();
        if (nota == null) {
            aviso("Seleccioná una calificación cargada.");
            return;
        }
        JTextField valor = new JTextField(String.valueOf(nota.getNota()));
        JTextField motivo = new JTextField();
        JPanel form =
                formulario(
                        new String[] {"Nueva nota:", "Motivo del cambio:"},
                        new Component[] {valor, motivo});
        if (JOptionPane.showConfirmDialog(
                        this, form, "Modificar calificación", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        String nueva = valor.getText(), razon = motivo.getText();
        ejecutar(
                () -> {
                    servicio.modificarNota(nota.getId_calificacion(), nueva, razon);
                    return true;
                },
                resultado -> {
                    cargarTabla();
                    JOptionPane.showMessageDialog(this, "Calificación e historial actualizados.");
                });
    }

    // consulta los cambios anteriores de la nota seleccionada
    private void mostrarHistorial() {
        if (ocupado) return;
        Calificacion nota = notaSeleccionada();
        if (nota == null) {
            aviso("Seleccioná una calificación cargada.");
            return;
        }
        ejecutar(
                () -> servicio.historial(nota.getId_calificacion()),
                lineas -> {
                    JTextArea detalle =
                            new JTextArea(
                                    lineas.isEmpty()
                                            ? "Esta calificación todavía no tiene modificaciones."
                                            : String.join("\n\n", lineas),
                                    12,
                                    55);
                    detalle.setLineWrap(true);
                    detalle.setWrapStyleWord(true);
                    detalle.setEditable(false);
                    detalle.setCaretPosition(0);
                    JOptionPane.showMessageDialog(
                            this,
                            new JScrollPane(detalle),
                            "Historial",
                            JOptionPane.INFORMATION_MESSAGE);
                    servicios.Auditoria_servicio.registrar(
                            "CONSULTAR",
                            "Calificaciones",
                            "Historial de nota " + nota.getId_calificacion());
                });
    }

    // verifica la selección antes de quitar la nota
    private void eliminarNota() {
        if (ocupado) return;
        Calificacion nota = notaSeleccionada();
        if (nota == null) {
            aviso("Seleccioná una calificación cargada.");
            return;
        }
        if (JOptionPane.showConfirmDialog(
                        this,
                        "¿Eliminar la calificación seleccionada?",
                        "Confirmar",
                        JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        ejecutar(
                () -> {
                    servicio.eliminarNota(nota.getId_calificacion());
                    return true;
                },
                resultado -> cargarTabla());
    }

    private Calificacion notaSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : calificacionesPorFila.get(tabla.convertRowIndexToModel(fila));
    }

    // prepara los campos que se completan en el formulario
    private JPanel formulario(String[] etiquetas, Component[] campos) {
        JPanel panel = new JPanel(new GridLayout(etiquetas.length, 2, 8, 8));
        for (int i = 0; i < etiquetas.length; i++) {
            panel.add(new JLabel(etiquetas[i]));
            panel.add(campos[i]);
        }
        return panel;
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "Sin dato" : texto.trim();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Calificaciones", JOptionPane.WARNING_MESSAGE);
    }
}
