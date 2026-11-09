package servicios;

import clases.*;
import dao.*;
import java.sql.Connection;
import java.time.Year;
import java.util.*;

// coordinamos las cuentas, sus perfiles y los avisos usando la misma transacción
public class Admin_servicio {
    private final Usuario_dao usuarios;
    private final Alumno_dao alumnos;
    private final Docente_dao docentes;
    private final Responsable_dao responsables;
    private final Personal_dao personal;
    private final Rol_dao roles;
    private final ConsultaAdmin_dao consultas;
    private final Notificaciones_servicio avisos;

    public Admin_servicio() {this(new Usuario_dao(),new Alumno_dao(),new Docente_dao(),new Responsable_dao(),new Personal_dao(),new Rol_dao(),new ConsultaAdmin_dao(),new Notificaciones_servicio());}
    // compartimos los daos para que la validación y la persistencia usen la misma conexión
    public Admin_servicio(Usuario_dao usuarios,Alumno_dao alumnos,Docente_dao docentes,Responsable_dao responsables,Personal_dao personal,Rol_dao roles,ConsultaAdmin_dao consultas,Notificaciones_servicio avisos) {
        this.usuarios=usuarios;this.alumnos=alumnos;this.docentes=docentes;this.responsables=responsables;this.personal=personal;this.roles=roles;this.consultas=consultas;this.avisos=avisos;
    }
    public record Datos(int[] totales,List<Map<String,Object>> cuentas,List<Map<String,Object>> logs,List<Rol> roles,int rolActor) {}
    public record SolicitudCuenta(int id,int rol,String usuario,String clave,String mail,String nombre,String apellido,String dni,String estado,String responsableNombre,String responsableApellido,String responsableDni,String relacion,Integer rolOriginal) {}
    public record Alta(int id,String numero,Notificaciones_servicio.Resultado correo) {}
    private record AltaGuardada(int id,String numero,Integer aviso) {}

    // consultamos los roles creables y los logs solamente cuando la sesión tiene ese permiso
    public Datos cargar(Integer actor) throws Exception {
        return usuarios.transaccion(db->{verificarGestion(db,actor);int rol=usuarios.obtenerRol(db,actor);
            List<Rol> disponibles=roles.listar(db).stream().filter(r->Permisos_servicio.rolesCreables(rol).contains(r.getId_rol())).toList();
            return new Datos(consultas.contadores(db),usuarios.listarCuentas(db),rol==Rol.DIRECTIVO?consultas.logs(db,actor):List.of(),disponibles,rol);});
    }

    // buscamos la cuenta, su perfil actual y el responsable sin exponer la contraseña
    public Map<String,Object> detalle(int id,Integer actor) throws Exception {
        return usuarios.transaccion(db->{verificarGestion(db,actor);var cuenta=new LinkedHashMap<>(usuarios.bloquear(db,id));
            int rol=Consulta_academica_dao.numero(cuenta,"id_rol");var perfil=usuarios.perfil(db,id,rol);cuenta.put("persona",perfil);
            cuenta.put("responsable",rol==Rol.ALUMNO&&!perfil.isEmpty()?usuarios.responsable(db,Consulta_academica_dao.numero(perfil,"id_responsable")):Map.of());
            cuenta.put("editable",Permisos_servicio.administrarCuenta(usuarios.obtenerRol(db,actor),rol));return cuenta;});
    }

    private void verificarGestion(Connection db,Integer actor) throws Exception {Permisos_servicio.verificar(db,usuarios,actor,Rol.DIRECTIVO,Rol.SECRETARIA);}

