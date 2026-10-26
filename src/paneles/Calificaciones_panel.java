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
import java.util.HashMap;
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
    private List<dao.ConsultaCalificaciones_dao.Fila> filasActuales = List.of();
    private boolean llenandoCursos;
    private int versionCursos;
    private int versionTabla;
    private javax.swing.SwingWorker<?, ?> cargaTabla;
    private final JLabel estadoCarga = new JLabel(" ");
    private final JComboBox<ItemCombo> cmbCurso = new JComboBox<>();
    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[] {
                        "Alumno",
                        "DNI",
                        "Evaluación",
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
        boolean edita = usuario != null && (usuario.getId_rol() == 1 || usuario.getId_rol() == 2);
        nuevaEvaluacion.setVisible(edita);
        cargarNota.setVisible(edita);
        modificar.setVisible(edita);
        borrar.setVisible(edita);
        acciones.add(nuevaEvaluacion);
        acciones.add(cargarNota);
        acciones.add(modificar);
        acciones.add(historial);
        acciones.add(borrar);
        add(acciones, BorderLayout.SOUTH);
        cargarCursos();
    }

    // Llenamos el selector una sola vez y consultamos solo los cursos visibles.
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
                                        + valor(c.getNombre_materia())
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

    // Consultamos solamente el curso elegido sin detener la ventana.
    private void cargarTabla() {
        if (llenandoCursos) {
            return;
        }
        int version = ++versionTabla;
        if (cargaTabla != null) {
            cargaTabla.cancel(true);
        }
        modelo.setRowCount(0);
        filasActuales = List.of();
        calificacionesPorFila.clear();
        columnasPorFila.clear();
        ItemCombo cursoItem = (ItemCombo) cmbCurso.getSelectedItem();
        if (cursoItem == null) {
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
                        // Una consulta anterior no debe reemplazar el curso que elegimos después.
                        if (isCancelled() || version != versionTabla) {
                            return;
                        }
                        try {
                            List<dao.ConsultaCalificaciones_dao.Fila> filas = get();
                            filasActuales = filas;
                            for (dao.ConsultaCalificaciones_dao.Fila dato : filas) {
                                Columna_Notas col = dato.columna();
                                Calificacion nota = dato.nota();
                                String estado =
                                        nota == null
                                                ? "Pendiente"
                                                : nota.getNota() >= col.getNota_aprobacion()
                                                        ? "Aprobado"
                                                        : "Desaprobado";
                                int fila = modelo.getRowCount();
                                modelo.addRow(
                                        new Object[] {
                                            dato.alumno(),
                                            dato.dni(),
                                            col.getTitulo(),
                                            col.getTipo(),
                                            nota == null ? "—" : nota.getNota(),
                                            col.getNota_maxima(),
                                            estado,
                                            nota == null || nota.getFecha_carga() == null
                                                    ? "—"
                                                    : nota.getFecha_carga()
                                                            .format(
                                                                    DateTimeFormatter.ofPattern(
                                                                            "dd/MM/yyyy")),
                                            nota == null ? "" : nota.getObservaciones()
                                        });
                                if (nota != null) {
                                    calificacionesPorFila.put(fila, nota);
                                }
                                columnasPorFila.put(fila, col);
                            }
                            estadoCarga.setText(filas.size() + " registros");
                            if (filas.isEmpty()) {
                                modelo.addRow(
                                        new Object[] {
                                            "Sin inscripciones activas o evaluaciones para este"
                                                    + " curso",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            ""
                                        });
                            }
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
                            "Título:",
                            "Tipo:",
                            "Nota máxima:",
                            "Nota de aprobación:",
                            "Fecha (AAAA-MM-DD):"
                        },
                        new Component[] {titulo, tipo, maximo, aprobacion, fecha});
        if (JOptionPane.showConfirmDialog(
                        this, form, "Nueva evaluación", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
        String nombre = titulo.getText(), tipoElegido = (String) tipo.getSelectedItem();
        String max = maximo.getText(), apr = aprobacion.getText(), dia = fecha.getText();
        ejecutar(
                () -> {
                    servicio.crearEvaluacion(curso.getId(), nombre, tipoElegido, max, apr, dia);
                    return true;
                },
                resultado -> {
                    cargarTabla();
                    JOptionPane.showMessageDialog(this, "Evaluación registrada.");
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
