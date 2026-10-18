package paneles;

import clases.Log_Usuario;
import clases.Usuario;
import clases.Rol;
import dao.*;
import servicios.Admin_servicio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    private Admin_servicio adminServicio;
    private Log_Usuario_dao logUsuarioDao;
    private Usuario_dao usuarioDao;
    private Rol_dao rolDao;

    private JPanel panelFormAlumno;
    private JPanel panelFormDocente;

    // Campos Alumno / Responsable (sin matrícula manual)
    private JTextField txtUsrAlu, txtPassAlu, txtNomAlu, txtApeAlu, txtDniAlu;
    private JComboBox<String> cmbEstAlu;
    private JTextField txtNomResp, txtApeResp, txtDniResp, txtRelResp;

    // Campos Docente
    private JTextField txtUsrDoc, txtPassDoc, txtNomDoc, txtApeDoc, txtDniDoc;
    private JComboBox<String> cmbEstDoc;

    public Admin_panel() {
        this(null);
    }

    public Admin_panel(Usuario usuario) {
        this.usuarioLogueado = usuario;
        adminServicio = new Admin_servicio();
        logUsuarioDao = new Log_Usuario_dao();
        usuarioDao = new Usuario_dao();
        rolDao = new Rol_dao();

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1150, 680));

        // --- ENCABEZADO ---
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(new Color(24, 61, 110));
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titulo = new JLabel("Panel de Administración");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Tahoma", Font.BOLD, 23));

        JLabel subtitulo = new JLabel("Gestión general del sistema");
        subtitulo.setForeground(new Color(220, 230, 245));
        subtitulo.setFont(new Font("Tahoma", Font.PLAIN, 13));

        JPanel textosTitulo = new JPanel();
        textosTitulo.setOpaque(false);
        textosTitulo.setLayout(new BoxLayout(textosTitulo, BoxLayout.Y_AXIS));
        textosTitulo.add(titulo);
        textosTitulo.add(Box.createRigidArea(new Dimension(0, 5)));
        textosTitulo.add(subtitulo);

        panelTitulo.add(textosTitulo, BorderLayout.WEST);
        add(panelTitulo, BorderLayout.NORTH);

        // --- CONTENIDO CENTRAL ---
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Tarjetas Estadísticas
        JPanel panelTarjetas = new JPanel(new GridLayout(1, 5, 12, 0));
        panelTarjetas.setBackground(Color.WHITE);
        panelTarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        panelTarjetas.add(crearTarjetaEstadistica("ALUMNOS TOTALES", lblAlumnosTotales = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("DOCENTES TOTALES", lblDocentesTotales = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("CURSOS ACTIVOS", lblCursosActivos = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("INSCRIPCIONES", lblInscripciones = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("CUENTAS DE USUARIOS", lblCuentasUsuarios = new JLabel("0", SwingConstants.CENTER)));

        panelCentral.add(panelTarjetas);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. Botones de Acción Rápida
        JPanel panelAcciones = new JPanel(new GridLayout(1, 2, 15, 0));
        panelAcciones.setBackground(Color.WHITE);
        panelAcciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel panelBtnAlumno = crearBotonAccionDesplegable("+ Registrar Nuevo Alumno y Responsable");
        panelBtnAlumno.addMouseListener(new MouseAdapter() {
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
        panelBtnDocente.addMouseListener(new MouseAdapter() {
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

        // 3. Formulario Alumno (Desplegable)
        panelFormAlumno = crearPanelFormularioAlumno();
        panelFormAlumno.setVisible(false);
        panelCentral.add(panelFormAlumno);

        // 4. Formulario Docente (Desplegable)
        panelFormDocente = crearPanelFormularioDocente();
        panelFormDocente.setVisible(false);
        panelCentral.add(panelFormDocente);

        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        // 5. Tabla Log de Usuarios
        JPanel panelLogContainer = new JPanel(new BorderLayout(0, 8));
        panelLogContainer.setBackground(Color.WHITE);
        panelLogContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 225, 233)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblLogTitulo = new JLabel("Registro de Actividades (Log de Usuarios)");
        lblLogTitulo.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblLogTitulo.setForeground(new Color(24, 61, 110));

        JLabel lblLogSub = new JLabel("Auditoría en tiempo real de acciones, modificaciones de calificaciones, periodos y accesos.");
        lblLogSub.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblLogSub.setForeground(new Color(100, 110, 120));

        JPanel panelLogTextos = new JPanel();
        panelLogTextos.setLayout(new BoxLayout(panelLogTextos, BoxLayout.Y_AXIS));
        panelLogTextos.setBackground(Color.WHITE);
        panelLogTextos.add(lblLogTitulo);
        panelLogTextos.add(Box.createRigidArea(new Dimension(0, 3)));
        panelLogTextos.add(lblLogSub);

        panelLogContainer.add(panelLogTextos, BorderLayout.NORTH);

        String[] columnasLog = {"ID LOG", "USUARIO", "ROL", "ACCIÓN", "CAMPO / MÓDULO", "DETALLE / VALOR NUEVO", "FECHA Y HORA"};
        modeloLog = new DefaultTableModel(columnasLog, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaLog = new JTable(modeloLog);
        tablaLog.setRowHeight(26);
        tablaLog.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaLog.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        tablaLog.getTableHeader().setBackground(new Color(240, 244, 248));
        tablaLog.getTableHeader().setForeground(new Color(24, 61, 110));
        tablaLog.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollLog = new JScrollPane(tablaLog);
        scrollLog.setPreferredSize(new Dimension(0, 200));
        panelLogContainer.add(scrollLog, BorderLayout.CENTER);

        panelCentral.add(panelLogContainer);

        add(new JScrollPane(panelCentral), BorderLayout.CENTER);

        cargarDatosAdmin();
    }

    private JPanel crearPanelFormularioAlumno() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 4;
        JLabel lblT = new JLabel("REGISTRO DE NUEVO ALUMNO Y RESPONSABLE (Matrícula automática)");
        lblT.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblT.setForeground(new Color(24, 61, 110));
        panel.add(lblT, gbc);

        gbc.gridwidth = 1;
        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Usuario:"), gbc);
        txtUsrAlu = new JTextField(12); gbc.gridx = 1; panel.add(txtUsrAlu, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Contraseña:"), gbc);
        txtPassAlu = new JPasswordField(12); gbc.gridx = 3; panel.add(txtPassAlu, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Nombre Alumno:"), gbc);
        txtNomAlu = new JTextField(12); gbc.gridx = 1; panel.add(txtNomAlu, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Apellido Alumno:"), gbc);
        txtApeAlu = new JTextField(12); gbc.gridx = 3; panel.add(txtApeAlu, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("DNI Alumno:"), gbc);
        txtDniAlu = new JTextField(12); gbc.gridx = 1; panel.add(txtDniAlu, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Estado:"), gbc);
        cmbEstAlu = new JComboBox<>(new String[]{"Activo", "Inactivo", "Suspendido"});
        gbc.gridx = 3; panel.add(cmbEstAlu, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Nombre Resp.:"), gbc);
        txtNomResp = new JTextField(12); gbc.gridx = 1; panel.add(txtNomResp, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Apellido Resp.:"), gbc);
        txtApeResp = new JTextField(12); gbc.gridx = 3; panel.add(txtApeResp, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("DNI Resp.:"), gbc);
        txtDniResp = new JTextField(12); gbc.gridx = 1; panel.add(txtDniResp, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Tipo Relación:"), gbc);
        txtRelResp = new JTextField(12); gbc.gridx = 3; panel.add(txtRelResp, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        JButton btnGuarAlu = new JButton("Guardar Alumno");
        btnGuarAlu.setBackground(new Color(37, 99, 235));
        btnGuarAlu.setForeground(Color.WHITE);
        btnGuarAlu.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnGuarAlu.addActionListener(_ -> {
            try {
                Integer idAdmin = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                adminServicio.registrarAlumnoConResponsableYUsuario(
                    idAdmin,
                    txtUsrAlu.getText().trim(), new String(((JPasswordField)txtPassAlu).getPassword()).trim(),
                    txtNomAlu.getText().trim(), txtApeAlu.getText().trim(), txtDniAlu.getText().trim(), (String)cmbEstAlu.getSelectedItem(),
                    txtNomResp.getText().trim(), txtApeResp.getText().trim(), txtDniResp.getText().trim(), txtRelResp.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Alumno registrado con éxito.");
                panelFormAlumno.setVisible(false);
                cargarDatosAdmin();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnGuarAlu, gbc);

        return panel;
    }

    private JPanel crearPanelFormularioDocente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 4;
        JLabel lblT = new JLabel("REGISTRO DE NUEVO DOCENTE (Legajo automático)");
        lblT.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblT.setForeground(new Color(24, 61, 110));
        panel.add(lblT, gbc);

        gbc.gridwidth = 1;
        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Usuario:"), gbc);
        txtUsrDoc = new JTextField(12); gbc.gridx = 1; panel.add(txtUsrDoc, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Contraseña:"), gbc);
        txtPassDoc = new JPasswordField(12); gbc.gridx = 3; panel.add(txtPassDoc, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Nombre Docente:"), gbc);
        txtNomDoc = new JTextField(12); gbc.gridx = 1; panel.add(txtNomDoc, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Apellido Docente:"), gbc);
        txtApeDoc = new JTextField(12); gbc.gridx = 3; panel.add(txtApeDoc, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("DNI Docente:"), gbc);
        txtDniDoc = new JTextField(12); gbc.gridx = 1; panel.add(txtDniDoc, gbc);
        gbc.gridx = 2; panel.add(new JLabel("Estado:"), gbc);
        cmbEstDoc = new JComboBox<>(new String[]{"Activo", "Inactivo", "Licencia"});
        gbc.gridx = 3; panel.add(cmbEstDoc, gbc);

        row++; gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        JButton btnGuarDoc = new JButton("Guardar Docente");
        btnGuarDoc.setBackground(new Color(37, 99, 235));
        btnGuarDoc.setForeground(Color.WHITE);
        btnGuarDoc.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnGuarDoc.addActionListener(_ -> {
            try {
                Integer idAdmin = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                adminServicio.registrarDocenteConUsuario(
                    idAdmin,
                    txtUsrDoc.getText().trim(), new String(((JPasswordField)txtPassDoc).getPassword()).trim(),
                    txtNomDoc.getText().trim(), txtApeDoc.getText().trim(), txtDniDoc.getText().trim(), (String)cmbEstDoc.getSelectedItem()
                );
                JOptionPane.showMessageDialog(this, "Docente registrado con éxito.");
                panelFormDocente.setVisible(false);
                cargarDatosAdmin();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnGuarDoc, gbc);

        return panel;
    }

    private JPanel crearTarjetaEstadistica(String titulo, JLabel lblValor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 4));
        tarjeta.setBackground(new Color(248, 250, 252));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 228, 236)),
            BorderFactory.createEmptyBorder(10, 8, 10, 8)
        ));

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
        panelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 223, 232)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Tahoma", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 41, 59));

        panelBtn.add(lbl, BorderLayout.WEST);

        return panelBtn;
    }

    private void cargarDatosAdmin() {
        new SwingWorker<Void, Void>() {
            int alumnos = 0, docentes = 0, cursos = 0, inscripciones = 0, usuariosCount = 0;
            List<Log_Usuario> listaLogs;
            List<Usuario> usuarios;
            List<Rol> roles;

            @Override
            protected Void doInBackground() {
                try {
                    alumnos = adminServicio.contarAlumnos();
                    docentes = adminServicio.contarDocentes();
                    cursos = adminServicio.contarCursosActivos();
                    inscripciones = adminServicio.contarInscripciones();
                    usuariosCount = adminServicio.contarUsuarios();

                    usuarios = usuarioDao.obtenerTodos();
                    listaLogs = logUsuarioDao.obtenerTodos();
                    roles = rolDao.obtenerTodos();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                lblAlumnosTotales.setText(String.valueOf(alumnos));
                lblDocentesTotales.setText(String.valueOf(docentes));
                lblCursosActivos.setText(String.valueOf(cursos));
                lblInscripciones.setText(String.valueOf(inscripciones));
                lblCuentasUsuarios.setText(String.valueOf(usuariosCount));

                modeloLog.setRowCount(0);
                if (listaLogs != null && !listaLogs.isEmpty()) {
                    java.util.Map<Integer, String> mapUsuarios = new java.util.HashMap<>();
                    if (usuarios != null) {
                        for (Usuario u : usuarios) mapUsuarios.put(u.getId_usuario(), u.getNombre_usuario());
                    }

                    java.util.Map<Integer, String> mapRoles = new java.util.HashMap<>();
                    if (roles != null) {
                        for (Rol r : roles) mapRoles.put(r.getId_rol(), r.getNombre());
                    }

                    for (Log_Usuario log : listaLogs) {
                        String nombreUsuario = mapUsuarios.getOrDefault(log.getId_usuario(), "Desconocido");
                        String rolUsuario = "N/A";
                        if (log.getId_usuario() != null) {
                            Usuario u = usuarios.stream().filter(x -> x.getId_usuario().equals(log.getId_usuario())).findFirst().orElse(null);
                            if (u != null) {
                                rolUsuario = mapRoles.getOrDefault(u.getId_rol(), "Rol " + u.getId_rol());
                            }
                        }

                        String fechaStr = log.getFecha_hora() != null ? 
                            log.getFecha_hora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";

                        modeloLog.addRow(new Object[]{
                            log.getId_log_usuario(),
                            nombreUsuario,
                            rolUsuario,
                            log.getAccion(),
                            log.getCampo_modificado(),
                            log.getValor_nuevo() != null ? log.getValor_nuevo() : log.getValor_anterior(),
                            fechaStr
                        });
                    }
                }
            }
        }.execute();
    }
}