package gestorverde.dominio;

/**
 * Tipos de evento de la tabla tipo_evento. El signo indica cómo afecta a la
 * existencia del lote: +1 suma, -1 resta y 0 no la modifica.
 */
public enum TipoEvento {
    SIEMBRA(1, "Siembra", 1),
    INGRESO_YEMAS(2, "Ingreso de yemas", 1),
    TRASPLANTE(3, "Trasplante", 0),
    MOVIMIENTO(4, "Movimiento", 0),
    DIVISION_SALIDA(5, "Salida por división", -1),
    DIVISION_ALTA(6, "Alta por división", 1),
    CONSUMO_INJERTACION(7, "Consumo por injertación", -1),
    INJERTACION(8, "Injertación", 1),
    MERMA(9, "Merma", -1),
    TRATAMIENTO(10, "Tratamiento", 0),
    EGRESO_VENTA(11, "Egreso por venta", -1),
    SALDO_INICIAL(12, "Saldo inicial migrado", 1),
    REVERSION_ALTA(13, "Reversión de alta", -1),
    REVERSION_BAJA(14, "Reversión de baja", 1),
    CAMBIO_ETAPA(15, "Cambio de etapa", 0);

    private final int id;
    private final String nombre;
    private final int signo;

    TipoEvento(int id, String nombre, int signo) {
        this.id = id;
        this.nombre = nombre;
        this.signo = signo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getSigno() { return signo; }

    public static TipoEvento desdeId(int id) {
        for (TipoEvento t : values()) {
            if (t.id == id) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de evento desconocido: " + id);
    }
}
