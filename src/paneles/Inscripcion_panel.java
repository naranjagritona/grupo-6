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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// organizamos el menú principal y los formularios para administrar inscripciones
public class Inscripcion_panel extends JFrame {

    private static final long serialVersionUID = 1L;

    private Usuario usuarioLogueado;
    private JButton btnInscripciones;
    private JButton btnCursos;
    private JButton btnMaterias;
    private JButton btnCalificaciones;
    private JButton btnFacturacion;
    private JButton btnAdmin;
    private JButton btnAsistencia;
    private JButton btnCerrarSesion;

    private JPanel contentPane;
    private JPanel panelContenido;
    private JSplitPane splitPane;
    private JTextField textFecha;
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
    // conservamos los datos cargados para filtrar por año, división y alumno sin repetir consultas
    private JComboBox<Integer> cmbAnio;
    private JComboBox<ItemCombo> cmbDivision;
    private List<Curso> cursosCargados = java.util.List.of();
    private List<Inscripcion> inscripcionesCargadas = java.util.List.of();
    private java.util.Map<Integer, String> nombresAlumnos = new java.util.HashMap<>();
    private java.util.Map<Integer, String> documentosAlumnos = new java.util.HashMap<>();
    private boolean cargandoFiltros;


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

        btnMaterias = new JButton("Materias");
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
        btnCerrarSesion.addActionListener(
                _ -> {
                    int confirm =
                            JOptionPane.showConfirmDialog(
                                    this,
                                    "¿Estás seguro de que querés cerrar sesión?",
                                    "Cerrar sesión",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        new dao.Usuario_dao().cerrarSesion(usuarioLogueado);
                        this.dispose();
                        servicios.Facturacion_servicio.cerrarSesion();
                        Login_panel login = new Login_panel();
                        login.setVisible(true);
                    }
                });

