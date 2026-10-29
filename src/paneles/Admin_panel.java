package paneles;

import clases.Alumno;
import clases.Docente;
import clases.Usuario;

import dao.*;

import servicios.Admin_servicio;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// mostramos los contadores, las cuentas y el registro de actividades del sistema
public class Admin_panel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Usuario usuarioLogueado;
    private JLabel lblAlumnosTotales;
    private JLabel lblDocentesTotales;
    private JLabel lblCursosActivos;
    private JLabel lblInscripciones;
    private JLabel lblCuentasUsuarios;

    private JTable tablaLog;
    private DefaultTableModel modeloLog;

    // tabla y combo para gestionar personas (alumnos o docentes)
    private JTable tablaPersonas;
    private DefaultTableModel modeloPersonas;
    private JComboBox<String> cmbTipoPersona;
    private JTextField txtBuscarUsuarios;
    private javax.swing.table.TableRowSorter<DefaultTableModel> filtroUsuarios;
    private List<Object[]> alumnosCargados = List.of();
    private List<Object[]> docentesCargados = List.of();
    private final JLabel estadoCarga = new JLabel("Cargando datos…");
    private int versionCarga;
    // conservamos los perfiles visibles para modificar el registro seleccionado en la tabla
    private java.util.List<java.util.Map<String, Object>> personalCargado = java.util.List.of();

    private Admin_servicio adminServicio;
    private Log_Usuario_dao logUsuarioDao;
    private Usuario_dao usuarioDao;
    private Rol_dao rolDao;
    private Alumno_dao alumnoDao;
    private Docente_dao docenteDao;

    private JPanel panelFormAlumno;
    private JPanel panelFormDocente;

    private JTextField txtUsrAlu, txtPassAlu, txtNomAlu, txtApeAlu, txtDniAlu;
    private JComboBox<String> cmbEstAlu;
    private JTextField txtNomResp, txtApeResp, txtDniResp, txtRelResp;
    private JTextField txtUsrDoc, txtPassDoc, txtNomDoc, txtApeDoc, txtDniDoc;
    private JComboBox<String> cmbEstDoc;

    public Admin_panel() {
        this(null);
    }

    public Admin_panel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        servicios.Auditoria_servicio.registrar(
                "ABRIR MODULO", "Administración", "Consulta del módulo");
        adminServicio = new Admin_servicio();
        logUsuarioDao = new Log_Usuario_dao();
        usuarioDao = new Usuario_dao();
        rolDao = new Rol_dao();
        alumnoDao = new Alumno_dao();
        docenteDao = new Docente_dao();

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1150, 680));

        JPanel panelTitulo = new JPanel(new BorderLayout(12, 8));
        panelTitulo.setBackground(Color.WHITE);
        panelTitulo.setBorder(new EmptyBorder(18, 18, 0, 18));
        JLabel titulo = new JLabel("Administración");
        titulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        panelTitulo.add(titulo, BorderLayout.NORTH);
        JPanel actualizarDatos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actualizarDatos.setOpaque(false);
        actualizarDatos.add(estadoCarga);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargarDatosAdmin());
        actualizarDatos.add(actualizar);
        panelTitulo.add(actualizarDatos, BorderLayout.SOUTH);
        add(panelTitulo, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(new EmptyBorder(15, 15, 15, 15));

        // tarjetas de estadisticas
        JPanel panelTarjetas = new JPanel(new GridLayout(1, 5, 12, 0));
        panelTarjetas.setBackground(Color.WHITE);
        panelTarjetas.setPreferredSize(new Dimension(0, 85));
        panelTarjetas.setMinimumSize(new Dimension(0, 85));
        panelTarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        panelTarjetas.add(
                crearTarjetaEstadistica(
                        "ALUMNOS TOTALES",
                        lblAlumnosTotales = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(
                crearTarjetaEstadistica(
                        "DOCENTES TOTALES",
                        lblDocentesTotales = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(
                crearTarjetaEstadistica(
                        "CURSOS ACTIVOS",
                        lblCursosActivos = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(
                crearTarjetaEstadistica(
                        "INSCRIPCIONES",
                        lblInscripciones = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(
                crearTarjetaEstadistica(
                        "CUENTAS DE USUARIOS",
                        lblCuentasUsuarios = new JLabel("0", SwingConstants.CENTER)));

        panelCentral.add(panelTarjetas);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        // botones para abrir los formularios de registro
        JPanel panelAcciones = new JPanel(new GridLayout(1, 2, 15, 0));
        panelAcciones.setBackground(Color.WHITE);
        panelAcciones.setPreferredSize(new Dimension(0, 48));
        panelAcciones.setMinimumSize(new Dimension(0, 48));
        panelAcciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel panelBtnAlumno =
                crearBotonAccionDesplegable("+ Registrar Nuevo Alumno y Responsable");
        panelBtnAlumno.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        boolean visible = !panelFormAlumno.isVisible();
                        panelFormAlumno.setVisible(visible);
                        panelFormDocente.setVisible(false);
                        revalidate();
                        repaint();
                    }
                });
        panelAcciones.add(panelBtnAlumno);

        JPanel panelBtnDocente = crearBotonAccionDesplegable("+ Registrar Nuevo Docente");
        panelBtnDocente.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        boolean visible = !panelFormDocente.isVisible();
                        panelFormDocente.setVisible(visible);
                        panelFormAlumno.setVisible(false);
                        revalidate();
                        repaint();
                    }
                });
        panelAcciones.add(panelBtnDocente);

        panelCentral.add(panelAcciones);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        panelFormAlumno = crearPanelFormularioAlumno();
        panelFormAlumno.setVisible(false);
        panelCentral.add(panelFormAlumno);

        panelFormDocente = crearPanelFormularioDocente();
        panelFormDocente.setVisible(false);
        panelCentral.add(panelFormDocente);

        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        // seccion con la tabla para gestionar alumnos y docentes
        JPanel panelGestionPersonas = new JPanel(new BorderLayout(0, 8));
        panelGestionPersonas.setBackground(Color.WHITE);
        panelGestionPersonas.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(218, 225, 233)),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JPanel panelGHeader = new JPanel(new BorderLayout());
        panelGHeader.setBackground(Color.WHITE);
        JLabel lblGTitle = new JLabel("Gestión de Alumnos y Docentes");
        lblGTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblGTitle.setForeground(new Color(24, 61, 110));
        panelGHeader.add(lblGTitle, BorderLayout.WEST);

        JPanel panelGFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelGFilter.setBackground(Color.WHITE);
        panelGFilter.add(new JLabel("Ver:"));
        cmbTipoPersona = new JComboBox<>(new String[] {"Alumnos", "Docentes", "Directivos", "Preceptores", "Secretaría"});
        cmbTipoPersona.addActionListener(_ -> cargarTablaPersonas());
        panelGFilter.add(cmbTipoPersona);
        panelGHeader.add(panelGFilter, BorderLayout.EAST);

        // la búsqueda filtra lo cargado sin volver a consultar la base en cada letra.
        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.setBackground(Color.WHITE);
        buscador.add(new JLabel("Buscar usuario:"), BorderLayout.WEST);
        txtBuscarUsuarios = new JTextField();
        txtBuscarUsuarios.setToolTipText(
                "Buscar por usuario, nombre, apellido, DNI, matrícula o legajo");
        buscador.add(txtBuscarUsuarios, BorderLayout.CENTER);
        JPanel cabeceraPersonas = new JPanel(new BorderLayout(0, 10));
        cabeceraPersonas.setBackground(Color.WHITE);
        cabeceraPersonas.add(panelGHeader, BorderLayout.NORTH);
        cabeceraPersonas.add(buscador, BorderLayout.SOUTH);
        panelGestionPersonas.add(cabeceraPersonas, BorderLayout.NORTH);

        modeloPersonas =
                new DefaultTableModel(
                        new String[] {
                            "ID",
                            "Nombre",
                            "Apellido",
                            "DNI",
                            "Matrícula / Legajo",
                            "Estado",
                            "Usuario",
                            "Mail"
                        },
                        0) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        tablaPersonas = new JTable(modeloPersonas);
        filtroUsuarios = new javax.swing.table.TableRowSorter<>(modeloPersonas);
        tablaPersonas.setRowSorter(filtroUsuarios);
        txtBuscarUsuarios
                .getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            private void filtrar() {
                                String texto = txtBuscarUsuarios.getText().trim();
                                servicios.Auditoria_servicio.buscar("Administración", texto);
                                filtroUsuarios.setRowFilter(
                                        texto.isEmpty()
                                                ? null
                                                : RowFilter.regexFilter(
                                                        "(?iu)"
                                                                + java.util.regex.Pattern.quote(
                                                                        texto)));
                            }

                            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                                filtrar();
                            }

                            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                                filtrar();
                            }

                            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                                filtrar();
                            }
                        });
        tablaPersonas.setRowHeight(26);
        tablaPersonas.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaPersonas.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        tablaPersonas.getTableHeader().setBackground(new Color(240, 244, 248));
        tablaPersonas.getTableHeader().setForeground(new Color(24, 61, 110));

        JScrollPane scrollPersonas = new JScrollPane(tablaPersonas);
        scrollPersonas.setColumnHeaderView(tablaPersonas.getTableHeader());
        scrollPersonas.setPreferredSize(new Dimension(0, 160));
        panelGestionPersonas.add(scrollPersonas, BorderLayout.CENTER);
        panelGestionPersonas.setMinimumSize(new Dimension(0, 285));

        JPanel panelGButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelGButtons.setBackground(Color.WHITE);
        JButton btnEditarPersona = new JButton("Modificar seleccionado");
        btnEditarPersona.setBackground(new Color(37, 99, 235));
        btnEditarPersona.setForeground(Color.WHITE);
        btnEditarPersona.addActionListener(_ -> modificarPersonaSeleccionada());

        JButton btnBajaPersona = new JButton("Dar de baja (Inactivo)");
        btnBajaPersona.setBackground(new Color(220, 38, 38));
        btnBajaPersona.setForeground(Color.WHITE);
        btnBajaPersona.addActionListener(_ -> darDeBajaPersonaSeleccionada());

        JButton btnPersonal = Ficha_Inscripcion_dialog.boton("Registrar personal");
        btnPersonal.addActionListener(e -> new Personal_dialog(SwingUtilities.getWindowAncestor(this), usuarioLogueado, tipoPersonalSeleccionado(), null, this::cargarDatosAdmin).setVisible(true));
        panelGButtons.add(btnPersonal);
        panelGButtons.add(btnEditarPersona);
        panelGButtons.add(btnBajaPersona);
        panelGestionPersonas.add(panelGButtons, BorderLayout.SOUTH);

        panelCentral.add(panelGestionPersonas);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        // seccion de logs y auditoria
        JPanel panelLogContainer = new JPanel(new BorderLayout(0, 8));
        panelLogContainer.setBackground(Color.WHITE);
        panelLogContainer.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(218, 225, 233)),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblLogTitulo = new JLabel("Registro de Actividades (Log de Usuarios)");
        lblLogTitulo.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblLogTitulo.setForeground(new Color(24, 61, 110));

        JLabel lblLogSub =
                new JLabel(
                        "Actividades más recientes primero. Doble clic para ver el detalle"
                                + " completo.");
        lblLogSub.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblLogSub.setForeground(new Color(100, 110, 120));

        JPanel panelLogTextos = new JPanel();
        panelLogTextos.setLayout(new BoxLayout(panelLogTextos, BoxLayout.Y_AXIS));
        panelLogTextos.setBackground(Color.WHITE);
        panelLogTextos.add(lblLogTitulo);
        panelLogTextos.add(Box.createRigidArea(new Dimension(0, 3)));
        panelLogTextos.add(lblLogSub);

        panelLogContainer.add(panelLogTextos, BorderLayout.NORTH);

        String[] columnasLog = {
            "ID LOG",
            "USUARIO",
            "ROL",
            "ACCIÓN",
            "CAMPO / MÓDULO",
            "DETALLE / VALOR NUEVO",
            "FECHA Y HORA"
        };
        modeloLog =
                new DefaultTableModel(columnasLog, 0) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        tablaLog = new JTable(modeloLog);
        tablaLog.setRowHeight(36);
        tablaLog.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaLog.setFillsViewportHeight(true);
        tablaLog.getTableHeader().setReorderingAllowed(false);
        int[] anchosLog = {75, 170, 125, 155, 170, 390, 175};
        for (int i = 0; i < anchosLog.length; i++) {
            tablaLog.getColumnModel().getColumn(i).setPreferredWidth(anchosLog[i]);
            tablaLog.getColumnModel().getColumn(i).setMinWidth(i == 0 ? 65 : 100);
        }
        tablaLog.getColumnModel().getColumn(5).setCellRenderer(new DetalleLogRenderer());
        tablaLog.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        int fila = tablaLog.rowAtPoint(e.getPoint());
                        if (e.getClickCount() == 2 && fila >= 0) {
                            StringBuilder detalle = new StringBuilder();
                            for (int i = 0; i < modeloLog.getColumnCount(); i++) {
                                Object valor = tablaLog.getValueAt(fila, i);
                                detalle.append(modeloLog.getColumnName(i))
                                        .append(": ")
                                        .append(valor == null ? "" : valor)
                                        .append("\n\n");
                            }
                            JTextArea texto = new JTextArea(detalle.toString(), 16, 65);
                            texto.setEditable(false);
                            texto.setLineWrap(true);
                            texto.setWrapStyleWord(true);
                            texto.setCaretPosition(0);
                            JOptionPane.showMessageDialog(
                                    Admin_panel.this,
                                    new JScrollPane(texto),
                                    "Detalle de actividad",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
        tablaLog.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaLog.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        tablaLog.getTableHeader().setBackground(new Color(240, 244, 248));
        tablaLog.getTableHeader().setForeground(new Color(24, 61, 110));

        JScrollPane scrollLog = new JScrollPane(tablaLog);
        scrollLog.setColumnHeaderView(tablaLog.getTableHeader());
        scrollLog.setPreferredSize(new Dimension(0, 280));
        scrollLog.setMinimumSize(new Dimension(0, 230));
        scrollLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelLogContainer.setMinimumSize(new Dimension(0, 330));
        panelLogContainer.add(scrollLog, BorderLayout.CENTER);

        if (servicios.Permisos_servicio.directivo(usuarioLogueado)) panelCentral.add(panelLogContainer);

        JScrollPane contenido = new JScrollPane(panelCentral);
        contenido.getVerticalScrollBar().setUnitIncrement(20);
        add(contenido, BorderLayout.CENTER);

        cargarDatosAdmin();
    }

    private JPanel crearPanelFormularioAlumno() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        int row = 0;
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.insets = new Insets(5, 5, 5, 5);
        gbc0.fill = GridBagConstraints.HORIZONTAL;
        gbc0.gridx = 0;
        gbc0.gridy = row++;
        gbc0.gridwidth = 4;
        JLabel lblT = new JLabel("REGISTRO DE NUEVO ALUMNO Y RESPONSABLE");
        lblT.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblT.setForeground(new Color(24, 61, 110));
        panel.add(lblT, gbc0);

        row++;
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(5, 5, 5, 5);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.gridx = 0;
        gbc1.gridy = row;
        panel.add(new JLabel("Usuario:"), gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.gridx = 1;
        gbc2.gridy = row;
        txtUsrAlu = new JTextField(12);
        panel.add(txtUsrAlu, gbc2);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = new Insets(5, 5, 5, 5);
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        gbc3.gridx = 2;
        gbc3.gridy = row;
        panel.add(new JLabel("Contraseña:"), gbc3);

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.insets = new Insets(5, 5, 5, 5);
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.gridx = 3;
        gbc4.gridy = row;
        txtPassAlu = new JPasswordField(12);
        panel.add(txtPassAlu, gbc4);

        row++;
        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.insets = new Insets(5, 5, 5, 5);
        gbc5.fill = GridBagConstraints.HORIZONTAL;
        gbc5.gridx = 0;
        gbc5.gridy = row;
        panel.add(new JLabel("Nombre Alumno:"), gbc5);

        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.insets = new Insets(5, 5, 5, 5);
        gbc6.fill = GridBagConstraints.HORIZONTAL;
        gbc6.gridx = 1;
        gbc6.gridy = row;
        txtNomAlu = new JTextField(12);
        panel.add(txtNomAlu, gbc6);

        GridBagConstraints gbc7 = new GridBagConstraints();
        gbc7.insets = new Insets(5, 5, 5, 5);
        gbc7.fill = GridBagConstraints.HORIZONTAL;
        gbc7.gridx = 2;
        gbc7.gridy = row;
        panel.add(new JLabel("Apellido Alumno:"), gbc7);

        GridBagConstraints gbc8 = new GridBagConstraints();
        gbc8.insets = new Insets(5, 5, 5, 5);
        gbc8.fill = GridBagConstraints.HORIZONTAL;
        gbc8.gridx = 3;
        gbc8.gridy = row;
        txtApeAlu = new JTextField(12);
        panel.add(txtApeAlu, gbc8);

        row++;
        GridBagConstraints gbc9 = new GridBagConstraints();
        gbc9.insets = new Insets(5, 5, 5, 5);
        gbc9.fill = GridBagConstraints.HORIZONTAL;
        gbc9.gridx = 0;
        gbc9.gridy = row;
        panel.add(new JLabel("DNI Alumno:"), gbc9);

        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.insets = new Insets(5, 5, 5, 5);
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        gbc10.gridx = 1;
        gbc10.gridy = row;
        txtDniAlu = new JTextField(12);
        panel.add(txtDniAlu, gbc10);

        GridBagConstraints gbc11 = new GridBagConstraints();
        gbc11.insets = new Insets(5, 5, 5, 5);
        gbc11.fill = GridBagConstraints.HORIZONTAL;
        gbc11.gridx = 2;
        gbc11.gridy = row;
        panel.add(new JLabel("Estado:"), gbc11);

        GridBagConstraints gbc12 = new GridBagConstraints();
        gbc12.insets = new Insets(5, 5, 5, 5);
        gbc12.fill = GridBagConstraints.HORIZONTAL;
        gbc12.gridx = 3;
        gbc12.gridy = row;
        cmbEstAlu = new JComboBox<>(new String[] {"Activo", "Inactivo", "Suspendido"});
        panel.add(cmbEstAlu, gbc12);

        row++;
        GridBagConstraints gbc13 = new GridBagConstraints();
        gbc13.insets = new Insets(5, 5, 5, 5);
        gbc13.fill = GridBagConstraints.HORIZONTAL;
        gbc13.gridx = 0;
        gbc13.gridy = row;
        panel.add(new JLabel("Nombre Resp.:"), gbc13);

        GridBagConstraints gbc14 = new GridBagConstraints();
        gbc14.insets = new Insets(5, 5, 5, 5);
        gbc14.fill = GridBagConstraints.HORIZONTAL;
        gbc14.gridx = 1;
        gbc14.gridy = row;
        txtNomResp = new JTextField(12);
        panel.add(txtNomResp, gbc14);

        GridBagConstraints gbc15 = new GridBagConstraints();
        gbc15.insets = new Insets(5, 5, 5, 5);
        gbc15.fill = GridBagConstraints.HORIZONTAL;
        gbc15.gridx = 2;
        gbc15.gridy = row;
        panel.add(new JLabel("Apellido Resp.:"), gbc15);

        GridBagConstraints gbc16 = new GridBagConstraints();
        gbc16.insets = new Insets(5, 5, 5, 5);
        gbc16.fill = GridBagConstraints.HORIZONTAL;
        gbc16.gridx = 3;
        gbc16.gridy = row;
        txtApeResp = new JTextField(12);
        panel.add(txtApeResp, gbc16);

        row++;
        GridBagConstraints gbc17 = new GridBagConstraints();
        gbc17.insets = new Insets(5, 5, 5, 5);
        gbc17.fill = GridBagConstraints.HORIZONTAL;
        gbc17.gridx = 0;
        gbc17.gridy = row;
        panel.add(new JLabel("DNI Resp.:"), gbc17);

        GridBagConstraints gbc18 = new GridBagConstraints();
        gbc18.insets = new Insets(5, 5, 5, 5);
        gbc18.fill = GridBagConstraints.HORIZONTAL;
        gbc18.gridx = 1;
        gbc18.gridy = row;
        txtDniResp = new JTextField(12);
        panel.add(txtDniResp, gbc18);

        GridBagConstraints gbc19 = new GridBagConstraints();
        gbc19.insets = new Insets(5, 5, 5, 5);
        gbc19.fill = GridBagConstraints.HORIZONTAL;
        gbc19.gridx = 2;
        gbc19.gridy = row;
        panel.add(new JLabel("Tipo Relación:"), gbc19);

        GridBagConstraints gbc20 = new GridBagConstraints();
        gbc20.insets = new Insets(5, 5, 5, 5);
        gbc20.fill = GridBagConstraints.HORIZONTAL;
        gbc20.gridx = 3;
        gbc20.gridy = row;
        txtRelResp = new JTextField(12);
        panel.add(txtRelResp, gbc20);

        row++;
        GridBagConstraints gbc21 = new GridBagConstraints();
        gbc21.insets = new Insets(5, 5, 5, 5);
        gbc21.fill = GridBagConstraints.HORIZONTAL;
        gbc21.gridx = 0;
        gbc21.gridy = row;
        gbc21.gridwidth = 4;
        JButton btnGuarAlu = new JButton("Guardar Alumno");
        btnGuarAlu.setBackground(new Color(37, 99, 235));
        btnGuarAlu.setForeground(Color.WHITE);
        btnGuarAlu.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnGuarAlu.addActionListener(
                e -> {
                    try {
                        Integer idAdmin =
                                usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                        adminServicio.registrarAlumnoConResponsableYUsuario(
                                idAdmin,
                                txtUsrAlu.getText().trim(),
                                new String(((JPasswordField) txtPassAlu).getPassword()).trim(),
                                txtNomAlu.getText().trim(),
                                txtApeAlu.getText().trim(),
                                txtDniAlu.getText().trim(),
                                (String) cmbEstAlu.getSelectedItem(),
                                txtNomResp.getText().trim(),
                                txtApeResp.getText().trim(),
                                txtDniResp.getText().trim(),
                                txtRelResp.getText().trim());
                        JOptionPane.showMessageDialog(this, "Alumno registrado con éxito.");
                        panelFormAlumno.setVisible(false);
                        cargarDatosAdmin();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Error: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
        panel.add(btnGuarAlu, gbc21);

        return panel;
    }

    private JPanel crearPanelFormularioDocente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        int row = 0;
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.insets = new Insets(5, 5, 5, 5);
        gbc0.fill = GridBagConstraints.HORIZONTAL;
        gbc0.gridx = 0;
        gbc0.gridy = row++;
        gbc0.gridwidth = 4;
        JLabel lblT = new JLabel("REGISTRO DE NUEVO DOCENTE");
        lblT.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblT.setForeground(new Color(24, 61, 110));
        panel.add(lblT, gbc0);

        row++;
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(5, 5, 5, 5);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.gridx = 0;
        gbc1.gridy = row;
        panel.add(new JLabel("Usuario:"), gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.gridx = 1;
        gbc2.gridy = row;
        txtUsrDoc = new JTextField(12);
        panel.add(txtUsrDoc, gbc2);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = new Insets(5, 5, 5, 5);
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        gbc3.gridx = 2;
        gbc3.gridy = row;
        panel.add(new JLabel("Contraseña:"), gbc3);

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.insets = new Insets(5, 5, 5, 5);
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.gridx = 3;
        gbc4.gridy = row;
        txtPassDoc = new JPasswordField(12);
        panel.add(txtPassDoc, gbc4);

        row++;
        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.insets = new Insets(5, 5, 5, 5);
        gbc5.fill = GridBagConstraints.HORIZONTAL;
        gbc5.gridx = 0;
        gbc5.gridy = row;
        panel.add(new JLabel("Nombre Docente:"), gbc5);

        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.insets = new Insets(5, 5, 5, 5);
        gbc6.fill = GridBagConstraints.HORIZONTAL;
        gbc6.gridx = 1;
        gbc6.gridy = row;
        txtNomDoc = new JTextField(12);
        panel.add(txtNomDoc, gbc6);

        GridBagConstraints gbc7 = new GridBagConstraints();
        gbc7.insets = new Insets(5, 5, 5, 5);
        gbc7.fill = GridBagConstraints.HORIZONTAL;
        gbc7.gridx = 2;
        gbc7.gridy = row;
        panel.add(new JLabel("Apellido Docente:"), gbc7);

        GridBagConstraints gbc8 = new GridBagConstraints();
        gbc8.insets = new Insets(5, 5, 5, 5);
        gbc8.fill = GridBagConstraints.HORIZONTAL;
        gbc8.gridx = 3;
        gbc8.gridy = row;
        txtApeDoc = new JTextField(12);
        panel.add(txtApeDoc, gbc8);

        row++;
        GridBagConstraints gbc9 = new GridBagConstraints();
        gbc9.insets = new Insets(5, 5, 5, 5);
        gbc9.fill = GridBagConstraints.HORIZONTAL;
        gbc9.gridx = 0;
        gbc9.gridy = row;
        panel.add(new JLabel("DNI Docente:"), gbc9);

        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.insets = new Insets(5, 5, 5, 5);
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        gbc10.gridx = 1;
        gbc10.gridy = row;
        txtDniDoc = new JTextField(12);
        panel.add(txtDniDoc, gbc10);

        GridBagConstraints gbc11 = new GridBagConstraints();
        gbc11.insets = new Insets(5, 5, 5, 5);
        gbc11.fill = GridBagConstraints.HORIZONTAL;
        gbc11.gridx = 2;
        gbc11.gridy = row;
        panel.add(new JLabel("Estado:"), gbc11);

        GridBagConstraints gbc12 = new GridBagConstraints();
        gbc12.insets = new Insets(5, 5, 5, 5);
        gbc12.fill = GridBagConstraints.HORIZONTAL;
        gbc12.gridx = 3;
        gbc12.gridy = row;
        cmbEstDoc = new JComboBox<>(new String[] {"Activo", "Inactivo", "Licencia"});
        panel.add(cmbEstDoc, gbc12);

        row++;
        GridBagConstraints gbc13 = new GridBagConstraints();
        gbc13.insets = new Insets(5, 5, 5, 5);
        gbc13.fill = GridBagConstraints.HORIZONTAL;
        gbc13.gridx = 0;
        gbc13.gridy = row;
        gbc13.gridwidth = 4;
        JButton btnGuarDoc = new JButton("Guardar Docente");
        btnGuarDoc.setBackground(new Color(37, 99, 235));
        btnGuarDoc.setForeground(Color.WHITE);
        btnGuarDoc.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnGuarDoc.addActionListener(
                e -> {
                    try {
                        Integer idAdmin =
                                usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                        adminServicio.registrarDocenteConUsuario(
                                idAdmin,
                                txtUsrDoc.getText().trim(),
                                new String(((JPasswordField) txtPassDoc).getPassword()).trim(),
                                txtNomDoc.getText().trim(),
                                txtApeDoc.getText().trim(),
                                txtDniDoc.getText().trim(),
                                (String) cmbEstDoc.getSelectedItem());
                        JOptionPane.showMessageDialog(this, "Docente registrado con éxito.");
                        panelFormDocente.setVisible(false);
                        cargarDatosAdmin();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Error: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
        panel.add(btnGuarDoc, gbc13);

        return panel;
    }

    private JPanel crearTarjetaEstadistica(String titulo, JLabel lblValor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 4));
        tarjeta.setBackground(new Color(248, 250, 252));
        tarjeta.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 228, 236)),
                        BorderFactory.createEmptyBorder(10, 8, 10, 8)));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 10));
        lblTitulo.setForeground(new Color(90, 105, 120));

        lblValor.setFont(new Font("Tahoma", Font.BOLD, 22));
        lblValor.setForeground(new Color(37, 99, 235));

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(lblValor, BorderLayout.CENTER);

        return tarjeta;
    }

    private JPanel crearBotonAccionDesplegable(String texto) {
        JPanel panelBtn = new JPanel(new BorderLayout());
        panelBtn.setBackground(new Color(248, 250, 252));
        panelBtn.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(215, 223, 232)),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        panelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 41, 59));

        panelBtn.add(lbl, BorderLayout.WEST);

        return panelBtn;
    }

    // cambiar el tipo de persona aprovecha los datos de la última carga.
    private void cargarTablaPersonas() {
        if (tipoPersonalSeleccionado() != null) {
            cargarPersonal();
            return;
        }
        modeloPersonas.setRowCount(0);
        List<Object[]> personas =
                "Docentes".equals(cmbTipoPersona.getSelectedItem())
                        ? docentesCargados
                        : alumnosCargados;
        for (Object[] persona : personas) {
            modeloPersonas.addRow(persona);
        }
    }

    // modificamos los datos de la persona seleccionada en la tabla
    private void modificarPersonaSeleccionada() {
        if (tipoPersonalSeleccionado() != null) {
            int seleccion = tablaPersonas.getSelectedRow();
            if (seleccion < 0) { JOptionPane.showMessageDialog(this, "Seleccioná una persona de la tabla."); return; }
            var persona = personalCargado.get(tablaPersonas.convertRowIndexToModel(seleccion));
            new Personal_dialog(SwingUtilities.getWindowAncestor(this), usuarioLogueado, tipoPersonalSeleccionado(), persona, this::cargarDatosAdmin).setVisible(true);
            return;
        }
        int fila = tablaPersonas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccioná un registro de la tabla.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idPersona =
                (Integer) modeloPersonas.getValueAt(tablaPersonas.convertRowIndexToModel(fila), 0);
        String tipo = (String) cmbTipoPersona.getSelectedItem();

        if ("Alumnos".equalsIgnoreCase(tipo)) {
            Alumno alu = alumnoDao.obtenerPorId(idPersona);
            if (alu == null) return;

            JTextField txtNom = new JTextField(alu.getNombre());
            JTextField txtApe = new JTextField(alu.getApellido());
            JTextField txtDni = new JTextField(alu.getDni());
            JComboBox<String> cmbEst =
                    new JComboBox<>(new String[] {"Activo", "Inactivo", "Suspendido"});
            cmbEst.setSelectedItem(alu.getEstado());

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.add(new JLabel("Nombre:"));
            form.add(txtNom);
            form.add(new JLabel("Apellido:"));
            form.add(txtApe);
            form.add(new JLabel("DNI:"));
            form.add(txtDni);
            form.add(new JLabel("Estado:"));
            form.add(cmbEst);

            int res =
                    JOptionPane.showConfirmDialog(
                            this, form, "Modificar Alumno", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                alu.setNombre(txtNom.getText().trim());
                alu.setApellido(txtApe.getText().trim());
                alu.setDni(txtDni.getText().trim());
                alu.setEstado((String) cmbEst.getSelectedItem());

                Integer idAdmin = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                adminServicio.modificarAlumno(alu, idAdmin);
                JOptionPane.showMessageDialog(this, "Alumno modificado con éxito.");
                cargarTablaPersonas();
            }

        } else if ("Docentes".equalsIgnoreCase(tipo)) {
            Docente doc = docenteDao.obtenerPorId(idPersona);
            if (doc == null) return;

            JTextField txtNom = new JTextField(doc.getNombre());
            JTextField txtApe = new JTextField(doc.getApellido());
            JTextField txtDni = new JTextField(doc.getDni());
            JComboBox<String> cmbEst =
                    new JComboBox<>(new String[] {"Activo", "Inactivo", "Licencia"});
            cmbEst.setSelectedItem(doc.getEstado());

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.add(new JLabel("Nombre:"));
            form.add(txtNom);
            form.add(new JLabel("Apellido:"));
            form.add(txtApe);
            form.add(new JLabel("DNI:"));
            form.add(txtDni);
            form.add(new JLabel("Estado:"));
            form.add(cmbEst);

            int res =
                    JOptionPane.showConfirmDialog(
                            this, form, "Modificar Docente", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                doc.setNombre(txtNom.getText().trim());
                doc.setApellido(txtApe.getText().trim());
                doc.setDni(txtDni.getText().trim());
                doc.setEstado((String) cmbEst.getSelectedItem());

                Integer idAdmin = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                adminServicio.modificarDocente(doc, idAdmin);
                JOptionPane.showMessageDialog(this, "Docente modificado con éxito.");
                cargarTablaPersonas();
            }
        }
        cargarDatosAdmin();
    }

    // pasamos el estado del usuario seleccionado a inactivo (dar de baja)
    private void darDeBajaPersonaSeleccionada() {
        if (tipoPersonalSeleccionado() != null) {
            int seleccion = tablaPersonas.getSelectedRow();
            if (seleccion < 0) { JOptionPane.showMessageDialog(this, "Seleccioná una persona de la tabla."); return; }
            var fila = personalCargado.get(tablaPersonas.convertRowIndexToModel(seleccion));
            if (JOptionPane.showConfirmDialog(this, "¿Dar de baja al personal seleccionado conservando sus datos?", "Confirmar baja", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            clases.Personal persona = new clases.Personal();
            persona.setTipo(tipoPersonalSeleccionado()); persona.setId(((Number) fila.get("id")).intValue());
            persona.setNombre(fila.get("nombre").toString()); persona.setApellido(fila.get("apellido").toString()); persona.setDni(fila.get("dni").toString()); persona.setMail(fila.get("mail").toString()); persona.setEstado("Inactivo");
            Ficha_Inscripcion_dialog.ejecutar(this, () -> { new servicios.Personal_servicio().modificar(persona, usuarioLogueado.getId_usuario()); return true; }, resultado -> cargarDatosAdmin());
            return;
        }
        int fila = tablaPersonas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccioná un registro de la tabla para dar de baja.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idPersona =
                (Integer) modeloPersonas.getValueAt(tablaPersonas.convertRowIndexToModel(fila), 0);
        String tipo = (String) cmbTipoPersona.getSelectedItem();

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "¿Querés dar de baja a este usuario? Pasará a estado Inactivo.",
                        "Confirmar baja",
                        JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Integer idAdmin = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
            if ("Alumnos".equalsIgnoreCase(tipo)) {
                adminServicio.darDeBajaAlumno(idPersona, idAdmin);
            } else {
                adminServicio.darDeBajaDocente(idPersona, idAdmin);
            }
            JOptionPane.showMessageDialog(this, "El usuario fue dado de baja correctamente.");
            cargarTablaPersonas();
            cargarDatosAdmin();
        }
    }

    // identificamos el perfil administrativo que está seleccionado en el filtro
    private clases.Personal.Tipo tipoPersonalSeleccionado() {
        if (cmbTipoPersona == null) return null;
        for (clases.Personal.Tipo tipo : clases.Personal.Tipo.values()) {
            if (tipo.nombre.equals(cmbTipoPersona.getSelectedItem())) return tipo;
        }
        return null;
    }

    // consultamos los perfiles y mostramos los mismos datos que se cargan manualmente en el formulario
    private void cargarPersonal() {
        clases.Personal.Tipo tipo = tipoPersonalSeleccionado();
        if (tipo == null) return;
        Ficha_Inscripcion_dialog.ejecutar(this, () -> new servicios.Personal_servicio().listar(tipo, usuarioLogueado.getId_usuario()), personas -> {
            if (tipoPersonalSeleccionado() != tipo) return;
            personalCargado = personas; modeloPersonas.setRowCount(0);
            for (var persona : personas) modeloPersonas.addRow(new Object[] {
                persona.get("id"), persona.get("nombre"), persona.get("apellido"), persona.get("dni"), persona.get("legajo"), persona.get("estado"), persona.get("nombre_usuario"), persona.get("mail")
            });
        });
    }

    // cargamos todo en segundo plano, con tres consultas y una sola conexión.
    private void cargarDatosAdmin() {
        servicios.Auditoria_servicio.registrar(
                "CONSULTAR", "Administración", "Actualizar contadores y actividades");
        int version = ++versionCarga;
        estadoCarga.setText("Cargando datos…");
        new SwingWorker<ConsultaAdmin_dao.Datos, Void>() {
            protected ConsultaAdmin_dao.Datos doInBackground() {
                return new ConsultaAdmin_dao().cargar();
            }

            protected void done() {
                if (version != versionCarga) {
                    return;
                }
                try {
                    ConsultaAdmin_dao.Datos datos = get();
                    int[] totales = datos.totales();
                    lblAlumnosTotales.setText(String.valueOf(totales[0]));
                    lblDocentesTotales.setText(String.valueOf(totales[1]));
                    lblCursosActivos.setText(String.valueOf(totales[2]));
                    lblInscripciones.setText(String.valueOf(totales[3]));
                    lblCuentasUsuarios.setText(String.valueOf(totales[4]));
                    alumnosCargados = datos.alumnos();
                    docentesCargados = datos.docentes();
                    cargarTablaPersonas();
                    modeloLog.setRowCount(0);
                    for (Object[] log : datos.logs()) {
                        modeloLog.addRow(log);
                    }
                    estadoCarga.setText("Datos actualizados");
                } catch (Exception e) {
                    estadoCarga.setText("No se pudo actualizar");
                    Throwable causa = e.getCause() == null ? e : e.getCause();
                    JOptionPane.showMessageDialog(
                            Admin_panel.this,
                            causa.getMessage(),
                            "Administración",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // el detalle usa varias líneas en lugar de apretar todo en una sola.
    private static class DetalleLogRenderer extends JTextArea
            implements javax.swing.table.TableCellRenderer {
        private static final long serialVersionUID = 1L;

        DetalleLogRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }

        public Component getTableCellRendererComponent(
                JTable tabla,
                Object valor,
                boolean seleccionado,
                boolean foco,
                int fila,
                int columna) {
            setText(valor == null ? "" : valor.toString());
            setFont(tabla.getFont());
            setBackground(seleccionado ? tabla.getSelectionBackground() : tabla.getBackground());
            setForeground(seleccionado ? tabla.getSelectionForeground() : tabla.getForeground());
            setSize(tabla.getColumnModel().getColumn(columna).getWidth(), Short.MAX_VALUE);
            int alto = Math.max(36, Math.min(120, getPreferredSize().height));
            if (tabla.getRowHeight(fila) != alto) {
                tabla.setRowHeight(fila, alto);
            }
            return this;
        }
    }
}
