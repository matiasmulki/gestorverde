package gestorverde.dominio;

/** Estados del ciclo de vida de un lote; los identificadores son los de la tabla estado_lote. */
public enum EstadoLote {
    GERMINACION(1, "Germinación"),
    PLANTIN(2, "Plantín"),
    DESARROLLO_PIE(3, "En desarrollo (portainjerto)"),
    INJERTADO(4, "Injertado"),
    DESARROLLO_INJ(5, "En desarrollo (planta injertada)"),
    RUSTIFICACION(6, "Rustificación"),
    LISTO_VENTA(7, "Listo para venta"),
    RESERVADO(8, "Reservado"),
    VENDIDO(9, "Vendido"),
    DESCARTADO(10, "Descartado"),
    CERRADO(11, "Cerrado (consumido)"),
    DISPONIBLE(12, "Yemas disponibles");

    private final int id;
    private final String nombre;

    EstadoLote(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    /** Un lote en estos estados ya no participa de la producción. */
    public boolean esFinal() {
        return this == VENDIDO || this == DESCARTADO || this == CERRADO;
    }

    /** Convierte el identificador de la base de datos en la constante del enum. */
    public static EstadoLote desdeId(int id) {
        for (EstadoLote e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de lote desconocido: " + id);
    }
}
