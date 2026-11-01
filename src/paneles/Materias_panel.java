package paneles;

import clases.Materia;
import clases.Usuario;
import dao.PlanCurso_dao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Consulta y administración del plan de materias por año. */
public class Materias_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color AZUL = new Color(24, 74, 132);
    private static final Color BORDE = new Color(218, 226, 236);
    private final PlanCurso_dao dao;
    private final Usuario usuario;
    private final JPanel anios = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JLabel estado = new JLabel(" ");
    private final JTextField buscar = new JTextField(24);
    private Map<Integer, List<Materia>> plan = new LinkedHashMap<>();
    private boolean cargando;

    public Materias_panel() { this(null); }
    public Materias_panel(Usuario usuario) { this(usuario, new PlanCurso_dao()); }

    Materias_panel(Usuario usuario, PlanCurso_dao dao) {
        this.usuario = usuario;
        this.dao = dao;
        servicios.Auditoria_servicio.registrar("ABRIR MODULO", "Materias", "Plan académico por año");
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel cabecera = new JPanel(new BorderLayout(8, 10));
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("Materias");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        JLabel descripcion = new JLabel("Organizá las materias de cada año y consultá su plan académico.");
        descripcion.setForeground(new Color(90, 104, 122));
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(descripcion);
        cabecera.add(textos, BorderLayout.NORTH);
        JPanel herramientas = new JPanel(new BorderLayout(8, 0));
        herramientas.setOpaque(false);
        JLabel etiqueta = new JLabel("Buscar materia:");
        buscar.setToolTipText("Filtrar materias por nombre");
        herramientas.add(etiqueta, BorderLayout.WEST);
        herramientas.add(buscar, BorderLayout.CENTER);
        cabecera.add(herramientas, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        anios.setBackground(new Color(248, 250, 253));
        anios.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane desplazamiento = new JScrollPane(anios);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(18);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        add(desplazamiento, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(8, 8));
        pie.setOpaque(false);
        pie.add(estado, BorderLayout.CENTER);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargar());
        pie.add(actualizar, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { mostrarPlan(); }
            public void removeUpdate(DocumentEvent e) { mostrarPlan(); }
            public void changedUpdate(DocumentEvent e) { mostrarPlan(); }
        });
        cargar();
    }

    private void cargar() {
        if (cargando) return;
        cargando = true;
        estado.setText("Cargando materias…");
        new SwingWorker<Map<Integer, List<Materia>>, Void>() {
            protected Map<Integer, List<Materia>> doInBackground() { return dao.planPorAnio(); }
            protected void done() {
                cargando = false;
                try {
                    plan = get();
                    mostrarPlan();
                    int cantidad = plan.values().stream().mapToInt(List::size).sum();
                    estado.setText(cantidad + " materias en el plan");
                } catch (Exception e) {
                    error(e);
                    estado.setText("No se pudieron cargar las materias.");
                }
            }
        }.execute();
    }

    private void mostrarPlan() {
        anios.removeAll();
        String filtro = buscar.getText().trim().toLowerCase(java.util.Locale.ROOT);
        boolean directivo = servicios.Permisos_servicio.directivo(usuario);
        for (int anio = 1; anio <= 6; anio++) {
            List<Materia> todas = plan.getOrDefault(anio, List.of());
            List<Materia> visibles = todas.stream()
                    .filter(m -> m.getNombre_materia().toLowerCase(java.util.Locale.ROOT).contains(filtro))
                    .toList();
            if (!filtro.isEmpty() && visibles.isEmpty()) continue;

            JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
            tarjeta.setBackground(Color.WHITE);
            tarjeta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            JPanel titulo = new JPanel(new BorderLayout(8, 0));
            titulo.setOpaque(false);
            JLabel anioLabel = new JLabel(anio + "° año");
            anioLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
            anioLabel.setForeground(AZUL);
            titulo.add(anioLabel, BorderLayout.WEST);
            JLabel cantidad = new JLabel(todas.size() + (todas.size() == 1 ? " materia" : " materias"));
            cantidad.setForeground(new Color(95, 109, 128));
            titulo.add(cantidad, BorderLayout.CENTER);
            if (directivo) {
                JButton agregar = new JButton("+ Agregar");
                agregar.setToolTipText("Agregar una materia a " + anio + "° año");
                agregar.addActionListener(e -> agregar(anio));
                titulo.add(agregar, BorderLayout.EAST);
            }
            tarjeta.add(titulo, BorderLayout.NORTH);

            JPanel lista = new JPanel(new GridLayout(0, 1, 0, 5));
            lista.setOpaque(false);
            if (visibles.isEmpty()) {
                JLabel vacio = new JLabel("Todavía no hay materias asignadas.");
                vacio.setForeground(new Color(125, 136, 150));
                lista.add(vacio);
            } else {
                for (Materia materia : visibles) {
                    JLabel item = new JLabel("  •  " + materia.getNombre_materia());
                    item.setOpaque(true);
                    item.setBackground(new Color(246, 249, 253));
                    item.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
                    item.setToolTipText("Materia " + materia.getId_materia());
                    lista.add(item);
                }
            }
            tarjeta.add(lista, BorderLayout.CENTER);
            tarjeta.setMinimumSize(new Dimension(240, 95));
            anios.add(tarjeta);
        }
        if (anios.getComponentCount() == 0) {
            JLabel sinResultados = new JLabel("No hay materias que coincidan con la búsqueda.");
            sinResultados.setForeground(new Color(95, 109, 128));
            anios.add(sinResultados);
        }
        anios.revalidate();
        anios.repaint();
    }

    private void agregar(int anio) {
        if (cargando) return;
        estado.setText("Preparando materias…");
        new SwingWorker<List<Materia>, Void>() {
            protected List<Materia> doInBackground() { return new dao.Materia_dao().obtenerTodos(); }
            protected void done() {
                try { elegir(anio, get()); estado.setText(" "); }
                catch (Exception e) { error(e); estado.setText("No se pudieron cargar las materias existentes."); }
            }
        }.execute();
    }

    private void elegir(int anio, List<Materia> materias) {
        JComboBox<Materia> existentes = new JComboBox<>();
        existentes.addItem(null);
        for (Materia materia : materias) existentes.addItem(materia);
        existentes.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override public java.awt.Component getListCellRendererComponent(
                    JList<?> lista, Object valor, int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor instanceof Materia materia ? materia.getNombre_materia() : "Crear una materia nueva");
                return this;
            }
        });
        JTextField nombre = new JTextField(24);
        existentes.addActionListener(e -> nombre.setEnabled(existentes.getSelectedItem() == null));
        if (JOptionPane.showConfirmDialog(this,
                new Object[] {"Materia para " + anio + "° año", existentes, "Nombre de la materia nueva", nombre},
                "Agregar materia", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Materia seleccionada = (Materia) existentes.getSelectedItem();
        String nombreNuevo = nombre.getText();
        estado.setText("Guardando…");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                dao.agregar(anio, seleccionada == null ? null : seleccionada.getId_materia(), nombreNuevo);
                return null;
            }
            protected void done() {
                try { get(); cargar(); }
                catch (Exception e) { error(e); estado.setText("No se guardó la materia."); }
            }
        }.execute();
    }

    private void error(Exception e) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        JOptionPane.showMessageDialog(this, causa.getMessage(), "Materias por año", JOptionPane.ERROR_MESSAGE);
    }
}
