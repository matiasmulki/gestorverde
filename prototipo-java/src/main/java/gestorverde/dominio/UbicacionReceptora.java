package gestorverde.dominio;

/** Hoja del patrón Composite: cantero, cámara de germinación o bloque de incremento. */
public class UbicacionReceptora extends Ubicacion {

    private final int capacidadMaxima;
    private int ocupacion;

    public UbicacionReceptora(long id, String codigo, String nombre, String tipo,
                              int capacidadMaxima, int ocupacion) {
        super(id, codigo, nombre, tipo);
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad de " + codigo + " debe ser positiva");
        }
        if (ocupacion < 0) {
            throw new IllegalArgumentException("La ocupación no puede ser negativa");
        }
        this.capacidadMaxima = capacidadMaxima;
        this.ocupacion = ocupacion;
    }

    @Override
    public int getCapacidad() {
        return capacidadMaxima;
    }

    @Override
    public int getOcupacion() {
        return ocupacion;
    }

    @Override
    public boolean admiteLotes() {
        return true;
    }

    /** Ajusta la ocupación en memoria luego de un ingreso o egreso confirmado. */
    public void registrarMovimiento(int cantidad) {
        if (ocupacion + cantidad < 0) {
            throw new IllegalStateException("La ocupación no puede quedar negativa");
        }
        ocupacion += cantidad;
    }
}
