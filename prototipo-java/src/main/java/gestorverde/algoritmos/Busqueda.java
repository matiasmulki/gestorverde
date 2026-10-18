package gestorverde.algoritmos;

import gestorverde.estructuras.ListaEnlazada;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

/** Algoritmos de búsqueda genéricos. */
public final class Busqueda {

    private Busqueda() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Búsqueda binaria sobre un arreglo ordenado por la clave indicada.
     * Complejidad O(log n): con 2.000 lotes se resuelve en 11 comparaciones como máximo.
     *
     * @param ordenado   arreglo ordenado ascendentemente por la clave
     * @param clave      valor buscado
     * @param extractor  función que obtiene la clave de cada elemento
     * @param comparador orden de las claves
     * @return la posición del elemento o -1 si no está
     */
    public static <T, K> int binaria(T[] ordenado, K clave, Function<T, K> extractor,
                                     Comparator<? super K> comparador) {
        int bajo = 0;
        int alto = ordenado.length - 1;
        while (bajo <= alto) {
            int medio = (bajo + alto) >>> 1;   // evita el desbordamiento de (bajo + alto) / 2
            int cmp = comparador.compare(extractor.apply(ordenado[medio]), clave);
            if (cmp == 0) {
                return medio;
            } else if (cmp < 0) {
                bajo = medio + 1;
            } else {
                alto = medio - 1;
            }
        }
        return -1;
    }

    /**
     * Búsqueda secuencial: recorre toda la lista y devuelve los elementos que
     * cumplen la condición. O(n); sirve para criterios que no tienen orden,
     * como "contiene el texto".
     */
    public static <T> ListaEnlazada<T> secuencial(ListaEnlazada<T> lista, Predicate<? super T> condicion) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (T elemento : lista) {
            if (condicion.test(elemento)) {
                resultado.agregar(elemento);
            }
        }
        return resultado;
    }
}
