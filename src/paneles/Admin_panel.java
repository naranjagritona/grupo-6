package paneles;

import clases.Rol;
import clases.Usuario;
import servicios.Admin_servicio;
import servicios.Permisos_servicio;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

// mostramos el alta por rol, las cuentas administrables y la auditoría exclusiva de dirección
public class Admin_panel extends JPanel {
    private static final long serialVersionUID=1L;
    private final Usuario usuario;
    private final Admin_servicio servicio;
    private final FormularioCuenta alta=new FormularioCuenta(false);
    private final JButton guardar=Ficha_Inscripcion_dialog.boton("Registrar cuenta"),modificar=Ficha_Inscripcion_dialog.boton("Modificar cuenta");
    private final JLabel estado=new JLabel("Cargando administración…");
    private final JLabel[] totales=new JLabel[5];
    private final DefaultTableModel modelo=soloLectura("ID","Usuario","Nombre","Mail","Rol","Estado","Matrícula / legajo");
    private final JTable tabla=new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> orden=new TableRowSorter<>(modelo);
    private final JTextField buscar=new JTextField();
    private final JComboBox<Rol> filtroRol=new JComboBox<>();
    private final DefaultTableModel modeloLogs=soloLectura("Fecha","Usuario","Rol","Acción","Módulo","Detalle");
    private final JTabbedPane secciones=new JTabbedPane();
    private List<Map<String,Object>> cuentas=List.of();
    private List<Rol> roles=List.of();
    private int rolActor;
    private boolean cargando;

