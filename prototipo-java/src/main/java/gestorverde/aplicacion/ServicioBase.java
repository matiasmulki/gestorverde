package gestorverde.aplicacion;

import gestorverde.excepciones.DatosInvalidosException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.Transacciones;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Superclase abstracta de todos los servicios: concentra las dependencias y
 * las utilidades comunes, que las subclases heredan sin repetir código.
 */
public abstract class ServicioBase {

    protected final Transacciones tx;
    protected final AuditoriaDAO auditoria;

    protected ServicioBase(Transacciones tx, AuditoriaDAO auditoria) {
        if (tx == null || auditoria == null) {
            throw new IllegalArgumentException("El servicio requiere transacciones y auditoría");
        }
        this.tx = tx;
        this.auditoria = auditoria;
    }

    /** Configuración regional argentina: separador de miles con punto (5.200). */
    public static final Locale ES_AR = Locale.forLanguageTag("es-AR");

    /** Formatea una cantidad con separador de miles. */
    protected static String n(int cantidad) {
        return String.format(ES_AR, "%,d", cantidad);
    }

    /** Momento de la operación, al minuto (la fecha del evento es evidencia de trazabilidad). */
    protected LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    }

    /** Valida que un código obligatorio haya sido ingresado. */
    protected static String exigirCodigo(String codigo, String campo) throws DatosInvalidosException {
        if (codigo == null || codigo.isBlank()) {
            throw new DatosInvalidosException("debe indicar " + campo + ".");
        }
        return codigo;
    }

    /** Valida que una cantidad sea positiva. */
    protected static void exigirPositivo(int cantidad) throws DatosInvalidosException {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("la cantidad debe ser mayor que cero (se ingresó " + cantidad + ").");
        }
    }
}
