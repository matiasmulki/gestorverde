package gestorverde.excepciones;

/** Credenciales incorrectas o permiso insuficiente. */
public class AutenticacionException extends GestorVerdeException {

    private static final long serialVersionUID = 1L;

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getMensajeParaUsuario() {
        return "Acceso denegado: " + getMessage();
    }
}
