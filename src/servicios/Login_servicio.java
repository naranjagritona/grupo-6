package servicios;

import clases.Usuario;
import dao.Usuario_dao;

public class Login_servicio {
    
	//Instanciamos la clase para que después se haga 
	//la consulta en la bd
    private Usuario_dao usuarioDao;

    public Login_servicio() {
        this.usuarioDao = new Usuario_dao();
    }


    public Usuario autenticarUsuario(String nombre_usuario, String contraseña) {
       //Verifica que los campos de usuario no esten vacios
        if (nombre_usuario == null || nombre_usuario.trim().isEmpty() || contraseña == null || contraseña.trim().isEmpty()) {
            return null;
        }

      //hace la consulta en la BD 
        return usuarioDao.autenticar(nombre_usuario.trim(), contraseña);
    }
}