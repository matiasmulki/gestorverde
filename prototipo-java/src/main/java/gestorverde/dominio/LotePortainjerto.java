package gestorverde.dominio;

import java.time.LocalDateTime;

/** Lote de plantas de portainjerto (pie) obtenidas de semilla certificada. */
public class LotePortainjerto extends Lote {

    public LotePortainjerto(long id, String codigo, int idCampania, Cultivar portainjerto,
                            MaterialCertificado semilla, Long idUbicacion, String codigoUbicacion,
                            EstadoLote estado, int cantidad, boolean saldoInicial, LocalDateTime fechaCreacion) {
        super(id, codigo, idCampania, null, portainjerto, semilla, idUbicacion, codigoUbicacion,
                estado, cantidad, saldoInicial, fechaCreacion);
        if (portainjerto == null) {
            throw new IllegalArgumentException("Un lote de portainjerto debe indicar su cultivar");
        }
    }

    @Override
    public ClaseLote getClase() {
        return ClaseLote.PORTAINJERTO;
    }

    @Override
    public String describirCombinacion() {
        return "pie " + getPortainjerto().getNombre();
    }

    /** Solo un portainjerto en desarrollo y con existencia puede injertarse. */
    @Override
    public boolean esAptoComoPortainjerto() {
        return getEstado() == EstadoLote.DESARROLLO_PIE && getCantidadActual() > 0;
    }

    @Override
    public Lote crearDerivado(Long idUbicacionDestino, String codigoDestino, LocalDateTime fecha) {
        return new LotePortainjerto(0, null, getIdCampania(), getPortainjerto(), getMaterialOrigen(),
                idUbicacionDestino, codigoDestino, getEstado(), 0, false, fecha);
    }
}
