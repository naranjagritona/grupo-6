package seguridad;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Contrasenas {
    private Contrasenas() { }

    public static boolean verificar(String ingresada, String guardada) {
        if (ingresada == null || guardada == null) return false;
        // La web guarda un hash bcrypt, por eso no comparamos las dos cadenas directamente.
        if (guardada.startsWith("$2")) {
            return BCrypt.verifyer().verify(ingresada.toCharArray(), guardada).verified;
        }
        // Mantenemos el acceso a las cuentas anteriores que todavía no tienen hash.
        return MessageDigest.isEqual(ingresada.getBytes(StandardCharsets.UTF_8),
                guardada.getBytes(StandardCharsets.UTF_8));
    }

    public static String paraGuardar(String clave) {
        if (clave == null || clave.isBlank()) throw new IllegalArgumentException("Ingresá una contraseña.");
        // Al editar un usuario, conservamos el hash que ya estaba guardado.
        if (clave.matches("\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}")) return clave;
        return BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(10, clave.toCharArray());
    }
}
