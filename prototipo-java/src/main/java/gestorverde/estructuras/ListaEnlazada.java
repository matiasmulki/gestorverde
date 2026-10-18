package gestorverde.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

/**
 * Lista simplemente enlazada genérica. Se usa para la historia de eventos de
 * un lote, los hijos de cada ubicación y los resultados de las consultas.
 * Mantiene referencias al primer y al último nodo para agregar al final en O(1).
 *
 * @param <T> tipo de los elementos
 */
public class ListaEnlazada<T> implements Iterable<T> {

    /** Nodo interno: encapsulado, el código cliente nunca lo ve. */
    private static final class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamanio;

    /** Crea una lista vacía. */
    public ListaEnlazada() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }

    /** Agrega al final de la lista. O(1). */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            primero = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamanio++;
    }

    /** Agrega al principio de la lista. O(1). */
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = primero;
        primero = nuevo;
        if (ultimo == null) {
            ultimo = nuevo;
        }
        tamanio++;
    }

    /** Devuelve el elemento de la posición indicada. O(n). */
    public T obtener(int indice) {
        validarIndice(indice);
        Nodo<T> actual = primero;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** Elimina y devuelve el elemento de la posición indicada. O(n). */
    public T eliminar(int indice) {
        validarIndice(indice);
        Nodo<T> eliminado;
        if (indice == 0) {
            eliminado = primero;
            primero = primero.siguiente;
            if (primero == null) {
                ultimo = null;
            }
        } else {
            Nodo<T> anterior = primero;
            for (int i = 0; i < indice - 1; i++) {
                anterior = anterior.siguiente;
            }
            eliminado = anterior.siguiente;
            anterior.siguiente = eliminado.siguiente;
            if (eliminado == ultimo) {
                ultimo = anterior;
            }
        }
        tamanio--;
        return eliminado.dato;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    /**
     * Copia los elementos a un arreglo, que es la representación que usan los
     * algoritmos de ordenación y búsqueda binaria (acceso directo por índice).
     * Recibe el constructor del arreglo (por ejemplo Lote[]::new) porque Java
     * no permite crear arreglos de un tipo genérico con new T[n].
     */
    public T[] aArreglo(IntFunction<T[]> generador) {
        T[] arreglo = generador.apply(tamanio);
        int i = 0;
        for (T dato : this) {
            arreglo[i++] = dato;
        }
        return arreglo;
    }

    /** Crea una lista a partir de un arreglo, conservando el orden. */
    public static <T> ListaEnlazada<T> desdeArreglo(T[] arreglo) {
        ListaEnlazada<T> lista = new ListaEnlazada<>();
        for (T dato : arreglo) {
            lista.agregar(dato);
        }
        return lista;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice " + indice + " fuera de rango (tamaño " + tamanio + ")");
        }
    }

    /** Permite recorrer la lista con for-each. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = primero;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}
