import clases.Usuario;
import servicios.*;
import paneles.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

public class PruebaPantallas {
 static Object campo(Object objeto,String nombre)throws Exception{var f=objeto.getClass().getDeclaredField(nombre);f.setAccessible(true);return f.get(objeto);}
 static void esperar(java.util.concurrent.Callable<Boolean> condicion)throws Exception{long fin=System.currentTimeMillis()+25000;while(System.currentTimeMillis()<fin){boolean[] listo={false};SwingUtilities.invokeAndWait(()->{try{listo[0]=condicion.call();}catch(Exception e){throw new RuntimeException(e);}});if(listo[0])return;Thread.sleep(100);}throw new AssertionError("La pantalla no terminó de cargar");}
 static void layout(Component componente){if(componente instanceof Container c){c.doLayout();for(Component hijo:c.getComponents())layout(hijo);}}
 static void guardar(JPanel panel,Path destino)throws Exception{SwingUtilities.invokeAndWait(()->{try{panel.setSize(1100,700);layout(panel);layout(panel);BufferedImage imagen=new BufferedImage(1100,700,BufferedImage.TYPE_INT_RGB);Graphics2D g=imagen.createGraphics();panel.printAll(g);g.dispose();ImageIO.write(imagen,"png",destino.toFile());}catch(Exception e){throw new RuntimeException(e);}});}
 public static void main(String[] args)throws Exception{
  System.setProperty("java.awt.headless","true");Usuario usuario=new Login_servicio().autenticarUsuario("admin","123");
  Cursos_panel[] cursos={null};SwingUtilities.invokeAndWait(()->cursos[0]=new Cursos_panel(usuario));
  JTable tablaCursos=(JTable)campo(cursos[0],"tabla");esperar(()->tablaCursos.getRowCount()==6);
  for(int i=0;i<tablaCursos.getColumnCount();i++)if(tablaCursos.getColumnName(i).equalsIgnoreCase("División"))throw new AssertionError("Sigue la división");
  javax.swing.JTextField[] buscador={null};
  java.util.function.Consumer<Component> encontrar=new java.util.function.Consumer<>(){public void accept(Component c){if(c instanceof javax.swing.JTextField f)buscador[0]=f;if(c instanceof Container padre)for(Component hijo:padre.getComponents())accept(hijo);}};
  encontrar.accept(cursos[0]);SwingUtilities.invokeAndWait(()->buscador[0].setText("NO.COINCIDE["));
  if(tablaCursos.getRowCount()!=0)throw new AssertionError("No filtra la búsqueda literal");
  SwingUtilities.invokeAndWait(()->buscador[0].setText(""));
  if(tablaCursos.getRowCount()!=6)throw new AssertionError("No recupera el listado");
  guardar(cursos[0],Path.of(args[0],"cursos-java.png"));System.out.println("OK: tabla de seis cursos, materias y búsqueda");
  Facturacion_panel[] facturacion={null};SwingUtilities.invokeAndWait(()->facturacion[0]=new Facturacion_panel(usuario));
  JTable cuotas=(JTable)campo(facturacion[0],"tc"),historial=(JTable)campo(facturacion[0],"th");esperar(()->cuotas.getRowCount()>=61);
  SwingUtilities.invokeAndWait(()->{for(int i=0;i<cuotas.getRowCount();i++)if(cuotas.getValueAt(i,0).toString().contains("Sofía")){cuotas.setRowSelectionInterval(i,i);break;}});
  esperar(()->historial.getRowCount()>0);guardar(facturacion[0],Path.of(args[0],"facturacion-java.png"));
  System.out.println("OK: panel de facturación carga cuotas e historial de la web");
  Facturacion_servicio.cerrarSesion();System.exit(0);
 }
}