        // listener para ir al panel de cursos
        btnCursos.addActionListener(
                _ -> {
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

        // abrimos el catálogo de materias como una vista independiente, igual que Cursos.
        btnMaterias.addActionListener(_ -> mostrarPanelMaterias());

        // listener para volver al panel de inscripciones
        btnInscripciones.addActionListener(
                _ -> {
                    servicios.Auditoria_servicio.registrar(
                            "ABRIR MODULO", "Inscripciones", "Consulta de inscripciones");
                    panelContenido.removeAll();
                    splitPane.setVisible(true);
                    panelContenido.add(splitPane, BorderLayout.CENTER);
                    btnInscripciones.setBackground(new Color(37, 99, 235));
                    btnInscripciones.setForeground(Color.WHITE);
                    btnCursos.setBackground(new Color(24, 61, 110));
                    btnCursos.setForeground(new Color(203, 213, 225));
                    btnMaterias.setBackground(new Color(24, 61, 110));
                    btnMaterias.setForeground(new Color(203, 213, 225));
                    panelContenido.revalidate();
                    panelContenido.repaint();
                });

        // listener para ir a calificaciones
        btnCalificaciones.addActionListener(
                _ -> {
                    panelContenido.removeAll();
                    splitPane.setVisible(false);
                    panelContenido.add(
                            new Calificaciones_panel(usuarioLogueado), BorderLayout.CENTER);
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
        btnAdmin.addActionListener(
                _ -> {
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

        JLabel lblCurso = new JLabel("Año / División:");
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

        JLabel lblMotivo = new JLabel("Motivo baja/traslado:");
        lblMotivo.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblMotivo.setBounds(10, 212, 135, 21);
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

        // caja de texto de lectura para ver el motivo si esta dada de baja
        txtMotivoBaja = new JTextArea();
        txtMotivoBaja.setEditable(false);
        txtMotivoBaja.setLineWrap(true);
        txtMotivoBaja.setWrapStyleWord(true);
        txtMotivoBaja.setBackground(new Color(245, 247, 250));
        txtMotivoBaja.setFont(new Font("Tahoma", Font.PLAIN, 12));
        JScrollPane scrollMotivo = new JScrollPane(txtMotivoBaja);
        scrollMotivo.setBounds(150, 212, 260, 55);
        panelGestionInscripcion.add(scrollMotivo);

        cmbEstado = new JComboBox<String>();
        cmbEstado.addItem("Pendiente");
        cmbEstado.addItem("Confirmada");
        cmbEstado.addItem("Cancelada");
        cmbEstado.addItem("Finalizada");
        cmbEstado.addItem("Trasladado");
        cmbEstado.addItem("Recuperacion");
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

        JButton btnCambiarCurso = new JButton("Traslado");
        btnCambiarCurso.setForeground(Color.WHITE);
        btnCambiarCurso.setFont(new Font("Arial", Font.BOLD, 11));
        btnCambiarCurso.setBackground(new Color(13, 148, 136));
        btnCambiarCurso.setBounds(312, 325, 105, 32);
        panelGestionInscripcion.add(btnCambiarCurso);
        btnCambiarCurso.addActionListener(_ -> abrirDialogoCambioCurso());

        // abrimos la revisión de alumnos del año y división elegidos antes de promocionarlos
        JButton btnPromocion = Ficha_Inscripcion_dialog.boton("Promoción de año");
        btnPromocion.setBounds(15, 375, 180, 32);
        btnPromocion.addActionListener(e -> {
            ItemCombo division = (ItemCombo) cmbDivision.getSelectedItem();
            new Promocion_dialog(this, usuarioLogueado, cursosCargados,
                    division == null ? null : division.getId(), this::cargarDatosAsincronos).setVisible(true);
        });
        panelGestionInscripcion.add(btnPromocion);
        JButton btnSolicitudes = Ficha_Inscripcion_dialog.boton("Solicitudes a Dirección");
        btnSolicitudes.setBounds(205, 375, 210, 32);
        btnSolicitudes.addActionListener(e -> new Solicitudes_Cambio_dialog(this, usuarioLogueado, this::cargarDatosAsincronos).setVisible(true));
        panelGestionInscripcion.add(btnSolicitudes);


        // panel derecho para la tabla y filtros
        panelTablaInscripcion = new JPanel();
        splitPane.setRightComponent(panelTablaInscripcion);
        panelTablaInscripcion.setLayout(new BorderLayout(0, 0));

        JPanel panelFiltros = new JPanel();
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setPreferredSize(new Dimension(10, 105));
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
        txtBuscar
                .getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            @Override
                            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                                timerBusqueda.restart();
                            }

                            @Override
                            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                                timerBusqueda.restart();
                            }

                            @Override
                            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                                timerBusqueda.restart();
                            }
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
        cmbFiltrarEstado.addItem("Trasladado");
        cmbFiltrarEstado.addItem("Recuperacion");
        cmbFiltrarEstado.setBounds(275, 18, 110, 22);
        panelFiltros.add(cmbFiltrarEstado);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Arial", Font.BOLD, 11));
        btnFiltrar.setBackground(new Color(30, 58, 138));
        btnFiltrar.setBounds(395, 17, 85, 25);
        panelFiltros.add(btnFiltrar);

        // elegimos el año y después mostramos las divisiones que pertenecen a ese año
        JLabel lblAnio = new JLabel("Año:");
        lblAnio.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblAnio.setBounds(10, 65, 45, 22);
        panelFiltros.add(lblAnio);
        cmbAnio = new JComboBox<>();
        cmbAnio.setBounds(60, 65, 100, 22);
        panelFiltros.add(cmbAnio);
        JLabel lblDivision = new JLabel("División:");
        lblDivision.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblDivision.setBounds(180, 65, 65, 22);
        panelFiltros.add(lblDivision);
        cmbDivision = new JComboBox<>();
        cmbDivision.setBounds(245, 65, 235, 22);
        panelFiltros.add(cmbDivision);
        cmbAnio.addActionListener(e -> cargarDivisiones());
        cmbDivision.addActionListener(e -> { if (!cargandoFiltros) filtrarListado(); });
        cmbFiltrarEstado.addActionListener(e -> filtrarListado());

        btnFiltrar.addActionListener(_ -> filtrarInscripciones());

        JScrollPane scrollPane = new JScrollPane();
        panelTablaInscripcion.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        scrollPane.setViewportView(table);

        table.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (table.rowAtPoint(e.getPoint()) < 0) return;
                        cargarSeleccionDeTabla();
                        if (e.getClickCount() == 2 && idInscripcionSeleccionada != null) {
                            int id = idInscripcionSeleccionada;
                            Ficha_Inscripcion_dialog.ejecutar(Inscripcion_panel.this,
                                    () -> inscripcionServicio.ficha(id, usuarioLogueado.getId_usuario()),
                                    ficha -> new Ficha_Inscripcion_dialog(Inscripcion_panel.this, ficha).setVisible(true));
                        }
                    }
                });

