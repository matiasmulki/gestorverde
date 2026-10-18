package gestorverde.estructuras;

/**
 * Pila genérica (LIFO: el último en entrar es el primero en salir) implementada
 * con nodos enlazados. Se usa para recorrer en profundidad la genealogía de un
 * lote hasta el material certificado de origen (CU-07).
 *
 * @param <T> tipo de los elementos
 */
public class Pila<T> {

    private static final class Nodo<T> {
        private final T dato;
        private final Nodo<T> debajo;

        private Nodo(T dato, Nodo<T> debajo) {
            this.dato = dato;
            this.debajo = debajo;
        }
    }

    private Nodo<T> tope;
    private int tamanio;

    public Pila() {
        this.tope = null;
        this.tamanio = 0;
    }

    /** Coloca un elemento en el tope. O(1). */
    public void apilar(T dato) {
        tope = new Nodo<>(dato, tope);
        tamanio++;
    }

    /** Quita y devuelve el elemento del tope. O(1). */
    public T desapilar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("pila");
        }
        T dato = tope.dato;
        tope = tope.debajo;
        tamanio--;
        return dato;
    }

    /** Devuelve el elemento del tope sin quitarlo. */
    public T verTope() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("pila");
        }
        return tope.dato;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int tamanio() {
        return tamanio;
    }
}
