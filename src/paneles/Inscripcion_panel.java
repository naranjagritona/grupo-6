package paneles;

import clases.Alumno;
import clases.Curso;
import clases.Inscripcion;
import clases.ItemCombo;
import clases.Materia;
import clases.Usuario; 
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
    
    private Usuario usuarioLogueado;
    private JButton btnInscripciones;
    private JButton btnCursos;
    private JButton btnMaterias;
    private JButton btnCalificaciones;
    private JButton btnFacturacion;
    private JButton btnAdmin; 
    private JButton btnCerrarSesion;
    
    private JPanel contentPane;
    private JPanel panelContenido;
    private JSplitPane splitPane;
    private JTextField textFecha;
    private JTextField txtImporteCuota;
    private JTextField txtBuscar;
    private JTextArea txtMotivoBaja; 
    private JTable table;
    private JPanel panelTablaInscripcion; 
    private JPanel panelGestionInscripcion;
    
    private JComboBox<ItemCombo> cmbAlumno;
    private JComboBox<ItemCombo> cmbCurso;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbFiltrarEstado;

    private Inscripcion_servicio inscripcionServicio;
    private Alumno_dao alumnoDao;
    private Curso_dao cursoDao;
    private Materia_dao materiaDao;
    
    private Integer idInscripcionSeleccionada = null;

    // constructor por defecto
    public Inscripcion_panel() {
        this(null);
    }

    // constructor que recibe el usuario logueado
    public Inscripcion_panel(Usuario usuario) {
        this.usuarioLogueado = usuario;

        this.inscripcionServicio = new Inscripcion_servicio();
        this.alumnoDao = new Alumno_dao();
        this.cursoDao = new Curso_dao();
        this.materiaDao = new Materia_dao();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1150, 680);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        panelContenido = new JPanel(new BorderLayout());
        
        // armamos el encabezado superior
        JPanel panelTop = new JPanel();
        panelTop.setPreferredSize(new Dimension(10, 50));
        panelTop.setBackground(new Color(31, 78, 140));
        contentPane.add(panelTop, BorderLayout.NORTH);
        panelTop.setLayout(new BorderLayout(0, 0));
        
        JLabel lblTituloAcademia = new JLabel(" Academia Integral XXI");
        lblTituloAcademia.setForeground(Color.WHITE);
        lblTituloAcademia.setFont(new Font("Tahoma", Font.BOLD, 18));
        panelTop.add(lblTituloAcademia, BorderLayout.CENTER);
        
        JPanel panelHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        panelHeaderRight.setOpaque(false);
        
        btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setBackground(new Color(20, 40, 75)); 
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelHeaderRight.add(btnCerrarSesion);
        panelTop.add(panelHeaderRight, BorderLayout.EAST);
        
        // armamos el menu lateral izquierdo
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
        
        btnInscripciones = new JButton("Inscripciones");
        btnInscripciones.setBounds(15, 61, 140, 35);
        btnInscripciones.setBackground(new Color(37, 99, 235));
        btnInscripciones.setForeground(Color.WHITE);
        btnInscripciones.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnInscripciones);
        
        btnCursos = new JButton("Cursos");
        btnCursos.setBounds(15, 107, 140, 35);
        btnCursos.setBackground(new Color(24, 61, 110));
        btnCursos.setForeground(new Color(203, 213, 225));
        btnCursos.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnCursos);

        btnMaterias = new JButton("Materia");
        btnMaterias.setBounds(15, 152, 140, 35);
        btnMaterias.setBackground(new Color(24, 61, 110));
        btnMaterias.setForeground(new Color(203, 213, 225));
        btnMaterias.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnMaterias);
        
        btnCalificaciones = new JButton("Calificaciones");
        btnCalificaciones.setBounds(15, 197, 140, 35);
        btnCalificaciones.setBackground(new Color(24, 61, 110));
        btnCalificaciones.setForeground(new Color(203, 213, 225));
        btnCalificaciones.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnCalificaciones);
        
        btnFacturacion = new JButton("Factura");
        btnFacturacion.setBounds(15, 288, 140, 35);
        btnFacturacion.setBackground(new Color(24, 61, 110));
        btnFacturacion.setForeground(new Color(203, 213, 225));
        btnFacturacion.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnFacturacion);
        btnFacturacion.addActionListener(e -> mostrarFacturacion());

        btnAdmin = new JButton("Admin");
        btnAdmin.setBounds(15, 243, 140, 35);
        btnAdmin.setBackground(new Color(24, 61, 110));
        btnAdmin.setForeground(new Color(203, 213, 225));
        btnAdmin.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnAdmin);

        // evento para cerrar sesion con cartel de confirmacion
        btnCerrarSesion.addActionListener(_ -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, "¿Estás seguro de que querés cerrar sesión?", "Cerrar sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                new dao.Usuario_dao().cerrarSesion(usuarioLogueado);
                this.dispose(); 
                servicios.Facturacion_servicio.cerrarSesion();
                Login_panel login = new Login_panel(); 
                login.setVisible(true);
            }
        });

        // listener para ir al panel de cursos
        btnCursos.addActionListener(_ -> {
            panelContenido.removeAll();
            splitPane.setVisible(false);
            Cursos_panel panelCursos = new Cursos_panel(usuarioLogueado);
            panelContenido.add(panelCursos, BorderLayout.CENTER);
            btnCursos.setBackground(new Color(37, 99, 235));
            btnCursos.setForeground(Color.WHITE);
            btnInscripciones.setBackground(new Color(24, 61, 110));
            btnInscripciones.setForeground(new Color(203, 213, 225));
            if (btnAdmin != null) {
                btnAdmin.setBackground(new Color(24, 61, 110));
                btnAdmin.setForeground(new Color(203, 213, 225));
            }
            panelContenido.revalidate();
            panelContenido.repaint();
        });
 
        // listener para volver al panel de inscripciones
        btnInscripciones.addActionListener(_ -> {
            panelContenido.removeAll();
            splitPane.setVisible(true);
            panelContenido.add(splitPane, BorderLayout.CENTER);
            btnInscripciones.setBackground(new Color(37, 99, 235));
            btnInscripciones.setForeground(Color.WHITE);
            btnCursos.setBackground(new Color(24, 61, 110));
            btnCursos.setForeground(new Color(203, 213, 225));
            panelContenido.revalidate();
            panelContenido.repaint();
        });

        // listener para ir a calificaciones
        btnCalificaciones.addActionListener(_ -> {
            panelContenido.removeAll();
            splitPane.setVisible(false);
            panelContenido.add(new Calificaciones_panel(usuarioLogueado), BorderLayout.CENTER);
            btnCalificaciones.setBackground(new Color(37, 99, 235));
            btnCalificaciones.setForeground(Color.WHITE);
            btnInscripciones.setBackground(new Color(24, 61, 110));
            btnInscripciones.setForeground(new Color(203, 213, 225));
            btnCursos.setBackground(new Color(24, 61, 110));
            btnCursos.setForeground(new Color(203, 213, 225));
            btnMaterias.setBackground(new Color(24, 61, 110));
            btnMaterias.setForeground(new Color(203, 213, 225));
            if (btnAdmin != null) {
                btnAdmin.setBackground(new Color(24, 61, 110));
                btnAdmin.setForeground(new Color(203, 213, 225));
            }
            panelContenido.revalidate();
            panelContenido.repaint();
        });
        
        // listener para ir al panel de admin
        btnAdmin.addActionListener(_ -> {
            panelContenido.removeAll();
            splitPane.setVisible(false);
            Admin_panel panelAdmin = new Admin_panel(usuarioLogueado);
            panelContenido.add(panelAdmin, BorderLayout.CENTER);
            btnAdmin.setBackground(new Color(37, 99, 235));
            btnAdmin.setForeground(Color.WHITE);
            btnInscripciones.setBackground(new Color(24, 61, 110));
            btnInscripciones.setForeground(new Color(203, 213, 225));
            btnCursos.setBackground(new Color(24, 61, 110));
            btnCursos.setForeground(new Color(203, 213, 225));
            panelContenido.revalidate();
            panelContenido.repaint();
        });
        
        contentPane.add(panelContenido, BorderLayout.CENTER);
        
        splitPane = new JSplitPane();
        splitPane.setDividerLocation(430);
        panelContenido.add(splitPane, BorderLayout.CENTER);
        
        // panel izquierdo para gestionar inscripciones (formulario)
        panelGestionInscripcion = new JPanel();
        panelGestionInscripcion.setLayout(null);
        splitPane.setLeftComponent(panelGestionInscripcion);
		
        JLabel lblNewLabel = new JLabel("Gestión de Inscripciones");
        lblNewLabel.setForeground(new Color(0, 128, 192));
        lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 17));
        lblNewLabel.setBounds(95, 15, 230, 21);
        panelGestionInscripcion.add(lblNewLabel);
		
        JLabel lblAlumno = new JLabel("Alumno:");
        lblAlumno.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblAlumno.setBounds(10, 60, 110, 14);
        panelGestionInscripcion.add(lblAlumno);
		
        JLabel lblCurso = new JLabel("Curso / Materia:");
        lblCurso.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblCurso.setBounds(10, 98, 110, 14);
        panelGestionInscripcion.add(lblCurso);
		
        JLabel lblFecha = new JLabel("Fecha (DD/MM/YYYY):");
        lblFecha.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblFecha.setBounds(10, 136, 138, 14);
        panelGestionInscripcion.add(lblFecha);
		
        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblEstado.setBounds(10, 174, 110, 14);
        panelGestionInscripcion.add(lblEstado);
		
        JLabel lblImporte = new JLabel("Importe Cuota ($):");
        lblImporte.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblImporte.setBounds(10, 212, 130, 21);
        panelGestionInscripcion.add(lblImporte);

        JLabel lblMotivo = new JLabel("Motivo baja/traslado:");
        lblMotivo.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblMotivo.setBounds(10, 250, 135, 21);
        panelGestionInscripcion.add(lblMotivo);
		
        cmbAlumno = new JComboBox<ItemCombo>();
        cmbAlumno.setBounds(150, 58, 260, 22);
        panelGestionInscripcion.add(cmbAlumno);

        cmbCurso = new JComboBox<ItemCombo>();
        cmbCurso.setBounds(150, 96, 260, 22);
        panelGestionInscripcion.add(cmbCurso);
		
        textFecha = new JTextField();
        textFecha.setBounds(150, 134, 260, 22);
        textFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        panelGestionInscripcion.add(textFecha);
        textFecha.setColumns(10);
		
        txtImporteCuota = new JTextField();
        txtImporteCuota.setColumns(10);
        txtImporteCuota.setBounds(150, 212, 260, 22);
        panelGestionInscripcion.add(txtImporteCuota);

        // caja de texto de lectura para ver el motivo si esta dada de baja
        txtMotivoBaja = new JTextArea();
        txtMotivoBaja.setEditable(false);
        txtMotivoBaja.setLineWrap(true);
        txtMotivoBaja.setWrapStyleWord(true);
        txtMotivoBaja.setBackground(new Color(245, 247, 250));
        txtMotivoBaja.setFont(new Font("Tahoma", Font.PLAIN, 12));
        JScrollPane scrollMotivo = new JScrollPane(txtMotivoBaja);
        scrollMotivo.setBounds(150, 250, 260, 55);
        panelGestionInscripcion.add(scrollMotivo);
		
        cmbEstado = new JComboBox<String>();
        cmbEstado.addItem("Pendiente");
        cmbEstado.addItem("Confirmada");
        cmbEstado.addItem("Cancelada");
        cmbEstado.addItem("Finalizada");
        cmbEstado.setBounds(150, 172, 260, 22);
        panelGestionInscripcion.add(cmbEstado);
		
        // botones de accion
        JButton btnRegistrar = new JButton("Registrar");
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 11));
        btnRegistrar.setBackground(new Color(37, 99, 235));
        btnRegistrar.setBounds(15, 325, 92, 32);
        panelGestionInscripcion.add(btnRegistrar);
        btnRegistrar.addActionListener(_ -> registrarInscripcion());
		
        JButton btnModificar = new JButton("Modificar");
        btnModificar.setForeground(Color.WHITE);
        btnModificar.setFont(new Font("Arial", Font.BOLD, 11));
        btnModificar.setBackground(new Color(59, 130, 246));
        btnModificar.setBounds(112, 325, 92, 32);
        panelGestionInscripcion.add(btnModificar);
        btnModificar.addActionListener(_ -> modificarInscripcion());
		
        JButton btnDarDeBaja = new JButton("Dar de baja");
        btnDarDeBaja.setForeground(Color.WHITE);
        btnDarDeBaja.setFont(new Font("Arial", Font.BOLD, 11));
        btnDarDeBaja.setBackground(new Color(100, 116, 139));
        btnDarDeBaja.setBounds(209, 325, 98, 32);
        panelGestionInscripcion.add(btnDarDeBaja);
        btnDarDeBaja.addActionListener(_ -> darDeBajaInscripcion());

        JButton btnCambiarCurso = new JButton("Cambiar curso");
        btnCambiarCurso.setForeground(Color.WHITE);
        btnCambiarCurso.setFont(new Font("Arial", Font.BOLD, 11));
        btnCambiarCurso.setBackground(new Color(13, 148, 136));
        btnCambiarCurso.setBounds(312, 325, 105, 32);
        panelGestionInscripcion.add(btnCambiarCurso);
        btnCambiarCurso.addActionListener(_ -> abrirDialogoCambioCurso());
		
        // panel derecho para la tabla y filtros
        panelTablaInscripcion = new JPanel();
        splitPane.setRightComponent(panelTablaInscripcion);
        panelTablaInscripcion.setLayout(new BorderLayout(0, 0));
		
        JPanel panelFiltros = new JPanel();
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setPreferredSize(new Dimension(10, 55));
        panelTablaInscripcion.add(panelFiltros, BorderLayout.NORTH);
        panelFiltros.setLayout(null);
        
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblBuscar.setBounds(10, 11, 55, 33);
        panelFiltros.add(lblBuscar);
        
        txtBuscar = new JTextField();
        txtBuscar.setBounds(60, 18, 150, 22);
        panelFiltros.add(txtBuscar);

        // timer para buscar en tiempo real mientras escribe
        Timer timerBusqueda = new Timer(300, _ -> buscarInscripciones());
        timerBusqueda.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timerBusqueda.restart(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timerBusqueda.restart(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timerBusqueda.restart(); }
        });
        
        JLabel lblFiltrarEstado = new JLabel("Estado:");
        lblFiltrarEstado.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblFiltrarEstado.setBounds(225, 11, 55, 33);
        panelFiltros.add(lblFiltrarEstado);
        
        cmbFiltrarEstado = new JComboBox<String>();
        cmbFiltrarEstado.addItem("Todos");
        cmbFiltrarEstado.addItem("Pendiente");
        cmbFiltrarEstado.addItem("Confirmada");
        cmbFiltrarEstado.addItem("Cancelada");
        cmbFiltrarEstado.addItem("Finalizada");
        cmbFiltrarEstado.setBounds(275, 18, 110, 22);
        panelFiltros.add(cmbFiltrarEstado);
        
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Arial", Font.BOLD, 11));
        btnFiltrar.setBackground(new Color(30, 58, 138));
        btnFiltrar.setBounds(395, 17, 85, 25);
        panelFiltros.add(btnFiltrar);
        btnFiltrar.addActionListener(_ -> filtrarInscripciones());
        
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

        aplicarPermisosPorRol();
        ordenarMenuLateral();
        cargarDatosAsincronos();
    }

    // aplicamos los permisos segun el rol del usuario que ingreso
    private void aplicarPermisosPorRol() {
        if (usuarioLogueado == null) return;

        int rol = usuarioLogueado.getId_rol();
        btnFacturacion.setVisible(rol==1 || rol==3);
        if (rol == 1) { // admin
            btnInscripciones.setVisible(true);
            btnCursos.setVisible(true);
            btnMaterias.setVisible(true);
            btnCalificaciones.setVisible(true);
            btnAdmin.setVisible(true);
            
            btnInscripciones.setBounds(15, 61, 140, 35);
            btnCursos.setBounds(15, 107, 140, 35);
            btnMaterias.setBounds(15, 152, 140, 35);
            btnCalificaciones.setBounds(15, 197, 140, 35);
            btnAdmin.setBounds(15, 243, 140, 35);
            
        } else if (rol == 2) { // docente
            btnInscripciones.setVisible(false);
            btnCursos.setVisible(true);
            btnMaterias.setVisible(true);
            btnCalificaciones.setVisible(true);
            btnAdmin.setVisible(false);
            
            btnCursos.setBounds(15, 61, 140, 35);
            btnMaterias.setBounds(15, 107, 140, 35);
            btnCalificaciones.setBounds(15, 152, 140, 35);
            
            mostrarPanelCursos();

        } else if (rol == 3) { // alumno
            btnAdmin.setVisible(false);
            btnInscripciones.setVisible(false);
            splitPane.setVisible(false);
            mostrarFacturacion();
        }
    }
    
    private void ordenarMenuLateral() {
        // Acomodamos solo las opciones visibles para no dejar espacios entre los botones.
        JButton[] opciones={btnCursos,btnMaterias,btnCalificaciones,btnInscripciones,btnFacturacion,btnAdmin};
        int posicion=61;
        Container menu=btnCursos.getParent();
        for(int i=0;i<opciones.length;i++) {
            JButton boton=opciones[i];
            menu.setComponentZOrder(boton,i);
            if(boton.isVisible()) {
                boton.setBounds(15,posicion,140,35);
                posicion+=45;
            }
        }
    }

    private void mostrarFacturacion() {
        if(usuarioLogueado==null || usuarioLogueado.getId_rol()==2) return;
        panelContenido.removeAll();
        panelContenido.add(new Facturacion_panel(usuarioLogueado),BorderLayout.CENTER);
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarPanelCursos() {
        panelContenido.removeAll();
        splitPane.setVisible(false);

        Cursos_panel panelCursos = new Cursos_panel(usuarioLogueado);
        panelContenido.add(panelCursos, BorderLayout.CENTER);

        btnCursos.setBackground(new Color(37, 99, 235));
        btnCursos.setForeground(Color.WHITE);

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    // filtramos solo las inscripciones del alumno logueado
    private void cargarInscripcionesPropiasAlumno() {
        new SwingWorker<Void, Void>() {
            List<Inscripcion> misInscripciones;

            @Override
            protected Void doInBackground() {
                List<Alumno> alumnos = alumnoDao.obtenerTodos();
                Integer idAlumnoLogueado = null;
                
                for (Alumno a : alumnos) {
                    if (a.getId_usuario() != null && a.getId_usuario().equals(usuarioLogueado.getId_usuario())) {
                        idAlumnoLogueado = a.getId_alumno();
                        break;
                    }
                }

                if (idAlumnoLogueado != null) {
                    List<Inscripcion> todas = inscripcionServicio.buscarCampo("");
                    misInscripciones = new java.util.ArrayList<>();
                    for (Inscripcion ins : todas) {
                        if (ins.getId_alumno().equals(idAlumnoLogueado)) {
                            misInscripciones.add(ins);
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (misInscripciones != null) {
                    actualizarTabla(misInscripciones);
                }
            }
        }.execute();
    }

    // cargamos los combos y la tabla de forma asincrona para que no se trabe la pantalla
    private void cargarDatosAsincronos() {
        if (usuarioLogueado != null && usuarioLogueado.getId_rol() == 3) {
            return; 
        }

        new SwingWorker<Void, Void>() {
            List<Alumno> alumnos;
            List<Curso> cursos;
            List<Materia> materias;
            List<Inscripcion> inscripciones;

            @Override
            protected Void doInBackground() {
                alumnos = alumnoDao.obtenerTodos();
                cursos = cursoDao.obtenerTodos();
                materias = materiaDao.obtenerTodos();
                inscripciones = inscripcionServicio.buscarCampo("");
                return null;
            }

            @Override
            protected void done() {
                cmbAlumno.removeAllItems();
                for (Alumno a : alumnos) {
                    String texto = a.getNombre() + ", " + a.getApellido() + " - " + a.getDni();
                    cmbAlumno.addItem(new ItemCombo(a.getId_alumno(), texto));
                }

                cmbCurso.removeAllItems();
                java.util.Map<Integer, String> mapaMaterias = new java.util.HashMap<>();
                for (Materia m : materias) {
                    mapaMaterias.put(m.getId_materia(), m.getNombre_materia());
                }

                for (Curso c : cursos) {
                    String nombreMateria = mapaMaterias.getOrDefault(c.getId_materia(), "Materia desconocida");
                    String texto = c.getAño() + "° - " + nombreMateria + " (" + c.getTurno() + ")";
                    cmbCurso.addItem(new ItemCombo(c.getId_curso(), texto));
                }

                actualizarTabla(inscripciones);
            }
        }.execute();
    }

    // actualizamos la tabla con los datos que le pasemos
    private void actualizarTabla(List<Inscripcion> lista) {
        DefaultTableModel modelo = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        modelo.addColumn("ID");
        modelo.addColumn("Alumno");
        modelo.addColumn("Curso");
        modelo.addColumn("Fecha");
        modelo.addColumn("Estado");
        modelo.addColumn("Importe ($)");

        List<Alumno> alumnos = alumnoDao.obtenerTodos();
        java.util.Map<Integer, String> mapaAlumnos = new java.util.HashMap<>();
        for (Alumno a : alumnos) {
            mapaAlumnos.put(a.getId_alumno(), a.getNombre() + " " + a.getApellido());
        }

        for (Inscripcion ins : lista) {
            String nombreAlumno = mapaAlumnos.getOrDefault(ins.getId_alumno(), "Desconocido");
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
    
    // validamos las reglas y registramos la inscripcion
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

            Integer idUsuarioActual = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
            inscripcionServicio.registrar(nueva, idUsuarioActual);
            
            JOptionPane.showMessageDialog(this, "Inscripción registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarTabla(inscripcionServicio.buscarCampo(""));
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Revise los datos ingresados (Formato de fecha DD/MM/YYYY e importe numérico).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // modificamos los datos de la inscripcion seleccionada
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

            Integer idUsuarioActual = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
            inscripcionServicio.modificar(mod, idUsuarioActual);
            
            JOptionPane.showMessageDialog(this, "Inscripción modificada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarTabla(inscripcionServicio.buscarCampo(""));
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al modificar. Verifique los campos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // damos de baja la inscripcion pidiendo el motivo obligatoriamente
    private void darDeBajaInscripcion() {
        if (idInscripcionSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione de la tabla la inscripción que desea dar de baja.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motivo = JOptionPane.showInputDialog(this, "Ingrese el motivo de la baja:", "Motivo de Baja", JOptionPane.WARNING_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La operación fue cancelada o el motivo está vacío.", "Operación Cancelada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idUsuarioActual = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
        inscripcionServicio.darDeBaja(idInscripcionSeleccionada, motivo.trim(), idUsuarioActual);
        
        JOptionPane.showMessageDialog(this, "La inscripción ha sido dada de baja.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        actualizarTabla(inscripcionServicio.buscarCampo(""));
        limpiarFormulario();
    }

    // abrimos el dialogo flotante con el filtro inteligente para cambiar de curso o pasar al año siguiente
    private void abrirDialogoCambioCurso() {
        if (idInscripcionSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná una inscripción de la tabla para cambiar de curso.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Inscripcion inscripcionActual = inscripcionServicio.buscarCampo("").stream()
            .filter(i -> i.getId_inscripcion().equals(idInscripcionSeleccionada))
            .findFirst().orElse(null);
        
        if (inscripcionActual == null) return;

        Curso cursoActual = cursoDao.obtenerPorId(inscripcionActual.getId_curso());
        if (cursoActual == null) return;

        int añoActualDelAlumno = cursoActual.getAño();

        JComboBox<ItemCombo> cmbNuevoCurso = new JComboBox<>();
        JTextField txtMotivo = new JTextField(20);

        Object[] opcionesTipo = {"Cambio de curso (mismo año)", "Promoción al Año Siguiente"};
        int eleccion = JOptionPane.showOptionDialog(
            this, 
            "¿Qué tipo de cambio deseas realizar para el alumno?", 
            "Tipo de Traslado", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, opcionesTipo, opcionesTipo[0]
        );

        if (eleccion == JOptionPane.CLOSED_OPTION) return;

        int añoObjetivo = (eleccion == 0) ? añoActualDelAlumno : (añoActualDelAlumno + 1);

        List<Curso> cursos = cursoDao.obtenerTodos();
        List<Materia> materias = materiaDao.obtenerTodos();
        java.util.Map<Integer, String> mapaMaterias = new java.util.HashMap<>();
        for (Materia m : materias) {
            mapaMaterias.put(m.getId_materia(), m.getNombre_materia());
        }

        for (Curso c : cursos) {
            if ("Activo".equalsIgnoreCase(c.getEstado()) && c.getAño() == añoObjetivo) {
                String nombreMateria = mapaMaterias.getOrDefault(c.getId_materia(), "Materia");
                String texto = c.getAño() + "° - " + nombreMateria + " (" + c.getTurno() + ")";
                cmbNuevoCurso.addItem(new ItemCombo(c.getId_curso(), texto));
            }
        }

        if (cmbNuevoCurso.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay cursos activos disponibles para el " + añoObjetivo + "° año.", "Sin Cursos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panelForm = new JPanel(new GridLayout(2, 2, 8, 8));
        panelForm.add(new JLabel("Curso de Destino (" + añoObjetivo + "° Año):"));
        panelForm.add(cmbNuevoCurso);
        panelForm.add(new JLabel("Motivo del traslado:"));
        panelForm.add(txtMotivo);

        int resultado = JOptionPane.showConfirmDialog(
            this, panelForm, "Confirmar Cambio de Curso",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {
            String motivo = txtMotivo.getText().trim();
            if (motivo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El motivo del traslado es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ItemCombo cursoSeleccionado = (ItemCombo) cmbNuevoCurso.getSelectedItem();
            if (cursoSeleccionado == null) return;

            try {
                Integer idUsuarioActual = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                inscripcionServicio.cambiarCurso(
                    idInscripcionSeleccionada, 
                    cursoSeleccionado.getId(), 
                    motivo, 
                    idUsuarioActual
                );

                JOptionPane.showMessageDialog(this, "¡Cambio de curso realizado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarTabla(inscripcionServicio.buscarCampo(""));
                limpiarFormulario();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo completar el cambio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void buscarInscripciones() {
        String texto = txtBuscar.getText().trim();
        actualizarTabla(inscripcionServicio.buscarCampo(texto));
    }

    private void filtrarInscripciones() {
        String estadoSeleccionado = (String) cmbFiltrarEstado.getSelectedItem();
        actualizarTabla(inscripcionServicio.filtrarEstadoInscrip(estadoSeleccionado));
    }

    // cargamos los datos seleccionados de la tabla al formulario e incluimos el motivo de baja
    private void cargarSeleccionDeTabla() {
        int fila = table.getSelectedRow();
        if (fila >= 0) {
            idInscripcionSeleccionada = (Integer) table.getValueAt(fila, 0);
            
            Inscripcion ins = inscripcionServicio.buscarCampo("").stream()
                .filter(i -> i.getId_inscripcion().equals(idInscripcionSeleccionada))
                .findFirst().orElse(null);
            
            if (ins != null) {
                txtImporteCuota.setText(String.valueOf(ins.getImporte_cuota()));
                txtMotivoBaja.setText(ins.getMotivo_baja() != null ? ins.getMotivo_baja() : "Sin motivo registrado");
                
                if (ins.getFecha_inscripcion() != null) {
                    textFecha.setText(ins.getFecha_inscripcion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                cmbEstado.setSelectedItem(ins.getEstado());

                for (int i = 0; i < cmbAlumno.getItemCount(); i++) {
                    ItemCombo item = cmbAlumno.getItemAt(i);
                    if (item.getId().equals(ins.getId_alumno())) {
                        cmbAlumno.setSelectedIndex(i);
                        break;
                    }
                }

                for (int i = 0; i < cmbCurso.getItemCount(); i++) {
                    ItemCombo item = cmbCurso.getItemAt(i);
                    if (item.getId().equals(ins.getId_curso())) {
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
        txtMotivoBaja.setText("");
        textFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (cmbAlumno.getItemCount() > 0) cmbAlumno.setSelectedIndex(0);
        if (cmbCurso.getItemCount() > 0) cmbCurso.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedIndex(0);
    }
}