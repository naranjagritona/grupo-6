package paneles;

import clases.*;
import dao.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AdminFrame extends JFrame {
    
    // guarda la informacion del usuario autenticado en el sistema
    private Usuario usuarioActual;
    
    // el cardlayout permite cambiar entre distintos paneles manteniendo la misma ventana activa
    private JPanel panelContenidoCentral;
    private CardLayout cardLayout;

    // clases de acceso a datos para realizar consultas y modificaciones en la base de datos
    private Alumno_dao alumnoDao = new Alumno_dao();
    private Docente_dao docenteDao = new Docente_dao();
    private Curso_dao cursoDao = new Curso_dao();
    private Materia_dao materiaDao = new Materia_dao();
    private Inscripcion_dao inscripcionDao = new Inscripcion_dao();
    private Usuario_dao usuarioDao = new Usuario_dao();
    private Responsable_dao responsableDao = new Responsable_dao();

    // constructor por defecto requerido para el diseño visual en windowbuilder
    public AdminFrame() {
        this(new Usuario(1, 1, "Administrador Default", ""));
    }

    public AdminFrame(Usuario usuario) {
        this.usuarioActual = usuario;

        // configuracion basica de la ventana principal
        setTitle("Academia Integral XXI - Panel de Administración");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        initComponentes();
    }

    private void initComponentes() {
        // borderlayout organiza la ventana en regiones principales
        setLayout(new BorderLayout());

        // panel lateral que funciona como menu principal de navegacion
        JPanel panelMenuLateral = new JPanel();
        panelMenuLateral.setPreferredSize(new Dimension(240, getHeight()));
        panelMenuLateral.setBackground(new Color(37, 99, 235));
        panelMenuLateral.setLayout(new GridBagLayout());

        JLabel lblTituloMenu = new JLabel("MENÚ ADMINISTRADOR", SwingConstants.CENTER);
        lblTituloMenu.setForeground(Color.WHITE);
        lblTituloMenu.setFont(new Font("Arial", Font.BOLD, 14));
        
        // gridbagconstraints define las reglas de posicion y margen para cada elemento del menu
        GridBagConstraints gbc_lblTituloMenu = new GridBagConstraints();
        gbc_lblTituloMenu.gridx = 0;
        gbc_lblTituloMenu.gridy = 0;
        gbc_lblTituloMenu.insets = new Insets(5, 10, 5, 10);
        gbc_lblTituloMenu.fill = GridBagConstraints.HORIZONTAL;
        panelMenuLateral.add(lblTituloMenu, gbc_lblTituloMenu);

        // instanciacion directa de cada boton para evitar que windowbuilder agrupe las restricciones en una sola variable
        JButton btnPanelAdmin = new JButton("Admin");
        estilarBotonMenu(btnPanelAdmin);
        GridBagConstraints gbc_btnPanelAdmin = new GridBagConstraints();
        gbc_btnPanelAdmin.gridx = 0;
        gbc_btnPanelAdmin.gridy = 1;
        gbc_btnPanelAdmin.insets = new Insets(5, 10, 5, 10);
        gbc_btnPanelAdmin.fill = GridBagConstraints.HORIZONTAL;
        panelMenuLateral.add(btnPanelAdmin, gbc_btnPanelAdmin);

        JButton btnGestionUsuarios = new JButton("Registrar un usuario");
        estilarBotonMenu(btnGestionUsuarios);
        GridBagConstraints gbc_btnGestionUsuarios = new GridBagConstraints();
        gbc_btnGestionUsuarios.gridx = 0;
        gbc_btnGestionUsuarios.gridy = 2;
        gbc_btnGestionUsuarios.insets = new Insets(5, 10, 5, 10);
        gbc_btnGestionUsuarios.fill = GridBagConstraints.HORIZONTAL;
        panelMenuLateral.add(btnGestionUsuarios, gbc_btnGestionUsuarios);

        // componente invisible que ocupa el espacio sobrante para empujar los elementos hacia los extremos
        JLabel lblFiller = new JLabel("");
        GridBagConstraints gbc_filler = new GridBagConstraints();
        gbc_filler.gridx = 0;
        gbc_filler.gridy = 3;
        gbc_filler.weighty = 1.0; 
        panelMenuLateral.add(lblFiller, gbc_filler);

        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        estilarBotonMenu(btnCerrarSesion);
        btnCerrarSesion.setBackground(new Color(220, 38, 38)); 
        GridBagConstraints gbc_btnCerrarSesion = new GridBagConstraints();
        gbc_btnCerrarSesion.gridx = 0;
        gbc_btnCerrarSesion.gridy = 4;
        gbc_btnCerrarSesion.insets = new Insets(5, 10, 5, 10);
        gbc_btnCerrarSesion.fill = GridBagConstraints.HORIZONTAL;
        panelMenuLateral.add(btnCerrarSesion, gbc_btnCerrarSesion);

        add(panelMenuLateral, BorderLayout.WEST);

        // panel central dinamico donde se intercambian las vistas segun la opcion seleccionada
        cardLayout = new CardLayout();
        panelContenidoCentral = new JPanel(cardLayout);

        panelContenidoCentral.add(crearPanelBienvenida(), "bienvenida");
        panelContenidoCentral.add(crearPanelUnificadoTablas(), "panelAdmin");

        add(panelContenidoCentral, BorderLayout.CENTER);

        // eventos de navegacion para conmutar los paneles visibles
        btnPanelAdmin.addActionListener(e -> cardLayout.show(panelContenidoCentral, "panelAdmin"));
        btnGestionUsuarios.addActionListener(e -> abrirFormularioCrearUsuario());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
    }

    // aplica los estilos visuales a los botones del menu lateral sin crear instancias dentro del metodo
    private void estilarBotonMenu(JButton boton) {
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(51, 65, 85));
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.PLAIN, 12));
        boton.setPreferredSize(new Dimension(200, 35));
    }

    private JPanel crearPanelBienvenida() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lblBienvenida = new JLabel("Panel de Control - Sesión iniciada como: " + usuarioActual.getNombre_usuario(), SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblBienvenida, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelUnificadoTablas() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // contenedor de pestañas para organizar las tablas de cada entidad del sistema
        JTabbedPane pestañasTablas = new JTabbedPane();
        
        pestañasTablas.addTab("Gestión de Alumnos", crearPanelTablaAlumnos());
        pestañasTablas.addTab("Gestión de Docentes", crearPanelTablaDocentes());
        pestañasTablas.addTab("Gestión de Cursos", crearPanelTablaCursos());
        pestañasTablas.addTab("Gestión de Materias", crearPanelTablaMaterias());
        pestañasTablas.addTab("Gestión de Inscripciones", crearPanelTablaInscripciones());
        
        panel.add(pestañasTablas, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelTablaAlumnos() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnas = {"ID", "ID Usuario", "ID Resp.", "Responsable (Tutor)", "Nombre", "Apellido", "DNI", "Matrícula", "Estado"};
        
        // el modelo define las columnas y deshabilita la edicion directa de celdas
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modelo);

        // bloque de codigo ejecutable que actualiza la tabla obteniendo los datos mas recientes
        Runnable cargarDatos = () -> {
            modelo.setRowCount(0); 
            for (Alumno a : alumnoDao.obtenerTodos()) {
                // obtiene el nombre completo del tutor relacionado a partir del id_responsable
                String nombreResponsable = "Sin asignar";
                if (a.getId_responsable() != null) {
                    Responsable resp = responsableDao.obtenerTodos().stream()
                            .filter(r -> r.getId_responsable().equals(a.getId_responsable()))
                            .findFirst().orElse(null);
                    if (resp != null) {
                        nombreResponsable = resp.getNombre() + " " + resp.getApellido() + " (" + resp.getTipo_relacion() + ")";
                    }
                }
                modelo.addRow(new Object[]{
                    a.getId_alumno(), 
                    a.getId_usuario(), 
                    a.getId_responsable(), 
                    nombreResponsable, 
                    a.getNombre(), 
                    a.getApellido(), 
                    a.getDni(), 
                    a.getMatricula(), 
                    a.getEstado()
                });
            }
        };
        cargarDatos.run(); 

        JPanel panelBotones = new JPanel();
        JButton btnModificar = new JButton("Modificar Alumno");
        JButton btnEliminar = new JButton("Eliminar Alumno");
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);

        btnModificar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                Alumno alumnoMod = alumnoDao.obtenerTodos().stream().filter(a -> a.getId_alumno() == id).findFirst().orElse(null);
                
                if(alumnoMod != null) {
                    // formulario dinamico para editar los datos de la entidad seleccionada
                    JTextField txtNombre = new JTextField(alumnoMod.getNombre(), 15);
                    JTextField txtApellido = new JTextField(alumnoMod.getApellido(), 15);
                    JTextField txtDni = new JTextField(alumnoMod.getDni(), 15);
                    JTextField txtMatricula = new JTextField(alumnoMod.getMatricula(), 15);
                    JTextField txtEstado = new JTextField(alumnoMod.getEstado(), 15);
                    JTextField txtIdResp = new JTextField(String.valueOf(alumnoMod.getId_responsable() != null ? alumnoMod.getId_responsable() : ""), 15);

                    Object[] mensajeFormulario = {
                        "Nombre:", txtNombre,
                        "Apellido:", txtApellido,
                        "DNI:", txtDni,
                        "Matrícula:", txtMatricula,
                        "Estado:", txtEstado,
                        "ID Responsable (Tutor):", txtIdResp
                    };

                    int option = JOptionPane.showConfirmDialog(this, mensajeFormulario, "Modificar Datos del Alumno", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            alumnoMod.setNombre(txtNombre.getText().trim());
                            alumnoMod.setApellido(txtApellido.getText().trim());
                            alumnoMod.setDni(txtDni.getText().trim());
                            alumnoMod.setMatricula(txtMatricula.getText().trim());
                            alumnoMod.setEstado(txtEstado.getText().trim());
                            if (!txtIdResp.getText().trim().isEmpty()) {
                                alumnoMod.setId_responsable(Integer.parseInt(txtIdResp.getText().trim()));
                            } else {
                                alumnoMod.setId_responsable(null);
                            }

                            // persistencia de los cambios mediante la funcion modificar del dao y recarga inmediata
                            alumnoDao.modificar(alumnoMod);
                            JOptionPane.showMessageDialog(this, "Alumno modificado con éxito.");
                            cargarDatos.run(); 
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al modificar los datos: Verifique que los formatos sean correctos.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un alumno de la tabla.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el alumno seleccionado definitivamente?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {
                    alumnoDao.eliminar(id);
                    JOptionPane.showMessageDialog(this, "Alumno eliminado correctamente.");
                    cargarDatos.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un alumno de la tabla.");
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTablaDocentes() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnas = {"ID", "ID Usuario", "Nombre", "Apellido", "DNI", "Legajo", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modelo);

        Runnable cargarDatos = () -> {
            modelo.setRowCount(0);
            for (Docente d : docenteDao.obtenerTodos()) {
                modelo.addRow(new Object[]{d.getId_docente(), d.getId_usuario(), d.getNombre(), d.getApellido(), d.getDni(), d.getLegajo(), d.getEstado()});
            }
        };
        cargarDatos.run();

        JPanel panelBotones = new JPanel();
        JButton btnModificar = new JButton("Modificar Docente");
        JButton btnEliminar = new JButton("Eliminar Docente");
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);

        btnModificar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                Docente docenteMod = docenteDao.obtenerTodos().stream().filter(d -> d.getId_docente() == id).findFirst().orElse(null);
                
                if(docenteMod != null) {
                    JTextField txtNombre = new JTextField(docenteMod.getNombre(), 15);
                    JTextField txtApellido = new JTextField(docenteMod.getApellido(), 15);
                    JTextField txtDni = new JTextField(docenteMod.getDni(), 15);
                    JTextField txtLegajo = new JTextField(docenteMod.getLegajo(), 15);
                    JTextField txtEstado = new JTextField(docenteMod.getEstado(), 15);

                    Object[] camposDocente = {
                        "Nombre:", txtNombre,
                        "Apellido:", txtApellido,
                        "DNI:", txtDni,
                        "Legajo:", txtLegajo,
                        "Estado:", txtEstado
                    };

                    int option = JOptionPane.showConfirmDialog(this, camposDocente, "Modificar Docente", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            docenteMod.setNombre(txtNombre.getText().trim());
                            docenteMod.setApellido(txtApellido.getText().trim());
                            docenteMod.setDni(txtDni.getText().trim());
                            docenteMod.setLegajo(txtLegajo.getText().trim());
                            docenteMod.setEstado(txtEstado.getText().trim());

                            docenteDao.modificar(docenteMod);
                            JOptionPane.showMessageDialog(this, "Docente modificado con éxito.");
                            cargarDatos.run();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al modificar docente.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un docente de la tabla.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                if(JOptionPane.showConfirmDialog(this, "¿Eliminar docente definitivamente?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    docenteDao.eliminar(id);
                    JOptionPane.showMessageDialog(this, "Docente eliminado correctamente.");
                    cargarDatos.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un docente de la tabla.");
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTablaCursos() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnas = {"ID Curso", "Periodo", "Materia", "Docente", "Año", "División", "Modalidad", "Turno", "Cupo", "Estado"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modelo);

        Runnable cargarDatos = () -> {
            modelo.setRowCount(0);
            for (Curso c : cursoDao.obtenerTodos()) {
                modelo.addRow(new Object[]{
                    c.getId_curso(), c.getId_periodo(), c.getId_materia(), c.getId_docente(), 
                    c.getAño(), c.getDivision(), c.getModalidad(), c.getTurno(), c.getCupo_maximo(), c.getEstado()
                });
            }
        };
        cargarDatos.run();

        JPanel panelBotones = new JPanel();
        JButton btnModificar = new JButton("Modificar Curso");
        JButton btnEliminar = new JButton("Eliminar Curso");
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);

        btnModificar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                Curso cursoMod = cursoDao.obtenerTodos().stream().filter(c -> c.getId_curso() == id).findFirst().orElse(null);
                if(cursoMod != null) {
                    JTextField txtIdPeriodo = new JTextField(String.valueOf(cursoMod.getId_periodo()), 15);
                    JTextField txtIdMateria = new JTextField(String.valueOf(cursoMod.getId_materia()), 15);
                    JTextField txtIdDocente = new JTextField(String.valueOf(cursoMod.getId_docente()), 15);
                    JTextField txtAnio = new JTextField(String.valueOf(cursoMod.getAño()), 15);
                    JTextField txtDivision = new JTextField(cursoMod.getDivision(), 15);
                    JTextField txtModalidad = new JTextField(cursoMod.getModalidad(), 15);
                    JTextField txtTurno = new JTextField(cursoMod.getTurno(), 15);
                    JTextField txtCupo = new JTextField(String.valueOf(cursoMod.getCupo_maximo()), 15);
                    JTextField txtEstado = new JTextField(cursoMod.getEstado(), 15);

                    Object[] camposCurso = {
                        "ID Periodo:", txtIdPeriodo,
                        "ID Materia:", txtIdMateria,
                        "ID Docente:", txtIdDocente,
                        "Año:", txtAnio,
                        "División:", txtDivision,
                        "Modalidad:", txtModalidad,
                        "Turno:", txtTurno,
                        "Cupo Máximo:", txtCupo,
                        "Estado:", txtEstado
                    };

                    int option = JOptionPane.showConfirmDialog(this, camposCurso, "Modificar Curso", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            cursoMod.setId_periodo(Integer.parseInt(txtIdPeriodo.getText().trim()));
                            cursoMod.setId_materia(Integer.parseInt(txtIdMateria.getText().trim()));
                            cursoMod.setId_docente(Integer.parseInt(txtIdDocente.getText().trim()));
                            cursoMod.setAño(Integer.parseInt(txtAnio.getText().trim()));
                            cursoMod.setDivision(txtDivision.getText().trim());
                            cursoMod.setModalidad(txtModalidad.getText().trim());
                            cursoMod.setTurno(txtTurno.getText().trim());
                            cursoMod.setCupo_maximo(Integer.parseInt(txtCupo.getText().trim()));
                            cursoMod.setEstado(txtEstado.getText().trim());

                            cursoDao.modificar(cursoMod);
                            JOptionPane.showMessageDialog(this, "Curso modificado con éxito.");
                            cargarDatos.run();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al modificar curso: Compruebe que los números sean válidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un curso.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                if(JOptionPane.showConfirmDialog(this, "¿Desea eliminar el Curso?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    cursoDao.eliminar(id);
                    JOptionPane.showMessageDialog(this, "Curso eliminado.");
                    cargarDatos.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un curso.");
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTablaMaterias() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnas = {"ID Materia", "Nombre Materia"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modelo);

        Runnable cargarDatos = () -> {
            modelo.setRowCount(0);
            for (Materia m : materiaDao.obtenerTodos()) {
                modelo.addRow(new Object[]{m.getId_materia(), m.getNombre_materia()});
            }
        };
        cargarDatos.run();

        JPanel panelBotones = new JPanel();
        JButton btnModificar = new JButton("Modificar Materia");
        JButton btnEliminar = new JButton("Eliminar Materia");
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);

        btnModificar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                Materia materiaMod = materiaDao.obtenerTodos().stream().filter(m -> m.getId_materia() == id).findFirst().orElse(null);
                if(materiaMod != null) {
                    JTextField txtNombreMateria = new JTextField(materiaMod.getNombre_materia(), 15);

                    Object[] camposMateria = {
                        "Nombre de Materia:", txtNombreMateria
                    };

                    int option = JOptionPane.showConfirmDialog(this, camposMateria, "Modificar Materia", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        String nuevoNombre = txtNombreMateria.getText().trim();
                        if(!nuevoNombre.isEmpty()) {
                            materiaMod.setNombre_materia(nuevoNombre);
                            materiaDao.modificar(materiaMod);
                            JOptionPane.showMessageDialog(this, "Materia modificada.");
                            cargarDatos.run();
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una materia.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                if(JOptionPane.showConfirmDialog(this, "¿Eliminar materia?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    materiaDao.eliminar(id);
                    JOptionPane.showMessageDialog(this, "Materia eliminada.");
                    cargarDatos.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una materia.");
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTablaInscripciones() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnas = {"ID Inscripción", "ID Alumno", "ID Curso", "Fecha", "Estado", "Motivo Baja", "Importe"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modelo);

        Runnable cargarDatos = () -> {
            modelo.setRowCount(0);
            for (Inscripcion i : inscripcionDao.obtenerTodos()) {
                modelo.addRow(new Object[]{
                    i.getId_inscripcion(), i.getId_alumno(), i.getId_curso(), 
                    i.getFecha_inscripcion(), i.getEstado(), i.getMotivo_baja(), i.getImporte_cuota()
                });
            }
        };
        cargarDatos.run();

        JPanel panelBotones = new JPanel();
        JButton btnModificar = new JButton("Modificar Inscripción");
        JButton btnEliminar = new JButton("Dar de Baja (Eliminar)");
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);

        btnModificar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                Inscripcion inscripcionMod = inscripcionDao.obtenerTodos().stream().filter(i -> i.getId_inscripcion() == id).findFirst().orElse(null);
                if(inscripcionMod != null) {
                    JTextField txtIdAlumno = new JTextField(String.valueOf(inscripcionMod.getId_alumno()), 15);
                    JTextField txtIdCurso = new JTextField(String.valueOf(inscripcionMod.getId_curso()), 15);
                    JTextField txtEstado = new JTextField(inscripcionMod.getEstado(), 15);
                    JTextField txtMotivoBaja = new JTextField(inscripcionMod.getMotivo_baja() != null ? inscripcionMod.getMotivo_baja() : "", 15);
                    JTextField txtImporte = new JTextField(String.valueOf(inscripcionMod.getImporte_cuota()), 15);

                    Object[] camposInscripcion = {
                        "ID Alumno:", txtIdAlumno,
                        "ID Curso:", txtIdCurso,
                        "Estado:", txtEstado,
                        "Motivo de Baja:", txtMotivoBaja,
                        "Importe de Cuota:", txtImporte
                    };

                    int option = JOptionPane.showConfirmDialog(this, camposInscripcion, "Modificar Inscripción", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            inscripcionMod.setId_alumno(Integer.parseInt(txtIdAlumno.getText().trim()));
                            inscripcionMod.setId_curso(Integer.parseInt(txtIdCurso.getText().trim()));
                            inscripcionMod.setEstado(txtEstado.getText().trim());
                            inscripcionMod.setMotivo_baja(txtMotivoBaja.getText().trim().isEmpty() ? null : txtMotivoBaja.getText().trim());
                            inscripcionMod.setImporte_cuota(Double.parseDouble(txtImporte.getText().trim()));

                            inscripcionDao.modificar(inscripcionMod);
                            JOptionPane.showMessageDialog(this, "Inscripción modificada.");
                            cargarDatos.run();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error al modificar inscripción: Verifique los tipos de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una inscripción.");
            }
        });

        btnEliminar.addActionListener(e -> {
            int filaSel = tabla.getSelectedRow();
            if (filaSel >= 0) {
                int id = (int) modelo.getValueAt(filaSel, 0);
                if(JOptionPane.showConfirmDialog(this, "¿Dar de baja inscripción de la base de datos?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    inscripcionDao.eliminar(id);
                    JOptionPane.showMessageDialog(this, "Inscripción eliminada.");
                    cargarDatos.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una inscripción.");
            }
        });

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private void abrirFormularioCrearUsuario() {
        // ventana modal para dar de alta nuevos usuarios sin cerrar el panel principal
        JDialog dialogo = new JDialog(this, "Crear Nuevo Usuario y Registro", true);
        dialogo.setSize(500, 500);
        dialogo.setLocationRelativeTo(this);

        JTabbedPane pestañas = new JTabbedPane();

        JPanel panelAlumno = new JPanel(new GridBagLayout());
        
        JTextField txtUserAlumno = new JTextField(15);
        JPasswordField txtPassAlumno = new JPasswordField(15);
        JTextField txtNombreAlumno = new JTextField(15);
        JTextField txtApellidoAlumno = new JTextField(15);
        JTextField txtDniAlumno = new JTextField(15);
        JTextField txtMatriculaAlumno = new JTextField(15);
        
        JTextField txtNombreTutor = new JTextField(15);
        JTextField txtApellidoTutor = new JTextField(15);
        JTextField txtDniTutor = new JTextField(15);
        JTextField txtRelacionTutor = new JTextField(15);

        // posicionamiento de cada elemento dentro de la cuadricula del formulario
        JLabel lblUserAlumno = new JLabel("Usuario Alumno:");
        GridBagConstraints gbc_lblUserAlumno = new GridBagConstraints();
        gbc_lblUserAlumno.gridx = 0; gbc_lblUserAlumno.gridy = 0;
        gbc_lblUserAlumno.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblUserAlumno, gbc_lblUserAlumno);

        GridBagConstraints gbc_txtUserAlumno = new GridBagConstraints();
        gbc_txtUserAlumno.gridx = 1; gbc_txtUserAlumno.gridy = 0;
        gbc_txtUserAlumno.insets = new Insets(4, 4, 4, 4);
        gbc_txtUserAlumno.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtUserAlumno, gbc_txtUserAlumno);

        JLabel lblPassAlumno = new JLabel("Contraseña Alumno:");
        GridBagConstraints gbc_lblPassAlumno = new GridBagConstraints();
        gbc_lblPassAlumno.gridx = 0; gbc_lblPassAlumno.gridy = 1;
        gbc_lblPassAlumno.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblPassAlumno, gbc_lblPassAlumno);

        GridBagConstraints gbc_txtPassAlumno = new GridBagConstraints();
        gbc_txtPassAlumno.gridx = 1; gbc_txtPassAlumno.gridy = 1;
        gbc_txtPassAlumno.insets = new Insets(4, 4, 4, 4);
        gbc_txtPassAlumno.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtPassAlumno, gbc_txtPassAlumno);

        JLabel lblNomAlumno = new JLabel("Nombre Alumno:");
        GridBagConstraints gbc_lblNomAlumno = new GridBagConstraints();
        gbc_lblNomAlumno.gridx = 0; gbc_lblNomAlumno.gridy = 2;
        gbc_lblNomAlumno.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblNomAlumno, gbc_lblNomAlumno);

        GridBagConstraints gbc_txtNomAlumno = new GridBagConstraints();
        gbc_txtNomAlumno.gridx = 1; gbc_txtNomAlumno.gridy = 2;
        gbc_txtNomAlumno.insets = new Insets(4, 4, 4, 4);
        gbc_txtNomAlumno.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtNombreAlumno, gbc_txtNomAlumno);

        JLabel lblApeAlumno = new JLabel("Apellido Alumno:");
        GridBagConstraints gbc_lblApeAlumno = new GridBagConstraints();
        gbc_lblApeAlumno.gridx = 0; gbc_lblApeAlumno.gridy = 3;
        gbc_lblApeAlumno.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblApeAlumno, gbc_lblApeAlumno);

        GridBagConstraints gbc_txtApeAlumno = new GridBagConstraints();
        gbc_txtApeAlumno.gridx = 1; gbc_txtApeAlumno.gridy = 3;
        gbc_txtApeAlumno.insets = new Insets(4, 4, 4, 4);
        gbc_txtApeAlumno.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtApellidoAlumno, gbc_txtApeAlumno);

        JLabel lblDniAlumno = new JLabel("DNI Alumno:");
        GridBagConstraints gbc_lblDniAlumno = new GridBagConstraints();
        gbc_lblDniAlumno.gridx = 0; gbc_lblDniAlumno.gridy = 4;
        gbc_lblDniAlumno.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblDniAlumno, gbc_lblDniAlumno);

        GridBagConstraints gbc_txtDniAlumno = new GridBagConstraints();
        gbc_txtDniAlumno.gridx = 1; gbc_txtDniAlumno.gridy = 4;
        gbc_txtDniAlumno.insets = new Insets(4, 4, 4, 4);
        gbc_txtDniAlumno.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtDniAlumno, gbc_txtDniAlumno);

        JLabel lblMatricula = new JLabel("Matrícula:");
        GridBagConstraints gbc_lblMatricula = new GridBagConstraints();
        gbc_lblMatricula.gridx = 0; gbc_lblMatricula.gridy = 5;
        gbc_lblMatricula.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblMatricula, gbc_lblMatricula);

        GridBagConstraints gbc_txtMatricula = new GridBagConstraints();
        gbc_txtMatricula.gridx = 1; gbc_txtMatricula.gridy = 5;
        gbc_txtMatricula.insets = new Insets(4, 4, 4, 4);
        gbc_txtMatricula.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtMatriculaAlumno, gbc_txtMatricula);

        JLabel lblTituloTutor = new JLabel("--- Datos Tutor / Responsable ---");
        GridBagConstraints gbc_lblTituloTutor = new GridBagConstraints();
        gbc_lblTituloTutor.gridx = 0; gbc_lblTituloTutor.gridy = 6;
        gbc_lblTituloTutor.gridwidth = 2; 
        gbc_lblTituloTutor.insets = new Insets(10, 4, 4, 4);
        panelAlumno.add(lblTituloTutor, gbc_lblTituloTutor);

        JLabel lblNomTutor = new JLabel("Nombre Tutor:");
        GridBagConstraints gbc_lblNomTutor = new GridBagConstraints();
        gbc_lblNomTutor.gridx = 0; gbc_lblNomTutor.gridy = 7;
        gbc_lblNomTutor.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblNomTutor, gbc_lblNomTutor);

        GridBagConstraints gbc_txtNomTutor = new GridBagConstraints();
        gbc_txtNomTutor.gridx = 1; gbc_txtNomTutor.gridy = 7;
        gbc_txtNomTutor.insets = new Insets(4, 4, 4, 4);
        gbc_txtNomTutor.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtNombreTutor, gbc_txtNomTutor);

        JLabel lblApeTutor = new JLabel("Apellido Tutor:");
        GridBagConstraints gbc_lblApeTutor = new GridBagConstraints();
        gbc_lblApeTutor.gridx = 0; gbc_lblApeTutor.gridy = 8;
        gbc_lblApeTutor.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblApeTutor, gbc_lblApeTutor);

        GridBagConstraints gbc_txtApeTutor = new GridBagConstraints();
        gbc_txtApeTutor.gridx = 1; gbc_txtApeTutor.gridy = 8;
        gbc_txtApeTutor.insets = new Insets(4, 4, 4, 4);
        gbc_txtApeTutor.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtApellidoTutor, gbc_txtApeTutor);

        JLabel lblDniTutor = new JLabel("DNI Tutor:");
        GridBagConstraints gbc_lblDniTutor = new GridBagConstraints();
        gbc_lblDniTutor.gridx = 0; gbc_lblDniTutor.gridy = 9;
        gbc_lblDniTutor.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblDniTutor, gbc_lblDniTutor);

        GridBagConstraints gbc_txtDniTutor = new GridBagConstraints();
        gbc_txtDniTutor.gridx = 1; gbc_txtDniTutor.gridy = 9;
        gbc_txtDniTutor.insets = new Insets(4, 4, 4, 4);
        gbc_txtDniTutor.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtDniTutor, gbc_txtDniTutor);

        JLabel lblRelacion = new JLabel("Relación (Ej. Padre):");
        GridBagConstraints gbc_lblRelacion = new GridBagConstraints();
        gbc_lblRelacion.gridx = 0; gbc_lblRelacion.gridy = 10;
        gbc_lblRelacion.insets = new Insets(4, 4, 4, 4);
        panelAlumno.add(lblRelacion, gbc_lblRelacion);

        GridBagConstraints gbc_txtRelacion = new GridBagConstraints();
        gbc_txtRelacion.gridx = 1; gbc_txtRelacion.gridy = 10;
        gbc_txtRelacion.insets = new Insets(4, 4, 4, 4);
        gbc_txtRelacion.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(txtRelacionTutor, gbc_txtRelacion);

        JButton btnGuardarAlumno = new JButton("Guardar Alumno");
        GridBagConstraints gbc_btnGuardarAlumno = new GridBagConstraints();
        gbc_btnGuardarAlumno.gridx = 0; gbc_btnGuardarAlumno.gridy = 11;
        gbc_btnGuardarAlumno.gridwidth = 2;
        gbc_btnGuardarAlumno.insets = new Insets(10, 4, 4, 4);
        gbc_btnGuardarAlumno.fill = GridBagConstraints.HORIZONTAL;
        panelAlumno.add(btnGuardarAlumno, gbc_btnGuardarAlumno);

        btnGuardarAlumno.addActionListener(e -> {
            try {
                // guarda secuencialmente el usuario, el responsable y el alumno obteniendo sus id correspondientes
                Usuario u = new Usuario(null, 3, txtUserAlumno.getText().trim(), new String(txtPassAlumno.getPassword()));
                usuarioDao.insertar(u);
                
                int idUsuarioNuevo = usuarioDao.obtenerTodos().stream().mapToInt(Usuario::getId_usuario).max().orElse(1);

                Responsable r = new Responsable(null, txtNombreTutor.getText().trim(), txtApellidoTutor.getText().trim(), txtDniTutor.getText().trim(), txtRelacionTutor.getText().trim());
                responsableDao.insertar(r);
                int idResponsableNuevo = responsableDao.obtenerTodos().stream().mapToInt(Responsable::getId_responsable).max().orElse(1);

                Alumno a = new Alumno(null, idUsuarioNuevo, idResponsableNuevo, txtNombreAlumno.getText().trim(), txtApellidoAlumno.getText().trim(), txtDniAlumno.getText().trim(), txtMatriculaAlumno.getText().trim(), "Activo");
                alumnoDao.insertar(a);

                JOptionPane.showMessageDialog(dialogo, "Alumno y tutor registrados exitosamente.");
                dialogo.dispose(); 
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error al registrar: " + ex.getMessage());
            }
        });

        JPanel panelDocente = new JPanel(new GridBagLayout());
        JTextField txtUserDocente = new JTextField(15);
        JPasswordField txtPassDocente = new JPasswordField(15);
        JTextField txtNombreDocente = new JTextField(15);
        JTextField txtApellidoDocente = new JTextField(15);
        JTextField txtDniDocente = new JTextField(15);
        JTextField txtLegajoDocente = new JTextField(15);

        JLabel lblUserDocente = new JLabel("Usuario Docente:");
        GridBagConstraints gbc_lblUserDocente = new GridBagConstraints();
        gbc_lblUserDocente.gridx = 0; gbc_lblUserDocente.gridy = 0;
        gbc_lblUserDocente.insets = new Insets(4, 4, 4, 4);
        panelDocente.add(lblUserDocente, gbc_lblUserDocente);

        GridBagConstraints gbc_txtUserDocente = new GridBagConstraints();
        gbc_txtUserDocente.gridx = 1; gbc_txtUserDocente.gridy = 0;
        gbc_txtUserDocente.insets = new Insets(4, 4, 4, 4);
        gbc_txtUserDocente.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(txtUserDocente, gbc_txtUserDocente);

        JLabel lblPassDocente = new JLabel("Contraseña Docente:");
        GridBagConstraints gbc_lblPassDocente = new GridBagConstraints();
        gbc_lblPassDocente.gridx = 0; gbc_lblPassDocente.gridy = 1;
        gbc_lblPassDocente.insets = new Insets(4, 4, 4, 4);
        panelDocente.add(lblPassDocente, gbc_lblPassDocente);

        GridBagConstraints gbc_txtPassDocente = new GridBagConstraints();
        gbc_txtPassDocente.gridx = 1; gbc_txtPassDocente.gridy = 1;
        gbc_txtPassDocente.insets = new Insets(4, 4, 4, 4);
        gbc_txtPassDocente.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(txtPassDocente, gbc_txtPassDocente);

        JLabel lblNomDocente = new JLabel("Nombre Docente:");
        GridBagConstraints gbc_lblNomDocente = new GridBagConstraints();
        gbc_lblNomDocente.gridx = 0; gbc_lblNomDocente.gridy = 2;
        gbc_lblNomDocente.insets = new Insets(4, 4, 4, 4);
        panelDocente.add(lblNomDocente, gbc_lblNomDocente);

        GridBagConstraints gbc_txtNomDocente = new GridBagConstraints();
        gbc_txtNomDocente.gridx = 1; gbc_txtNomDocente.gridy = 2;
        gbc_txtNomDocente.insets = new Insets(4, 4, 4, 4);
        gbc_txtNomDocente.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(txtNombreDocente, gbc_txtNomDocente);

        JLabel lblApeDocente = new JLabel("Apellido Docente:");
        GridBagConstraints gbc_lblApeDocente = new GridBagConstraints();
        gbc_lblApeDocente.gridx = 0; gbc_lblApeDocente.gridy = 3;
        gbc_lblApeDocente.insets = new Insets(4, 4, 4, 4);
        panelDocente.add(lblApeDocente, gbc_lblApeDocente);

        GridBagConstraints gbc_txtApeDocente = new GridBagConstraints();
        gbc_txtApeDocente.gridx = 1; gbc_txtApeDocente.gridy = 3;
        gbc_txtApeDocente.insets = new Insets(4, 4, 4, 4);
        gbc_txtApeDocente.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(txtApellidoDocente, gbc_txtApeDocente);

        JLabel lblDniDocente = new JLabel("DNI Docente:");
        GridBagConstraints gbc_lblDniDocente = new GridBagConstraints();
        gbc_lblDniDocente.gridx = 0; gbc_lblDniDocente.gridy = 4;
        gbc_lblDniDocente.insets = new Insets(4, 4, 4, 4);
        panelDocente.add(lblDniDocente, gbc_lblDniDocente);

        GridBagConstraints gbc_txtDniDocente = new GridBagConstraints();
        gbc_txtDniDocente.gridx = 1; gbc_txtDniDocente.gridy = 4;
        gbc_txtDniDocente.insets = new Insets(4, 4, 4, 4);
        gbc_txtDniDocente.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(txtDniDocente, gbc_txtDniDocente);

        JLabel lblLegajo = new JLabel("Legajo:");
        GridBagConstraints gbc_lblLegajo = new GridBagConstraints();
        gbc_lblLegajo.gridx = 0; gbc_lblLegajo.gridy = 5;
        gbc_lblLegajo.insets = new Insets(4, 4, 4, 4);
        panelDocente.add(lblLegajo, gbc_lblLegajo);

        GridBagConstraints gbc_txtLegajo = new GridBagConstraints();
        gbc_txtLegajo.gridx = 1; gbc_txtLegajo.gridy = 5;
        gbc_txtLegajo.insets = new Insets(4, 4, 4, 4);
        gbc_txtLegajo.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(txtLegajoDocente, gbc_txtLegajo);

        JButton btnGuardarDocente = new JButton("Guardar Docente");
        GridBagConstraints gbc_btnGuardarDocente = new GridBagConstraints();
        gbc_btnGuardarDocente.gridx = 0; gbc_btnGuardarDocente.gridy = 6;
        gbc_btnGuardarDocente.gridwidth = 2;
        gbc_btnGuardarDocente.insets = new Insets(10, 4, 4, 4);
        gbc_btnGuardarDocente.fill = GridBagConstraints.HORIZONTAL;
        panelDocente.add(btnGuardarDocente, gbc_btnGuardarDocente);

        btnGuardarDocente.addActionListener(e -> {
            try {
                Usuario u = new Usuario(null, 2, txtUserDocente.getText().trim(), new String(txtPassDocente.getPassword()));
                usuarioDao.insertar(u);
                int idUsuarioNuevo = usuarioDao.obtenerTodos().stream().mapToInt(Usuario::getId_usuario).max().orElse(1);

                Docente d = new Docente(null, idUsuarioNuevo, txtNombreDocente.getText().trim(), txtApellidoDocente.getText().trim(), txtDniDocente.getText().trim(), txtLegajoDocente.getText().trim(), "Activo");
                docenteDao.insertar(d);

                JOptionPane.showMessageDialog(dialogo, "Docente registrado exitosamente.");
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error al registrar docente: " + ex.getMessage());
            }
        });

        pestañas.addTab("Alumnos", panelAlumno);
        pestañas.addTab("Docentes", panelDocente);

        dialogo.add(pestañas);
        dialogo.setVisible(true);
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true); 
            this.dispose(); 
        }
    }
}