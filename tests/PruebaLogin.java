import clases.Usuario;
import servicios.Login_servicio;
import seguridad.Contrasenas;

public class PruebaLogin {
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        System.out.println("OK: " + mensaje);
    }
    public static void main(String[] args) {
        String hash = "$2y$10$WCdu3aRxzqwB/05dS.otJu0/DxrmOhKi/ktaDMHmnWVgG/3n.XHVm";
        comprobar(Contrasenas.verificar("Colegio2026", hash), "contraseña bcrypt generada por PHP");
        comprobar(!Contrasenas.verificar("incorrecta", hash), "rechaza una contraseña incorrecta");
        comprobar(hash.equals(Contrasenas.paraGuardar(hash)), "no vuelve a cifrar el hash al editar");
        String nueva = Contrasenas.paraGuardar("PruebaEscolar2026");
        comprobar(nueva.startsWith("$2y$") && Contrasenas.verificar("PruebaEscolar2026", nueva), "guarda nuevas claves con bcrypt compatible con PHP");
        comprobar(Contrasenas.verificar("claveAnterior", "claveAnterior") && !Contrasenas.verificar("otra", "claveAnterior"), "compatibilidad con cuentas anteriores");
        comprobar(!Contrasenas.verificar("x", "$2y$hash-incompleto"), "rechaza un hash inválido");
        if (args.length == 0 || !args[0].equals("--base")) return;
        Login_servicio login = new Login_servicio();
        String[][] cuentas = {{"admin", "123", "1"}, {"ana.castillo", "Colegio2026", "2"}, {"sofia.fernandez", "Colegio2026", "3"}, {"abril.mendoza", "Colegio2026", "3"}};
        // Estas consultas solo comprueban el acceso; no cambian registros de la base.
        for (String[] cuenta : cuentas) {
            Usuario u = login.autenticarUsuario(cuenta[0], cuenta[1]);
            comprobar(u != null && u.getId_rol() == Integer.parseInt(cuenta[2]), "acceso y rol de " + cuenta[0]);
        }
        comprobar(login.autenticarUsuario("admin", "claveIncorrecta") == null, "no acepta claves incorrectas en la base");
        comprobar(login.autenticarUsuario("usuario_que_no_existe_2026", "123") == null, "no acepta usuarios inexistentes");
        comprobar(login.autenticarUsuario("", "") == null, "rechaza campos vacíos");
        dao.Curso_dao cursos = new dao.Curso_dao();
        comprobar(cursos.obtenerTodos().size() == 6, "carga los seis cursos sin consultar división");
        clases.Curso curso = cursos.obtenerPorId(1);
        comprobar(curso != null && curso.getAño() == 1 && curso.getModalidad() != null, "carga los datos completos del curso");
        comprobar(!new dao.Inscripcion_dao().obtenerTodos().isEmpty(), "carga las inscripciones después del acceso");
    }
}
