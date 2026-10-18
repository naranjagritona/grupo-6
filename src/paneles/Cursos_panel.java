package paneles;

import clases.Curso;
import clases.Periodo_Lectivo;
import clases.Materia;
import clases.Docente;
import dao.Curso_dao;
import dao.Periodo_Lectivo_dao;
import dao.Materia_dao;
import dao.Docente_dao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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

public class Cursos_panel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Curso_dao cursoDao;
    private final DefaultTableModel modelo;
    private final JTable tablaCursos;
    private final List<Curso> cursosCargados = new ArrayList<>();

    public Cursos_panel() {
        cursoDao = new Curso_dao();

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        // Encabezado
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(new Color(24, 61, 110));
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titulo = new JLabel("Gestión de Cursos");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Tahoma", Font.BOLD, 23));

        JLabel subtitulo = new JLabel("Consulta de cursos registrados");
        subtitulo.setForeground(new Color(220, 230, 245));
        subtitulo.setFont(new Font("Tahoma", Font.PLAIN, 13));

        JPanel textosTitulo = new JPanel();
        textosTitulo.setOpaque(false);
        textosTitulo.setLayout(new BoxLayout(textosTitulo, BoxLayout.Y_AXIS));
        textosTitulo.add(titulo);
        textosTitulo.add(Box.createVerticalStrut(5));
        textosTitulo.add(subtitulo);

        panelTitulo.add(textosTitulo, BorderLayout.WEST);
        add(panelTitulo, BorderLayout.NORTH);

        // Tabla de cursos
        String[] columnas = {
            "Período", "Materia", "Docente", "Año",
            "División", "Modalidad", "Turno", "Cupo máximo",
            "Fecha de inicio", "Fecha de fin", "Estado"
        };

        modelo = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaCursos = new JTable(modelo);
        tablaCursos.setRowHeight(28);
        tablaCursos.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaCursos.setSelectionBackground(new Color(220, 232, 248));
        tablaCursos.setSelectionForeground(Color.BLACK);
        tablaCursos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaCursos.setFillsViewportHeight(true);

        tablaCursos.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        tablaCursos.getTableHeader().setBackground(new Color(235, 240, 247));
        tablaCursos.getTableHeader().setForeground(new Color(24, 61, 110));
        tablaCursos.getTableHeader().setReorderingAllowed(false);

        int[] anchos = {120, 130, 160, 60, 80, 100, 90, 100, 120, 120, 100};
        for (int i = 0; i < anchos.length; i++) {
            tablaCursos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tablaCursos);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        // Botones de acciones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JButton btnNuevo = new JButton("Nuevo curso");
        btnNuevo.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnNuevo.setBackground(new Color(35, 130, 75));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> mostrarFormularioCurso(null));

        JButton btnModificar = new JButton("Modificar curso");
        btnModificar.addActionListener(e -> modificarCurso());

        JButton btnCancelarCurso = new JButton("Cancelar curso");
        btnCancelarCurso.addActionListener(e -> cancelarCurso());

        JButton btnEliminar = new JButton("Eliminar curso");
        btnEliminar.addActionListener(e -> eliminarCurso());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnActualizar.setBackground(new Color(24, 61, 110));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.addActionListener(e -> cargarCursos());

        panelBotones.add(btnNuevo);
        panelBotones.add(btnModificar);
        panelBotones.add(btnCancelarCurso);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        cargarCursos();
    }

    private void cargarCursos() {
        modelo.setRowCount(0);

        try {
            List<Curso> cursos = cursoDao.obtenerTodos();
            cursosCargados.clear();
            cursosCargados.addAll(cursos);

            for (Curso curso : cursos) {
                String nombreDocente = curso.getNombre_docente();
                if (nombreDocente == null || nombreDocente.trim().isEmpty()) {
                    nombreDocente = "Docente desconocido";
                } else {
                    nombreDocente = nombreDocente.trim();
                }

                modelo.addRow(new Object[] {
                    curso.getNombre_periodo() != null
                        ? curso.getNombre_periodo() : "Período desconocido",
                    curso.getNombre_materia() != null
                        ? curso.getNombre_materia() : "Materia desconocida",
                    nombreDocente,
                    curso.getAño(),
                    curso.getDivision(),
                    curso.getModalidad(),
                    curso.getTurno(),
                    curso.getCupo_maximo(),
                    formatearFecha(curso.getFecha_inicio()),
                    formatearFecha(curso.getFecha_fin()),
                    curso.getEstado()
                });
            }
        } catch (Exception error) {
            error.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los cursos.\n" + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void mostrarFormularioCurso(Curso cursoExistente) {
        try {
            Periodo_Lectivo_dao periodoDao = new Periodo_Lectivo_dao();
            Materia_dao materiaDao = new Materia_dao();
            Docente_dao docenteDao = new Docente_dao();

            List<Periodo_Lectivo> periodos = periodoDao.obtenerTodos();
            List<Materia> materias = materiaDao.obtenerTodos();
            List<Docente> docentes = docenteDao.obtenerTodos();

            if (periodos.isEmpty() || materias.isEmpty() || docentes.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Necesitás tener al menos un período, una materia y un docente registrados.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            JComboBox<Periodo_Lectivo> cbPeriodo = new JComboBox<>();
            for (Periodo_Lectivo p : periodos) {
                cbPeriodo.addItem(p);
            }

            JComboBox<Materia> cbMateria = new JComboBox<>();
            for (Materia m : materias) {
                cbMateria.addItem(m);
            }

            JComboBox<Docente> cbDocente = new JComboBox<>();
            for (Docente d : docentes) {
                cbDocente.addItem(d);
            }

            cbPeriodo.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                    );
                    if (value instanceof Periodo_Lectivo) {
                        setText(((Periodo_Lectivo) value).getNombre());
                    }
                    return this;
                }
            });

            cbMateria.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                    );
                    if (value instanceof Materia) {
                        setText(((Materia) value).getNombre_materia());
                    }
                    return this;
                }
            });

            cbDocente.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                    );
                    if (value instanceof Docente) {
                        Docente d = (Docente) value;
                        setText(d.getNombre() + " " + d.getApellido());
                    }
                    return this;
                }
            });

            JTextField txtAnio = new JTextField();
            JTextField txtDivision = new JTextField();
            JComboBox<String> cbModalidad = new JComboBox<>(
                new String[] {"Presencial", "Virtual", "Híbrida"}
            );
            JComboBox<String> cbTurno = new JComboBox<>(
                new String[] {"Mañana", "Tarde", "Noche"}
            );
            JTextField txtCupo = new JTextField();
            JTextField txtFechaInicio = new JTextField();
            JTextField txtFechaFin = new JTextField();

            txtFechaInicio.setToolTipText("Formato: AAAA-MM-DD");
            txtFechaFin.setToolTipText("Formato: AAAA-MM-DD");

            JComboBox<String> cbEstado = new JComboBox<>(
                new String[] {"Activo", "Inactivo", "Cancelado"}
            );

            // Si se está modificando, cargar los datos del curso seleccionado.
            if (cursoExistente != null) {
                txtAnio.setText(String.valueOf(cursoExistente.getAño()));
                txtDivision.setText(cursoExistente.getDivision());
                txtCupo.setText(String.valueOf(cursoExistente.getCupo_maximo()));
                txtFechaInicio.setText(formatearFecha(cursoExistente.getFecha_inicio()));
                txtFechaFin.setText(formatearFecha(cursoExistente.getFecha_fin()));

                seleccionarPeriodo(cbPeriodo, cursoExistente.getId_periodo());
                seleccionarMateria(cbMateria, cursoExistente.getId_materia());
                seleccionarDocente(cbDocente, cursoExistente.getId_docente());

                seleccionarTexto(cbModalidad, cursoExistente.getModalidad());
                seleccionarTexto(cbTurno, cursoExistente.getTurno());
                seleccionarTexto(cbEstado, cursoExistente.getEstado());
            }

            JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
            formulario.add(new JLabel("Período lectivo:"));
            formulario.add(cbPeriodo);
            formulario.add(new JLabel("Materia:"));
            formulario.add(cbMateria);
            formulario.add(new JLabel("Docente:"));
            formulario.add(cbDocente);
            formulario.add(new JLabel("Año:"));
            formulario.add(txtAnio);
            formulario.add(new JLabel("División:"));
            formulario.add(txtDivision);
            formulario.add(new JLabel("Modalidad:"));
            formulario.add(cbModalidad);
            formulario.add(new JLabel("Turno:"));
            formulario.add(cbTurno);
            formulario.add(new JLabel("Cupo máximo:"));
            formulario.add(txtCupo);
            formulario.add(new JLabel("Fecha de inicio (AAAA-MM-DD):"));
            formulario.add(txtFechaInicio);
            formulario.add(new JLabel("Fecha de fin (AAAA-MM-DD):"));
            formulario.add(txtFechaFin);
            formulario.add(new JLabel("Estado:"));
            formulario.add(cbEstado);

            int resultado = JOptionPane.showConfirmDialog(
                this,
                formulario,
                cursoExistente == null ? "Registrar nuevo curso" : "Modificar curso",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (resultado != JOptionPane.OK_OPTION) {
                return;
            }

            String anioTexto = txtAnio.getText().trim();
            String division = txtDivision.getText().trim();
            String cupoTexto = txtCupo.getText().trim();
            String inicioTexto = txtFechaInicio.getText().trim();
            String finTexto = txtFechaFin.getText().trim();

            if (anioTexto.isEmpty() || division.isEmpty() || cupoTexto.isEmpty()
                    || inicioTexto.isEmpty() || finTexto.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this, "Completá todos los campos.", "Validación",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int anio;
            int cupo;
            LocalDate fechaInicio;
            LocalDate fechaFin;

            try {
                anio = Integer.parseInt(anioTexto);
                cupo = Integer.parseInt(cupoTexto);
                fechaInicio = LocalDate.parse(inicioTexto);
                fechaFin = LocalDate.parse(finTexto);
            } catch (NumberFormatException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Revisá el año, el cupo y las fechas. Usá el formato AAAA-MM-DD.",
                    "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (anio <= 0 || cupo <= 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "El año y el cupo máximo deben ser mayores que cero.",
                    "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (fechaFin.isBefore(fechaInicio)) {
                JOptionPane.showMessageDialog(
                    this,
                    "La fecha de fin no puede ser anterior a la fecha de inicio.",
                    "Fechas inválidas",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Periodo_Lectivo periodo = (Periodo_Lectivo) cbPeriodo.getSelectedItem();
            Materia materia = (Materia) cbMateria.getSelectedItem();
            Docente docente = (Docente) cbDocente.getSelectedItem();

            if (periodo == null || materia == null || docente == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Seleccioná un período, una materia y un docente.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Curso curso = cursoExistente != null ? cursoExistente : new Curso();
            curso.setId_periodo(periodo.getId_periodo());
            curso.setId_materia(materia.getId_materia());
            curso.setId_docente(docente.getId_docente());
            curso.setAño(anio);
            curso.setDivision(division);
            curso.setModalidad((String) cbModalidad.getSelectedItem());
            curso.setTurno((String) cbTurno.getSelectedItem());
            curso.setCupo_maximo(cupo);
            curso.setFecha_inicio(fechaInicio);
            curso.setFecha_fin(fechaFin);
            curso.setEstado((String) cbEstado.getSelectedItem());

            if (cursoExistente == null) {
                cursoDao.insertar(curso);
            } else {
                cursoDao.modificar(curso);
            }

            cargarCursos();
            JOptionPane.showMessageDialog(
                this,
                cursoExistente == null
                    ? "Se agrego el cursos correctamente."
                    : "Se modifico el curso correctamente.",
                "Gestión de cursos",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception error) {
            error.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Ocurrió un error al preparar el formulario:\n" + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void seleccionarPeriodo(JComboBox<Periodo_Lectivo> combo, Integer idPeriodo) {
        if (idPeriodo == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Periodo_Lectivo periodo = combo.getItemAt(i);
            if (periodo.getId_periodo() == idPeriodo) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarMateria(JComboBox<Materia> combo, Integer idMateria) {
        if (idMateria == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Materia materia = combo.getItemAt(i);
            if (materia.getId_materia() == idMateria) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarDocente(JComboBox<Docente> combo, Integer idDocente) {
        if (idDocente == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Docente docente = combo.getItemAt(i);
            if (docente.getId_docente() == idDocente) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarTexto(JComboBox<String> combo, String valor) {
        if (valor != null) {
            combo.setSelectedItem(valor);
        }
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.toString() : "";
    }

    private Curso obtenerCursoSeleccionado() {
        int filaVista = tablaCursos.getSelectedRow();

        if (filaVista == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Seleccioná un curso de la tabla.",
                "Curso no seleccionado",
                JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        int filaModelo = tablaCursos.convertRowIndexToModel(filaVista);
        if (filaModelo < 0 || filaModelo >= cursosCargados.size()) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo identificar el curso seleccionado. Actualizá la tabla e intentá de nuevo.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        return cursosCargados.get(filaModelo);
    }

    private void modificarCurso() {
        Curso curso = obtenerCursoSeleccionado();
        if (curso != null) {
            mostrarFormularioCurso(curso);
        }
    }

    private void cancelarCurso() {
        Curso curso = obtenerCursoSeleccionado();
        if (curso == null) {
            return;
        }

        if ("Cancelado".equalsIgnoreCase(curso.getEstado())) {
            JOptionPane.showMessageDialog(
                this,
                "Este curso ya está cancelado.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Querés cancelar el curso seleccionado?\n"
                + "El curso se conservará, pero su estado cambiará a Cancelado.",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        curso.setEstado("Cancelado");
        cursoDao.modificar(curso);
        cargarCursos();

        JOptionPane.showMessageDialog(
            this,
            "Se cancelo el curso correctamente.",
            "Cancelar curso",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void eliminarCurso() {
        Curso curso = obtenerCursoSeleccionado();
        if (curso == null) {
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que querés eliminar este curso?\n"
                + "Esta acción puede no ser reversible.",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        cursoDao.eliminar(curso.getId_curso());
        cargarCursos();

        JOptionPane.showMessageDialog(
            this,
            "Se elimino el curso correctamente.",
            "Eliminar curso",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
