package gestorverde.estructuras;

/**
 * Se lanza al intentar quitar o consultar un elemento de una estructura vacía.
 * Es una excepción no verificada (RuntimeException) porque indica un error de
 * programación: el código cliente debe consultar estaVacia() antes de operar.
 */
public class EstructuraVaciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstructuraVaciaException(String estructura) {
        super("La " + estructura + " está vacía");
    }
}
