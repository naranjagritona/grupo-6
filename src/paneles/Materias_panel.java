package paneles;

import clases.Curso;
import clases.Materia;
import clases.Usuario;

import dao.Curso_dao;
import dao.Materia_dao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// mostramos las materias y los cursos con los que están relacionadas
public class Materias_panel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Usuario usuario;
    private final Materia_dao materiaDao = new Materia_dao();
    private final Curso_dao cursoDao = new Curso_dao();
    private final DefaultTableModel modelo =
            new DefaultTableModel(new String[] {"ID", "Materia", "Cursos vinculados"}, 0) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> filtro = new TableRowSorter<>(modelo);
    private final List<Materia> materias = new ArrayList<>();
    private final JLabel estado = new JLabel(" ");
    private final JTextArea detalleRelaciones = new JTextArea(3, 30);
    private Map<Integer, List<String>> cursosPorMateria = Map.of();

    public Materias_panel() {
        this(null);
    }

    public Materias_panel(Usuario usuario) {
        this.usuario = usuario;
        servicios.Auditoria_servicio.registrar("ABRIR MODULO", "Materias", "Consulta del módulo");
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(Color.WHITE);

        JPanel arriba = new JPanel(new BorderLayout(8, 12));
        JLabel titulo = new JLabel("Materias");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        arriba.add(titulo, BorderLayout.NORTH);

        JTextField buscar = new JTextField();
        buscar.setToolTipText("Buscar por nombre, ID o cantidad de cursos");
        JPanel busqueda = new JPanel(new BorderLayout(8, 8));
        busqueda.add(new JLabel("Buscar materias:"), BorderLayout.WEST);
        busqueda.add(buscar, BorderLayout.CENTER);
        arriba.add(busqueda, BorderLayout.CENTER);
        add(arriba, BorderLayout.NORTH);

        tabla.setRowSorter(filtro);
        tabla.setRowHeight(30);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getSelectionModel().addListSelectionListener(this::mostrarRelaciones);

        detalleRelaciones.setEditable(false);
        detalleRelaciones.setLineWrap(true);
        detalleRelaciones.setWrapStyleWord(true);
        detalleRelaciones.setBackground(getBackground());
        detalleRelaciones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel relaciones = new JPanel(new BorderLayout(8, 4));
        relaciones.setBorder(BorderFactory.createTitledBorder("Relaciones con cursos"));
        relaciones.add(new JScrollPane(detalleRelaciones), BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        centro.add(relaciones, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
        detalleRelaciones.setText("Seleccioná una materia para ver los cursos que la utilizan.");

        buscar.getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            private void filtrar() {
                                servicios.Auditoria_servicio.buscar(
                                        "Materias", buscar.getText().trim());
                                filtro.setRowFilter(
                                        RowFilter.regexFilter(
                                                "(?iu)" + Pattern.quote(buscar.getText().trim())));
                            }

                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                filtrar();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                filtrar();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                filtrar();
                            }
                        });

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (esAdministrador()) {
            boton(acciones, "Nueva materia", () -> formulario(null));
            boton(
                    acciones,
                    "Editar",
                    () -> {
                        Materia materia = seleccion();
                        if (materia != null) {
                            formulario(materia);
                        }
                    });
            boton(acciones, "Eliminar", this::eliminarSeleccionada);
        }
        boton(acciones, "Actualizar", this::cargar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);

        cargar();
    }

    private boolean esAdministrador() {
        return usuario != null && usuario.getId_rol() == 1;
    }

    private void boton(JPanel panel, String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(
                e -> {
                    servicios.Auditoria_servicio.registrar("ACCION", "Materias", texto);
                    accion.run();
                });
        panel.add(boton);
    }

    // devuelve el registro que corresponde a la fila seleccionada
    private Materia seleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná una materia.");
            return null;
        }
        return materias.get(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarRelaciones(ListSelectionEvent evento) {
        if (evento.getValueIsAdjusting()) {
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            detalleRelaciones.setText("Seleccioná una materia para ver sus cursos.");
            return;
        }
        Materia materia = materias.get(tabla.convertRowIndexToModel(fila));
        List<String> cursos = cursosPorMateria.getOrDefault(materia.getId_materia(), List.of());
        if (cursos.isEmpty()) {
            detalleRelaciones.setText(
                    "“"
                            + materia.getNombre_materia()
                            + "” todavía no está vinculada a ningún curso.");
            return;
        }
        detalleRelaciones.setText(
                "“"
                        + materia.getNombre_materia()
                        + "” está vinculada a "
                        + cursos.size()
                        + " curso(s):\n- "
                        + String.join("\n- ", cursos));
        detalleRelaciones.setCaretPosition(0);
    }

    private void cargar() {
        trabajar(this::cargarCatalogo);
    }

    // consulta las materias y agrupa sus cursos relacionados
    private CatalogoMaterias cargarCatalogo() {
        List<Materia> listaMaterias = materiaDao.obtenerTodos();
        List<Curso> cursos = cursoDao.listar(usuario);
        Map<Integer, List<Integer>> materiasDeCursos = cursoDao.materiasPorCurso(cursos);
        Map<Integer, Set<String>> relaciones = new LinkedHashMap<>();

        for (Curso curso : cursos) {
            Set<Integer> idsMaterias = new LinkedHashSet<>();
            if (curso.getId_materia() != null) {
                idsMaterias.add(curso.getId_materia());
            }
            idsMaterias.addAll(materiasDeCursos.getOrDefault(curso.getId_curso(), List.of()));

            String descripcion = describirCurso(curso);
            for (Integer idMateria : idsMaterias) {
                relaciones.computeIfAbsent(idMateria, id -> new LinkedHashSet<>()).add(descripcion);
            }
        }

        Map<Integer, List<String>> relacionesVisibles = new LinkedHashMap<>();
        for (Map.Entry<Integer, Set<String>> relacion : relaciones.entrySet()) {
            relacionesVisibles.put(relacion.getKey(), new ArrayList<>(relacion.getValue()));
        }

        if (usuario != null && !esAdministrador()) {
            listaMaterias.removeIf(
                    materia -> !relacionesVisibles.containsKey(materia.getId_materia()));
        }
        return new CatalogoMaterias(listaMaterias, relacionesVisibles);
    }

    private String describirCurso(Curso curso) {
        return "Curso #"
                + curso.getId_curso()
                + " · "
                + curso.getAño()
                + "° año · "
                + curso.getTurno()
                + " · "
                + curso.getEstado();
    }

    // ejecuta la consulta en segundo plano y actualiza la pantalla al terminar
    private void trabajar(Callable<CatalogoMaterias> tarea) {
        estado.setText("Cargando…");
        new SwingWorker<CatalogoMaterias, Void>() {
            @Override
            protected CatalogoMaterias doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                try {
                    CatalogoMaterias catalogo = get();
                    materias.clear();
                    materias.addAll(catalogo.materias);
                    cursosPorMateria = catalogo.cursosPorMateria;
                    modelo.setRowCount(0);
                    for (Materia materia : materias) {
                        int cantidad =
                                cursosPorMateria
                                        .getOrDefault(materia.getId_materia(), List.of())
                                        .size();
                        modelo.addRow(
                                new Object[] {
                                    materia.getId_materia(), materia.getNombre_materia(), cantidad
                                });
                    }
                    estado.setText(materias.size() + " materias");
                    detalleRelaciones.setText(
                            "Seleccioná una materia para ver los cursos que la utilizan.");
                } catch (Exception e) {
                    estado.setText("No se pudo completar la operación.");
                    error(e);
                }
            }
        }.execute();
    }

    // prepara los campos que se completan en el formulario
    private void formulario(Materia original) {
        JTextField nombre =
                new JTextField(original == null ? "" : original.getNombre_materia(), 24);
        JPanel formulario = new JPanel(new BorderLayout(8, 8));
        formulario.add(new JLabel("Nombre de la materia:"), BorderLayout.NORTH);
        formulario.add(nombre, BorderLayout.CENTER);

        while (JOptionPane.showConfirmDialog(
                        this,
                        formulario,
                        original == null ? "Nueva materia" : "Editar materia",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION) {
            String valor = nombre.getText().trim();
            if (valor.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ingresá el nombre de la materia.",
                        "Materias",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (nombreDuplicado(valor, original)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe una materia con ese nombre.",
                        "Materias",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            Materia materia =
                    new Materia(original == null ? null : original.getId_materia(), valor);
            trabajar(
                    () -> {
                        if (original == null) {
                            materiaDao.insertar(materia);
                        } else {
                            materiaDao.modificar(materia);
                        }
                        CatalogoMaterias actualizado = cargarCatalogo();
                        boolean guardada =
                                actualizado.materias.stream()
                                        .anyMatch(
                                                existente ->
                                                        original == null
                                                                ? normalizarNombre(
                                                                                existente
                                                                                        .getNombre_materia())
                                                                        .equals(
                                                                                normalizarNombre(
                                                                                        materia
                                                                                                .getNombre_materia()))
                                                                : original.getId_materia()
                                                                                .equals(
                                                                                        existente
                                                                                                .getId_materia())
                                                                        && normalizarNombre(
                                                                                        existente
                                                                                                .getNombre_materia())
                                                                                .equals(
                                                                                        normalizarNombre(
                                                                                                materia
                                                                                                        .getNombre_materia())));
                        if (!guardada) {
                            throw new IllegalStateException(
                                    "No se pudo guardar la materia. Revisá la conexión y volvé a"
                                            + " intentar.");
                        }
                        return actualizado;
                    });
            break;
        }
    }

    private boolean nombreDuplicado(String valor, Materia original) {
        String buscado = normalizarNombre(valor);
        for (Materia materia : materias) {
            if ((original == null || !original.getId_materia().equals(materia.getId_materia()))
                    && normalizarNombre(materia.getNombre_materia()).equals(buscado)) {
                return true;
            }
        }
        return false;
    }

    private String normalizarNombre(String nombre) {
        return nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    // comprueba las relaciones antes de eliminar la materia
    private void eliminarSeleccionada() {
        Materia materia = seleccion();
        if (materia == null) {
            return;
        }

        List<String> cursos = cursosPorMateria.getOrDefault(materia.getId_materia(), List.of());
        if (!cursos.isEmpty()) {
            mostrarBloqueoPorCursos(materia, cursos);
            return;
        }

        int confirmar =
                JOptionPane.showConfirmDialog(
                        this,
                        "¿Eliminar la materia “" + materia.getNombre_materia() + "”?",
                        "Eliminar materia",
                        JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        trabajar(
                () -> {
                    CatalogoMaterias actualizado = cargarCatalogo();
                    List<String> referencias =
                            actualizado.cursosPorMateria.getOrDefault(
                                    materia.getId_materia(), List.of());
                    if (!referencias.isEmpty()) {
                        throw new IllegalStateException(
                                "No se puede eliminar la materia porque está vinculada a cursos: "
                                        + String.join(", ", referencias));
                    }
                    materiaDao.eliminar(materia.getId_materia());
                    actualizado = cargarCatalogo();
                    boolean sigueExistiendo =
                            actualizado.materias.stream()
                                    .anyMatch(
                                            existente ->
                                                    materia.getId_materia()
                                                            .equals(existente.getId_materia()));
                    if (sigueExistiendo) {
                        throw new IllegalStateException(
                                "No se pudo eliminar la materia. Revisá sus relaciones y volvé a"
                                        + " intentar.");
                    }
                    return actualizado;
                });
    }

    private void mostrarBloqueoPorCursos(Materia materia, List<String> cursos) {
        JOptionPane.showMessageDialog(
                this,
                "No se puede eliminar “"
                        + materia.getNombre_materia()
                        + "” porque está vinculada a cursos. La relación se conserva para proteger"
                        + " las inscripciones y evaluaciones.\n\n"
                        + String.join("\n", cursos),
                "Materia en uso",
                JOptionPane.WARNING_MESSAGE);
    }

    // muestra el motivo por el que no se pudo completar la operación
    private void error(Exception e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        JOptionPane.showMessageDialog(
                this,
                causa.getMessage() == null ? "Error inesperado." : causa.getMessage(),
                "Materias",
                JOptionPane.ERROR_MESSAGE);
    }

    private static class CatalogoMaterias {
        private final List<Materia> materias;
        private final Map<Integer, List<String>> cursosPorMateria;

        private CatalogoMaterias(
                List<Materia> materias, Map<Integer, List<String>> cursosPorMateria) {
            this.materias = materias;
            this.cursosPorMateria = cursosPorMateria;
        }
    }
}
