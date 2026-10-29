package dao;

import clases.Periodo_Lectivo;

import conexion.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// consultamos y modificamos los registros de periodo lectivo en la base de datos
public class Periodo_Lectivo_dao implements Dao<Periodo_Lectivo, Integer> {

    // inserta el registro recibido en la tabla correspondiente
    @Override
    public void insertar(Periodo_Lectivo objeto) {
        String consulta =
                "INSERT INTO periodo_lectivo (nombre, tipo, fecha_inicio, fecha_fin, estado) VALUES"
                        + " (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getTipo());
            ps.setObject(3, objeto.getFecha_inicio());
            ps.setObject(4, objeto.getFecha_fin());
            ps.setString(5, objeto.getEstado());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al insertar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // consulta un registro usando su identificador
    @Override
    public Periodo_Lectivo obtenerPorId(Integer id) {
        Periodo_Lectivo periodo = null;
        String consulta = "SELECT * FROM periodo_lectivo WHERE id_periodo_lectivo = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    periodo = new Periodo_Lectivo();

                    periodo.setId_periodo(rs.getInt("id_periodo_lectivo"));
                    periodo.setNombre(rs.getString("nombre"));
                    periodo.setTipo(rs.getString("tipo"));

                    Date fInicio = rs.getDate("fecha_inicio");
                    periodo.setFecha_inicio(fInicio != null ? fInicio.toLocalDate() : null);

                    Date fFin = rs.getDate("fecha_fin");
                    periodo.setFecha_fin(fFin != null ? fFin.toLocalDate() : null);

                    periodo.setEstado(rs.getString("estado"));
                }
            }

        } catch (SQLException error) {
            System.err.println("Error al obtener el período lectivo por ID: " + error.getMessage());
            error.printStackTrace();
        }

        return periodo;
    }

    @Override
    public List<Periodo_Lectivo> obtenerTodos() {
        List<Periodo_Lectivo> listaPeriodos = new ArrayList<>();

        String consulta = "SELECT * FROM periodo_lectivo";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Periodo_Lectivo periodo = new Periodo_Lectivo();

                // la columna real en la base de datos es id_periodo_lectivo
                periodo.setId_periodo(rs.getInt("id_periodo_lectivo"));
                periodo.setNombre(rs.getString("nombre"));
                periodo.setTipo(rs.getString("tipo"));

                Date fInicio = rs.getDate("fecha_inicio");
                periodo.setFecha_inicio(fInicio != null ? fInicio.toLocalDate() : null);

                Date fFin = rs.getDate("fecha_fin");
                periodo.setFecha_fin(fFin != null ? fFin.toLocalDate() : null);

                periodo.setEstado(rs.getString("estado"));

                listaPeriodos.add(periodo);
            }

        } catch (SQLException error) {
            System.err.println(
                    "Error al obtener la lista de períodos lectivos: " + error.getMessage());
            error.printStackTrace();
        }

        return listaPeriodos;
    }

    // actualiza los datos del registro seleccionado
    @Override
    public void modificar(Periodo_Lectivo objeto) {
        String consulta =
                "UPDATE periodo_lectivo SET nombre = ?, tipo = ?, fecha_inicio = ?, fecha_fin = ?,"
                        + " estado = ? WHERE id_periodo_lectivo = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, objeto.getNombre());
            ps.setString(2, objeto.getTipo());
            ps.setObject(3, objeto.getFecha_inicio());
            ps.setObject(4, objeto.getFecha_fin());
            ps.setString(5, objeto.getEstado());
            ps.setInt(6, objeto.getId_periodo());

            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al modificar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }

    // elimina el registro seleccionado si las relaciones lo permiten
    @Override
    public void eliminar(Integer id) {
        String consulta = "DELETE FROM periodo_lectivo WHERE id_periodo_lectivo = ?";

        try (Connection conexion = Conexion.getConnection();
                PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException error) {
            System.err.println("Error al eliminar el período lectivo: " + error.getMessage());
            error.printStackTrace();
        }
    }
}
