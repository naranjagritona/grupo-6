package servicios;

import clases.Personal;
import dao.Personal_dao;
import java.util.*;

// validamos los datos y coordinamos el registro de los perfiles administrativos
public class Personal_servicio {
    private final Personal_dao dao;

    public Personal_servicio() { this(new Personal_dao()); }

    // preparamos el dao para guardar la cuenta y el perfil en la misma transacción
    public Personal_servicio(Personal_dao dao) { this.dao = dao; }

    // comprobamos los datos obligatorios antes de enviarlos a la base de datos
    private void validar(Personal personal) {
        if (personal.getTipo() == null) throw new IllegalArgumentException("Seleccioná el tipo de personal.");
        personal.setNombre(Inscripcion_servicio.motivo(personal.getNombre()));
        personal.setApellido(Inscripcion_servicio.motivo(personal.getApellido()));
        if (personal.getNombre().length() > 200 || personal.getApellido().length() > 200) throw new IllegalArgumentException("El nombre y el apellido admiten hasta 200 caracteres.");
        if (personal.getDni() == null || !personal.getDni().trim().matches("[0-9]{6,12}")) throw new IllegalArgumentException("Ingresá un DNI válido usando solamente números.");
        personal.setDni(personal.getDni().trim());
        if (personal.getMail() == null || personal.getMail().trim().length() > 254 || !personal.getMail().trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Ingresá un correo electrónico válido.");
        personal.setMail(personal.getMail().trim());
        if (!List.of("Activo", "Inactivo").contains(personal.getEstado())) throw new IllegalArgumentException("Seleccioná un estado válido.");
    }

    // consultamos los perfiles que puede administrar la cuenta de la sesión
    public List<Map<String, Object>> listar(Personal.Tipo tipo, Integer usuario) throws Exception {
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            return dao.listar(conexion, tipo);
        });
    }

    // creamos la cuenta y el perfil y devolvemos el legajo que se generó automáticamente
    public String registrar(Personal personal, Integer usuario) throws Exception {
        validar(personal);
        if (personal.getUsuario() == null || personal.getUsuario().isBlank() || personal.getUsuario().trim().length() > 200) throw new IllegalArgumentException("Ingresá un nombre de usuario válido.");
        personal.setUsuario(personal.getUsuario().trim());
        if (personal.getContraseña() == null || personal.getContraseña().length() < 8) throw new IllegalArgumentException("La contraseña debe tener al menos ocho caracteres.");
        String clave = seguridad.Contrasenas.paraGuardar(personal.getContraseña());
        return dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            if (dao.usuarioExiste(conexion, personal.getUsuario())) throw new IllegalArgumentException("El nombre de usuario ya está registrado.");
            int cuenta = dao.crearUsuario(conexion, personal, clave);
            return dao.crear(conexion, personal, cuenta);
        });
    }

    // guardamos los datos editados sin regenerar el legajo ni modificar la contraseña
    public void modificar(Personal personal, Integer usuario) throws Exception {
        validar(personal);
        dao.transaccion(conexion -> {
            Permisos_servicio.verificar(conexion, dao, usuario, 1, 5);
            var anterior = dao.obtener(conexion, personal.getTipo(), personal.getId());
            if (((Number) anterior.get("id_usuario")).intValue() == usuario && "Inactivo".equals(personal.getEstado())) throw new IllegalArgumentException("No podés dar de baja tu propia cuenta.");
            dao.modificar(conexion, personal);
            return null;
        });
    }
}
