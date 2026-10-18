package gestorverde.dominio;

import java.time.LocalDateTime;

/**
 * Hecho inmutable de la historia de un lote (RN-02): todos sus atributos son
 * final y no hay métodos que los modifiquen. Como tiene muchos datos
 * opcionales, se construye con el patrón Builder en lugar de un constructor
 * con once parámetros.
 */
public final class EventoLote {

    private final long id;
    private final long idLote;
    private final TipoEvento tipo;
    private final LocalDateTime fechaHora;
    private final int cantidad;
    private final Long idUbicacionOrigen;
    private final Long idUbicacionDestino;
    private final Long idEmpleado;
    private final Integer idMotivo;
    private final Long idEventoRelacionado;
    private final String observacion;
    // Datos descriptivos que se completan al leer la historia (para mostrar)
    private final String codigoOrigen;
    private final String codigoDestino;
    private final String nombreEmpleado;
    private final String nombreMotivo;

    private EventoLote(Builder b) {
        this.id = b.id;
        this.idLote = b.idLote;
        this.tipo = b.tipo;
        this.fechaHora = b.fechaHora;
        this.cantidad = b.cantidad;
        this.idUbicacionOrigen = b.idUbicacionOrigen;
        this.idUbicacionDestino = b.idUbicacionDestino;
        this.idEmpleado = b.idEmpleado;
        this.idMotivo = b.idMotivo;
        this.idEventoRelacionado = b.idEventoRelacionado;
        this.observacion = b.observacion;
        this.codigoOrigen = b.codigoOrigen;
        this.codigoDestino = b.codigoDestino;
        this.nombreEmpleado = b.nombreEmpleado;
        this.nombreMotivo = b.nombreMotivo;
    }

    /** Punto de entrada del Builder: los datos obligatorios van en la llamada. */
    public static Builder de(long idLote, TipoEvento tipo, LocalDateTime fechaHora, int cantidad) {
        return new Builder(idLote, tipo, fechaHora, cantidad);
    }

    /** Efecto sobre la existencia del lote: signo del tipo × cantidad. */
    public int getEfecto() {
        return tipo.getSigno() * cantidad;
    }

    public long getId() { return id; }
    public long getIdLote() { return idLote; }
    public TipoEvento getTipo() { return tipo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getCantidad() { return cantidad; }
    public Long getIdUbicacionOrigen() { return idUbicacionOrigen; }
    public Long getIdUbicacionDestino() { return idUbicacionDestino; }
    public Long getIdEmpleado() { return idEmpleado; }
    public Integer getIdMotivo() { return idMotivo; }
    public Long getIdEventoRelacionado() { return idEventoRelacionado; }
    public String getObservacion() { return observacion; }
    public String getCodigoOrigen() { return codigoOrigen; }
    public String getCodigoDestino() { return codigoDestino; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public String getNombreMotivo() { return nombreMotivo; }

    /** Constructor paso a paso de un EventoLote. */
    public static final class Builder {
        private long id;
        private final long idLote;
        private final TipoEvento tipo;
        private final LocalDateTime fechaHora;
        private final int cantidad;
        private Long idUbicacionOrigen;
        private Long idUbicacionDestino;
        private Long idEmpleado;
        private Integer idMotivo;
        private Long idEventoRelacionado;
        private String observacion;
        private String codigoOrigen;
        private String codigoDestino;
        private String nombreEmpleado;
        private String nombreMotivo;

        private Builder(long idLote, TipoEvento tipo, LocalDateTime fechaHora, int cantidad) {
            if (tipo == null || fechaHora == null) {
                throw new IllegalArgumentException("El evento requiere tipo y fecha");
            }
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad de un evento debe ser positiva");
            }
            this.idLote = idLote;
            this.tipo = tipo;
            this.fechaHora = fechaHora;
            this.cantidad = cantidad;
        }

        public Builder id(long v) { this.id = v; return this; }
        public Builder origen(Long v) { this.idUbicacionOrigen = v; return this; }
        public Builder destino(Long v) { this.idUbicacionDestino = v; return this; }
        public Builder empleado(Long v) { this.idEmpleado = v; return this; }
        public Builder motivo(Integer v) { this.idMotivo = v; return this; }
        public Builder relacionadoCon(Long v) { this.idEventoRelacionado = v; return this; }
        public Builder observacion(String v) { this.observacion = v; return this; }
        public Builder descripcion(String origen, String destino, String empleado, String motivo) {
            this.codigoOrigen = origen;
            this.codigoDestino = destino;
            this.nombreEmpleado = empleado;
            this.nombreMotivo = motivo;
            return this;
        }

        public EventoLote construir() {
            return new EventoLote(this);
        }
    }
}
