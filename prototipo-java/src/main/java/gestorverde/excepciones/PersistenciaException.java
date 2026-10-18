package gestorverde.excepciones;

import java.sql.SQLException;

/**
 * Envuelve los errores de JDBC para que las capas superiores no dependan de
 * java.sql. Traduce los códigos de MySQL más frecuentes a mensajes claros.
 */
public class PersistenciaException extends GestorVerdeException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getMensajeParaUsuario() {
        Throwable c = getCause();
        if (c instanceof SQLException) {
            SQLException sql = (SQLException) c;
            switch (sql.getErrorCode()) {
                case 1045: return "No se pudo ingresar a MySQL: usuario o clave de la base incorrectos.";
                case 1049: return "La base de datos 'gestorverde' no existe. Ejecute 01_esquema.sql.";
                case 1644: return "La base de datos rechazó la operación: " + sql.getMessage();
                case 3819: return "La base de datos rechazó la operación por una restricción CHECK: " + sql.getMessage();
                default:   return getMessage() + " (MySQL " + sql.getErrorCode() + ": " + sql.getMessage() + ")";
            }
        }
        return getMessage();
    }
}
