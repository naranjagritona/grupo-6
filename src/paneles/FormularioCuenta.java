package paneles;

import clases.Rol;
import servicios.Admin_servicio.SolicitudCuenta;
import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;

// mostramos los campos de la cuenta y los datos del responsable según el rol elegido
public class FormularioCuenta extends JPanel {
    private static final long serialVersionUID=1L;
    private final JComboBox<Rol> rol=new JComboBox<>();
    private final JTextField usuario=new JTextField(),mail=new JTextField(),nombre=new JTextField(),apellido=new JTextField(),dni=new JTextField();
    private final JPasswordField clave=new JPasswordField();
    private final JTextField responsableNombre=new JTextField(),responsableApellido=new JTextField(),responsableDni=new JTextField(),relacion=new JTextField();
    private final JComboBox<String> estado=new JComboBox<>(new String[]{"Activo","Inactivo"});
    private final JPanel datos=new JPanel(new BorderLayout(0,12)),responsable=new JPanel(new GridLayout(0,2,12,10));
    private final JLabel ayuda=new JLabel("Seleccioná un rol para completar los datos de la cuenta.");
    private final boolean edicion;
    private int id;
    private Integer rolOriginal;

    public FormularioCuenta(boolean edicion){
        super(new BorderLayout(0,12));this.edicion=edicion;setOpaque(false);
        rol.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){super.getListCellRendererComponent(l,v,i,s,f);setText(v instanceof Rol r?r.getNombre():"Seleccioná un rol…");return this;}});
        JPanel elegir=new JPanel(new BorderLayout(0,6));elegir.setOpaque(false);JLabel etiqueta=new JLabel("Rol de la cuenta");etiqueta.setLabelFor(rol);elegir.add(etiqueta,BorderLayout.NORTH);elegir.add(rol,BorderLayout.CENTER);elegir.add(ayuda,BorderLayout.SOUTH);add(elegir,BorderLayout.NORTH);
        JPanel comunes=new JPanel(new GridLayout(0,2,12,10));comunes.setOpaque(false);
        campo(comunes,"Usuario",usuario);campo(comunes,"Mail",mail);if(!edicion)campo(comunes,"Contraseña",clave);
        campo(comunes,"Nombre",nombre);campo(comunes,"Apellido",apellido);campo(comunes,"DNI",dni);if(edicion)campo(comunes,"Estado",estado);
        datos.setOpaque(false);datos.add(comunes,BorderLayout.NORTH);responsable.setOpaque(false);responsable.setBorder(BorderFactory.createTitledBorder("Responsable del alumno"));
        campo(responsable,"Nombre del responsable",responsableNombre);campo(responsable,"Apellido del responsable",responsableApellido);campo(responsable,"DNI del responsable",responsableDni);campo(responsable,"Relación con el alumno",relacion);datos.add(responsable,BorderLayout.CENTER);
        JLabel numeros=new JLabel("La matrícula y el legajo se asignan automáticamente.");numeros.setForeground(new Color(100,116,139));datos.add(numeros,BorderLayout.SOUTH);add(datos,BorderLayout.CENTER);datos.setVisible(false);
        rol.addActionListener(e->mostrarPerfil());
    }

    // asociamos cada etiqueta a su entrada para facilitar el uso con teclado y lectores de pantalla
    private void campo(JPanel contenedor,String titulo,JComponent entrada){JPanel p=new JPanel(new BorderLayout(0,5));p.setOpaque(false);JLabel l=new JLabel(titulo);l.setFont(new Font("Tahoma",Font.PLAIN,13));entrada.setFont(new Font("Tahoma",Font.PLAIN,13));l.setLabelFor(entrada);p.add(l,BorderLayout.NORTH);entrada.setPreferredSize(new Dimension(200,30));entrada.getAccessibleContext().setAccessibleName(titulo);p.add(entrada,BorderLayout.CENTER);contenedor.add(p);}

    public void roles(List<Rol> disponibles){Rol anterior=(Rol)rol.getSelectedItem();rol.removeAllItems();rol.addItem(null);for(Rol r:disponibles)rol.addItem(r);if(anterior!=null)seleccionarRol(anterior.getId_rol());}
    private void seleccionarRol(int id){for(int i=0;i<rol.getItemCount();i++)if(rol.getItemAt(i)!=null&&rol.getItemAt(i).getId_rol()==id){rol.setSelectedIndex(i);break;}}
    private void mostrarPerfil(){Rol r=(Rol)rol.getSelectedItem();datos.setVisible(r!=null);responsable.setVisible(r!=null&&r.getId_rol()==Rol.ALUMNO);ayuda.setText(r==null?"Seleccioná un rol para completar los datos de la cuenta.":r.getId_rol()==Rol.ALUMNO?"Completá también los datos del responsable del alumno.":"Completá los datos de la cuenta y de la persona.");revalidate();repaint();}
    public boolean rolSeleccionado(){return rol.getSelectedItem()!=null;}
    public void alCambiarRol(Runnable cambio){rol.addActionListener(e->cambio.run());}

    // cargamos los datos actuales sin ofrecer campos editables para los números automáticos
    @SuppressWarnings("unchecked") public void cargar(Map<String,Object> cuenta,Integer actor){
        id=((Number)cuenta.get("id_usuario")).intValue();rolOriginal=((Number)cuenta.get("id_rol")).intValue();seleccionarRol(rolOriginal);
        usuario.setText(texto(cuenta,"nombre_usuario"));mail.setText(texto(cuenta,"mail"));Map<String,Object> p=(Map<String,Object>)cuenta.get("persona");
        nombre.setText(texto(p,"nombre"));apellido.setText(texto(p,"apellido"));dni.setText(texto(p,"dni"));estado.setSelectedItem(p.getOrDefault("estado","Activo"));
        Map<String,Object> r=(Map<String,Object>)cuenta.get("responsable");responsableNombre.setText(texto(r,"nombre"));responsableApellido.setText(texto(r,"apellido"));responsableDni.setText(texto(r,"dni"));relacion.setText(texto(r,"tipo_relacion"));
        if(java.util.Objects.equals(actor,id)){rol.setEnabled(false);estado.setEnabled(false);}mostrarPerfil();
    }
    private String texto(Map<String,Object> mapa,String campo){return mapa.get(campo)==null?"":mapa.get(campo).toString();}
    public SolicitudCuenta solicitud(){Rol r=(Rol)rol.getSelectedItem();return new SolicitudCuenta(id,r==null?0:r.getId_rol(),usuario.getText(),new String(clave.getPassword()),mail.getText(),nombre.getText(),apellido.getText(),dni.getText(),edicion?estado.getSelectedItem().toString():"Activo",responsableNombre.getText(),responsableApellido.getText(),responsableDni.getText(),relacion.getText(),rolOriginal);}
    public void limpiar(){for(JTextField t:List.of(usuario,mail,nombre,apellido,dni,responsableNombre,responsableApellido,responsableDni,relacion))t.setText("");clave.setText("");rol.setSelectedItem(null);}
}
