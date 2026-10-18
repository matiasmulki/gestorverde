package gestorverde.excepciones;

/** La sesión superó el tiempo máximo sin actividad (RNF-09). */
public class SesionExpiradaException extends AutenticacionException {

    private static final long serialVersionUID = 1L;

    public SesionExpiradaException(int minutos) {
        super("la sesión expiró tras " + minutos + " minutos sin actividad. Vuelva a ingresar.");
    }
}
