package paneles;

import clases.Alumno;
import clases.Curso;
import clases.Inscripcion;
import clases.ItemCombo;
import clases.Materia;
import dao.Alumno_dao;
import dao.Curso_dao;
import dao.Materia_dao;
import servicios.Inscripcion_servicio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Inscripcion_panel extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textFecha;
    private JTextField txtImporteCuota;
    private JTextField txtBuscar;
    private JTable table;
    
    // Declaración explícita de tipos para evitar errores en WindowBuilder
    private JComboBox<ItemCombo> cmbAlumno;
    private JComboBox<ItemCombo> cmbCurso;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbFiltrarEstado;

    private Inscripcion_servicio inscripcionServicio;
    private Alumno_dao alumnoDao;
    private Curso_dao cursoDao;
    private Materia_dao materiaDao;
    
    private Integer idInscripcionSeleccionada = null;

    public Inscripcion_panel() {
        this.inscripcionServicio = new Inscripcion_servicio();
        this.alumnoDao = new Alumno_dao();
        this.cursoDao = new Curso_dao();
        this.materiaDao = new Materia_dao();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1150, 680);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        JPanel panelTop = new JPanel();
        panelTop.setPreferredSize(new Dimension(10, 50));
        panelTop.setBackground(new Color(31, 78, 140));
        contentPane.add(panelTop, BorderLayout.NORTH);
        panelTop.setLayout(new BorderLayout(0, 0));
        
        JLabel lblTituloAcademia = new JLabel(" Academia Integral XXI");
        lblTituloAcademia.setForeground(Color.WHITE);
        lblTituloAcademia.setFont(new Font("Tahoma", Font.BOLD, 18));
        panelTop.add(lblTituloAcademia, BorderLayout.CENTER);
        
        JPanel panelMenu = new JPanel();
        panelMenu.setBackground(new Color(0, 0, 64));
        panelMenu.setPreferredSize(new Dimension(170, 10));
        contentPane.add(panelMenu, BorderLayout.WEST);
        panelMenu.setLayout(null);
        
        JLabel lblMenu = new JLabel("Menú");
        lblMenu.setForeground(new Color(255, 255, 255));
        lblMenu.setFont(new Font("Tahoma", Font.PLAIN, 25));
        lblMenu.setBounds(49, 0, 60, 57);
        panelMenu.add(lblMenu);
        
        JButton btnInscripciones = new JButton("Inscripciones");
        btnInscripciones.setBounds(15, 61, 140, 35);
        btnInscripciones.setBackground(new Color(37, 99, 235));
        btnInscripciones.setForeground(Color.WHITE);
        btnInscripciones.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnInscripciones);
        
        JButton btnCursos = new JButton("Cursos");
        btnCursos.setBounds(15, 107, 140, 35);
        btnCursos.setBackground(new Color(24, 61, 110));
        btnCursos.setForeground(new Color(203, 213, 225));
        btnCursos.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnCursos);
        
        JButton btnMaterias = new JButton("Materias");
        btnMaterias.setBounds(15, 152, 140, 35);
        btnMaterias.setBackground(new Color(24, 61, 110));
        btnMaterias.setForeground(new Color(203, 213, 225));
        btnMaterias.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnMaterias);
        
        JButton btnCalificaciones = new JButton("Calificaciones");
        btnCalificaciones.setBounds(15, 197, 140, 35);
        btnCalificaciones.setBackground(new Color(24, 61, 110));
        btnCalificaciones.setForeground(new Color(203, 213, 225));
        btnCalificaciones.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnCalificaciones);
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(420);
        contentPane.add(splitPane, BorderLayout.CENTER);
		
        JPanel panelGestionInscripcion = new JPanel();
        panelGestionInscripcion.setLayout(null);
        splitPane.setLeftComponent(panelGestionInscripcion);
		
        JLabel lblNewLabel = new JLabel("Gestión de Inscripciones");
        lblNewLabel.setForeground(new Color(0, 128, 192));
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
        lblNewLabel.setBounds(95, 24, 230, 21);
        panelGestionInscripcion.add(lblNewLabel);
		
        JLabel lblAlumno = new JLabel("Alumno:");
        lblAlumno.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblAlumno.setBounds(10, 86, 110, 14);
        panelGestionInscripcion.add(lblAlumno);
		
        JLabel lblCurso = new JLabel("Curso / Materia:");
        lblCurso.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblCurso.setBounds(10, 126, 110, 14);
        panelGestionInscripcion.add(lblCurso);
		
        JLabel lblFecha = new JLabel("Fecha (DD/MM/YYYY):");
        lblFecha.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblFecha.setBounds(10, 166, 130, 14);
        panelGestionInscripcion.add(lblFecha);
		
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblEstado.setBounds(10, 206, 110, 14);
        panelGestionInscripcion.add(lblEstado);
		
        JLabel lblImporte = new JLabel("Importe Cuota ($):");
        lblImporte.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblImporte.setBounds(10, 246, 130, 21);
        panelGestionInscripcion.add(lblImporte);
		
        // Inicialización correcta de los JComboBox con tipos explícitos
        cmbAlumno = new JComboBox<ItemCombo>();
        cmbAlumno.setBounds(140, 84, 250, 22);
        panelGestionInscripcion.add(cmbAlumno);
        cargarComboAlumnos();

        cmbCurso = new JComboBox<ItemCombo>();
        cmbCurso.setBounds(140, 124, 250, 22);
        panelGestionInscripcion.add(cmbCurso);
        cargarComboCursos();
		
        textFecha = new JTextField();
        textFecha.setBounds(140, 165, 250, 22);
        textFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        panelGestionInscripcion.add(textFecha);
        textFecha.setColumns(10);
		
        txtImporteCuota = new JTextField();
        txtImporteCuota.setColumns(10);
        txtImporteCuota.setBounds(140, 247, 250, 22);
        panelGestionInscripcion.add(txtImporteCuota);
		
        cmbEstado = new JComboBox<String>();
        cmbEstado.addItem("Pendiente");
        cmbEstado.addItem("Confirmada");
        cmbEstado.addItem("Cancelada");
        cmbEstado.addItem("Finalizada");
        cmbEstado.setBounds(140, 204, 250, 22);
        panelGestionInscripcion.add(cmbEstado);
		
        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 12));
        btnRegistrar.setBackground(new Color(37, 99, 235));
        btnRegistrar.setBounds(48, 311, 100, 32);
        panelGestionInscripcion.add(btnRegistrar);
        btnRegistrar.addActionListener(e -> registrarInscripcion());
		
        JButton btnModificar = new JButton("Modificar");
        btnModificar.setForeground(Color.WHITE);
        btnModificar.setFont(new Font("Arial", Font.BOLD, 12));
        btnModificar.setBackground(new Color(59, 130, 246));
        btnModificar.setBounds(161, 311, 100, 32);
        panelGestionInscripcion.add(btnModificar);
        btnModificar.addActionListener(e -> modificarInscripcion());
		
        JButton btnDarDeBaja = new JButton("Dar de baja");
        btnDarDeBaja.setForeground(Color.WHITE);
        btnDarDeBaja.setFont(new Font("Arial", Font.BOLD, 12));
        btnDarDeBaja.setBackground(new Color(100, 116, 139));
        btnDarDeBaja.setBounds(274, 311, 100, 32);
        panelGestionInscripcion.add(btnDarDeBaja);
        btnDarDeBaja.addActionListener(e -> darDeBajaInscripcion());
		
        JPanel panelTablaInscripcion = new JPanel();
        splitPane.setRightComponent(panelTablaInscripcion);
        panelTablaInscripcion.setLayout(new BorderLayout(0, 0));
		
        JPanel panelFiltros = new JPanel();
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setPreferredSize(new Dimension(10, 55));
        panelTablaInscripcion.add(panelFiltros, BorderLayout.NORTH);
        panelFiltros.setLayout(null);
        
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblBuscar.setBounds(10, 11, 55, 33);
        panelFiltros.add(lblBuscar);
        
        txtBuscar = new JTextField();
        txtBuscar.setBounds(65, 18, 160, 22);
        panelFiltros.add(txtBuscar);
        txtBuscar.addActionListener(e -> buscarInscripciones());
        
        JLabel lblFiltrarEstado = new JLabel("Estado:");
        lblFiltrarEstado.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblFiltrarEstado.setBounds(235, 11, 55, 33);
        panelFiltros.add(lblFiltrarEstado);
        
        cmbFiltrarEstado = new JComboBox<String>();
        cmbFiltrarEstado.addItem("Todos");
        cmbFiltrarEstado.addItem("Pendiente");
        cmbFiltrarEstado.addItem("Confirmada");
        cmbFiltrarEstado.addItem("Cancelada");
        cmbFiltrarEstado.addItem("Finalizada");
        cmbFiltrarEstado.setBounds(290, 18, 120, 22);
        panelFiltros.add(cmbFiltrarEstado);
        
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Arial", Font.BOLD, 11));
        btnFiltrar.setBackground(new Color(30, 58, 138));
        btnFiltrar.setBounds(420, 17, 90, 25);
        panelFiltros.add(btnFiltrar);
        btnFiltrar.addActionListener(e -> filtrarInscripciones());
        
        JScrollPane scrollPane = new JScrollPane();
        panelTablaInscripcion.add(scrollPane, BorderLayout.CENTER);
        
        table = new JTable();
        scrollPane.setViewportView(table);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarSeleccionDeTabla();
            }
        });

        actualizarTabla(inscripcionServicio.buscarCampo(""));
    }

    private void cargarComboAlumnos() {
        cmbAlumno.removeAllItems();
        List<Alumno> alumnos = alumnoDao.obtenerTodos();
        for (Alumno a : alumnos) {
            String texto = a.getNombre() + ", " + a.getApellido() + " - " + a.getDni();
            cmbAlumno.addItem(new ItemCombo(a.getId_alumno(), texto));
        }
    }

    private void cargarComboCursos() {
        cmbCurso.removeAllItems();
        List<Curso> cursos = cursoDao.obtenerTodos();
        for (Curso c : cursos) {
            Materia m = materiaDao.obtenerPorId(c.getId_materia());
            String nombreMateria = m != null ? m.getNombre_materia() : "Materia desconocida";
            String texto = c.getAño() + "° - " + nombreMateria + " (" + c.getTurno() + ")";
            cmbCurso.addItem(new ItemCombo(c.getId_curso(), texto));
        }
    }

    private void actualizarTabla(List<Inscripcion> lista) {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        modelo.addColumn("ID");
        modelo.addColumn("Alumno");
        modelo.addColumn("Curso");
        modelo.addColumn("Fecha");
        modelo.addColumn("Estado");
        modelo.addColumn("Importe ($)");

        for (Inscripcion ins : lista) {
            Alumno a = alumnoDao.obtenerPorId(ins.getId_alumno());
            String nombreAlumno = a != null ? a.getNombre() + " " + a.getApellido() : "Desconocido";
            
            modelo.addRow(new Object[]{
                ins.getId_inscripcion(),
                nombreAlumno,
                ins.getId_curso(),
                ins.getFecha_inscripcion(),
                ins.getEstado(),
                ins.getImporte_cuota()
            });
        }
        table.setModel(modelo);
    }

    private void registrarInscripcion() {
        try {
            ItemCombo alumnoSeleccionado = (ItemCombo) cmbAlumno.getSelectedItem();
            ItemCombo cursoSeleccionado = (ItemCombo) cmbCurso.getSelectedItem();

            if (alumnoSeleccionado == null || cursoSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un alumno y un curso válidos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer idAlumno = alumnoSeleccionado.getId();
            Integer idCurso = cursoSeleccionado.getId();

            String validacion = inscripcionServicio.puedeInscribirse(idAlumno, idCurso);
            if (!"OK".equals(validacion)) {
                JOptionPane.showMessageDialog(this, validacion, "Validación de Inscripción", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fecha = LocalDate.parse(textFecha.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Double importe = Double.parseDouble(txtImporteCuota.getText().trim());
            String estado = (String) cmbEstado.getSelectedItem();

            Inscripcion nueva = new Inscripcion();
            nueva.setId_alumno(idAlumno);
            nueva.setId_curso(idCurso);
            nueva.setFecha_inscripcion(fecha);
            nueva.setEstado(estado);
            nueva.setImporte_cuota(importe);

            inscripcionServicio.registrar(nueva);
            JOptionPane.showMessageDialog(this, "Inscripción registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarTabla(inscripcionServicio.buscarCampo(""));
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Revise los datos ingresados (Formato de fecha DD/MM/YYYY e importe numérico).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarInscripcion() {
        if (idInscripcionSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una inscripción de la tabla para modificar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ItemCombo alumnoSeleccionado = (ItemCombo) cmbAlumno.getSelectedItem();
            ItemCombo cursoSeleccionado = (ItemCombo) cmbCurso.getSelectedItem();

            LocalDate fecha = LocalDate.parse(textFecha.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Double importe = Double.parseDouble(txtImporteCuota.getText().trim());
            String estado = (String) cmbEstado.getSelectedItem();

            Inscripcion mod = new Inscripcion();
            mod.setId_inscripcion(idInscripcionSeleccionada);
            mod.setId_alumno(alumnoSeleccionado.getId());
            mod.setId_curso(cursoSeleccionado.getId());
            mod.setFecha_inscripcion(fecha);
            mod.setEstado(estado);
            mod.setImporte_cuota(importe);

            inscripcionServicio.modificar(mod);
            JOptionPane.showMessageDialog(this, "Inscripción modificada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarTabla(inscripcionServicio.buscarCampo(""));
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al modificar. Verifique los campos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void darDeBajaInscripcion() {
        if (idInscripcionSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione de la tabla la inscripción que desea dar de baja.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motivo = JOptionPane.showInputDialog(this, "Ingrese obligatoriamente el motivo de la baja:", "Motivo de Baja", JOptionPane.WARNING_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La operación fue cancelada o el motivo está vacío.", "Operación Cancelada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        inscripcionServicio.darDeBaja(idInscripcionSeleccionada, motivo.trim());
        JOptionPane.showMessageDialog(this, "La inscripción ha sido dada de baja (Finalizada).", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        actualizarTabla(inscripcionServicio.buscarCampo(""));
        limpiarFormulario();
    }

    private void buscarInscripciones() {
        String texto = txtBuscar.getText().trim();
        actualizarTabla(inscripcionServicio.buscarCampo(texto));
    }

    private void filtrarInscripciones() {
        String estadoSeleccionado = (String) cmbFiltrarEstado.getSelectedItem();
        actualizarTabla(inscripcionServicio.filtrarEstadoInscrip(estadoSeleccionado));
    }

    private void cargarSeleccionDeTabla() {
        int fila = table.getSelectedRow();
        if (fila >= 0) {
            idInscripcionSeleccionada = (Integer) table.getValueAt(fila, 0);
            
            dao.Inscripcion_dao dao = new dao.Inscripcion_dao();
            Inscripcion ins = dao.obtenerPorId(idInscripcionSeleccionada);
            
            if (ins != null) {
                txtImporteCuota.setText(String.valueOf(ins.getImporte_cuota()));
                if (ins.getFecha_inscripcion() != null) {
                    textFecha.setText(ins.getFecha_inscripcion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                cmbEstado.setSelectedItem(ins.getEstado());

                for (int i = 0; i < cmbAlumno.getItemCount(); i++) {
                    ItemCombo item = cmbAlumno.getItemAt(i);
                    if (item.getId() == ins.getId_alumno()) {
                        cmbAlumno.setSelectedIndex(i);
                        break;
                    }
                }

                for (int i = 0; i < cmbCurso.getItemCount(); i++) {
                    ItemCombo item = cmbCurso.getItemAt(i);
                    if (item.getId() == ins.getId_curso()) {
                        cmbCurso.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void limpiarFormulario() {
        idInscripcionSeleccionada = null;
        txtImporteCuota.setText("");
        textFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (cmbAlumno.getItemCount() > 0) cmbAlumno.setSelectedIndex(0);
        if (cmbCurso.getItemCount() > 0) cmbCurso.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedIndex(0);
    }
}