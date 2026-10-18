package gestorverde.aplicacion;

import gestorverde.dominio.Lote;
import java.util.Comparator;

/**
 * Criterios de ordenación del listado de lotes. Cada constante del enum
 * encapsula su propio Comparator: el algoritmo de ordenación es uno solo y
 * el criterio se elige en tiempo de ejecución.
 */
public enum CriterioOrden {
    CODIGO("Código de lote", Comparator.naturalOrder()),
    EXISTENCIA("Existencia (de mayor a menor)",
            Comparator.comparingInt(Lote::getCantidadActual).reversed().thenComparing(Comparator.naturalOrder())),
    ETAPA("Etapa del ciclo de vida",
            Comparator.comparing((Lote l) -> l.getEstado().ordinal()).thenComparing(Comparator.naturalOrder())),
    UBICACION("Ubicación",
            Comparator.comparing((Lote l) -> l.getCodigoUbicacion() == null ? "" : l.getCodigoUbicacion())
                      .thenComparing(Comparator.naturalOrder()));

    private final String descripcion;
    private final Comparator<Lote> comparador;

    CriterioOrden(String descripcion, Comparator<Lote> comparador) {
        this.descripcion = descripcion;
        this.comparador = comparador;
    }

    public String getDescripcion() { return descripcion; }
    public Comparator<Lote> getComparador() { return comparador; }

    /** Texto que muestran las listas desplegables de la interfaz gráfica. */
    @Override
    public String toString() { return descripcion; }
}
