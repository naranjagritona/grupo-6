package paneles;

import clases.Curso;
import clases.ItemCombo;
import clases.Usuario;
import servicios.Promocion_servicio;
import servicios.Permisos_servicio;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import static paneles.Ficha_Inscripcion_dialog.*;

// revisamos los alumnos de un año y división antes de confirmar su promoción
public class Promocion_dialog extends JDialog {
    private final Promocion_servicio servicio = new Promocion_servicio();
    private final Usuario usuario;
    private final List<Curso> cursos;
    private final JComboBox<ItemCombo> origen = new JComboBox<>();
    private final JComboBox<ItemCombo> destino = new JComboBox<>();
    private final JPanel listado = new JPanel(new BorderLayout());
    private final JButton confirmar = boton("Confirmar promoción");
    private final JLabel estado = new JLabel("Seleccioná el año y la división de origen y revisá sus alumnos.");

    public Promocion_dialog(JFrame padre, Usuario usuario, List<Curso> cursos, Integer seleccionado, Runnable actualizar) {
        super(padre, "Promoción de año", true);
        this.usuario = usuario;
        this.cursos = cursos;
        setLayout(new BorderLayout(10, 10));
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formulario.setBackground(Color.WHITE);
        for (Curso curso : cursos) origen.addItem(new ItemCombo(curso.getId_curso(), describir(curso)));
        formulario.add(new JLabel("Año y división de origen:")); formulario.add(origen);
        formulario.add(new JLabel("Año y división de destino:")); formulario.add(destino);
        add(formulario, BorderLayout.NORTH);
        listado.setBackground(Color.WHITE);
        add(listado, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setBackground(Color.WHITE);
        JButton revisar = boton("Revisar alumnos");
        revisar.addActionListener(e -> revisar());
        acciones.add(revisar);
        if (Permisos_servicio.directivo(usuario)) {
            JButton condiciones = boton("Condiciones de promoción");
            condiciones.addActionListener(e -> condiciones());
            acciones.add(condiciones);
        }
        confirmar.setEnabled(false);
        confirmar.addActionListener(e -> {
            ItemCombo desde = (ItemCombo) origen.getSelectedItem(), hasta = (ItemCombo) destino.getSelectedItem();
            if (desde == null || hasta == null) return;
            if (JOptionPane.showConfirmDialog(this, "Se crearán las inscripciones de quienes aprueben y el resto quedará en recuperación. ¿Continuar?", "Confirmar promoción", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            confirmar.setEnabled(false);
            ejecutar(this, () -> servicio.promocionar(desde.getId(), hasta.getId(), usuario.getId_usuario()), resultado -> {
                JOptionPane.showMessageDialog(this, "Promocionados: " + resultado.promocionados() + "\nEn recuperación: " + resultado.recuperacion());
                actualizar.run();
                revisar();
            });
        });
        acciones.add(confirmar);
        JButton cerrar = boton("Cerrar"); cerrar.addActionListener(e -> dispose()); acciones.add(cerrar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(estado, BorderLayout.NORTH); pie.add(acciones, BorderLayout.SOUTH);
        add(pie, BorderLayout.SOUTH);
        origen.addActionListener(e -> destinos());
        destino.addActionListener(e -> confirmar.setEnabled(false));
        for (int i = 0; i < origen.getItemCount(); i++) if (origen.getItemAt(i).getId().equals(seleccionado)) origen.setSelectedIndex(i);
        destinos();
        setSize(950, 560); setLocationRelativeTo(padre);
    }

    // identificamos los cursos por año y división, sin incluir el nombre de las materias
    private String describir(Curso curso) {
        return curso.getAño() + "° " + curso.getDivision() + " · " + curso.getTurno() + " · " + curso.getNombre_periodo();
    }

    // mostramos los cursos activos del año siguiente para elegir su división y período
    private void destinos() {
        destino.removeAllItems(); confirmar.setEnabled(false); listado.removeAll();
        if (!(origen.getSelectedItem() instanceof ItemCombo seleccion)) return;
        Curso actual = cursos.stream().filter(c -> c.getId_curso().equals(seleccion.getId())).findFirst().orElse(null);
        if (actual != null) for (Curso curso : cursos) if (curso.getAño() == actual.getAño() + 1 && "Activo".equals(curso.getEstado())) destino.addItem(new ItemCombo(curso.getId_curso(), describir(curso)));
        listado.revalidate(); listado.repaint();
    }

    // consultamos materia por materia y mostramos el motivo de aprobación o recuperación de cada alumno
    private void revisar() {
        if (!(origen.getSelectedItem() instanceof ItemCombo desde)) return;
        confirmar.setEnabled(false);
        ejecutar(this, () -> servicio.revisar(desde.getId(), usuario.getId_usuario()), revisiones -> {
            if (!desde.equals(origen.getSelectedItem())) return;
            List<Map<String, Object>> filas = new ArrayList<>();
            for (var revision : revisiones) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("alumno", revision.alumno());
                fila.put("promedio", revision.promedio());
                fila.put("situacion", revision.aprobado() ? "Puede promocionar" : "Recuperacion");
                fila.put("motivo", revision.motivo());
                filas.add(fila);
            }
            listado.removeAll(); listado.add(new JScrollPane(tabla(filas, "alumno", "promedio", "situacion", "motivo")));
            listado.revalidate(); listado.repaint();
            confirmar.setEnabled(!filas.isEmpty() && destino.getSelectedItem() != null);
            estado.setText(revisiones.size() + " alumnos revisados. La confirmación vuelve a verificar las condiciones y el cupo.");
        });
    }

    // pedimos las notas mínimas y el criterio que dirección define para el curso
    private void condiciones() {
        if (!(origen.getSelectedItem() instanceof ItemCombo desde)) return;
        Curso curso = cursos.stream().filter(c -> c.getId_curso().equals(desde.getId())).findFirst().orElseThrow();
        JTextField materia = new JTextField(Objects.toString(curso.getNota_aprobacion_materia(), ""));
        JTextField promocion = new JTextField(Objects.toString(curso.getPromedio_promocion(), ""));
        JComboBox<String> criterio = new JComboBox<>(new String[] {"Promediar", "Reemplazar menor", "Ultimo recuperatorio"});
        if (curso.getCriterio_recuperatorio() == null) criterio.setSelectedIndex(-1); else criterio.setSelectedItem(curso.getCriterio_recuperatorio());
        Object[] campos = {"Nota mínima por materia (escala 0 a 10)", materia, "Promedio mínimo para promocionar", promocion, "Recuperatorios", criterio, "Promediar incluye los recuperatorios; Reemplazar menor mejora la nota más baja; Último recuperatorio usa la última instancia.", "Los resultados finales se guardan con dos decimales, sin redondear hacia arriba."};
        if (JOptionPane.showConfirmDialog(this, campos, "Condiciones de promoción", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        ejecutar(this, () -> {
            double minimo = Double.parseDouble(materia.getText().replace(',', '.'));
            double promedio = Double.parseDouble(promocion.getText().replace(',', '.'));
            servicio.configurar(curso.getId_curso(), minimo, promedio, Objects.toString(criterio.getSelectedItem(), ""), usuario.getId_usuario());
            return new double[] {minimo, promedio};
        }, valores -> {
            curso.setNota_aprobacion_materia(valores[0]); curso.setPromedio_promocion(valores[1]); curso.setCriterio_recuperatorio(criterio.getSelectedItem().toString());
            confirmar.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Condiciones guardadas. Volvé a revisar los alumnos.");
        });
    }
}
