package gestorverde.dominio;

/** Integrante del personal que ejecuta las tareas de campo. */
public class Empleado {

    private final long id;
    private final String legajo;
    private final String apellido;
    private final String nombre;

    public Empleado(long id, String legajo, String apellido, String nombre) {
        this.id = id;
        this.legajo = legajo;
        this.apellido = apellido;
        this.nombre = nombre;
    }

    public long getId() { return id; }
    public String getLegajo() { return legajo; }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    @Override
    public String toString() {
        return legajo + " " + getNombreCompleto();
    }
}
