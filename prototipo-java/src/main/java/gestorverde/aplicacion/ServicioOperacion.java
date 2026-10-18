package gestorverde.aplicacion;

import gestorverde.dominio.Permiso;
import gestorverde.excepciones.DatosInvalidosException;
import gestorverde.excepciones.GestorVerdeException;
import gestorverde.excepciones.PersistenciaException;
import gestorverde.excepciones.ReglaNegocioException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.Transacciones;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Patrón Template Method: define el esqueleto común de toda operación que
 * modifica la producción y deja a cada subclase los pasos que varían.
 *
 *   1. verificar sesión y permiso       (común)
 *   2. validar los datos ingresados     (abstracto)
 *   3. realizar la operación en una transacción (abstracto)
 *   4. si una regla la rechaza, registrar el intento (común, RF-02)
 *
 * @param <S> tipo de la solicitud que recibe la operación
 */
public abstract class ServicioOperacion<S> extends ServicioBase {

    protected ServicioOperacion(Transacciones tx, AuditoriaDAO auditoria) {
        super(tx, auditoria);
    }

    /** Método plantilla: es final para que ninguna subclase altere el orden de los pasos. */
    public final Resultado ejecutar(Sesion sesion, S solicitud) throws GestorVerdeException {
        sesion.exigir(getPermisoRequerido());
        validarDatos(solicitud);
        try {
            return tx.ejecutar(c -> realizar(c, sesion, solicitud));
        } catch (ReglaNegocioException rechazo) {
            registrarRechazo(sesion, rechazo);
            throw rechazo;   // se relanza para que la interfaz informe causa y acción correctiva
        }
    }

    /**
     * El intento rechazado se guarda en una transacción separada, porque la de
     * la operación ya se deshizo (TP2, sección 1.5).
     */
    private void registrarRechazo(Sesion sesion, ReglaNegocioException rechazo) {
        try {
            tx.ejecutar(c -> {
                auditoria.registrarRechazo(c, sesion.getUsuario().getId(), getNombreOperacion(), rechazo);
                return null;
            });
        } catch (GestorVerdeException e) {
            // No se oculta el rechazo original si falla su registro; solo se avisa.
            String detalle = (e instanceof PersistenciaException)
                    ? ((PersistenciaException) e).getMensajeParaUsuario() : e.getMessage();
            System.err.println("Aviso: no se pudo registrar el intento rechazado: " + detalle);
        }
    }

    /** Nombre de la operación tal como se registra en intento_rechazado.operacion. */
    protected abstract String getNombreOperacion();

    protected abstract Permiso getPermisoRequerido();

    protected abstract void validarDatos(S solicitud) throws DatosInvalidosException;

    protected abstract Resultado realizar(Connection c, Sesion sesion, S solicitud)
            throws SQLException, GestorVerdeException;
}
