package gestorverde.excepciones;

/**
 * Raíz de la jerarquía de excepciones propias del sistema.
 * Es abstracta: nunca se lanza directamente, sino alguna de sus subclases,
 * lo que permite capturar "cualquier error del sistema" con un único catch.
 */
public abstract class GestorVerdeException extends Exception {

    private static final long serialVersionUID = 1L;

    protected GestorVerdeException(String mensaje) {
        super(mensaje);
    }

    protected GestorVerdeException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Texto que se muestra al usuario; cada subclase decide su formato. */
    public abstract String getMensajeParaUsuario();
}
