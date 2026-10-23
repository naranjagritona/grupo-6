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
        adminServicio = new Admin_servicio();
        logUsuarioDao = new Log_Usuario_dao();
        usuarioDao = new Usuario_dao();
        rolDao = new Rol_dao();

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1150, 680));

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

        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(Color.WHITE);
        panelCentral.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 5, 12, 0));
        panelTarjetas.setBackground(Color.WHITE);
        panelTarjetas.setPreferredSize(new Dimension(0, 85));
        panelTarjetas.setMinimumSize(new Dimension(0, 85));
        panelTarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        panelTarjetas.add(crearTarjetaEstadistica("ALUMNOS TOTALES", lblAlumnosTotales = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("DOCENTES TOTALES", lblDocentesTotales = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("CURSOS ACTIVOS", lblCursosActivos = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("INSCRIPCIONES", lblInscripciones = new JLabel("0", SwingConstants.CENTER)));
        panelTarjetas.add(crearTarjetaEstadistica("CUENTAS DE USUARIOS", lblCuentasUsuarios = new JLabel("0", SwingConstants.CENTER)));

        panelCentral.add(panelTarjetas);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel panelAcciones = new JPanel(new GridLayout(1, 2, 15, 0));
        panelAcciones.setBackground(Color.WHITE);
        panelAcciones.setPreferredSize(new Dimension(0, 48));
        panelAcciones.setMinimumSize(new Dimension(0, 48));
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

        panelFormAlumno = crearPanelFormularioAlumno();
        panelFormAlumno.setVisible(false);
        panelCentral.add(panelFormAlumno);

        panelFormDocente = crearPanelFormularioDocente();
        panelFormDocente.setVisible(false);
        panelCentral.add(panelFormDocente);

        panelCentral.add(Box.createRigidArea(new Dimension(0, 15)));

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

        int row = 0;

        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.insets = new Insets(5, 5, 5, 5);
        gbc0.fill = GridBagConstraints.HORIZONTAL;
        gbc0.gridx = 0; gbc0.gridy = row++; gbc0.gridwidth = 4;
        JLabel lblT = new JLabel("REGISTRO DE NUEVO ALUMNO Y RESPONSABLE");
        lblT.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblT.setForeground(new Color(24, 61, 110));
        panel.add(lblT, gbc0);

        row++;
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(5, 5, 5, 5);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.gridx = 0; gbc1.gridy = row; gbc1.gridwidth = 1;
        panel.add(new JLabel("Usuario:"), gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.gridx = 1; gbc2.gridy = row; gbc2.gridwidth = 1;
        txtUsrAlu = new JTextField(12);
        panel.add(txtUsrAlu, gbc2);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = new Insets(5, 5, 5, 5);
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        gbc3.gridx = 2; gbc3.gridy = row; gbc3.gridwidth = 1;
        panel.add(new JLabel("Contraseña:"), gbc3);

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.insets = new Insets(5, 5, 5, 5);
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.gridx = 3; gbc4.gridy = row; gbc4.gridwidth = 1;
        txtPassAlu = new JPasswordField(12);
        panel.add(txtPassAlu, gbc4);

        row++;
        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.insets = new Insets(5, 5, 5, 5);
        gbc5.fill = GridBagConstraints.HORIZONTAL;
        gbc5.gridx = 0; gbc5.gridy = row; gbc5.gridwidth = 1;
        panel.add(new JLabel("Nombre Alumno:"), gbc5);

        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.insets = new Insets(5, 5, 5, 5);
        gbc6.fill = GridBagConstraints.HORIZONTAL;
        gbc6.gridx = 1; gbc6.gridy = row; gbc6.gridwidth = 1;
        txtNomAlu = new JTextField(12);
        panel.add(txtNomAlu, gbc6);

        GridBagConstraints gbc7 = new GridBagConstraints();
        gbc7.insets = new Insets(5, 5, 5, 5);
        gbc7.fill = GridBagConstraints.HORIZONTAL;
        gbc7.gridx = 2; gbc7.gridy = row; gbc7.gridwidth = 1;
        panel.add(new JLabel("Apellido Alumno:"), gbc7);

        GridBagConstraints gbc8 = new GridBagConstraints();
        gbc8.insets = new Insets(5, 5, 5, 5);
        gbc8.fill = GridBagConstraints.HORIZONTAL;
        gbc8.gridx = 3; gbc8.gridy = row; gbc8.gridwidth = 1;
        txtApeAlu = new JTextField(12);
        panel.add(txtApeAlu, gbc8);

        row++;
        GridBagConstraints gbc9 = new GridBagConstraints();
        gbc9.insets = new Insets(5, 5, 5, 5);
        gbc9.fill = GridBagConstraints.HORIZONTAL;
        gbc9.gridx = 0; gbc9.gridy = row; gbc9.gridwidth = 1;
        panel.add(new JLabel("DNI Alumno:"), gbc9);

        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.insets = new Insets(5, 5, 5, 5);
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        gbc10.gridx = 1; gbc10.gridy = row; gbc10.gridwidth = 1;
        txtDniAlu = new JTextField(12);
        panel.add(txtDniAlu, gbc10);

        GridBagConstraints gbc11 = new GridBagConstraints();
        gbc11.insets = new Insets(5, 5, 5, 5);
        gbc11.fill = GridBagConstraints.HORIZONTAL;
        gbc11.gridx = 2; gbc11.gridy = row; gbc11.gridwidth = 1;
        panel.add(new JLabel("Estado:"), gbc11);

        GridBagConstraints gbc12 = new GridBagConstraints();
        gbc12.insets = new Insets(5, 5, 5, 5);
        gbc12.fill = GridBagConstraints.HORIZONTAL;
        gbc12.gridx = 3; gbc12.gridy = row; gbc12.gridwidth = 1;
        cmbEstAlu = new JComboBox<>(new String[]{"Activo", "Inactivo", "Suspendido"});
        panel.add(cmbEstAlu, gbc12);

        row++;
        GridBagConstraints gbc13 = new GridBagConstraints();
        gbc13.insets = new Insets(5, 5, 5, 5);
        gbc13.fill = GridBagConstraints.HORIZONTAL;
        gbc13.gridx = 0; gbc13.gridy = row; gbc13.gridwidth = 1;
        panel.add(new JLabel("Nombre Resp.:"), gbc13);

        GridBagConstraints gbc14 = new GridBagConstraints();
        gbc14.insets = new Insets(5, 5, 5, 5);
        gbc14.fill = GridBagConstraints.HORIZONTAL;
        gbc14.gridx = 1; gbc14.gridy = row; gbc14.gridwidth = 1;
        txtNomResp = new JTextField(12);
        panel.add(txtNomResp, gbc14);

        GridBagConstraints gbc15 = new GridBagConstraints();
        gbc15.insets = new Insets(5, 5, 5, 5);
        gbc15.fill = GridBagConstraints.HORIZONTAL;
        gbc15.gridx = 2; gbc15.gridy = row; gbc15.gridwidth = 1;
        panel.add(new JLabel("Apellido Resp.:"), gbc15);

        GridBagConstraints gbc16 = new GridBagConstraints();
        gbc16.insets = new Insets(5, 5, 5, 5);
        gbc16.fill = GridBagConstraints.HORIZONTAL;
        gbc16.gridx = 3; gbc16.gridy = row; gbc16.gridwidth = 1;
        txtApeResp = new JTextField(12);
        panel.add(txtApeResp, gbc16);

        row++;
        GridBagConstraints gbc17 = new GridBagConstraints();
        gbc17.insets = new Insets(5, 5, 5, 5);
        gbc17.fill = GridBagConstraints.HORIZONTAL;
        gbc17.gridx = 0; gbc17.gridy = row; gbc17.gridwidth = 1;
        panel.add(new JLabel("DNI Resp.:"), gbc17);

        GridBagConstraints gbc18 = new GridBagConstraints();
        gbc18.insets = new Insets(5, 5, 5, 5);
        gbc18.fill = GridBagConstraints.HORIZONTAL;
        gbc18.gridx = 1; gbc18.gridy = row; gbc18.gridwidth = 1;
        txtDniResp = new JTextField(12);
        panel.add(txtDniResp, gbc18);

        GridBagConstraints gbc19 = new GridBagConstraints();
        gbc19.insets = new Insets(5, 5, 5, 5);
        gbc19.fill = GridBagConstraints.HORIZONTAL;
        gbc19.gridx = 2; gbc19.gridy = row; gbc19.gridwidth = 1;
        panel.add(new JLabel("Tipo Relación:"), gbc19);

        GridBagConstraints gbc20 = new GridBagConstraints();
        gbc20.insets = new Insets(5, 5, 5, 5);
        gbc20.fill = GridBagConstraints.HORIZONTAL;
        gbc20.gridx = 3; gbc20.gridy = row; gbc20.gridwidth = 1;
        txtRelResp = new JTextField(12);
        panel.add(txtRelResp, gbc20);

        row++;
        GridBagConstraints gbc21 = new GridBagConstraints();
        gbc21.insets = new Insets(5, 5, 5, 5);
        gbc21.fill = GridBagConstraints.HORIZONTAL;
        gbc21.gridx = 0; gbc21.gridy = row; gbc21.gridwidth = 4;
        JButton btnGuarAlu = new JButton("Guardar Alumno");
        btnGuarAlu.setBackground(new Color(37, 99, 235));
        btnGuarAlu.setForeground(Color.WHITE);
        btnGuarAlu.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnGuarAlu.addActionListener(e -> {
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
        panel.add(btnGuarAlu, gbc21);

        return panel;
    }

    private JPanel crearPanelFormularioDocente() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        int row = 0;

        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.insets = new Insets(5, 5, 5, 5);
        gbc0.fill = GridBagConstraints.HORIZONTAL;
        gbc0.gridx = 0; gbc0.gridy = row++; gbc0.gridwidth = 4;
        JLabel lblT = new JLabel("REGISTRO DE NUEVO DOCENTE");
        lblT.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblT.setForeground(new Color(24, 61, 110));
        panel.add(lblT, gbc0);

        row++;
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(5, 5, 5, 5);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.gridx = 0; gbc1.gridy = row; gbc1.gridwidth = 1;
        panel.add(new JLabel("Usuario:"), gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.gridx = 1; gbc2.gridy = row; gbc2.gridwidth = 1;
        txtUsrDoc = new JTextField(12);
        panel.add(txtUsrDoc, gbc2);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = new Insets(5, 5, 5, 5);
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        gbc3.gridx = 2; gbc3.gridy = row; gbc3.gridwidth = 1;
        panel.add(new JLabel("Contraseña:"), gbc3);

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.insets = new Insets(5, 5, 5, 5);
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.gridx = 3; gbc4.gridy = row; gbc4.gridwidth = 1;
        txtPassDoc = new JPasswordField(12);
        panel.add(txtPassDoc, gbc4);

        row++;
        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.insets = new Insets(5, 5, 5, 5);
        gbc5.fill = GridBagConstraints.HORIZONTAL;
        gbc5.gridx = 0; gbc5.gridy = row; gbc5.gridwidth = 1;
        panel.add(new JLabel("Nombre Docente:"), gbc5);

        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.insets = new Insets(5, 5, 5, 5);
        gbc6.fill = GridBagConstraints.HORIZONTAL;
        gbc6.gridx = 1; gbc6.gridy = row; gbc6.gridwidth = 1;
        txtNomDoc = new JTextField(12);
        panel.add(txtNomDoc, gbc6);

        GridBagConstraints gbc7 = new GridBagConstraints();
        gbc7.insets = new Insets(5, 5, 5, 5);
        gbc7.fill = GridBagConstraints.HORIZONTAL;
        gbc7.gridx = 2; gbc7.gridy = row; gbc7.gridwidth = 1;
        panel.add(new JLabel("Apellido Docente:"), gbc7);

        GridBagConstraints gbc8 = new GridBagConstraints();
        gbc8.insets = new Insets(5, 5, 5, 5);
        gbc8.fill = GridBagConstraints.HORIZONTAL;
        gbc8.gridx = 3; gbc8.gridy = row; gbc8.gridwidth = 1;
        txtApeDoc = new JTextField(12);
        panel.add(txtApeDoc, gbc8);

        row++;
        GridBagConstraints gbc9 = new GridBagConstraints();
        gbc9.insets = new Insets(5, 5, 5, 5);
        gbc9.fill = GridBagConstraints.HORIZONTAL;
        gbc9.gridx = 0; gbc9.gridy = row; gbc9.gridwidth = 1;
        panel.add(new JLabel("DNI Docente:"), gbc9);

        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.insets = new Insets(5, 5, 5, 5);
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        gbc10.gridx = 1; gbc10.gridy = row; gbc10.gridwidth = 1;
        txtDniDoc = new JTextField(12);
        panel.add(txtDniDoc, gbc10);

        GridBagConstraints gbc11 = new GridBagConstraints();
        gbc11.insets = new Insets(5, 5, 5, 5);
        gbc11.fill = GridBagConstraints.HORIZONTAL;
        gbc11.gridx = 2; gbc11.gridy = row; gbc11.gridwidth = 1;
        panel.add(new JLabel("Estado:"), gbc11);

        GridBagConstraints gbc12 = new GridBagConstraints();
        gbc12.insets = new Insets(5, 5, 5, 5);
        gbc12.fill = GridBagConstraints.HORIZONTAL;
        gbc12.gridx = 3; gbc12.gridy = row; gbc12.gridwidth = 1;
        cmbEstDoc = new JComboBox<>(new String[]{"Activo", "Inactivo", "Licencia"});
        panel.add(cmbEstDoc, gbc12);

        row++;
        GridBagConstraints gbc13 = new GridBagConstraints();
        gbc13.insets = new Insets(5, 5, 5, 5);
        gbc13.fill = GridBagConstraints.HORIZONTAL;
        gbc13.gridx = 0; gbc13.gridy = row; gbc13.gridwidth = 4;
        JButton btnGuarDoc = new JButton("Guardar Docente");
        btnGuarDoc.setBackground(new Color(37, 99, 235));
        btnGuarDoc.setForeground(Color.WHITE);
        btnGuarDoc.setFont(new Font("Tahoma", Font.BOLD, 12));
      //configura el evento del boton para guardar el docente al hacer clic
        btnGuarDoc.addActionListener(e -> {
            try {
                //recupera el id del admin actual o deja null si no esta logueado
                Integer idAdmin = usuarioLogueado != null ? usuarioLogueado.getId_usuario() : null;
                
                //ejecuta el metodo del servicio pasandole los textos limpios
                //para crear el usuario, el legajo y el docente en la base de datos
                adminServicio.registrarDocenteConUsuario(
                    idAdmin,
                    txtUsrDoc.getText().trim(), new String(((JPasswordField)txtPassDoc).getPassword()).trim(),
                    txtNomDoc.getText().trim(), txtApeDoc.getText().trim(), txtDniDoc.getText().trim(), (String)cmbEstDoc.getSelectedItem()
                );
                
                //muestra un cartel indicando que se guardo correctamente
                JOptionPane.showMessageDialog(this, "Docente registrado con éxito.");
                
                //oculta el panel del formulario y recarga los datos de la vista
                panelFormDocente.setVisible(false);
                cargarDatosAdmin();
                
            } catch (Exception ex) {
                //captura cualquier error y lo muestra en una ventana emergente
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        //agrega el boton al panel con sus restricciones correspondientes
        panel.add(btnGuarDoc, gbc13);

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

   
  //carga de forma asincrona los contadores de las tarjetas y los registros de la tabla de logs
    private void cargarDatosAdmin() {
        new SwingWorker<Void, Void>() {
            int alumnos = 0, docentes = 0, cursos = 0, inscripciones = 0, usuariosCount = 0;
            List<Log_Usuario> listaLogs;
            List<Usuario> usuarios;
            List<Rol> roles;

            @Override
            protected Void doInBackground() {
                try {
                    //obtiene los totales llamando a los metodos contadores del admin servicio
                    alumnos = adminServicio.contarAlumnos();
                    docentes = adminServicio.contarDocentes();
                    cursos = adminServicio.contarCursosActivos();
                    inscripciones = adminServicio.contarInscripciones();
                    usuariosCount = adminServicio.contarUsuarios();

                    //trae las listas completas para despues cruzarlas en la tabla
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
                //pone los valores numericos obtenidos en las etiquetas de las tarjetas superiores
                lblAlumnosTotales.setText(String.valueOf(alumnos));
                lblDocentesTotales.setText(String.valueOf(docentes));
                lblCursosActivos.setText(String.valueOf(cursos));
                lblInscripciones.setText(String.valueOf(inscripciones));
                lblCuentasUsuarios.setText(String.valueOf(usuariosCount));

                //limpia la tabla de logs antes de cargar los datos nuevos
                modeloLog.setRowCount(0);
                if (listaLogs != null && !listaLogs.isEmpty()) {
                    //crea un mapa para traducir el id de usuario a su nombre de usuario
                    java.util.Map<Integer, String> mapUsuarios = new java.util.HashMap<>();
                    if (usuarios != null) {
                        for (Usuario u : usuarios) mapUsuarios.put(u.getId_usuario(), u.getNombre_usuario());
                    }

                    //crea otro mapa para saber el nombre del rol a partir de su id
                    java.util.Map<Integer, String> mapRoles = new java.util.HashMap<>();
                    if (roles != null) {
                        for (Rol r : roles) mapRoles.put(r.getId_rol(), r.getNombre());
                    }

                    //recorre cada log registrado para armar la fila de la tabla
                    for (Log_Usuario log : listaLogs) {
                        String nombreUsuario = mapUsuarios.getOrDefault(log.getId_usuario(), "Desconocido");
                        String rolUsuario = "N/A";
                        if (log.getId_usuario() != null) {
                            Usuario u = usuarios.stream().filter(x -> x.getId_usuario().equals(log.getId_usuario())).findFirst().orElse(null);
                            if (u != null) {
                                rolUsuario = mapRoles.getOrDefault(u.getId_rol(), "Rol " + u.getId_rol());
                            }
                        }

                        //le da formato legible a la fecha y hora del log
                        String fechaStr = log.getFecha_hora() != null ? 
                            log.getFecha_hora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";

                        //agrega la fila armada al modelo de la tabla de auditoria
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