import clases.*;
import dao.*;
import servicios.*;
import java.sql.*;
import java.util.*;
import java.lang.reflect.Proxy;

// ejercitamos el servicio de cuentas con fallos controlados sin dar de alta personas reales
public class CuentasJava {
    static int pruebas;
    static void comprobar(boolean valor,String texto){pruebas++;if(!valor)throw new AssertionError(texto);}
    interface Accion{void ejecutar()throws Exception;}
    static void denegado(Accion a)throws Exception{try{a.ejecutar();throw new AssertionError("Debía rechazarse");}catch(IllegalArgumentException e){pruebas++;}}
    static class Usuarios extends Usuario_dao {
        int rol=Rol.SECRETARIA,rolDestino=Rol.ALUMNO,altas,modificaciones,commits,rollbacks;boolean duplicada;
        protected Connection abrirConexion(){int antes=altas,mods=modificaciones;return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(p,m,a)->{switch(m.getName()){
            case "commit":commits++;return null;
            case "rollback":rollbacks++;altas=antes;modificaciones=mods;return null;
            case "close","setAutoCommit":return null;
            default:throw new UnsupportedOperationException(m.getName());
        }});}
        public int obtenerRol(Connection db,int actor){return rol;}
        public void reservarAlta(Connection db){}
        public boolean nombreOMailOcupado(Connection db,String nombre,String mail,int excepto){return duplicada;}
        public int crear(Connection db,Usuario u){comprobar(u.getContraseña().startsWith("$2"),"Contraseña protegida");altas++;return 777;}
        public Map<String,Object> bloquear(Connection db,int id){return Map.of("id_usuario",id,"id_rol",rolDestino,"nombre_usuario","existente","mail","prueba@example.com");}
        public Map<String,Object> perfil(Connection db,int id,int rol){return Map.of();}
        public void inactivarPerfil(Connection db,int id,int rol){modificaciones++;}
        public void actualizarCuenta(Connection db,Usuario u){modificaciones++;}
    }
    static class Avisos extends Notificaciones_servicio {
        final Usuarios usuarios;int enviados;
        Avisos(Usuarios u){usuarios=u;}
        public Integer alta(Connection db,int id,String nombre,String cuenta,int actor){comprobar(usuarios.commits==0,"El aviso se prepara antes del commit");return 11;}
        public Resultado enviar(Integer id){if(id!=null){comprobar(usuarios.commits==1,"El envío ocurre después del commit");enviados++;}return new Resultado(id!=null,id,"Prueba");}
    }
    static Admin_servicio.SolicitudCuenta datos(int id,int rol,Integer original){return new Admin_servicio.SolicitudCuenta(id,rol,"cuenta.prueba","clavePrueba8","prueba@example.com","Nombre","Apellido","12345678","Activo","Nombre responsable","Apellido responsable","12345679","Madre",original);}
    static class Entorno {
        Usuarios usuarios=new Usuarios();Avisos avisos=new Avisos(usuarios);boolean fallaPerfil;
        Admin_servicio servicio=new Admin_servicio(usuarios,new Alumno_dao(){public int crear(Connection c,Alumno a){if(fallaPerfil)throw new IllegalArgumentException("Falla del perfil");comprobar(a.getMatricula().endsWith("-U777"),"Matrícula automática");return 123;}},new Docente_dao(){public int crear(Connection c,Docente d){comprobar(d.getLegajo().equals("DOC-U777"),"Legajo automático");return 124;}},new Responsable_dao(){public int crear(Connection c,Responsable r){return 125;}},new Personal_dao(){public String crear(Connection c,Personal p,int id){return p.getTipo().prefijo+"-U"+id;}},new Rol_dao(){public List<Rol> listar(Connection c){return java.util.stream.IntStream.rangeClosed(1,5).mapToObj(r->new Rol(r,"Rol "+r,"")).toList();}},new ConsultaAdmin_dao(),avisos);
    }
    public static void main(String[] args)throws Exception{
        Auditoria_servicio.iniciar(900001);
        for(int rol:List.of(3,4,5)){Entorno e=new Entorno();e.servicio.registrar(datos(0,rol,null),900001);comprobar(e.usuarios.altas==1&&e.usuarios.commits==1,"Alta del perfil "+rol);comprobar(e.avisos.enviados==(rol==5?0:1),"Aviso sólo para alumno o docente");}
        for(int rol:List.of(1,2)){Entorno e=new Entorno();denegado(()->e.servicio.registrar(datos(0,rol,null),900001));comprobar(e.usuarios.altas==0,"Secretaría no registra dirección ni secretaría");}
        for(int anterior:List.of(1,2)){Entorno e=new Entorno();e.usuarios.rolDestino=anterior;denegado(()->e.servicio.modificar(datos(2,3,anterior),900001));comprobar(e.usuarios.modificaciones==0,"Secretaría no modifica perfiles superiores");}
        Entorno promover=new Entorno();denegado(()->promover.servicio.modificar(datos(2,1,4),900001));comprobar(promover.usuarios.modificaciones==0,"Secretaría no convierte alumnos en directivos");
        Entorno viejo=new Entorno();denegado(()->viejo.servicio.modificar(datos(2,3,3),900001));
        Entorno dup=new Entorno();dup.usuarios.duplicada=true;denegado(()->dup.servicio.registrar(datos(0,3,null),900001));comprobar(dup.usuarios.altas==0,"Usuario o mail duplicado");
        Entorno fallo=new Entorno();fallo.fallaPerfil=true;denegado(()->fallo.servicio.registrar(datos(0,4,null),900001));comprobar(fallo.usuarios.altas==0&&fallo.usuarios.rollbacks==1&&fallo.avisos.enviados==0,"Fallo de perfil revierte cuenta y no envía");
        Entorno propia=new Entorno();propia.usuarios.rol=1;propia.usuarios.rolDestino=1;denegado(()->propia.servicio.modificar(datos(900001,3,1),900001));
        Entorno director=new Entorno();director.usuarios.rol=1;director.servicio.registrar(datos(0,2,null),900001);comprobar(director.usuarios.altas==1,"Dirección registra secretaría");
        Auditoria_servicio.cerrar();Entorno sinSesion=new Entorno();denegado(()->sinSesion.servicio.registrar(datos(0,3,null),900001));
        System.out.println("Altas, cambios de rol, permisos, correo y rollback de cuentas: "+pruebas+" comprobaciones OK");
    }
}
