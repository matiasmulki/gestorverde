package gestorverde.dominio;

import gestorverde.estructuras.ListaEnlazada;

/**
 * Compuesto del patrón Composite: vivero, invernadero, nave o sector.
 * No aloja lotes; su capacidad y su ocupación son la suma de las de sus hijos.
 */
public class UbicacionContenedora extends Ubicacion {

    private final ListaEnlazada<Ubicacion> hijos;

    public UbicacionContenedora(long id, String codigo, String nombre, String tipo) {
        super(id, codigo, nombre, tipo);
        this.hijos = new ListaEnlazada<>();
    }

    public void agregarHijo(Ubicacion hijo) {
        if (hijo == this) {
            throw new IllegalArgumentException("Una ubicación no puede contenerse a sí misma");
        }
        hijos.agregar(hijo);
    }

    @Override
    public ListaEnlazada<Ubicacion> getHijos() {
        return hijos;
    }

    /** Recorre los hijos y suma: la llamada es polimórfica, cada hijo resuelve la suya. */
    @Override
    public int getCapacidad() {
        int total = 0;
        for (Ubicacion hijo : hijos) {
            total += hijo.getCapacidad();
        }
        return total;
    }

    @Override
    public int getOcupacion() {
        int total = 0;
        for (Ubicacion hijo : hijos) {
            total += hijo.getOcupacion();
        }
        return total;
    }

    @Override
    public boolean admiteLotes() {
        return false;
    }
}
