package dao;

import clases.Personal;
import java.sql.*;
import java.util.*;

// consultamos y guardamos los perfiles de dirección, preceptoría y secretaría
public class Personal_dao extends Consulta_academica_dao {
    // consultamos los perfiles y sus cuentas para mostrarlos en administración
    public List<Map<String, Object>> listar(Connection conexion, Personal.Tipo tipo) throws SQLException {
        return consultar(conexion, "SELECT p.id_" + tipo.tabla + " id,p.*,u.nombre_usuario FROM " + tipo.tabla + " p JOIN usuario u ON u.id_usuario=p.id_usuario ORDER BY p.apellido,p.nombre");
    }

    // buscamos el registro que se quiere modificar sin permitir cambiar su legajo
    public Map<String, Object> obtener(Connection conexion, Personal.Tipo tipo, int id) throws SQLException {
        return uno(conexion, "SELECT p.*,u.nombre_usuario FROM " + tipo.tabla + " p JOIN usuario u ON u.id_usuario=p.id_usuario WHERE p.id_" + tipo.tabla + "=? FOR UPDATE", id);
    }

    // verificamos si el nombre de usuario ya está registrado antes de crear la cuenta
    public boolean usuarioExiste(Connection conexion, String usuario) throws SQLException {
        return !consultar(conexion, "SELECT id_usuario FROM usuario WHERE nombre_usuario=? FOR UPDATE", usuario).isEmpty();
    }

    // creamos la cuenta con el rol correspondiente y la contraseña protegida
    public int crearUsuario(Connection conexion, Personal personal, String contraseña) throws SQLException {
        return insertarRegistro(conexion, "INSERT INTO usuario(id_rol,nombre_usuario,contraseña) VALUES(?,?,?)", personal.getTipo().rol, personal.getUsuario(), contraseña);
    }

    // guardamos el perfil y generamos su legajo a partir del identificador de la tabla
    public String crear(Connection conexion, Personal personal, int usuario) throws SQLException {
        String tabla = personal.getTipo().tabla;
        int id = insertarRegistro(conexion, "INSERT INTO " + tabla + "(id_usuario,nombre,apellido,dni,mail,estado) VALUES(?,?,?,?,?,?)", usuario, personal.getNombre(), personal.getApellido(), personal.getDni(), personal.getMail(), personal.getEstado());
        String legajo = personal.getTipo().prefijo + "-" + String.format(java.util.Locale.ROOT, "%06d", id);
        ejecutar(conexion, "UPDATE " + tabla + " SET legajo=? WHERE id_" + tabla + "=?", legajo, id);
        return legajo;
    }

    // modificamos los datos personales conservando la cuenta, el rol y el legajo originales
    public void modificar(Connection conexion, Personal personal) throws SQLException {
        String tabla = personal.getTipo().tabla;
        ejecutar(conexion, "UPDATE " + tabla + " SET nombre=?,apellido=?,dni=?,mail=?,estado=? WHERE id_" + tabla + "=?", personal.getNombre(), personal.getApellido(), personal.getDni(), personal.getMail(), personal.getEstado(), personal.getId());
    }
}
