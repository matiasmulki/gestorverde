package gestorverde.dominio;

import gestorverde.excepciones.ReglaNegocioException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Lote de yemas certificadas de una variedad de copa. */
public class LoteYemas extends Lote {

    public LoteYemas(long id, String codigo, int idCampania, Cultivar copa, MaterialCertificado yemas,
                     Long idUbicacion, String codigoUbicacion, EstadoLote estado, int cantidad,
                     boolean saldoInicial, LocalDateTime fechaCreacion) {
        super(id, codigo, idCampania, copa, null, yemas, idUbicacion, codigoUbicacion,
                estado, cantidad, saldoInicial, fechaCreacion);
        if (copa == null) {
            throw new IllegalArgumentException("Un lote de yemas debe indicar su cultivar de copa");
        }
    }

    @Override
    public ClaseLote getClase() {
        return ClaseLote.YEMAS;
    }

    @Override
    public String describirCombinacion() {
        return "yemas " + getCopa().getNombre();
    }

    /** Redefine el comportamiento heredado: un lote de yemas sí aporta yemas, si su certificado está vigente (RN-03). */
    @Override
    public void validarYemasUtilizables(LocalDate fecha) throws ReglaNegocioException {
        if (getMaterialOrigen() == null) {
            throw new ReglaNegocioException("RN-03",
                    "El lote " + getCodigo() + " no tiene material certificado asociado.",
                    "Registre el ingreso del material certificado antes de injertar.")
                    .conDatos(getId(), getIdUbicacion(), null);
        }
        try {
            getMaterialOrigen().verificarVigencia(fecha);
        } catch (ReglaNegocioException e) {
            // Se completa la excepción con el lote involucrado y se relanza.
            throw e.conDatos(getId(), getIdUbicacion(), null);
        }
    }

    @Override
    public Lote crearDerivado(Long idUbicacionDestino, String codigoDestino, LocalDateTime fecha) {
        return new LoteYemas(0, null, getIdCampania(), getCopa(), getMaterialOrigen(),
                idUbicacionDestino, codigoDestino, getEstado(), 0, false, fecha);
    }
}
