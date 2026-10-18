package gestorverde.aplicacion;

/**
 * Datos que el usuario ingresa en cada formulario. Se usan records de Java:
 * clases inmutables cuyo constructor canónico se genera automáticamente; el
 * constructor compacto normaliza los códigos ingresados.
 */
public final class Solicitudes {

    private Solicitudes() {
    }

    /** CU-03 Registrar injertación. codigoDestino es opcional: por defecto, injertación en el lugar. */
    public record Injertacion(String codigoPortainjerto, String codigoYemas, int cantidad,
                              long idEmpleado, String codigoDestino) {
        public Injertacion {
            codigoPortainjerto = normalizar(codigoPortainjerto);
            codigoYemas = normalizar(codigoYemas);
            codigoDestino = normalizar(codigoDestino);
        }
    }

    /** CU-04 Movilizar lote (total o parcial). */
    public record Movimiento(String codigoLote, String codigoDestino, int cantidad, Long idEmpleado) {
        public Movimiento {
            codigoLote = normalizar(codigoLote);
            codigoDestino = normalizar(codigoDestino);
        }
    }

    /** CU-06 Registrar merma. */
    public record Merma(String codigoLote, int cantidad, int idMotivo, String observacion) {
        public Merma {
            codigoLote = normalizar(codigoLote);
        }
    }

    static String normalizar(String codigo) {
        return (codigo == null || codigo.isBlank()) ? null : codigo.trim().toUpperCase();
    }
}
