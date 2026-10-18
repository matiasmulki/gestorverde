package gestorverde.excepciones;

/**
 * Se lanza cuando una operación viola una regla de negocio (RN-01 a RN-08)
 * o una precondición de un caso de uso. Informa la regla, la causa y la
 * acción correctiva (RNF-06) y conserva los datos del intento para RF-02.
 */
public class ReglaNegocioException extends GestorVerdeException {

    private static final long serialVersionUID = 1L;

    private final String codigoRegla;
    private final String accionCorrectiva;
    private Long idLote;
    private Long idUbicacion;
    private Integer cantidadPretendida;

    public ReglaNegocioException(String codigoRegla, String causa, String accionCorrectiva) {
        super(causa);
        this.codigoRegla = codigoRegla;
        this.accionCorrectiva = accionCorrectiva;
    }

    /** Asocia el lote, la ubicación y la cantidad del intento rechazado (estilo fluido). */
    public ReglaNegocioException conDatos(Long idLote, Long idUbicacion, Integer cantidad) {
        this.idLote = idLote;
        this.idUbicacion = idUbicacion;
        this.cantidadPretendida = cantidad;
        return this;
    }

    public String getCodigoRegla() { return codigoRegla; }
    public String getAccionCorrectiva() { return accionCorrectiva; }
    public Long getIdLote() { return idLote; }
    public Long getIdUbicacion() { return idUbicacion; }
    public Integer getCantidadPretendida() { return cantidadPretendida; }

    @Override
    public String getMensajeParaUsuario() {
        return "[" + codigoRegla + "] " + getMessage() + System.lineSeparator()
                + "    Acción sugerida: " + accionCorrectiva;
    }
}
