import clases.Rol;
import dao.*;
import servicios.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

// ejercitamos resoluciones y lotes concurrentes usando el mismo mecanismo transaccional del servicio
public class SolicitudesPromocionJava {
    static int pruebas;
    interface Accion{void ejecutar()throws Exception;}
    static void comprobar(boolean c,String mensaje){pruebas++;if(!c)throw new AssertionError(mensaje);}
    static void denegado(Accion a)throws Exception{try{a.ejecutar();throw new AssertionError("Debía rechazarse");}catch(IllegalArgumentException e){pruebas++;}}
    static class Solicitudes extends Solicitud_Cambio_dao {
        final ReglasJava.Memoria db;Solicitudes(ReglasJava.Memoria db){this.db=db;}
        public <T>T transaccion(Operacion<T> op)throws Exception{return db.transaccion(op);}
        public int obtenerRol(Connection c,int actor){return db.rol;}
        public boolean pendiente(Connection c,int inscripcion){return db.solicitudes.values().stream().anyMatch(s->s.get("id_inscripcion").equals(inscripcion)&&s.get("estado").equals("Pendiente"));}
        public int crear(Connection c,int inscripcion,int destino,String motivo,int actor){int id=db.solicitudes.size()+1;db.solicitudes.put(id,new HashMap<>(Map.of("id_inscripcion",inscripcion,"id_curso_destino",destino,"observacion",motivo,"estado","Pendiente")));return id;}
        public Map<String,Object> bloquear(Connection c,int id){return new HashMap<>(db.solicitudes.get(id));}
        public void resolver(Connection c,int id,boolean aprobar,int actor,String motivo,Integer nueva){var s=db.solicitudes.get(id);s.put("estado",aprobar?"Aprobada":"Rechazada");s.put("motivo_resolucion",motivo);s.put("id_inscripcion_nueva",nueva);}
    }
    static Solicitud_Cambio_servicio servicio(ReglasJava.Memoria m){return new Solicitud_Cambio_servicio(new Solicitudes(m),m,new ReglasJava.Servicio(m));}
    static class Promociones extends ReglasJava.Memoria {
        boolean todasAprobadas=true;
        Promociones(){
            rol=Rol.DIRECTIVO;cursos.get(10).put("nota_aprobacion_materia",6.0);cursos.get(10).put("promedio_promocion",7.0);cursos.get(10).put("inicio_periodo",java.sql.Date.valueOf("2025-03-01"));
            cursos.get(20).put("año",2);cursos.get(20).put("id_periodo",2);cursos.get(20).put("inicio_periodo",java.sql.Date.valueOf("2026-03-01"));cursos.get(20).put("cupo_maximo",2);
            filas.put(2,new HashMap<>(filas.get(1)));filas.get(2).put("id_inscripcion",2);filas.get(2).put("id_alumno",2);notas.put(2,List.of(7.0));
        }
        public List<Map<String,Object>> alumnosPromocion(Connection c,int curso){return filas.entrySet().stream().filter(e->e.getValue().get("id_curso").equals(curso)&&List.of("Confirmada","Recuperacion").contains(e.getValue().get("estado"))).map(e->{var f=new HashMap<>(e.getValue());f.put("alumno","Alumno "+e.getKey());return (Map<String,Object>)f;}).toList();}
        public List<Map<String,Object>> resultados(Connection c,int inscripcion){return List.of(Map.of("nombre_materia","Materia","resultado",inscripcion==2&&!todasAprobadas?"Desaprobada":"Aprobada","promedio",8.0));}
    }
    public static void main(String[] args)throws Exception{
        Auditoria_servicio.iniciar(900001);
        ReglasJava.Memoria m=new ReglasJava.Memoria();var s=servicio(m);s.solicitar(1,20,"Excepción",900001);
        comprobar(m.filas.size()==1&&m.ocupacion(null,20)==0,"Solicitar no traslada ni reserva cupo");denegado(()->s.solicitar(1,20,"Otra",900001));denegado(()->s.resolver(1,true,"Aprobación",900001));
        m.rol=1;m.cursos.get(20).put("año",5);m.cursos.get(20).put("id_periodo",99);s.resolver(1,true,"Excepción justificada",900001);
        comprobar(m.solicitudes.get(1).get("estado").equals("Aprobada")&&m.solicitudes.get(1).get("id_inscripcion_nueva").equals(2),"Aprobación excepcional vinculada");denegado(()->s.resolver(1,true,"Reintento",900001));
        ReglasJava.Memoria lleno=new ReglasJava.Memoria();var sl=servicio(lleno);sl.solicitar(1,20,"Cambio",900001);lleno.rol=1;lleno.cursos.get(20).put("cupo_maximo",0);denegado(()->sl.resolver(1,true,"Aprobación",900001));comprobar(lleno.solicitudes.get(1).get("estado").equals("Pendiente")&&lleno.filas.size()==1,"Error conserva solicitud pendiente y origen");
        sl.resolver(1,false,"Sin vacante",900001);comprobar(lleno.solicitudes.get(1).get("estado").equals("Rechazada")&&lleno.filas.get(1).get("estado").equals("Confirmada"),"Rechazo conserva inscripción");
        ReglasJava.Memoria ultimo=new ReglasJava.Memoria();ultimo.filas.put(2,new HashMap<>(ultimo.filas.get(1)));ultimo.filas.get(2).put("id_inscripcion",2);ultimo.filas.get(2).put("id_alumno",2);var su=servicio(ultimo);su.solicitar(1,20,"Cambio 1",900001);su.solicitar(2,20,"Cambio 2",900001);ultimo.rol=1;
        ExecutorService pool=Executors.newFixedThreadPool(2);try{List<Future<Boolean>> f=new ArrayList<>();for(int id:List.of(1,2)){f.add(pool.submit(()->{try{su.resolver(id,true,"Aprobación",900001);return true;}catch(IllegalArgumentException e){return false;}}));}int aprobadas=0;for(var r:f)if(r.get())aprobadas++;comprobar(aprobadas==1&&ultimo.ocupacion(null,20)==1,"Dos solicitudes simultáneas no consumen el mismo lugar");comprobar(ultimo.solicitudes.values().stream().filter(r->r.get("estado").equals("Pendiente")).count()==1,"La segunda solicitud sigue pendiente");}finally{pool.shutdownNow();}
        Promociones p=new Promociones();p.todasAprobadas=false;var ps=new Promocion_servicio(p,new ReglasJava.Servicio(p));var revision=ps.revisar(10,900001);comprobar(revision.size()==2&&revision.getFirst().aprobado()&&!revision.getLast().aprobado(),"Revisión por alumno");var resultado=ps.promocionar(10,20,900001);comprobar(resultado.promocionados()==1&&resultado.recuperacion()==1,"Promueve aprobados y conserva recuperación");comprobar(p.notas.size()==2&&p.filas.get(1).get("estado").equals("Finalizada")&&p.filas.get(2).get("estado").equals("Recuperacion"),"Conserva notas originales y registra los estados");
        Promociones fallo=new Promociones();fallo.fallarMovimientoNumero=4;denegado(()->new Promocion_servicio(fallo,new ReglasJava.Servicio(fallo)).promocionar(10,20,900001));comprobar(fallo.filas.size()==2&&fallo.avisos.isEmpty()&&fallo.filas.values().stream().allMatch(i->i.get("estado").equals("Confirmada")),"Fallo del segundo alumno revierte el lote y los avisos");
        Promociones sinCupo=new Promociones();sinCupo.cursos.get(20).put("cupo_maximo",1);denegado(()->new Promocion_servicio(sinCupo,new ReglasJava.Servicio(sinCupo)).promocionar(10,20,900001));comprobar(sinCupo.filas.size()==2,"Cupo insuficiente rechaza todo el lote");
        Auditoria_servicio.cerrar();System.out.println("Solicitudes, último cupo concurrente y promoción del lote: "+pruebas+" comprobaciones OK");
    }
}
