package gestorverde.dominio;

import java.time.LocalDateTime;

/** Operación rechazada por una regla de negocio y registrada para su análisis (RF-02). */
public class IntentoRechazado {

    private final LocalDateTime fechaHora;
    private final String usuario;
    private final String operacion;
    private final String regla;
    private final String codigoLote;
    private final Integer cantidadPretendida;
    private final String motivo;

    public IntentoRechazado(LocalDateTime fechaHora, String usuario, String operacion, String regla,
                            String codigoLote, Integer cantidadPretendida, String motivo) {
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.operacion = operacion;
        this.regla = regla;
        this.codigoLote = codigoLote;
        this.cantidadPretendida = cantidadPretendida;
        this.motivo = motivo;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getUsuario() { return usuario; }
    public String getOperacion() { return operacion; }
    public String getRegla() { return regla; }
    public String getCodigoLote() { return codigoLote; }
    public Integer getCantidadPretendida() { return cantidadPretendida; }
    public String getMotivo() { return motivo; }
}
