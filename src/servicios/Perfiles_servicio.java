package servicios;

import clases.*;

import dao.*;

import dto.Personal;
import dto.Vistas;

import java.sql.*;
import java.util.*;

// Coordina perfiles de distintas tablas, cada escritura pertenece a su DAO.
public class Perfiles_servicio {
    public Object entidad(Connection db, int usuario, int rol) throws SQLException {
        return switch (rol) {
            case Rol.DIRECTIVO -> new Directivo_dao().porUsuario(db, usuario);
            case Rol.SECRETARIA -> new Secretaria_dao().porUsuario(db, usuario);
            case Rol.COOPERADORA -> new Cooperadora_dao().porUsuario(db, usuario);
            case Rol.ALUMNO -> new Alumno_dao().porUsuario(db, usuario);
            case Rol.DOCENTE -> new Docente_dao().porUsuario(db, usuario);
            default -> null;
        };
    }

    public Map<String, Object> perfil(Connection db, int usuario, int rol) throws SQLException {
        return Vistas.fila(entidad(db, usuario, rol));
    }

    public void inactivar(Connection db, int usuario, int rol) throws SQLException {
        switch (rol) {
            case Rol.DIRECTIVO -> new Directivo_dao().inactivar(db, usuario);
            case Rol.SECRETARIA -> new Secretaria_dao().inactivar(db, usuario);
            case Rol.COOPERADORA -> new Cooperadora_dao().inactivar(db, usuario);
            case Rol.ALUMNO -> new Alumno_dao().inactivar(db, usuario);
            case Rol.DOCENTE -> new Docente_dao().inactivar(db, usuario);
            default -> throw new IllegalArgumentException("Seleccioná un rol válido.");
        }
    }

    public List<Map<String, Object>> listar(Connection db, Personal.Tipo tipo) throws SQLException {
        List<?> entidades =
                switch (tipo) {
                    case DIRECTIVO -> new Directivo_dao().listarTabla(db);
                    case SECRETARIA -> new Secretaria_dao().listarTabla(db);
                    case COOPERADORA -> new Cooperadora_dao().listarTabla(db);
                };
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Object entidad : entidades) {
            var fila = Vistas.fila(entidad);
            fila.put("id", fila.get("id_" + tipo.tabla));
            var usuario =
                    new Usuario_dao().buscar(db, ((Number) fila.get("id_usuario")).intValue());
            fila.put("nombre_usuario", usuario == null ? null : usuario.getNombre_usuario());
            filas.add(fila);
        }
        filas.sort(Comparator.comparing(f -> f.get("apellido") + " " + f.get("nombre")));
        return filas;
    }

    public Map<String, Object> obtener(Connection db, Personal.Tipo tipo, int id)
            throws SQLException {
        return Vistas.fila(
                switch (tipo) {
                    case DIRECTIVO -> new Directivo_dao().requerir(db, id);
                    case SECRETARIA -> new Secretaria_dao().requerir(db, id);
                    case COOPERADORA -> new Cooperadora_dao().requerir(db, id);
                });
    }

    public String crear(Connection db, Personal p, int usuario) throws SQLException {
        p.setId_usuario(usuario);
        p.setLegajo(p.getTipo().prefijo + "-U" + usuario);
        guardar(db, p, true);
        return p.getLegajo();
    }

    public void modificar(Connection db, Personal p) throws SQLException {
        guardar(db, p, false);
    }

    private void guardar(Connection db, Personal p, boolean nuevo) throws SQLException {
        switch (p.getTipo()) {
            case DIRECTIVO -> {
                Directivo d = new Directivo();
                d.setId_directivo(p.getId());
                d.setId_usuario(p.getId_usuario());
                d.setNombre(p.getNombre());
                d.setApellido(p.getApellido());
                d.setDni(p.getDni());
                d.setMail(p.getMail());
                d.setLegajo(p.getLegajo());
                d.setEstado(p.getEstado());
                if (nuevo) new Directivo_dao().crear(db, d);
                else new Directivo_dao().actualizar(db, d);
            }
            case SECRETARIA -> {
                Secretaria d = new Secretaria();
                d.setId_secretaria(p.getId());
                d.setId_usuario(p.getId_usuario());
                d.setNombre(p.getNombre());
                d.setApellido(p.getApellido());
                d.setDni(p.getDni());
                d.setMail(p.getMail());
                d.setLegajo(p.getLegajo());
                d.setEstado(p.getEstado());
                if (nuevo) new Secretaria_dao().crear(db, d);
                else new Secretaria_dao().actualizar(db, d);
            }
            case COOPERADORA -> {
                Cooperadora d = new Cooperadora();
                d.setId_cooperadora(p.getId());
                d.setId_usuario(p.getId_usuario());
                d.setNombre(p.getNombre());
                d.setApellido(p.getApellido());
                d.setDni(p.getDni());
                d.setMail(p.getMail());
                d.setLegajo(p.getLegajo());
                d.setEstado(p.getEstado());
                if (nuevo) new Cooperadora_dao().crear(db, d);
                else new Cooperadora_dao().actualizar(db, d);
            }
        }
    }
}
