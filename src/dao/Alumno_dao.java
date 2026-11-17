package dao;

import clases.Alumno;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de alumno en la base de datos
public class Alumno_dao implements Dao<Alumno, Integer> {
    // guardamos el alumno con la cuenta y el responsable dentro de la misma transacción
    public int crear(java.sql.Connection db, Alumno a) throws java.sql.SQLException {
        // la matrícula se genera acá también para que todas las altas usen el mismo formato
        int anio =
                java.time.Year.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires"))
                        .getValue();
        a.setMatricula(Alumno.generarMatricula(anio, a.getId_usuario()));
        String consulta =
                "INSERT INTO alumno(id_usuario,id_responsable,nombre,apellido,dni,matricula,estado)"
                        + " VALUES(?,?,?,?,?,?,?)";
        try (java.sql.PreparedStatement ps =
                db.prepareStatement(consulta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, a.getId_usuario());
            ps.setObject(2, a.getId_responsable());
            ps.setObject(3, a.getNombre());
            ps.setObject(4, a.getApellido());
            ps.setObject(5, a.getDni());
            ps.setObject(6, a.getMatricula());
            ps.setObject(7, a.getEstado());
            ps.executeUpdate();
            // usamos el ID que generó la base para relacionar el registro con los demás datos
            try (java.sql.ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next())
                    throw new java.sql.SQLException("No se obtuvo el identificador del registro.");
                return claves.getInt(1);
            }
        }
    }

    // conservamos la matrícula y la cuenta al modificar los datos administrativos del alumno
    public void actualizarAdministracion(java.sql.Connection db, Alumno a)
            throws java.sql.SQLException {
        String consulta =
                "UPDATE alumno SET id_responsable=?,nombre=?,apellido=?,dni=?,estado=? WHERE"
                        + " id_alumno=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, a.getId_responsable());
            ps.setObject(2, a.getNombre());
            ps.setObject(3, a.getApellido());
            ps.setObject(4, a.getDni());
            ps.setObject(5, a.getEstado());
            ps.setObject(6, a.getId_alumno());
            ps.executeUpdate();
        }
    }

    // buscamos únicamente alumnos activos para las sugerencias del formulario de inscripción
    public java.util.List<Alumno> activosParaInscripcion() {
        return obtenerTodos().stream().filter(a -> "Activo".equals(a.getEstado())).toList();
    }

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Alumno objeto) {
        int anio =
                java.time.Year.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires"))
                        .getValue();
        objeto.setMatricula(Alumno.generarMatricula(anio, objeto.getId_usuario()));
        String consulta =
                "INSERT INTO alumno (id_usuario, id_responsable, nombre, apellido, dni, matricula,"
                        + " estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            // reemplazamos los signos ? por los atributos del objeto recibido
            ps.setObject(1, objeto.getId_usuario());
            ps.setObject(2, objeto.getId_responsable());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getDni());
            ps.setString(6, objeto.getMatricula());
            ps.setString(7, objeto.getEstado());

            // ejecutamos la consulta en la BD
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el alumno: " + error.getMessage());
            error.printStackTrace(); // imprime el error
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Alumno obtenerPorId(Integer id) {
        String consulta = "SELECT * FROM alumno WHERE id_alumno=?";
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, id);
            try (ResultSet registros = ps.executeQuery()) {
                return registros.next() ? leerEntidad(registros) : null;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el registro de alumno.", error);
        }
    }

    // consulta los registros y los devuelve como una lista de objetos
    @Override
    public List<Alumno> obtenerTodos() {
        String consulta = "SELECT * FROM alumno";
        List<Alumno> lista = new ArrayList<>();
        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet registros = ps.executeQuery()) {
            while (registros.next()) {
                lista.add(leerEntidad(registros));
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo consultar el listado de alumno.", error);
        }
        return lista;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Alumno objeto) {
        // al editar conservamos el año de alta y volvemos a armar la matrícula con su cuenta
        String matricula = objeto.getMatricula();
        int anio =
                java.time.Year.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires"))
                        .getValue();
        if (matricula != null && matricula.matches("[0-9]{4}-[0-9]+")) {
            anio = Integer.parseInt(matricula.substring(0, 4));
        }
        objeto.setMatricula(Alumno.generarMatricula(anio, objeto.getId_usuario()));

        String consulta =
                "UPDATE alumno SET id_usuario = ?, id_responsable = ?, nombre = ?, apellido = ?,"
                        + " dni = ?, matricula = ?, estado = ? WHERE id_alumno = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setObject(1, objeto.getId_usuario());
            ps.setObject(2, objeto.getId_responsable());
            ps.setString(3, objeto.getNombre());
            ps.setString(4, objeto.getApellido());
            ps.setString(5, objeto.getDni());
            ps.setString(6, objeto.getMatricula());
            ps.setString(7, objeto.getEstado());

            // seteamos la clave primaria para modificar los datos del id del alumno deseado (dnd
            // esta el WHERE)
            ps.setInt(8, objeto.getId_alumno());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el alumno: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM alumno WHERE id_alumno = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el alumno: " + error.getMessage());
            error.printStackTrace();
        }
    }

    public clases.Alumno porUsuario(java.sql.Connection db, int usuario)
            throws java.sql.SQLException {
        var filas = seleccionar(db, "SELECT * FROM alumno WHERE id_usuario=? FOR UPDATE", usuario);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    public void inactivar(java.sql.Connection db, int usuario) throws java.sql.SQLException {
        String consulta = "UPDATE alumno SET estado='Inactivo' WHERE id_usuario=?";
        try (java.sql.PreparedStatement ps = db.prepareStatement(consulta)) {
            ps.setObject(1, usuario);
            ps.executeUpdate();
        }
    }

    protected Alumno leerEntidad(java.sql.ResultSet registros) throws java.sql.SQLException {
        Alumno objeto = new Alumno();
        objeto.setId_alumno((Integer) registros.getObject("id_alumno"));
        objeto.setId_usuario((Integer) registros.getObject("id_usuario"));
        objeto.setId_responsable((Integer) registros.getObject("id_responsable"));
        objeto.setNombre(registros.getString("nombre"));
        objeto.setApellido(registros.getString("apellido"));
        objeto.setDni(registros.getString("dni"));
        objeto.setMatricula(registros.getString("matricula"));
        objeto.setEstado(registros.getString("estado"));
        return objeto;
    }

    public java.util.List<clases.Alumno> delCurso(java.sql.Connection db, int curso)
            throws java.sql.SQLException {
        return seleccionar(
                db,
                "SELECT DISTINCT a.* FROM alumno a JOIN inscripcion i ON i.id_alumno=a.id_alumno"
                        + " WHERE i.id_curso=?",
                curso);
    }

    // ejecutamos la consulta con sus valores y pasamos cada fila a un objeto Alumno
    private java.util.List<Alumno> seleccionar(
            java.sql.Connection conexion, String consulta, Object... valores)
            throws java.sql.SQLException {
        java.util.List<Alumno> lista = new java.util.ArrayList<>();
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            try (java.sql.ResultSet registros = ps.executeQuery()) {
                while (registros.next()) {
                    lista.add(leerEntidad(registros));
                }
            }
        }
        return lista;
    }

    // recibimos la conexión del servicio para no separar las consultas de una transacción
    public java.util.List<Alumno> listarTabla(java.sql.Connection conexion)
            throws java.sql.SQLException {
        return seleccionar(conexion, "SELECT * FROM alumno");
    }

    public Alumno buscar(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM alumno WHERE id_alumno=?", id);
        return filas.isEmpty() ? null : filas.getFirst();
    }

    // bloqueamos el registro mientras se revisa para evitar dos cambios al mismo tiempo
    public Alumno requerir(java.sql.Connection conexion, int id) throws java.sql.SQLException {
        var filas = seleccionar(conexion, "SELECT * FROM alumno WHERE id_alumno=? FOR UPDATE", id);
        if (filas.isEmpty())
            throw new IllegalArgumentException("El registro ya no existe. Actualizá el listado.");
        return filas.getFirst();
    }

    public int contar(java.sql.Connection conexion) throws java.sql.SQLException {
        String consulta = "SELECT COUNT(*) FROM alumno";
        try (java.sql.PreparedStatement ps = conexion.prepareStatement(consulta);
                java.sql.ResultSet registros = ps.executeQuery()) {
            registros.next();
            return registros.getInt(1);
        }
    }
}
