package paneles;

import clases.*;
import dao.PlanCurso_dao;
import java.awt.*;
import java.util.List;
import javax.swing.*;

// El botón + de cada año permite reutilizar una materia o crear una nueva.
public class Materias_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final PlanCurso_dao dao;
    private final JPanel anios = new JPanel(new GridLayout(0, 1, 8, 8));
    private final JLabel estado = new JLabel(" ");
    private final Usuario usuario;

    public Materias_panel() { this(null); }

    public Materias_panel(Usuario usuario) { this(usuario, new PlanCurso_dao()); }

    Materias_panel(Usuario usuario, PlanCurso_dao dao) {
        this.usuario = usuario; this.dao = dao;
        servicios.Auditoria_servicio.registrar("ABRIR MODULO", "Materias", "Materias agrupadas por año");
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel cabecera = new JPanel(new BorderLayout(8, 8));
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("Materias");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        cabecera.add(titulo, BorderLayout.NORTH);
        cabecera.add(new JLabel("Las materias de cada año se incorporan a todos sus cursos."), BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);
        anios.setOpaque(false);
        JScrollPane desplazamiento = new JScrollPane(anios);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(18);
        add(desplazamiento, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(8, 8)); pie.setOpaque(false);
        pie.add(estado, BorderLayout.CENTER);
        JButton actualizar = new JButton("Actualizar"); actualizar.addActionListener(e -> cargar());
        pie.add(actualizar, BorderLayout.EAST); add(pie, BorderLayout.SOUTH);
        cargar();
    }

    private void cargar() {
        estado.setText("Cargando…"); anios.removeAll();
        new SwingWorker<java.util.Map<Integer,List<Materia>>,Void>() {
            protected java.util.Map<Integer,List<Materia>> doInBackground() {
                return dao.planPorAnio();
            }
            protected void done() {
                try {
                    for (var entrada : get().entrySet()) {
                        int anio = entrada.getKey(); JPanel fila = new JPanel(new BorderLayout(12,8));
                        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT)); titulo.add(new JLabel(anio + "° año"));
                        if (servicios.Permisos_servicio.directivo(usuario)) {
                            JButton mas = new JButton("+"); mas.setToolTipText("Agregar materia a " + anio + "° año");
                            mas.addActionListener(e -> agregar(anio)); titulo.add(mas);
                        }
                        fila.add(titulo, BorderLayout.WEST);
                        JTextArea nombres = new JTextArea(entrada.getValue().isEmpty() ? "Sin materias" : String.join(" · ", entrada.getValue().stream().map(Materia::getNombre_materia).toList()));
                        nombres.setEditable(false); nombres.setLineWrap(true); nombres.setWrapStyleWord(true); nombres.setOpaque(false);
                        fila.add(nombres, BorderLayout.CENTER);
                        fila.setBackground(Color.WHITE);
                        fila.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(12, 8, 12, 8)));
                        fila.setPreferredSize(new Dimension(400, 88)); anios.add(fila);
                    }
                    estado.setText(" "); anios.revalidate(); anios.repaint();
                } catch (Exception e) { error(e); estado.setText("No se pudieron cargar las materias."); }
            }
        }.execute();
    }

    private void agregar(int anio) {
        estado.setText("Preparando materias…");
        new SwingWorker<List<Materia>,Void>() {
            protected List<Materia> doInBackground() { return new dao.Materia_dao().obtenerTodos(); }
            protected void done() {
                try { elegir(anio, get()); estado.setText(" "); } catch (Exception e) { error(e); }
            }
        }.execute();
    }

    private void elegir(int anio, List<Materia> materias) {
        JComboBox<Materia> existentes = new JComboBox<>(); existentes.addItem(null);
        for (Materia m : materias) existentes.addItem(m);
        existentes.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l,v,i,s,f); setText(v instanceof Materia m ? m.getNombre_materia() : "Crear una materia nueva"); return this;
            }
        });
        JTextField nombre = new JTextField(24);
        existentes.addActionListener(e -> nombre.setEnabled(existentes.getSelectedItem() == null));
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Materia para " + anio + "° año", existentes, "Nombre de la materia nueva", nombre}, "Agregar materia", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Materia seleccionada = (Materia) existentes.getSelectedItem();
        String nombreNuevo = nombre.getText();
        estado.setText("Guardando…"); anios.setVisible(false);
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception { dao.agregar(anio, seleccionada == null ? null : seleccionada.getId_materia(), nombreNuevo); return null; }
            protected void done() {
                try { get(); cargar(); } catch (Exception e) { error(e); estado.setText("No se guardó la materia."); }
                finally { anios.setVisible(true); }
            }
        }.execute();
    }

    private void error(Exception e) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        JOptionPane.showMessageDialog(this, causa.getMessage(), "Materias por año", JOptionPane.ERROR_MESSAGE);
    }
}
