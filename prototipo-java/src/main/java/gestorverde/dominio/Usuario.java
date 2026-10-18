package gestorverde.dominio;

/** Usuario del sistema con su rol. La clave nunca se guarda en memoria, solo su hash en la base. */
public class Usuario {

    private final long id;
    private final String nombreUsuario;
    private final Rol rol;
    private final Long idEmpleado;

    public Usuario(long id, String nombreUsuario, Rol rol, Long idEmpleado) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.idEmpleado = idEmpleado;
    }

    public boolean tienePermiso(Permiso permiso) {
        return rol.otorga(permiso);
    }

    public long getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public Rol getRol() { return rol; }
    public Long getIdEmpleado() { return idEmpleado; }

    @Override
    public String toString() {
        return nombreUsuario + " (" + rol + ")";
    }
}