    public Admin_panel(){this(Permisos_servicio.actual());}
    public Admin_panel(Usuario usuario){this(usuario,new Admin_servicio());}
    public Admin_panel(Usuario usuario,Admin_servicio servicio){
        super(new BorderLayout(0,12));if(!Permisos_servicio.gestion(usuario))throw new IllegalArgumentException("No tenés acceso a Administración.");
        this.usuario=usuario;this.servicio=servicio;rolActor=usuario.getId_rol();setBackground(Color.WHITE);setBorder(new EmptyBorder(18,20,18,20));
        JPanel encabezado=new JPanel(new BorderLayout(0,7));encabezado.setOpaque(false);JLabel titulo=new JLabel("Administración");titulo.setFont(new Font("Tahoma",Font.BOLD,24));encabezado.add(titulo,BorderLayout.NORTH);encabezado.add(estado,BorderLayout.SOUTH);add(encabezado,BorderLayout.NORTH);
        JPanel contenido=new JPanel();contenido.setLayout(new BoxLayout(contenido,BoxLayout.Y_AXIS));contenido.setBackground(Color.WHITE);
        JPanel tarjetas=new JPanel(new GridLayout(1,5,12,0));tarjetas.setBackground(Color.WHITE);String[] nombres={"ALUMNOS TOTALES","DOCENTES TOTALES","CURSOS ACTIVOS","INSCRIPCIONES","CUENTAS DE USUARIOS"};for(int i=0;i<5;i++){totales[i]=new JLabel("—",SwingConstants.CENTER);tarjetas.add(crearTarjetaEstadistica(nombres[i],totales[i]));}tarjetas.setPreferredSize(new Dimension(0,85));tarjetas.setMaximumSize(new Dimension(Integer.MAX_VALUE,85));contenido.add(tarjetas);contenido.add(Box.createVerticalStrut(15));
        JPanel registro=new JPanel(new BorderLayout(0,10));registro.setBackground(Color.WHITE);registro.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Crear cuenta"),new EmptyBorder(12,12,12,12)));registro.add(alta,BorderLayout.CENTER);JPanel pie=new JPanel(new FlowLayout(FlowLayout.RIGHT));pie.setOpaque(false);guardar.setEnabled(false);pie.add(guardar);registro.add(pie,BorderLayout.SOUTH);registro.setAlignmentX(LEFT_ALIGNMENT);contenido.add(registro);contenido.add(Box.createVerticalStrut(15));
        JPanel cuentasPanel=new JPanel(new BorderLayout(0,10));cuentasPanel.setBackground(Color.WHITE);JPanel filtros=new JPanel(new BorderLayout(10,0));filtros.setOpaque(false);JLabel etiqueta=new JLabel("Buscar usuario");etiqueta.setLabelFor(buscar);filtros.add(etiqueta,BorderLayout.WEST);filtros.add(buscar,BorderLayout.CENTER);filtros.add(filtroRol,BorderLayout.EAST);cuentasPanel.add(filtros,BorderLayout.NORTH);
        tabla.setRowSorter(orden);tabla.setRowHeight(28);tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);JScrollPane scroll=new JScrollPane(tabla);scroll.setPreferredSize(new Dimension(850,240));cuentasPanel.add(scroll,BorderLayout.CENTER);JPanel acciones=new JPanel(new FlowLayout(FlowLayout.RIGHT));acciones.setOpaque(false);modificar.setEnabled(false);acciones.add(modificar);JButton reintentar=Ficha_Inscripcion_dialog.boton("Reintentar avisos");acciones.add(reintentar);reintentar.addActionListener(e->{reintentar.setEnabled(false);new SwingWorker<String,Void>(){protected String doInBackground()throws Exception{return new servicios.Notificaciones_servicio().reintentar(usuario.getId_usuario());}protected void done(){reintentar.setEnabled(true);try{JOptionPane.showMessageDialog(Admin_panel.this,get());}catch(Exception error){mostrarError(error);}}}.execute();});cuentasPanel.add(acciones,BorderLayout.SOUTH);secciones.addTab("Cuentas de usuarios",cuentasPanel);
        if(Permisos_servicio.verLogs(usuario)){JTable logs=new JTable(modeloLogs);logs.setRowHeight(27);JScrollPane auditoria=new JScrollPane(logs);auditoria.setPreferredSize(new Dimension(850,280));secciones.addTab("Actividad del sistema",auditoria);}
        contenido.add(secciones);JScrollPane pagina=new JScrollPane(contenido);pagina.setBorder(null);pagina.getVerticalScrollBar().setUnitIncrement(18);add(pagina,BorderLayout.CENTER);
        filtroRol.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){super.getListCellRendererComponent(l,v,i,s,f);setText(v instanceof Rol r?r.getNombre():"Todos los roles");return this;}});
        buscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){filtrar();}public void removeUpdate(javax.swing.event.DocumentEvent e){filtrar();}public void changedUpdate(javax.swing.event.DocumentEvent e){filtrar();}});filtroRol.addActionListener(e->filtrar());alta.alCambiarRol(()->guardar.setEnabled(!cargando&&alta.rolSeleccionado()));guardar.addActionListener(e->registrar());modificar.addActionListener(e->editar());tabla.getSelectionModel().addListSelectionListener(e->habilitarEdicion());tabla.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2&&tabla.rowAtPoint(e.getPoint())>=0)editar();}});
        cargar();
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

    private static DefaultTableModel soloLectura(String... columnas){return new DefaultTableModel(columnas,0){public boolean isCellEditable(int fila,int columna){return false;}};}
    // la consulta se ejecuta fuera del hilo de Swing para mantener disponible el formulario
    private void cargar(){cargando=true;guardar.setEnabled(false);new SwingWorker<Admin_servicio.Datos,Void>(){protected Admin_servicio.Datos doInBackground()throws Exception{return servicio.cargar(usuario.getId_usuario());}protected void done(){cargando=false;try{var d=get();rolActor=d.rolActor();cuentas=d.cuentas();roles=d.roles();for(int i=0;i<totales.length;i++)totales[i].setText(String.valueOf(d.totales()[i]));alta.roles(roles);modelo.setRowCount(0);for(var c:cuentas)modelo.addRow(new Object[]{c.get("id_usuario"),c.get("nombre_usuario"),String.valueOf(c.get("nombre"))+" "+String.valueOf(c.get("apellido")),c.get("mail"),c.get("rol"),c.get("estado"),c.get("numero")});filtroRol.removeAllItems();filtroRol.addItem(null);Map<Integer,Rol> disponibles=new TreeMap<>();for(var c:cuentas){int id=((Number)c.get("id_rol")).intValue();disponibles.put(id,new Rol(id,String.valueOf(c.get("rol")),""));}for(Rol r:disponibles.values())filtroRol.addItem(r);modeloLogs.setRowCount(0);for(var l:d.logs())modeloLogs.addRow(new Object[]{l.get("fecha_hora"),l.get("usuario"),l.get("rol"),l.get("accion"),l.get("campo_modificado"),l.get("detalle")});if(rolActor!=Rol.DIRECTIVO&&secciones.getTabCount()>1)secciones.removeTabAt(1);estado.setText(rolActor==Rol.DIRECTIVO?"Dirección: administración completa y acceso a la actividad.":"Secretaría: altas y gestión de alumnos, docentes y cooperadora. Sin acceso a logs.");filtrar();}catch(Exception e){mostrarError(e);estado.setText("No se pudo cargar Administración.");}guardar.setEnabled(alta.rolSeleccionado());}}.execute();}

    // filtramos los datos ya cargados por texto y por rol sin consultar la base en cada letra
    private void filtrar(){if(modelo==null)return;String texto=buscar.getText().trim().toLowerCase(Locale.ROOT);Rol elegido=(Rol)filtroRol.getSelectedItem();orden.setRowFilter(new RowFilter<>(){public boolean include(Entry<? extends DefaultTableModel,? extends Integer> e){Object id=e.getValue(0);Map<String,Object> cuenta=cuentas.stream().filter(c->Objects.equals(c.get("id_usuario"),id)).findFirst().orElse(Map.of());if(elegido!=null&&((Number)cuenta.getOrDefault("id_rol",0)).intValue()!=elegido.getId_rol())return false;String campos=(cuenta.values().toString()).toLowerCase(Locale.ROOT);return texto.isEmpty()||campos.contains(texto);}});habilitarEdicion();}
    private Map<String,Object> seleccionado(){int fila=tabla.getSelectedRow();if(fila<0)return null;int id=((Number)modelo.getValueAt(tabla.convertRowIndexToModel(fila),0)).intValue();return cuentas.stream().filter(c->((Number)c.get("id_usuario")).intValue()==id).findFirst().orElse(null);}
    private void habilitarEdicion(){var c=seleccionado();modificar.setEnabled(c!=null&&Permisos_servicio.administrarCuenta(rolActor,((Number)c.get("id_rol")).intValue()));}

    // bloqueamos el botón mientras se guarda la cuenta y mostramos por separado el resultado del correo
    private void registrar(){if(cargando)return;var datos=alta.solicitud();cargando=true;guardar.setEnabled(false);estado.setText("Guardando cuenta y preparando el aviso por mail…");new SwingWorker<Admin_servicio.Alta,Void>(){protected Admin_servicio.Alta doInBackground()throws Exception{return servicio.registrar(datos,usuario.getId_usuario());}protected void done(){cargando=false;try{var r=get();alta.limpiar();JOptionPane.showMessageDialog(Admin_panel.this,"Cuenta registrada. Identificador: "+r.numero()+".\n"+r.correo().detalle());cargar();}catch(Exception e){mostrarError(e);guardar.setEnabled(alta.rolSeleccionado());estado.setText("Revisá los datos del formulario.");}}}.execute();}

    // el servicio vuelve a comprobar los permisos y el rol original antes de modificar una cuenta
    private void editar(){var c=seleccionado();if(c==null)return;if(!Permisos_servicio.administrarCuenta(rolActor,((Number)c.get("id_rol")).intValue())){JOptionPane.showMessageDialog(this,"No tenés permiso para modificar esa cuenta.");return;}int id=((Number)c.get("id_usuario")).intValue();modificar.setEnabled(false);new SwingWorker<Map<String,Object>,Void>(){protected Map<String,Object> doInBackground()throws Exception{return servicio.detalle(id,usuario.getId_usuario());}protected void done(){habilitarEdicion();try{abrirEdicion(get());}catch(Exception e){mostrarError(e);}}}.execute();}
    private void abrirEdicion(Map<String,Object> cuenta){if(!Boolean.TRUE.equals(cuenta.get("editable")))return;JDialog dialogo=new JDialog(SwingUtilities.getWindowAncestor(this),"Modificar cuenta",Dialog.ModalityType.APPLICATION_MODAL);FormularioCuenta form=new FormularioCuenta(true);form.roles(roles);form.cargar(cuenta,usuario.getId_usuario());form.setBorder(new EmptyBorder(16,16,16,16));dialogo.add(new JScrollPane(form),BorderLayout.CENTER);JPanel acciones=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton aceptar=Ficha_Inscripcion_dialog.boton("Guardar cambios"),cancelar=Ficha_Inscripcion_dialog.boton("Cancelar");acciones.add(aceptar);acciones.add(cancelar);dialogo.add(acciones,BorderLayout.SOUTH);cancelar.addActionListener(e->dialogo.dispose());aceptar.addActionListener(e->{var solicitud=form.solicitud();aceptar.setEnabled(false);new SwingWorker<Void,Void>(){protected Void doInBackground()throws Exception{servicio.modificar(solicitud,usuario.getId_usuario());return null;}protected void done(){aceptar.setEnabled(true);try{get();dialogo.dispose();cargar();}catch(Exception error){mostrarError(error);}}}.execute();});dialogo.setSize(650,600);dialogo.setLocationRelativeTo(this);dialogo.setVisible(true);}
    private void mostrarError(Exception e){Throwable causa=e.getCause()==null?e:e.getCause();JOptionPane.showMessageDialog(this,causa.getMessage(),"Administración",JOptionPane.WARNING_MESSAGE);}
}
