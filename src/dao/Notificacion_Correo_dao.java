package dao;

import clases.Notificacion_Correo;
import clases.Rol;
import servicios.Auditoria_servicio;
import servicios.Permisos_servicio;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

// persistimos los avisos en la cola compartida con el servicio de correo de la web
public class Notificacion_Correo_dao extends Consulta_academica_dao implements Dao<Notificacion_Correo, Integer> {
    // el evento es único para no generar el mismo aviso dos veces al repetir una operación
    public int agregar(Connection db, Notificacion_Correo n) throws SQLException {
        return insertarRegistro(db,"INSERT INTO notificacion_correo(id_usuario,destinatario,tipo,clave_evento,asunto,mensaje) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id_notificacion=LAST_INSERT_ID(id_notificacion)",n.getId_usuario(),n.getDestinatario(),n.getTipo(),n.getClave_evento(),n.getAsunto(),n.getMensaje());
    }

    // obtenemos el destinatario de la cuenta sin aceptar un correo elegido en la asignación
    public Map<String,Object> destinatario(Connection db, int usuario) throws SQLException {
        return uno(db,"SELECT id_usuario,nombre_usuario,mail FROM usuario WHERE id_usuario=?",usuario);
    }
    public Map<String,Object> curso(Connection db, int curso) throws SQLException {
        return uno(db,"SELECT c.año,c.division,c.turno,p.nombre periodo FROM curso c JOIN periodo_lectivo p ON p.id_periodo_lectivo=c.id_periodo WHERE c.id_curso=?",curso);
    }
    public int usuarioAlumno(Connection db, int alumno) throws SQLException { return numero(uno(db,"SELECT id_usuario FROM alumno WHERE id_alumno=?",alumno),"id_usuario"); }
    public int usuarioDocente(Connection db, int docente) throws SQLException { return numero(uno(db,"SELECT id_usuario FROM docente WHERE id_docente=?",docente),"id_usuario"); }

    // reunimos los avisos generados por una sesión después de confirmar su transacción
    public List<Integer> pendientesDelActor(int actor) throws Exception {
        return transaccion(db->{Permisos_servicio.verificar(db,this,actor,Rol.DIRECTIVO,Rol.SECRETARIA);
            List<Integer> ids=new ArrayList<>();for(var fila:consultar(db,"SELECT id_notificacion FROM notificacion_correo WHERE clave_evento LIKE ? AND estado IN ('Pendiente','Error') ORDER BY id_notificacion","java-"+actor+"-%"))ids.add(numero(fila,"id_notificacion"));return ids;});
    }

    // transformamos las columnas de la tabla en el objeto que consulta administración
    private Notificacion_Correo leer(Map<String,Object> f) {
        Notificacion_Correo n=new Notificacion_Correo();n.setId_notificacion(numero(f,"id_notificacion"));n.setId_usuario(f.get("id_usuario")==null?null:numero(f,"id_usuario"));
        n.setDestinatario((String)f.get("destinatario"));n.setTipo((String)f.get("tipo"));n.setClave_evento((String)f.get("clave_evento"));n.setAsunto((String)f.get("asunto"));n.setMensaje((String)f.get("mensaje"));n.setEstado((String)f.get("estado"));n.setIntentos(numero(f,"intentos"));
        n.setCreado(fecha(f.get("creado")));n.setUltimo_intento(fecha(f.get("ultimo_intento")));n.setEnviado(fecha(f.get("enviado")));return n;
    }
    private LocalDateTime fecha(Object valor) { return valor==null?null:valor instanceof Timestamp t?t.toLocalDateTime():valor instanceof LocalDateTime t?t:LocalDateTime.parse(valor.toString().replace(' ','T')); }

    @Override public void insertar(Notificacion_Correo n) {try{n.setId_notificacion(transaccion(db->agregar(db,n)));}catch(Exception e){throw new IllegalStateException("No se pudo guardar el aviso.",e);}}
    @Override public Notificacion_Correo obtenerPorId(Integer id) {
        try{return transaccion(db->{
            Integer actor=Auditoria_servicio.usuarioActual();Permisos_servicio.verificar(db,this,actor,Rol.DIRECTIVO,Rol.SECRETARIA);
            var filas=consultar(db,"SELECT * FROM notificacion_correo WHERE id_notificacion=?",id);
            if(filas.isEmpty())return null;var fila=filas.getFirst();
            if(obtenerRol(db,actor)!=Rol.DIRECTIVO && !String.valueOf(fila.get("clave_evento")).startsWith("java-"+actor+"-"))throw new IllegalArgumentException("No tenés permiso para consultar ese aviso.");
            return leer(fila);
        });}catch(Exception e){throw new IllegalArgumentException(e.getMessage(),e);}
    }
    @Override public List<Notificacion_Correo> obtenerTodos() {
        try{return transaccion(db->{Permisos_servicio.verificar(db,this,Auditoria_servicio.usuarioActual(),Rol.DIRECTIVO);return consultar(db,"SELECT * FROM notificacion_correo ORDER BY id_notificacion DESC LIMIT 100").stream().map(this::leer).toList();});}catch(Exception e){throw new IllegalArgumentException(e.getMessage(),e);}
    }
    // el servicio web administra los estados para conservar los intentos y evitar envíos simultáneos
    @Override public void modificar(Notificacion_Correo n) {throw new UnsupportedOperationException("El estado del aviso se actualiza mediante el servicio de correo.");}
    @Override public void eliminar(Integer id) {throw new UnsupportedOperationException("Los avisos conservan su historial de envío.");}
}
