package clases;

import dao.Usuario_dao;

// guardamos los datos de la cuenta que se utiliza para ingresar
public class Usuario {
    private Integer id_usuario;
    private Integer id_rol;
    private String nombre_usuario;
    private String contraseña;

    public Usuario() {}

    public Usuario(Integer id_usuario, Integer id_rol, String nombre_usuario, String contraseña) {
        this.id_usuario = id_usuario;
        this.id_rol = id_rol;
        this.nombre_usuario = nombre_usuario;
        this.contraseña = contraseña;
    }

    // busca la cuenta y verifica su contraseña antes de permitir el acceso
    public static Usuario autenticar(String nombre_usuario, String contraseña) {
        Usuario_dao usuarioDao = new Usuario_dao();
        return usuarioDao.autenticar(nombre_usuario, contraseña);
    }

    // consultamos y modificamos los datos del objeto con sus métodos de acceso
    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Integer getId_rol() {
        return id_rol;
    }

    public void setId_rol(Integer id_rol) {
        this.id_rol = id_rol;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
}
