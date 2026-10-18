package gestorverde.estructuras;

/**
 * Cola genérica (FIFO: el primero en entrar es el primero en salir) implementada
 * con nodos enlazados. Se usa para recorrer por niveles la estructura del vivero
 * y los lotes derivados de un lote (trazabilidad hacia adelante).
 *
 * @param <T> tipo de los elementos
 */
public class Cola<T> {

    private static final class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;

    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }

    /** Agrega un elemento al final. O(1). */
    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamanio++;
    }

    /** Quita y devuelve el elemento del frente. O(1). */
    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("cola");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("cola");
        }
        return frente.dato;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamanio() {
        return tamanio;
    }
}