    // recortamos los textos y comprobamos los campos que corresponden al perfil elegido
    public static SolicitudCuenta validar(SolicitudCuenta d,boolean nueva) {
        if(d==null)throw new IllegalArgumentException("Completá los datos de la cuenta.");
        if(!List.of(Rol.DIRECTIVO,Rol.SECRETARIA,Rol.DOCENTE,Rol.ALUMNO,Rol.COOPERADORA).contains(d.rol()))throw new IllegalArgumentException("Seleccioná un rol válido.");
        String usuario=texto(d.usuario(),200,"nombre de usuario"),mail=texto(d.mail(),254,"mail"),nombre=texto(d.nombre(),200,"nombre"),apellido=texto(d.apellido(),200,"apellido"),dni=texto(d.dni(),9,"DNI");
        if(usuario.contains("@"))throw new IllegalArgumentException("Usá un nombre de usuario sin @ para distinguirlo del mail.");
        if(!mail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("Ingresá un mail válido.");
        if(!dni.matches("[0-9]{1,9}"))throw new IllegalArgumentException("El DNI debe contener hasta nueve números.");
        if(nueva&&(d.clave()==null||d.clave().length()<8||d.clave().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72))throw new IllegalArgumentException("La contraseña debe tener al menos ocho caracteres y no superar el máximo permitido.");
        if(!List.of("Activo","Inactivo").contains(d.estado()))throw new IllegalArgumentException("Seleccioná un estado válido.");
        String rn="",ra="",rd="",rel="";
        if(d.rol()==Rol.ALUMNO){rn=texto(d.responsableNombre(),200,"nombre del responsable");ra=texto(d.responsableApellido(),200,"apellido del responsable");rd=texto(d.responsableDni(),9,"DNI del responsable");rel=texto(d.relacion(),200,"relación con el alumno");if(!rd.matches("[0-9]{1,9}"))throw new IllegalArgumentException("Revisá el DNI del responsable.");}
        return new SolicitudCuenta(d.id(),d.rol(),usuario,d.clave(),mail,nombre,apellido,dni,d.estado(),rn,ra,rd,rel,d.rolOriginal());
    }
    private static String texto(String valor,int maximo,String campo) {String limpio=valor==null?"":valor.trim();if(limpio.isBlank()||limpio.codePointCount(0,limpio.length())>maximo)throw new IllegalArgumentException("Completá correctamente el campo "+campo+".");return limpio;}

    // generamos la matrícula o el legajo a partir del identificador de la cuenta, como en la web
    public static String numeroAutomatico(int rol,int id) {
        return rol==Rol.ALUMNO?"AIXX-"+Year.now().getValue()+"-U"+id:(rol==Rol.DOCENTE?"DOC":rol==Rol.DIRECTIVO?"DIR":rol==Rol.SECRETARIA?"SEC":"COO")+"-U"+id;
    }

    // confirmamos el alta antes de intentar el envío para conservar la cuenta si el correo falla
    public Alta registrar(SolicitudCuenta solicitud,Integer actor) throws Exception {
        SolicitudCuenta d=validar(solicitud,true);String clave=seguridad.Contrasenas.paraGuardar(d.clave());
        AltaGuardada alta=usuarios.transaccion(db->{verificarGestion(db,actor);int rolActor=usuarios.obtenerRol(db,actor);
            if(!Permisos_servicio.rolesCreables(rolActor).contains(d.rol()))throw new IllegalArgumentException("No tenés permiso para crear ese rol.");
            if(roles.listar(db).stream().noneMatch(r->r.getId_rol()==d.rol()))throw new IllegalArgumentException("El rol ya no está disponible.");
            usuarios.reservarAlta(db);if(usuarios.nombreOMailOcupado(db,d.usuario(),d.mail(),0))throw new IllegalArgumentException("Ese usuario o mail ya está registrado.");
            Usuario cuenta=new Usuario(null,d.rol(),d.usuario(),clave);cuenta.setMail(d.mail());int id=usuarios.crear(db,cuenta);
            String numero=guardarPerfil(db,id,d,Map.of());Integer aviso=(d.rol()==Rol.ALUMNO || d.rol()==Rol.DOCENTE)?avisos.alta(db,id,d.nombre(),d.usuario(),actor):null;return new AltaGuardada(id,numero,aviso);});
        return new Alta(alta.id(),alta.numero(),avisos.enviar(alta.aviso()));
    }

    // validamos tanto el rol anterior como el solicitado y conservamos los perfiles históricos
    public void modificar(SolicitudCuenta solicitud,Integer actor) throws Exception {
        SolicitudCuenta d=validar(solicitud,false);
        usuarios.transaccion(db->{verificarGestion(db,actor);int rolActor=usuarios.obtenerRol(db,actor);var anterior=usuarios.bloquear(db,d.id());int rolAnterior=Consulta_academica_dao.numero(anterior,"id_rol");
            if(d.rolOriginal()==null||d.rolOriginal()!=rolAnterior)throw new IllegalArgumentException("Otro usuario cambió el rol. Volvé a abrir la cuenta.");
            if(!Permisos_servicio.cambiarRol(rolActor,rolAnterior,d.rol()))throw new IllegalArgumentException("No tenés permiso para modificar esa cuenta o asignarle ese rol.");
            if(Objects.equals(actor,d.id())&&(rolAnterior!=d.rol()||!"Activo".equals(d.estado())))throw new IllegalArgumentException("No podés cambiar tu propio rol ni dar de baja tu sesión.");
            usuarios.reservarAlta(db);if(usuarios.nombreOMailOcupado(db,d.usuario(),d.mail(),d.id()))throw new IllegalArgumentException("Ese usuario o mail ya está registrado.");
            Map<String,Object> perfil=usuarios.perfil(db,d.id(),d.rol());if(rolAnterior!=d.rol())usuarios.inactivarPerfil(db,d.id(),rolAnterior);
            Usuario cuenta=new Usuario(d.id(),d.rol(),d.usuario(),null);cuenta.setMail(d.mail());usuarios.actualizarCuenta(db,cuenta);guardarPerfil(db,d.id(),d,perfil);return null;});
    }

    // cada dao guarda su tabla usando la conexión de la cuenta; los identificadores existentes se conservan
    private String guardarPerfil(Connection db,int cuenta,SolicitudCuenta d,Map<String,Object> anterior) throws Exception {
        String numero=numeroAutomatico(d.rol(),cuenta);
        if(d.rol()==Rol.ALUMNO){
            Responsable r=new Responsable(null,d.responsableNombre(),d.responsableApellido(),d.responsableDni(),d.relacion());
            if(anterior.isEmpty()){r.setId_responsable(responsables.crear(db,r));Alumno a=new Alumno(null,cuenta,r.getId_responsable(),d.nombre(),d.apellido(),d.dni(),numero,d.estado());alumnos.crear(db,a);}
            else{int resp=Consulta_academica_dao.numero(anterior,"id_responsable");if(resp<=0 || usuarios.responsable(db,resp).isEmpty()){resp=responsables.crear(db,r);r.setId_responsable(resp);}else{r.setId_responsable(resp);responsables.actualizarAdministracion(db,r);}Alumno a=new Alumno(Consulta_academica_dao.numero(anterior,"id_alumno"),cuenta,resp,d.nombre(),d.apellido(),d.dni(),(String)anterior.get("matricula"),d.estado());alumnos.actualizarAdministracion(db,a);numero=a.getMatricula();}
        }else if(d.rol()==Rol.DOCENTE){Docente docente=new Docente(null,cuenta,d.nombre(),d.apellido(),d.dni(),numero,d.estado());if(anterior.isEmpty())docentes.crear(db,docente);else{docente.setId_docente(Consulta_academica_dao.numero(anterior,"id_docente"));docente.setLegajo((String)anterior.get("legajo"));docentes.actualizarAdministracion(db,docente);numero=docente.getLegajo();}}
        else{Personal p=new Personal();p.setTipo(d.rol()==Rol.DIRECTIVO?Personal.Tipo.DIRECTIVO:d.rol()==Rol.SECRETARIA?Personal.Tipo.SECRETARIA:Personal.Tipo.COOPERADORA);p.setId_usuario(cuenta);p.setNombre(d.nombre());p.setApellido(d.apellido());p.setDni(d.dni());p.setMail(d.mail());p.setEstado(d.estado());if(anterior.isEmpty())numero=personal.crear(db,p,cuenta);else{p.setId(Consulta_academica_dao.numero(anterior,"id_"+p.getTipo().tabla));personal.modificar(db,p);numero=(String)anterior.get("legajo");}}
        return numero;
    }
}