        btnAsistencia = new JButton("Asistencia");
        btnAsistencia.setBackground(new Color(24, 61, 110));
        btnAsistencia.setForeground(new Color(203, 213, 225));
        btnAsistencia.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelMenu.add(btnAsistencia);
        btnAsistencia.addActionListener(e -> {
            panelContenido.removeAll(); splitPane.setVisible(false);
            panelContenido.add(new Asistencia_panel(usuarioLogueado), BorderLayout.CENTER);
            panelContenido.revalidate(); panelContenido.repaint();
        });
        aplicarPermisosPorRol();
        boolean secretario = servicios.Permisos_servicio.secretario(usuarioLogueado);
        btnRegistrar.setVisible(secretario);
        btnModificar.setVisible(secretario);
        btnDarDeBaja.setVisible(secretario);
        if (!secretario) { cmbAlumno.setEnabled(false); cmbCurso.setEnabled(false); cmbEstado.setEnabled(false); textFecha.setEditable(false); }

        ordenarMenuLateral();
        cargarDatosAsincronos();
    }

    // mostramos solamente los módulos que corresponden al rol del usuario que ingresó
    private void aplicarPermisosPorRol() {
        if (usuarioLogueado == null) return;
        boolean gestion = servicios.Permisos_servicio.gestion(usuarioLogueado);
        int rol = usuarioLogueado.getId_rol();
        btnInscripciones.setVisible(gestion);
        btnAdmin.setVisible(gestion);
        btnFacturacion.setVisible(gestion || rol == 3);
        btnAsistencia.setVisible(true);
        if (!gestion) {
            mostrarPanelCursos();
        }
    }

    private void ordenarMenuLateral() {
        // acomodamos solo las opciones visibles para no dejar espacios entre los botones.
        JButton[] opciones = {
            btnCursos, btnMaterias, btnCalificaciones, btnInscripciones, btnFacturacion, btnAsistencia, btnAdmin
        };
        int posicion = 61;
        Container menu = btnCursos.getParent();
        for (int i = 0; i < opciones.length; i++) {
            JButton boton = opciones[i];
            menu.setComponentZOrder(boton, i);
            if (boton.isVisible()) {
                boton.setBounds(15, posicion, 140, 35);
                posicion += 45;
            }
        }
    }

    private void mostrarFacturacion() {
        if (usuarioLogueado == null || (!servicios.Permisos_servicio.gestion(usuarioLogueado) && usuarioLogueado.getId_rol() != 3)) return;
        panelContenido.removeAll();
        panelContenido.add(new Facturacion_panel(usuarioLogueado), BorderLayout.CENTER);
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
        btnMaterias.setBackground(new Color(24, 61, 110));
        btnMaterias.setForeground(new Color(203, 213, 225));

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarPanelMaterias() {
        panelContenido.removeAll();
        splitPane.setVisible(false);
        panelContenido.add(new Materias_panel(usuarioLogueado), BorderLayout.CENTER);

        btnMaterias.setBackground(new Color(37, 99, 235));
        btnMaterias.setForeground(Color.WHITE);
        btnInscripciones.setBackground(new Color(24, 61, 110));
        btnInscripciones.setForeground(new Color(203, 213, 225));
        btnCursos.setBackground(new Color(24, 61, 110));
        btnCursos.setForeground(new Color(203, 213, 225));
        btnCalificaciones.setBackground(new Color(24, 61, 110));
        btnCalificaciones.setForeground(new Color(203, 213, 225));
        if (btnAdmin != null) {
            btnAdmin.setBackground(new Color(24, 61, 110));
            btnAdmin.setForeground(new Color(203, 213, 225));
        }

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
                    if (a.getId_usuario() != null
                            && a.getId_usuario().equals(usuarioLogueado.getId_usuario())) {
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

    // cargamos los cursos y los alumnos en segundo plano para mantener la pantalla disponible
    private void cargarDatosAsincronos() {
        if (!servicios.Permisos_servicio.gestion(usuarioLogueado)) return;
        Integer anioAnterior = (Integer) cmbAnio.getSelectedItem();
        ItemCombo divisionAnterior = (ItemCombo) cmbDivision.getSelectedItem();
        new SwingWorker<Object[], Void>() {
            protected Object[] doInBackground() {
                return new Object[] {alumnoDao.obtenerTodos(), cursoDao.obtenerTodos(), inscripcionServicio.buscarCampo("")};
            }

            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] datos = get();
                    List<Alumno> alumnos = (List<Alumno>) datos[0];
                    cursosCargados = (List<Curso>) datos[1];
                    inscripcionesCargadas = (List<Inscripcion>) datos[2];
                    cargandoFiltros = true;
                    cmbAlumno.removeAllItems(); cmbCurso.removeAllItems(); cmbAnio.removeAllItems();
                    nombresAlumnos.clear(); documentosAlumnos.clear();
                    for (Alumno alumno : alumnos) {
                        String nombre = alumno.getApellido() + ", " + alumno.getNombre();
                        nombresAlumnos.put(alumno.getId_alumno(), nombre);
                        documentosAlumnos.put(alumno.getId_alumno(), alumno.getDni());
                        cmbAlumno.addItem(new ItemCombo(alumno.getId_alumno(), nombre + " - " + alumno.getDni()));
                    }
                    java.util.Set<Integer> anios = new java.util.TreeSet<>();
                    for (Curso curso : cursosCargados) {
                        anios.add(curso.getAño());
                        cmbCurso.addItem(new ItemCombo(curso.getId_curso(), curso.getAño() + "° " + curso.getDivision()));
                    }
                    for (int anio : anios) cmbAnio.addItem(anio);
                    if (anioAnterior != null && anios.contains(anioAnterior)) cmbAnio.setSelectedItem(anioAnterior);
                    cargandoFiltros = false;
                    cargarDivisiones();
                    if (divisionAnterior != null) for (int i = 0; i < cmbDivision.getItemCount(); i++) {
                        if (cmbDivision.getItemAt(i).getId().equals(divisionAnterior.getId())) cmbDivision.setSelectedIndex(i);
                    }
                    filtrarListado();
                } catch (Exception error) {
                    cargandoFiltros = false;
                    JOptionPane.showMessageDialog(Inscripcion_panel.this,
                            error.getCause() == null ? error.getMessage() : error.getCause().getMessage());
                }
            }
        }.execute();
    }

    // mostramos las divisiones del año seleccionado con el turno y el período para distinguirlas
    private void cargarDivisiones() {
        if (cargandoFiltros) return;
        cargandoFiltros = true;
        cmbDivision.removeAllItems();
        Integer anio = (Integer) cmbAnio.getSelectedItem();
        for (Curso curso : cursosCargados) if (curso.getAño().equals(anio)) {
            cmbDivision.addItem(new ItemCombo(curso.getId_curso(), curso.getDivision() + " · " + curso.getTurno() + " · " + curso.getNombre_periodo()));
        }
        cargandoFiltros = false;
        filtrarListado();
    }

    // combinamos el año, la división, el estado y el texto de búsqueda en un mismo listado
    private void filtrarListado() {
        if (cargandoFiltros || table == null || cmbDivision == null) return;
        ItemCombo division = (ItemCombo) cmbDivision.getSelectedItem();
        String busqueda = txtBuscar.getText().trim().toLowerCase(java.util.Locale.ROOT);
        String estado = (String) cmbFiltrarEstado.getSelectedItem();
        List<Inscripcion> lista = new java.util.ArrayList<>();
        if (division != null) for (Inscripcion inscripcion : inscripcionesCargadas) {
            String alumno = nombresAlumnos.getOrDefault(inscripcion.getId_alumno(), "");
            String documento = documentosAlumnos.getOrDefault(inscripcion.getId_alumno(), "");
            if (inscripcion.getId_curso().equals(division.getId())
                    && ("Todos".equals(estado) || inscripcion.getEstado().equals(estado))
                    && (alumno + " " + documento).toLowerCase(java.util.Locale.ROOT).contains(busqueda)) lista.add(inscripcion);
        }
        actualizarTabla(lista);
    }

    // completamos la tabla de alumnos de la división sin incluir los nombres de las materias
    private void actualizarTabla(List<Inscripcion> lista) {
        DefaultTableModel modelo = new DefaultTableModel(new String[] {"ID", "Alumno", "DNI", "Año / División", "Fecha", "Estado"}, 0) {
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
        for (Inscripcion inscripcion : lista) {
            Curso curso = cursosCargados.stream().filter(c -> c.getId_curso().equals(inscripcion.getId_curso())).findFirst().orElse(null);
            modelo.addRow(new Object[] {inscripcion.getId_inscripcion(), nombresAlumnos.get(inscripcion.getId_alumno()), documentosAlumnos.get(inscripcion.getId_alumno()),
                    curso == null ? "" : curso.getAño() + "° " + curso.getDivision(), inscripcion.getFecha_inscripcion(), inscripcion.getEstado()});
        }
        table.setModel(modelo);
        table.setAutoCreateRowSorter(true);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 240, 247));
        table.getTableHeader().setForeground(new Color(24, 61, 110));
    }

    // validamos las reglas y registramos la inscripcion
    private void registrarInscripcion() {
        try {
            ItemCombo alumnoSeleccionado = (ItemCombo) cmbAlumno.getSelectedItem();
            ItemCombo cursoSeleccionado = (ItemCombo) cmbCurso.getSelectedItem();

            if (alumnoSeleccionado == null || cursoSeleccionado == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Seleccione un alumno y un curso válidos.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer idAlumno = alumnoSeleccionado.getId();
            Integer idCurso = cursoSeleccionado.getId();

            String validacion = inscripcionServicio.puedeInscribirse(idAlumno, idCurso);
            if (!"OK".equals(validacion)) {
                JOptionPane.showMessageDialog(
                        this, validacion, "Validación de Inscripción", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fecha =
                    LocalDate.parse(
                            textFecha.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String estado = (String) cmbEstado.getSelectedItem();

            Inscripcion nueva = new Inscripcion();
            nueva.setId_alumno(idAlumno);
            nueva.setId_curso(idCurso);
            nueva.setFecha_inscripcion(fecha);
            nueva.setEstado(estado);

            Integer idUsuarioActual =
                    usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
            inscripcionServicio.registrar(nueva, idUsuarioActual);

            JOptionPane.showMessageDialog(
                    this,
                    "Inscripción registrada con éxito.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarDatosAsincronos();
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // modificamos los datos de la inscripcion seleccionada
    private void modificarInscripcion() {
        if (idInscripcionSeleccionada == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione una inscripción de la tabla para modificar.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ItemCombo alumnoSeleccionado = (ItemCombo) cmbAlumno.getSelectedItem();
            ItemCombo cursoSeleccionado = (ItemCombo) cmbCurso.getSelectedItem();

            LocalDate fecha =
                    LocalDate.parse(
                            textFecha.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String estado = (String) cmbEstado.getSelectedItem();

            Inscripcion mod = new Inscripcion();
            mod.setId_inscripcion(idInscripcionSeleccionada);
            mod.setId_alumno(alumnoSeleccionado.getId());
            mod.setId_curso(cursoSeleccionado.getId());
            mod.setFecha_inscripcion(fecha);
            mod.setEstado(estado);

            Integer idUsuarioActual =
                    usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
            inscripcionServicio.modificar(mod, idUsuarioActual);

            JOptionPane.showMessageDialog(
                    this,
                    "Inscripción modificada con éxito.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            cargarDatosAsincronos();
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // damos de baja la inscripcion pidiendo el motivo obligatoriamente
    private void darDeBajaInscripcion() {
        if (idInscripcionSeleccionada == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione de la tabla la inscripción que desea dar de baja.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motivo =
                JOptionPane.showInputDialog(
                        this,
                        "Ingrese el motivo de la baja:",
                        "Motivo de Baja",
                        JOptionPane.WARNING_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "La operación fue cancelada o el motivo está vacío.",
                    "Operación Cancelada",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idUsuarioActual = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
        inscripcionServicio.darDeBaja(idInscripcionSeleccionada, motivo.trim(), idUsuarioActual);

        JOptionPane.showMessageDialog(
                this,
                "La inscripción ha sido dada de baja.",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
        cargarDatosAsincronos();
        limpiarFormulario();
    }

    // pedimos el destino y el motivo para trasladar de división o enviar una solicitud excepcional
    private void abrirDialogoCambioCurso() {
        if (idInscripcionSeleccionada == null) { JOptionPane.showMessageDialog(this, "Seleccioná una inscripción de la tabla."); return; }
        Inscripcion actual = inscripcionesCargadas.stream().filter(i -> i.getId_inscripcion().equals(idInscripcionSeleccionada)).findFirst().orElse(null);
        if (actual == null) return;
        Curso origen = cursosCargados.stream().filter(c -> c.getId_curso().equals(actual.getId_curso())).findFirst().orElse(null);
        if (origen == null) return;
        Object[] opciones = servicios.Permisos_servicio.secretario(usuarioLogueado)
                ? new Object[] {"Cambio de división", "Solicitud excepcional a Dirección"}
                : new Object[] {"Solicitud excepcional a Dirección"};
        int opcion = JOptionPane.showOptionDialog(this, "Elegí el tipo de cambio", "Cambio de curso o división", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (opcion < 0) return;
        boolean excepcional = opciones.length == 1 || opcion == 1;
        JComboBox<ItemCombo> destino = new JComboBox<>();
        for (Curso curso : cursosCargados) {
            if (!curso.getId_curso().equals(origen.getId_curso()) && "Activo".equals(curso.getEstado())
                    && (excepcional || (curso.getAño().equals(origen.getAño()) && curso.getId_periodo().equals(origen.getId_periodo())))) {
                destino.addItem(new ItemCombo(curso.getId_curso(), curso.getAño() + "° " + curso.getDivision() + " · " + curso.getTurno() + " · " + curso.getNombre_periodo()));
            }
        }
        if (destino.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "No hay divisiones de destino disponibles. Dirección puede crearlas desde Cursos."); return; }
        JTextArea motivo = new JTextArea(4, 30); motivo.setLineWrap(true); motivo.setWrapStyleWord(true);
        Object[] campos = {"Año y división de destino", destino, excepcional ? "Explicación de la solicitud" : "Motivo del traslado", new JScrollPane(motivo)};
        if (JOptionPane.showConfirmDialog(this, campos, excepcional ? "Solicitud a Dirección" : "Cambio de división", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        int id = actual.getId_inscripcion(), curso = ((ItemCombo) destino.getSelectedItem()).getId();
        Ficha_Inscripcion_dialog.ejecutar(this, () -> {
            if (excepcional) new servicios.Solicitud_Cambio_servicio().solicitar(id, curso, motivo.getText(), usuarioLogueado.getId_usuario());
            else inscripcionServicio.cambiarCurso(id, curso, motivo.getText(), usuarioLogueado.getId_usuario());
            return true;
        }, resultado -> { JOptionPane.showMessageDialog(this, excepcional ? "Solicitud enviada a la bandeja de Dirección." : "Traslado realizado. Las notas permanecen en la inscripción anterior."); cargarDatosAsincronos(); limpiarFormulario(); });
    }

    // filtramos el listado cargado mientras se escribe el nombre o el dni
    private void buscarInscripciones() {
        servicios.Auditoria_servicio.buscar("Inscripciones", txtBuscar.getText());
        filtrarListado();
    }

    // aplicamos el estado junto al año y la división que están seleccionados
    private void filtrarInscripciones() {
        filtrarListado();
    }

    // cargamos los datos seleccionados de la tabla al formulario e incluimos el motivo de baja
    private void cargarSeleccionDeTabla() {
        int fila = table.getSelectedRow();
        if (fila >= 0) {
            idInscripcionSeleccionada = (Integer) table.getValueAt(fila, 0);
            textFecha.setEditable(false);

            Inscripcion ins =
                    inscripcionesCargadas.stream()
                            .filter(i -> i.getId_inscripcion().equals(idInscripcionSeleccionada))
                            .findFirst()
                            .orElse(null);

            if (ins != null) {
                txtMotivoBaja.setText(
                        ins.getMotivo_baja() != null
                                ? ins.getMotivo_baja()
                                : "Sin motivo registrado");

                if (ins.getFecha_inscripcion() != null) {
                    textFecha.setText(
                            ins.getFecha_inscripcion()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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
        textFecha.setEditable(servicios.Permisos_servicio.secretario(usuarioLogueado));
        txtMotivoBaja.setText("");
        textFecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (cmbAlumno.getItemCount() > 0) cmbAlumno.setSelectedIndex(0);
        if (cmbCurso.getItemCount() > 0) cmbCurso.setSelectedIndex(0);
        if (cmbEstado.getItemCount() > 0) cmbEstado.setSelectedIndex(0);
    }
}
