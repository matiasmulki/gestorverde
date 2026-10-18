package gestorverde.dominio;

import gestorverde.estructuras.ListaEnlazada;
import gestorverde.excepciones.ReglaNegocioException;
import java.util.Locale;

/**
 * Componente del patrón Composite que modela la estructura física del vivero
 * (invernadero → sector → cantero). Clientes como el mapa de ocupación tratan
 * igual a un invernadero completo que a un cantero: le piden capacidad y
 * ocupación sin saber cómo se calculan.
 */
public abstract class Ubicacion implements Identificable {

    private final long id;
    private final String codigo;
    private final String nombre;
    private final String tipo;

    protected Ubicacion(long id, String codigo, String nombre, String tipo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    /** Plantas que la ubicación puede alojar. */
    public abstract int getCapacidad();

    /** Plantas que aloja actualmente. */
    public abstract int getOcupacion();

    /** Indica si la ubicación recibe lotes directamente (cantero, cámara, bloque). */
    public abstract boolean admiteLotes();

    /** Hijos en la jerarquía. Una hoja no tiene hijos: devuelve una lista vacía. */
    public ListaEnlazada<Ubicacion> getHijos() {
        return new ListaEnlazada<>();
    }

    public int getDisponible() {
        return getCapacidad() - getOcupacion();
    }

    public double getPorcentajeOcupacion() {
        int capacidad = getCapacidad();
        return capacidad == 0 ? 0.0 : 100.0 * getOcupacion() / capacidad;
    }

    public NivelOcupacion getNivelOcupacion() {
        return NivelOcupacion.clasificar(getCapacidad(), getOcupacion());
    }

    /** RN-05: solo una ubicación receptora aloja lotes, y nunca por encima de su capacidad. */
    public void validarRecepcion(int cantidad) throws ReglaNegocioException {
        if (!admiteLotes()) {
            throw new ReglaNegocioException("RN-05",
                    codigo + " es un " + tipo.toLowerCase() + " y no aloja lotes directamente.",
                    "Elija un cantero u otra ubicación receptora dentro de " + codigo + ".")
                    .conDatos(null, id, cantidad);
        }
        if (cantidad > getDisponible()) {
            throw new ReglaNegocioException("RN-05",
                    "Capacidad insuficiente en " + codigo + ": disponible " + miles(getDisponible())
                            + ", pretendido " + miles(cantidad) + ".",
                    "Elija otra ubicación o traslade una cantidad menor.")
                    .conDatos(null, id, cantidad);
        }
    }

    private static String miles(int cantidad) {
        return String.format(Locale.forLanguageTag("es-AR"), "%,d", cantidad);
    }

    @Override public long getId() { return id; }
    @Override public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }

    @Override
    public String toString() {
        return codigo + " (" + nombre + ")";
    }
}
