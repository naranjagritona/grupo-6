import clases.*;
import dao.*;
import servicios.*;
import paneles.SelectorAlumno;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;
import java.lang.reflect.Proxy;

// ejecutamos las reglas con daos en memoria para comprobar fallos sin alterar registros de personas
public class ReglasJava {
    private static int comprobaciones;
    private static void comprobar(boolean condicion,String mensaje) { comprobaciones++; if(!condicion)throw new AssertionError(mensaje); }
    private interface Accion { void ejecutar() throws Exception; }
    private static void denegado(Accion accion) throws Exception { try{accion.ejecutar();throw new AssertionError("La operación debía rechazarse");}catch(IllegalArgumentException esperado){comprobaciones++;} }
    static class Memoria extends Inscripcion_dao {
        Map<Integer,Map<String,Object>> filas=new LinkedHashMap<>();
        Map<Integer,Map<String,Object>> cursos=new LinkedHashMap<>();
        Map<Integer,List<Double>> notas=new HashMap<>();
        Map<Integer,Map<String,Object>> solicitudes=new LinkedHashMap<>();
        int fallarMovimientoNumero;
        List<String> movimientos=new ArrayList<>(),avisos=new ArrayList<>();
        int rol=Rol.SECRETARIA,confirmaciones,reversiones; boolean fallaMovimiento;
        Memoria(){
            cursos.put(10,new HashMap<>(Map.of("año",1,"id_periodo",1,"estado","Activo","cupo_maximo",1)));
            cursos.put(20,new HashMap<>(cursos.get(10)));
            filas.put(1,new HashMap<>(Map.of("id_inscripcion",1,"id_alumno",1,"id_curso",10,"estado","Confirmada","estado_alumno","Activo","año",1,"id_periodo",1)));
            notas.put(1,List.of(8.0,9.0));
        }
        private Map<Integer,Map<String,Object>> copia(Map<Integer,Map<String,Object>> original){var copia=new LinkedHashMap<Integer,Map<String,Object>>();original.forEach((id,f)->copia.put(id,new HashMap<>(f)));return copia;}
        @Override protected Connection abrirConexion(){
            var antes=copia(filas);var solicitudesAntes=copia(solicitudes);var movAntes=new ArrayList<>(movimientos);var avisosAntes=new ArrayList<>(avisos);
            return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(p,m,a)->{
                switch(m.getName()){
                    case "commit":confirmaciones++;return null;
                    case "rollback":reversiones++;filas=copia(antes);solicitudes=copia(solicitudesAntes);movimientos=new ArrayList<>(movAntes);avisos=new ArrayList<>(avisosAntes);return null;
                    case "setAutoCommit", "close":return null;
                    case "isClosed":return false;
                    default:throw new UnsupportedOperationException(m.getName());
                }
            });
        }
        @Override public synchronized <T>T transaccion(Operacion<T> operacion)throws Exception{return super.transaccion(operacion);}
        @Override public int obtenerRol(Connection c,int usuario){return rol;}
        @Override public Map<String,Object> bloquear(Connection c,int id){var fila=filas.get(id);if(fila==null)throw new IllegalArgumentException("Inscripción inexistente");return new HashMap<>(fila);}
        @Override public Map<String,Object> curso(Connection c,int id){return cursos.get(id);}
        @Override public Map<String,Object> alumno(Connection c,int id){return Map.of("estado",filas.get(1).get("estado_alumno"));}
        @Override public int ocupacion(Connection c,int curso){return (int)filas.values().stream().filter(f->Objects.equals(f.get("id_curso"),curso)&&List.of("Pendiente","Confirmada","Recuperacion").contains(f.get("estado"))).count();}
        @Override public boolean duplicada(Connection c,int alumno,int curso){return filas.values().stream().anyMatch(f->Objects.equals(f.get("id_alumno"),alumno)&&Objects.equals(f.get("id_curso"),curso)&&List.of("Pendiente","Confirmada","Recuperacion").contains(f.get("estado")));}
        @Override public void estado(Connection c,int id,String estado,String motivo){filas.get(id).put("estado",estado);filas.get(id).put("motivo_baja",motivo);}
        @Override public int destino(Connection c,int alumno,int curso,int origen){int id=filas.size()+1;filas.put(id,new HashMap<>(Map.of("id_inscripcion",id,"id_alumno",alumno,"id_curso",curso,"estado","Confirmada","id_inscripcion_origen",origen)));return id;}
        @Override public void movimiento(Connection c,int id,String anterior,String nuevo,String motivo,int actor){movimientos.add(id+":"+nuevo);if((fallaMovimiento&&movimientos.size()==2)||fallarMovimientoNumero>0&&movimientos.size()==fallarMovimientoNumero)throw new IllegalArgumentException("Falla de persistencia");}
    }
    static class Servicio extends Inscripcion_servicio {
        final Memoria memoria;int despachos;
        Servicio(Memoria m){super(m);memoria=m;}
        @Override protected void avisarAsignacion(Connection c,int alumno,int curso,int inscripcion,int actor){memoria.avisos.add("inscripcion-"+inscripcion);}
        @Override protected void enviarAvisos(int actor){despachos++;}
    }
    public static void main(String[] args)throws Exception{
        for(int actor=1;actor<=5;actor++)for(int anterior=1;anterior<=5;anterior++)for(int nuevo=1;nuevo<=5;nuevo++){
            boolean permitido=actor==1 || actor==2&&anterior>=3&&nuevo>=3;
            comprobar(Permisos_servicio.cambiarRol(actor,anterior,nuevo)==permitido,"Matriz de roles");
        }
        comprobar(Admin_servicio.numeroAutomatico(Rol.ALUMNO,678).endsWith("-U678"),"Matrícula automática");
        comprobar(Admin_servicio.numeroAutomatico(Rol.DOCENTE,678).equals("DOC-U678"),"Legajo automático");
        comprobar(Inscripcion_servicio.motivo("  Cambio justificado  ").equals("Cambio justificado"),"Motivo recortado");
        denegado(()->Inscripcion_servicio.motivo(" "));denegado(()->Inscripcion_servicio.motivo("x".repeat(256)));
        Auditoria_servicio.iniciar(900001);
        Memoria m=new Memoria();Servicio servicio=new Servicio(m);servicio.cambiarCurso(1,20,"  Cambio  ",900001);
        comprobar(m.filas.get(1).get("id_curso").equals(10),"El curso original se conserva");
        comprobar(m.filas.get(1).get("estado").equals("Trasladado"),"Origen trasladado");
        comprobar(m.filas.get(2).get("id_inscripcion_origen").equals(1),"Vínculo de origen");
        comprobar(m.notas.get(1).equals(List.of(8.0,9.0))&&!m.notas.containsKey(2),"Las notas no se copian ni se mueven");
        comprobar(m.movimientos.size()==2&&m.avisos.size()==1&&servicio.despachos==1,"Movimientos y aviso después del commit");
        denegado(()->servicio.cambiarCurso(1,20,"Reintento",900001));comprobar(m.filas.size()==2,"No duplica un traslado");
        for(String estado:List.of("Pendiente","Cancelada","Finalizada","Trasladado")){Memoria caso=new Memoria();caso.filas.get(1).put("estado",estado);denegado(()->new Servicio(caso).cambiarCurso(1,20,"Cambio",900001));comprobar(caso.filas.size()==1,"Estado rechazado sin destino");}
        for(String caso:List.of("alumno","destino","cupo","duplicada","año","periodo","permiso")){
            Memoria db=new Memoria();switch(caso){
                case "alumno":db.filas.get(1).put("estado_alumno","Inactivo");break;
                case "destino":db.cursos.get(20).put("estado","Inactivo");break;
                case "cupo":db.cursos.get(20).put("cupo_maximo",0);break;
                case "duplicada":db.filas.put(2,new HashMap<>(Map.of("id_alumno",1,"id_curso",20,"estado","Recuperacion")));break;
                case "año":db.cursos.get(20).put("año",2);break;
                case "periodo":db.cursos.get(20).put("id_periodo",2);break;
                case "permiso":db.rol=Rol.DIRECTIVO;break;
            }
            int cantidad=db.filas.size();denegado(()->new Servicio(db).cambiarCurso(1,20,"Cambio",900001));comprobar(db.filas.size()==cantidad&&db.filas.get(1).get("estado").equals("Confirmada"),"Sin cambios parciales: "+caso);
        }
        Memoria fallo=new Memoria();fallo.fallaMovimiento=true;Servicio conFallo=new Servicio(fallo);denegado(()->conFallo.cambiarCurso(1,20,"Cambio",900001));
        comprobar(fallo.reversiones==1&&fallo.filas.size()==1&&fallo.movimientos.isEmpty()&&fallo.avisos.isEmpty()&&conFallo.despachos==0,"Rollback y ausencia de envío");
        denegado(()->new Servicio(new Memoria()).cambiarCurso(1,20,"Cambio",123));
        Auditoria_servicio.cerrar();denegado(()->new Servicio(new Memoria()).cambiarCurso(1,20,"Cambio",900001));
        List<ItemCombo> opciones=List.of(new ItemCombo(45,"Álvarez, Nicolás · DNI 12345678 · AIXX-2026-U45"),new ItemCombo(67,"Gómez, Ana · DNI 87654321 · AIXX-2026-U67"));
        comprobar(SelectorAlumno.coincidencias(opciones,"nicolas").getFirst().getId()==45,"Busca sin tildes");
        comprobar(SelectorAlumno.coincidencias(opciones,"87654321").getFirst().getId()==67,"Busca por DNI");
        comprobar(SelectorAlumno.coincidencias(opciones,"U45").getFirst().getId()==45,"Busca por matrícula");
        SwingUtilities.invokeAndWait(()->{SelectorAlumno selector=new SelectorAlumno();selector.setOpciones(opciones);selector.setModel(new DefaultComboBoxModel<>(opciones.toArray(ItemCombo[]::new)));selector.setSelectedIndex(1);comprobar(selector.getSelectedItem()!=null && selector.getSelectedItem().getId()==67,"Elegir una sugerencia confirma su ID");selector.seleccionar(opciones.getFirst());comprobar(selector.getSelectedItem().getId()==45,"Conserva el ID seleccionado");((JTextField)selector.getEditor().getEditorComponent()).setText("Persona que no existe");comprobar(selector.getSelectedItem()==null,"Texto libre no confirma alumno");selector.limpiarSeleccion();comprobar(selector.getSelectedItem()==null,"Limpiar selección");});
        java.util.concurrent.CountDownLatch filtrado=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> errorFiltro=new java.util.concurrent.atomic.AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{
            SelectorAlumno selector=new SelectorAlumno();selector.setOpciones(opciones);((JTextField)selector.getEditor().getEditorComponent()).setText("ana");
            javax.swing.Timer comprobarFiltro=new javax.swing.Timer(350,e->{try{comprobar(selector.getItemCount()==1&&selector.getSelectedItem()==null,"Filtrar no elige el primer alumno");selector.setSelectedIndex(0);comprobar(selector.getSelectedItem().getId()==67,"Elección de sugerencia filtrada");}catch(Throwable falloFiltro){errorFiltro.set(falloFiltro);}finally{filtrado.countDown();}});
            comprobarFiltro.setRepeats(false);comprobarFiltro.start();
        });
        if(!filtrado.await(3,java.util.concurrent.TimeUnit.SECONDS))throw new AssertionError("No terminó el filtro");
        if(errorFiltro.get()!=null)throw new AssertionError(errorFiltro.get());
        System.out.println("Reglas, permisos, traslado, rollback y autocompletado: "+comprobaciones+" comprobaciones OK");
    }
}
