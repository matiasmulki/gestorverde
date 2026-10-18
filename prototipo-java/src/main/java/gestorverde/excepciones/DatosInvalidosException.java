package gestorverde.excepciones;

/** Error en los datos ingresados por el usuario (formato, rango, inexistencia). */
public class DatosInvalidosException extends GestorVerdeException {

    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getMensajeParaUsuario() {
        return "Dato inválido: " + getMessage();
    }
}
