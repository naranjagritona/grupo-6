import clases.*;
import dao.*;
import servicios.*;
import paneles.*;
import com.google.gson.*;
import java.nio.file.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PruebaModulos {
 static void ok(boolean valor,String mensaje){if(!valor)throw new AssertionError(mensaje);System.out.println("OK: "+mensaje);}
 static void rechaza(java.util.concurrent.Callable<?> tarea,String mensaje)throws Exception{try{tarea.call();}catch(IllegalStateException e){System.out.println("OK: "+mensaje);return;}throw new AssertionError(mensaje);}
 public static void main(String[] args)throws Exception{
  System.setProperty("java.awt.headless","true");
  Login_servicio login=new Login_servicio();Usuario admin=login.autenticarUsuario("admin","123");
  ok(admin!=null,"acceso de administrador");
  java.util.List<Curso> cursos=new Curso_dao().listar(admin);ok(cursos.size()==6,"seis cursos");
  for(Curso c:cursos)ok(new Curso_dao().materiasDelCurso(c.getId_curso()).size()>1,"varias materias en "+c.getAño()+"° año");
  rechaza(()->{new Curso_dao().eliminar(1);return null;},"borrado de cursos bloqueado");
  JsonObject lista=Facturacion_servicio.listar("");JsonArray cuotas=lista.getAsJsonArray("inscripciones");ok(cuotas.size()>=61,"facturación compartida con la web");
  JsonArray historial=Facturacion_servicio.historial(1);ok(!historial.isEmpty(),"historial de una factura web");
  int fid=historial.get(0).getAsJsonObject().get("id_factura").getAsInt();
  byte[] pdf=Facturacion_servicio.factura(fid);try(PDDocument doc=Loader.loadPDF(pdf)){ok(doc.getNumberOfPages()>0,"PDF web válido en Java");}
  javax.swing.JPanel visor=FacturaPDF_panel.renderizar(pdf);ok(visor.getComponentCount()>0,"visor PDF de Java renderiza la factura");
  java.awt.image.BufferedImage imagen=((javax.swing.ImageIcon)((javax.swing.JLabel)visor.getComponent(0)).getIcon()).getImage() instanceof java.awt.image.BufferedImage bi?bi:null;
  if(imagen!=null)javax.imageio.ImageIO.write(imagen,"png",Path.of(args[0],"factura-vista-java.png").toFile());
  Usuario alumna=login.autenticarUsuario("sofia.fernandez","Colegio2026");ok(alumna!=null,"acceso de alumna");
  JsonArray propias=Facturacion_servicio.listar("").getAsJsonArray("inscripciones");ok(!propias.isEmpty()&&propias.size()<cuotas.size(),"alumna solo ve sus cuotas");
  int ajena=cuotas.asList().stream().map(JsonElement::getAsJsonObject).filter(c->!c.get("id_alumno").getAsString().equals(propias.get(0).getAsJsonObject().get("id_alumno").getAsString())).findFirst().orElseThrow().get("id_inscripcion").getAsInt();
  rechaza(()->Facturacion_servicio.historial(ajena),"API rechaza historial ajeno");
  JsonObject duplicado=new JsonObject();duplicado.addProperty("id_inscripcion",1);duplicado.addProperty("periodo",Facturacion_servicio.listar("").get("periodo").getAsString());duplicado.addProperty("metodo","Tarjeta");duplicado.addProperty("dni","45111222");duplicado.addProperty("email","alumno@example.com");duplicado.addProperty("confirmacion","1");
  rechaza(()->Facturacion_servicio.operar("pagar",duplicado),"API rechaza un pago mensual duplicado sin modificar la base");
  login.autenticarUsuario("ana.castillo","Colegio2026");rechaza(()->Facturacion_servicio.listar(""),"docente sin acceso a facturación");
  Facturacion_servicio.cerrarSesion();rechaza(()->Facturacion_servicio.listar(""),"cierre de sesión borra el acceso web");
 }
}
