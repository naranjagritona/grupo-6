package paneles;

import clases.*;

import dao.*;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

// este panel reúne la consulta y la gestión de los cursos.
public class Cursos_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Usuario usuario;
    private final servicios.Curso_servicio dao = new servicios.Curso_servicio();
    // dejamos las celdas sin edición: los cambios se hacen desde el formulario.
    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[] {
                        "Año",
                        "División",
                        "Materias",
                        "Período",
                        "Modalidad",
                        "Turno",
                        "Cupo",
                        "Inicio",
                        "Fin",
                        "Estado"
                    },
                    0) {
                public Class<?> getColumnClass(int columna) {
                    return columna == 2 ? Integer.class : Object.class;
                }
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final List<Curso> cursos = new ArrayList<>();
    private final JLabel estado = new JLabel(" ");
    private boolean cargando;
    private final Timer actualizacionAutomatica = new Timer(60_000, e -> {
        if (isShowing() && !cargando) cargar();
    });

    public Cursos_panel() {
        this(null);
    }

    // armamos el listado, el buscador y los botones según el usuario que ingresó.
    public Cursos_panel(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(Color.WHITE);
        JPanel arriba = new JPanel(new BorderLayout(8, 12));
        JLabel titulo = new JLabel("Cursos");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        arriba.add(titulo, BorderLayout.NORTH);
        JTextField buscar = new JTextField();
        buscar.setToolTipText("Buscar por año, división, período, turno o estado");
        JPanel busqueda = new JPanel(new BorderLayout(8, 8));
        busqueda.add(new JLabel("Buscar cursos:"), BorderLayout.WEST);
        busqueda.add(buscar);
        arriba.add(busqueda);
        add(arriba, BorderLayout.NORTH);
        tabla.setRowSorter(filtro);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setToolTipText("Doble clic para ver las materias y sus docentes");
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int fila = tabla.rowAtPoint(e.getPoint());
                    if (fila >= 0) { tabla.setRowSelectionInterval(fila, fila); detalle(); }
                }
            }
        });
        tabla.setRowHeight(30);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setColumnHeaderView(tabla.getTableHeader());
        add(desplazamiento);
        // filtramos lo que ya está cargado para buscar sin consultar la base en cada letra.
        buscar.getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            public void insertUpdate(DocumentEvent e) {
                                filtrar();
                            }

                            public void removeUpdate(DocumentEvent e) {
                                filtrar();
                            }

                            public void changedUpdate(DocumentEvent e) {
                                filtrar();
                            }

                            private void filtrar() {
                                filtro.setRowFilter(
                                        RowFilter.regexFilter(
                                                "(?iu)" + Pattern.quote(buscar.getText().trim())));
                            }
                        });
        JPanel abajo = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new GridLayout(0, 4, 6, 6));
        // solo el administrador puede crear, editar o cancelar un curso.
        if (servicios.Permisos_servicio.directivo(usuario)) {
            boton(acciones, "Nuevo curso", () -> formulario(null));
            boton(
                    acciones,
                    "Editar",
                    () -> {
                        Curso c = seleccion();
                        if (c != null) formulario(c);
                    });
            boton(
                    acciones,
                    "Cancelar curso",
                    () -> {
                        Curso c = seleccion();
                        if (c != null
                                && JOptionPane.showConfirmDialog(
                                                this,
                                                "¿Cancelar el curso de " + c.getAño() + "° año?",
                                                "Cancelar curso",
                                                JOptionPane.YES_NO_OPTION)
                                        == JOptionPane.YES_OPTION)
                            trabajar(
                                    () -> {
                                        dao.cancelar(c.getId_curso());
                                        return dao.listar(usuario);
                                    });
                    });
        }
        if (servicios.Permisos_servicio.secretario(usuario)) {
            boton(acciones, "Modificar cupo", () -> {
                Curso curso = seleccion(); if (curso == null) return;
                String cupo = JOptionPane.showInputDialog(this, "Cupo máximo:", curso.getCupo_maximo());
                if (cupo != null) trabajar(() -> { dao.modificarCupo(curso.getId_curso(), Integer.parseInt(cupo)); return dao.listar(usuario); });
            });

        }
        boton(acciones, "Actualizar", this::cargar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
        cargar();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        actualizacionAutomatica.start();
    }

    @Override
    public void removeNotify() {
        actualizacionAutomatica.stop();
        super.removeNotify();
    }

    private void boton(JPanel p, String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> accion.run());
        p.add(b);
    }

    // convertimos la fila visible a la original porque la tabla puede estar filtrada.
    private Curso seleccion() {
        int r = tabla.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un curso.");
            return null;
        }
        return cursos.get(tabla.convertRowIndexToModel(r));
    }

    private void detalle() {
        Curso curso = seleccion();
        if (curso != null) new DetalleCurso_dialog(this, curso, usuario, this::cargar).setVisible(true);
    }

    private void cargar() {
        trabajar(() -> dao.listar(usuario));
    }

    // hacemos las consultas en segundo plano para que la ventana siga respondiendo.
    private void trabajar(java.util.concurrent.Callable<List<Curso>> tarea) {
        cargando = true;
        estado.setText("Cargando…");
        new SwingWorker<List<Curso>, Void>() {
            protected List<Curso> doInBackground() throws Exception {
                return tarea.call();
            }

            protected void done() {
                try {
                    List<Curso> lista = get();
                    cursos.clear();
                    cursos.addAll(lista);
                    modelo.setRowCount(0);
                    for (Curso c : lista)
                        modelo.addRow(
                                new Object[] {
                                    c.getAño() + "°",
                                    c.getDivision(),
                                    c.getCantidad_materias(),
                                    c.getNombre_periodo(),
                                    c.getModalidad(),
                                    c.getTurno(),
                                    c.getCupo_maximo(),
                                    c.getFecha_inicio(),
                                    c.getFecha_fin(),
                                    c.getEstado()
                                });
                    estado.setText(lista.size() + " cursos");
                } catch (Exception e) {
                    estado.setText("No se pudo completar la operación.");
                    error(e);
                } finally {
                    cargando = false;
                }
            }
        }.execute();
    }

    private void error(Exception e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        JOptionPane.showMessageDialog(
                this, causa.getMessage(), "Cursos", JOptionPane.ERROR_MESSAGE);
    }

    // antes de abrir el formulario, buscamos las opciones y las materias que ya tiene.
    private void formulario(Curso original) {
        estado.setText("Preparando formulario…");
        new SwingWorker<Object[], Void>() {
            protected Object[] doInBackground() {
                return new Object[] {new Periodo_Lectivo_dao().obtenerTodos()};
            }

            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] datos = get();
                    editar(
                            original,
                            (List<Periodo_Lectivo>) datos[0]);
                    estado.setText(" ");
                } catch (Exception e) {
                    error(e);
                }
            }
        }.execute();
    }

    private void editar(
            Curso original,
            List<Periodo_Lectivo> periodos) {
        JComboBox<Periodo_Lectivo> periodo =
                new JComboBox<>(periodos.toArray(Periodo_Lectivo[]::new));
        periodo.setRenderer(
                new DefaultListCellRenderer() {
                    public Component getListCellRendererComponent(
                            JList<?> l, Object v, int i, boolean s, boolean f) {
                        super.getListCellRendererComponent(l, v, i, s, f);
                        if (v instanceof Periodo_Lectivo p) setText(p.getNombre());
                        return this;
                    }
                });
        JTextArea materiasPanel = new JTextArea(3, 28);
        materiasPanel.setEditable(false); materiasPanel.setLineWrap(true); materiasPanel.setWrapStyleWord(true);
        List<Materia> materiasAnio = new ArrayList<>();
        JTextField division = new JTextField(original == null ? "" : original.getDivision());
        JComboBox<Integer> anio = new JComboBox<>(new Integer[] {1, 2, 3, 4, 5, 6});
        JComboBox<String> modalidad =
                new JComboBox<>(new String[] {"Presencial", "Virtual", "Híbrido"});
        JComboBox<String> turno = new JComboBox<>(new String[] {"mañana", "tarde", "vespertino"});
        JComboBox<String> situacion =
                new JComboBox<>(new String[] {"Activo", "Finalizado", "Cancelado"});
        JSpinner cupo =
                new JSpinner(
                        new SpinnerNumberModel(
                                original == null ? 30 : original.getCupo_maximo().intValue(),
                                1,
                                1000,
                                1));
        JTextField inicio =
                new JTextField(
                        original == null ? "2026-03-02" : original.getFecha_inicio().toString());
        JTextField fin =
                new JTextField(
                        original == null ? "2026-12-18" : original.getFecha_fin().toString());
        // si estamos editando, completamos el formulario con los datos guardados.
        if (original != null) {
            anio.setSelectedItem(original.getAño());
            modalidad.setSelectedItem(original.getModalidad());
            turno.setSelectedItem(original.getTurno());
            situacion.setSelectedItem(original.getEstado());
            for (Periodo_Lectivo p : periodos)
                if (p.getId_periodo().equals(original.getId_periodo())) periodo.setSelectedItem(p);

        }
        Runnable cargarMateriasAnio = () -> {
            int elegido = (Integer) anio.getSelectedItem();
            materiasAnio.clear(); materiasPanel.setText("Cargando materias…");
            new SwingWorker<List<Materia>, Void>() {
                protected List<Materia> doInBackground() { return new PlanCurso_dao().materias(elegido); }
                protected void done() {
                    if (!Integer.valueOf(elegido).equals(anio.getSelectedItem())) return;
                    try {
                        materiasAnio.clear(); materiasAnio.addAll(get());
                        materiasPanel.setText(materiasAnio.isEmpty() ? "Este año no tiene materias. Configuralas desde el módulo Materias." : String.join(", ", materiasAnio.stream().map(Materia::getNombre_materia).toList()));
                    } catch (Exception e) { materiasAnio.clear(); materiasPanel.setText("No se pudieron cargar las materias."); error(e); }
                }
            }.execute();
        };
        anio.addActionListener(e -> cargarMateriasAnio.run()); cargarMateriasAnio.run();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        Object[][] campos = {
            {"Año", anio},
            {"División", division},
            {"Período", periodo},
            {"Materias", materiasPanel},
            {"Modalidad", modalidad},
            {"Turno", turno},
            {"Cupo", cupo},
            {"Inicio (AAAA-MM-DD)", inicio},
            {"Fin (AAAA-MM-DD)", fin},
            {"Estado", situacion}
        };
        for (Object[] par : campos) {
            form.add(new JLabel((String) par[0]));
            form.add((Component) par[1]);
        }
        JScrollPane formularioVisible = new JScrollPane(form);
        formularioVisible.getVerticalScrollBar().setUnitIncrement(18);
        Rectangle pantalla = getGraphicsConfiguration().getBounds();
        Insets margenes = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int altoDisponible = pantalla.height - margenes.top - margenes.bottom;
        int anchoDisponible = pantalla.width - margenes.left - margenes.right;
        formularioVisible.setPreferredSize(medidaFormulario(anchoDisponible, altoDisponible));
        // el formulario queda abierto si falta completar un dato o las fechas no son válidas.
        while (JOptionPane.showConfirmDialog(
                        this,
                        formularioVisible,
                        original == null ? "Nuevo curso" : "Editar curso",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            try {
                List<Integer> ids = materiasAnio.stream().map(Materia::getId_materia).toList();
                if (ids.isEmpty())
                    throw new IllegalArgumentException("Agregá materias al año desde el módulo Materias antes de crear el curso.");
                Curso c = new Curso();
                c.setId_curso(original == null ? null : original.getId_curso());
                c.setAño((Integer) anio.getSelectedItem());
                c.setDivision(division.getText());
                if (periodo.getSelectedItem() == null) throw new IllegalArgumentException("Creá un período lectivo antes de crear el curso.");
                c.setId_periodo(((Periodo_Lectivo) periodo.getSelectedItem()).getId_periodo());
                c.setId_docente(original == null ? null : original.getId_docente());
                c.setModalidad((String) modalidad.getSelectedItem());
                c.setTurno((String) turno.getSelectedItem());
                c.setEstado((String) situacion.getSelectedItem());
                c.setCupo_maximo((Integer) cupo.getValue());
                c.setFecha_inicio(LocalDate.parse(inicio.getText().trim()));
                c.setFecha_fin(LocalDate.parse(fin.getText().trim()));
                if (c.getFecha_fin().isBefore(c.getFecha_inicio()))
                    throw new IllegalArgumentException("La fecha de fin es anterior al inicio.");
                trabajar(
                        () -> {
                            dao.guardar(c, ids);
                            return dao.listar(usuario);
                        });
                break;
            } catch (Exception e) {
                error(e);
            }
        }
    }

    // dejamos espacio para los botones aunque la pantalla tenga poca altura
    private static Dimension medidaFormulario(int anchoDisponible, int altoDisponible) {
        return new Dimension(
                Math.min(660, Math.max(280, anchoDisponible - 100)),
                Math.min(500, Math.max(150, altoDisponible - 180)));
    }
}
