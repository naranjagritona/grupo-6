package clases;

// guardamos los datos de los perfiles administrativos y su cuenta de usuario
public class Personal {
    public enum Tipo {
        DIRECTIVO("directivo", "Directivos", 5, "DIR"),
        PRECEPTOR("preceptor", "Preceptores", 4, "PRE"),
        SECRETARIA("secretaria", "Secretaría", 1, "SEC");

        public final String tabla;
        public final String nombre;
        public final int rol;
        public final String prefijo;

        Tipo(String tabla, String nombre, int rol, String prefijo) {
            this.tabla = tabla;
            this.nombre = nombre;
            this.rol = rol;
            this.prefijo = prefijo;
        }

        public String toString() { return nombre; }
    }

    private Integer id;
    private Integer id_usuario;
    private Tipo tipo;
    private String nombre;
    private String apellido;
    private String dni;
    private String mail;
    private String legajo;
    private String estado = "Activo";
    private String usuario;
    private String contraseña;

    // consultamos y modificamos los datos que se cargan en el formulario de administración
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id) { this.id_usuario = id; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }
}
