
package paneles;

import clases.Curso;
import dao.Curso_dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class Cursos_panel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Curso_dao cursoDao;
    private final DefaultTableModel modelo;
    private final JTable tablaCursos;

    public Cursos_panel() {
        cursoDao = new Curso_dao();

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        // Encabezado
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(new Color(24, 61, 110));
        panelTitulo.setBorder(
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        );

        JLabel titulo = new JLabel("Gestión de Cursos");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Tahoma", Font.BOLD, 23));

        JLabel subtitulo = new JLabel("Consulta de cursos registrados");
        subtitulo.setForeground(new Color(220, 230, 245));
        subtitulo.setFont(new Font("Tahoma", Font.PLAIN, 13));

        JPanel textosTitulo = new JPanel();
        textosTitulo.setOpaque(false);
        textosTitulo.setLayout(
            new BoxLayout(textosTitulo, BoxLayout.Y_AXIS)
        );
        textosTitulo.add(titulo);
        textosTitulo.add(Box.createVerticalStrut(5));
        textosTitulo.add(subtitulo);

        panelTitulo.add(textosTitulo, BorderLayout.WEST);
        add(panelTitulo, BorderLayout.NORTH);

        // Tabla de cursos
        String[] columnas = {
            "ID", "Período", "Materia", "Docente", "Año",
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

        tablaCursos.getTableHeader().setFont(
            new Font("Tahoma", Font.BOLD, 12)
        );
        tablaCursos.getTableHeader().setBackground(
            new Color(235, 240, 247)
        );
        tablaCursos.getTableHeader().setForeground(
            new Color(24, 61, 110)
        );
        tablaCursos.getTableHeader().setReorderingAllowed(false);

        int[] anchos = {
            50, 75, 75, 75, 60, 80,
            100, 90, 100, 120, 120, 100
        };

        for (int i = 0; i < anchos.length; i++) {
            tablaCursos.getColumnModel()
                .getColumn(i)
                .setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tablaCursos);
        scroll.setBorder(
            BorderFactory.createEmptyBorder(0, 15, 0, 15)
        );

        add(scroll, BorderLayout.CENTER);

        // Botón para actualizar la tabla
        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(0, 15, 15, 15)
        );

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnActualizar.setBackground(new Color(24, 61, 110));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);

        btnActualizar.addActionListener(_ -> cargarCursos());

        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        cargarCursos();
    }

    private void cargarCursos() {
        modelo.setRowCount(0);

        try {
            List<Curso> cursos = cursoDao.obtenerTodos();

            for (Curso curso : cursos) {
                modelo.addRow(new Object[] {
                    curso.getId_curso(),
                    curso.getId_periodo(),
                    curso.getId_materia(),
                    curso.getId_docente(),
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
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los cursos.\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.toString() : "";
    }
}
