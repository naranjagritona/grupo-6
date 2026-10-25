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

// Este panel reúne la consulta y la gestión de los cursos.
public class Cursos_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Usuario usuario;
    private final Curso_dao dao = new Curso_dao();
    // Dejamos las celdas sin edición: los cambios se hacen desde el formulario.
    private final DefaultTableModel modelo =
            new DefaultTableModel(
                    new String[] {
                        "Año",
                        "Materias",
                        "Período",
                        "Docente",
                        "Modalidad",
                        "Turno",
                        "Cupo",
                        "Inicio",
                        "Fin",
                        "Estado"
                    },
                    0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final List<Curso> cursos = new ArrayList<>();
    private final JLabel estado = new JLabel(" ");

    public Cursos_panel() {
        this(null);
    }

    // Armamos el listado, el buscador y los botones según el usuario que ingresó.
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
        buscar.setToolTipText("Buscar por año, materia, docente o estado");
        JPanel busqueda = new JPanel(new BorderLayout(8, 8));
        busqueda.add(new JLabel("Buscar cursos:"), BorderLayout.WEST);
        busqueda.add(buscar);
        arriba.add(busqueda);
        add(arriba, BorderLayout.NORTH);
        tabla.setRowSorter(filtro);
        tabla.setRowHeight(30);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(330);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setColumnHeaderView(tabla.getTableHeader());
        add(desplazamiento);
        // Filtramos lo que ya está cargado para buscar sin consultar la base en cada letra.
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
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // Solo el administrador puede crear, editar o cancelar un curso.
        if (usuario != null && usuario.getId_rol() == 1) {
            boton(acciones, "Nuevo curso", () -> formulario(null));
            boton(
                    acciones,
                    "Editar",
                    () -> {
                        Curso c = seleccion();
                        if (c != null)
                            formulario(c);
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
        boton(acciones, "Actualizar", this::cargar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
        cargar();
    }

    private void boton(JPanel p, String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> accion.run());
        p.add(b);
    }

    // Convertimos la fila visible a la original porque la tabla puede estar filtrada.
    private Curso seleccion() {
        int r = tabla.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un curso.");
            return null;
        }
        return cursos.get(tabla.convertRowIndexToModel(r));
    }

    private void cargar() {
        trabajar(() -> dao.listar(usuario));
    }

    // Hacemos las consultas en segundo plano para que la ventana siga respondiendo.
    private void trabajar(java.util.concurrent.Callable<List<Curso>> tarea) {
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
                                    c.getAño(),
                                    c.getNombre_materia(),
                                    c.getNombre_periodo(),
                                    c.getNombre_docente(),
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
                }
            }
        }.execute();
    }

    private void error(Exception e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        JOptionPane.showMessageDialog(
                this, causa.getMessage(), "Cursos", JOptionPane.ERROR_MESSAGE);
    }

    // Antes de abrir el formulario, buscamos las opciones y las materias que ya tiene.
    private void formulario(Curso original) {
        estado.setText("Preparando formulario…");
        new SwingWorker<Object[], Void>() {
            protected Object[] doInBackground() {
                return new Object[] {
                    new Periodo_Lectivo_dao().obtenerTodos(),
                    new Materia_dao().obtenerTodos(),
                    new Docente_dao().obtenerTodos(),
                    original == null ? List.of() : dao.materiasDelCurso(original.getId_curso())
                };
            }

            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] datos = get();
                    editar(
                            original,
                            (List<Periodo_Lectivo>) datos[0],
                            (List<Materia>) datos[1],
                            (List<Docente>) datos[2],
                            (List<Integer>) datos[3]);
                    estado.setText(" ");
                } catch (Exception e) {
                    error(e);
                }
            }
        }.execute();
    }

    private void editar(
            Curso original,
            List<Periodo_Lectivo> periodos,
            List<Materia> materias,
            List<Docente> docentes,
            List<Integer> elegidas) {
        JComboBox<Periodo_Lectivo> periodo =
                new JComboBox<>(periodos.toArray(Periodo_Lectivo[]::new));
        periodo.setRenderer(
                new DefaultListCellRenderer() {
                    public Component getListCellRendererComponent(
                            JList<?> l, Object v, int i, boolean s, boolean f) {
                        super.getListCellRendererComponent(l, v, i, s, f);
                        if (v instanceof Periodo_Lectivo p)
                            setText(p.getNombre());
                        return this;
                    }
                });
        JComboBox<Docente> docente = new JComboBox<>(docentes.toArray(Docente[]::new));
        docente.setRenderer(
                new DefaultListCellRenderer() {
                    public Component getListCellRendererComponent(
                            JList<?> l, Object v, int i, boolean s, boolean f) {
                        super.getListCellRendererComponent(l, v, i, s, f);
                        if (v instanceof Docente d)
                            setText(d.getApellido() + ", " + d.getNombre());
                        return this;
                    }
                });
        // Cada casilla representa una materia; se pueden marcar varias para el mismo curso.
        JPanel materiasPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        List<JCheckBox> casillas = new ArrayList<>();
        for (Materia m : materias) {
            JCheckBox c =
                    new JCheckBox(m.getNombre_materia(), elegidas.contains(m.getId_materia()));
            casillas.add(c);
            materiasPanel.add(c);
        }
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
        // Si estamos editando, completamos el formulario con los datos guardados.
        if (original != null) {
            anio.setSelectedItem(original.getAño());
            modalidad.setSelectedItem(original.getModalidad());
            turno.setSelectedItem(original.getTurno());
            situacion.setSelectedItem(original.getEstado());
            for (Periodo_Lectivo p : periodos)
                if (p.getId_periodo().equals(original.getId_periodo()))
                    periodo.setSelectedItem(p);
            for (Docente d : docentes)
                if (d.getId_docente().equals(original.getId_docente()))
                    docente.setSelectedItem(d);
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        Object[][] campos = {
            {"Año", anio},
            {"Período", periodo},
            {"Materias", materiasPanel},
            {"Docente", docente},
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
        // El formulario queda abierto si falta completar un dato o las fechas no son válidas.
        while (JOptionPane.showConfirmDialog(
                        this,
                        form,
                        original == null ? "Nuevo curso" : "Editar curso",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            try {
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < casillas.size(); i++)
                    if (casillas.get(i).isSelected())
                        ids.add(materias.get(i).getId_materia());
                if (ids.isEmpty())
                    throw new IllegalArgumentException("Seleccioná al menos una materia.");
                Curso c = new Curso();
                c.setId_curso(original == null ? null : original.getId_curso());
                c.setAño((Integer) anio.getSelectedItem());
                c.setId_periodo(((Periodo_Lectivo) periodo.getSelectedItem()).getId_periodo());
                c.setId_docente(((Docente) docente.getSelectedItem()).getId_docente());
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
}
