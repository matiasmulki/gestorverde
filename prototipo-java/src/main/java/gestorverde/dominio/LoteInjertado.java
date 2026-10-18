package gestorverde.dominio;

import java.time.LocalDateTime;

/** Lote de plantas injertadas: combina una copa y un portainjerto. */
public class LoteInjertado extends Lote {

    /** Constructor para reconstruir un lote existente. */
    public LoteInjertado(long id, String codigo, int idCampania, Cultivar copa, Cultivar portainjerto,
                         Long idUbicacion, String codigoUbicacion, EstadoLote estado, int cantidad,
                         boolean saldoInicial, LocalDateTime fechaCreacion) {
        super(id, codigo, idCampania, copa, portainjerto, null, idUbicacion, codigoUbicacion,
                estado, cantidad, saldoInicial, fechaCreacion);
        if (copa == null || portainjerto == null) {
            throw new IllegalArgumentException("Una planta injertada debe tener copa y portainjerto");
        }
    }

    /**
     * Constructor sobrecargado para el lote que nace de una injertación (CU-03):
     * todavía no tiene clave ni código, su existencia la fija el evento de alta
     * y su estado inicial es siempre "Injertado". Delega en el constructor completo con this(...).
     */
    public LoteInjertado(int idCampania, Cultivar copa, Cultivar portainjerto,
                         Long idUbicacion, String codigoUbicacion, LocalDateTime fecha) {
        this(0, null, idCampania, copa, portainjerto, idUbicacion, codigoUbicacion,
                EstadoLote.INJERTADO, 0, false, fecha);
    }

    @Override
    public ClaseLote getClase() {
        return ClaseLote.INJERTADA;
    }

    @Override
    public String describirCombinacion() {
        return getCopa().getNombre() + " / " + getPortainjerto().getNombre();
    }

    @Override
    public Lote crearDerivado(Long idUbicacionDestino, String codigoDestino, LocalDateTime fecha) {
        return new LoteInjertado(0, null, getIdCampania(), getCopa(), getPortainjerto(),
                idUbicacionDestino, codigoDestino, getEstado(), 0, false, fecha);
    }
}
