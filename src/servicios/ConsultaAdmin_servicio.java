package servicios;

import clases.*;

import dao.*;

import dto.Vistas;

import java.sql.*;
import java.util.*;

public class ConsultaAdmin_servicio {
    public int[] contadores(Connection db) throws SQLException {
        int activos =
                (int)
                        new Curso_dao()
                                .listarTabla(db).stream()
                                        .filter(c -> "Activo".equals(c.getEstado()))
                                        .count();
        return new int[] {
            new Alumno_dao().contar(db),
            new Docente_dao().contar(db),
            activos,
            new Inscripcion_dao().contar(db),
            new Usuario_dao().contar(db)
        };
    }

    public List<Map<String, Object>> cuentas(Connection db) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        List<Usuario> usuarios = new Usuario_dao().listarTabla(db);
        usuarios.sort(Comparator.comparing(Usuario::getId_usuario).reversed());
        Map<Integer, Rol> roles = new HashMap<>();
        for (Rol rol : new Rol_dao().listarTabla(db)) roles.put(rol.getId_rol(), rol);
        Map<Integer, Map<Integer, Map<String, Object>>> perfiles = new HashMap<>();
        perfiles.put(Rol.DIRECTIVO, porUsuario(new Directivo_dao().listarTabla(db)));
        perfiles.put(Rol.SECRETARIA, porUsuario(new Secretaria_dao().listarTabla(db)));
        perfiles.put(Rol.COOPERADORA, porUsuario(new Cooperadora_dao().listarTabla(db)));
        perfiles.put(Rol.DOCENTE, porUsuario(new Docente_dao().listarTabla(db)));
        perfiles.put(Rol.ALUMNO, porUsuario(new Alumno_dao().listarTabla(db)));
        for (Usuario usuario : usuarios) {
            var fila = Vistas.fila(usuario);
            var perfil =
                    perfiles.getOrDefault(usuario.getId_rol(), Map.of())
                            .getOrDefault(usuario.getId_usuario(), Map.of());
            Rol rol = roles.get(usuario.getId_rol());
            fila.put("rol", rol == null ? null : rol.getNombre());
            for (String campo : List.of("nombre", "apellido", "dni"))
                fila.put(campo, perfil.getOrDefault(campo, ""));
            fila.put(
                    "numero",
                    perfil.getOrDefault(
                            usuario.getId_rol() == Rol.ALUMNO ? "matricula" : "legajo", ""));
            fila.put("estado", perfil.getOrDefault("estado", "Sin perfil"));
            filas.add(fila);
        }
        return filas;
    }

    public List<Map<String, Object>> logs(Connection db, Integer actor) throws Exception {
        Permisos_servicio.verificar(db, actor, Rol.DIRECTIVO);
        List<Map<String, Object>> filas = new ArrayList<>();
        Map<Integer, Usuario> usuarios = new HashMap<>();
        for (Usuario usuario : new Usuario_dao().listarTabla(db))
            usuarios.put(usuario.getId_usuario(), usuario);
        Map<Integer, Rol> roles = new HashMap<>();
        for (Rol rol : new Rol_dao().listarTabla(db)) roles.put(rol.getId_rol(), rol);
        for (Log_Usuario log : new Log_Usuario_dao().ultimos(db)) {
            var fila = Vistas.fila(log);
            Usuario usuario = usuarios.get(log.getId_usuario());
            Rol rol = usuario == null ? null : roles.get(usuario.getId_rol());
            fila.put("usuario", usuario == null ? null : usuario.getNombre_usuario());
            fila.put("rol", rol == null ? null : rol.getNombre());
            fila.put(
                    "detalle",
                    log.getValor_nuevo() == null ? log.getValor_anterior() : log.getValor_nuevo());
            filas.add(fila);
        }
        return filas;
    }

    private Map<Integer, Map<String, Object>> porUsuario(List<?> entidades) {
        Map<Integer, Map<String, Object>> filas = new HashMap<>();
        for (Object entidad : entidades) {
            var fila = Vistas.fila(entidad);
            if (fila.get("id_usuario") != null)
                filas.put(((Number) fila.get("id_usuario")).intValue(), fila);
        }
        return filas;
    }
}
