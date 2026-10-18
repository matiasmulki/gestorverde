package gestorverde.algoritmos;

import gestorverde.estructuras.ListaEnlazada;
import java.util.Comparator;

/**
 * Algoritmos de ordenación genéricos. Reciben el criterio como Comparator, de
 * modo que el mismo algoritmo ordena lotes por código, por existencia o por
 * ubicación (polimorfismo a través de la interfaz Comparator).
 */
public final class Ordenamiento {

    /** Por debajo de este tamaño, quicksort delega en inserción (más rápido en tramos chicos). */
    private static final int UMBRAL_INSERCION = 10;

    private Ordenamiento() {
        // Clase utilitaria: no se instancia.
    }

    /** Ordena una lista enlazada y devuelve una nueva lista ordenada. */
    @SuppressWarnings("unchecked")
    public static <T> ListaEnlazada<T> ordenar(ListaEnlazada<T> lista, Comparator<? super T> criterio) {
        // El arreglo es local y no sale del método: el cast desde Object[] es seguro.
        T[] datos = lista.aArreglo(n -> (T[]) new Object[n]);
        quickSort(datos, criterio);
        return ListaEnlazada.desdeArreglo(datos);
    }

    /**
     * Quicksort con pivote "mediana de tres". Complejidad promedio O(n log n);
     * la mediana de tres evita el peor caso O(n²) con datos ya ordenados.
     */
    public static <T> void quickSort(T[] datos, Comparator<? super T> criterio) {
        if (datos == null || datos.length < 2) {
            return;
        }
        quickSort(datos, 0, datos.length - 1, criterio);
    }

    private static <T> void quickSort(T[] a, int izq, int der, Comparator<? super T> c) {
        while (izq < der) {
            if (der - izq < UMBRAL_INSERCION) {
                insercion(a, izq, der, c);
                return;
            }
            int p = particionar(a, izq, der, c);
            // Se recurre sobre la mitad más chica para acotar la profundidad de la pila a O(log n).
            if (p - izq < der - p) {
                quickSort(a, izq, p - 1, c);
                izq = p + 1;
            } else {
                quickSort(a, p + 1, der, c);
                der = p - 1;
            }
        }
    }

    private static <T> int particionar(T[] a, int izq, int der, Comparator<? super T> c) {
        int medio = izq + (der - izq) / 2;
        // Mediana de tres: deja el menor en izq, la mediana en medio y el mayor en der.
        if (c.compare(a[medio], a[izq]) < 0) intercambiar(a, medio, izq);
        if (c.compare(a[der], a[izq]) < 0) intercambiar(a, der, izq);
        if (c.compare(a[der], a[medio]) < 0) intercambiar(a, der, medio);
        intercambiar(a, medio, der - 1);
        T pivote = a[der - 1];

        int i = izq;
        int j = der - 1;
        while (true) {
            while (c.compare(a[++i], pivote) < 0) { /* avanza */ }
            while (c.compare(a[--j], pivote) > 0) { /* retrocede */ }
            if (i >= j) {
                break;
            }
            intercambiar(a, i, j);
        }
        intercambiar(a, i, der - 1);
        return i;
    }

    /** Ordenación por inserción: O(n²) pero estable y eficiente en tramos cortos. */
    public static <T> void insercion(T[] a, int izq, int der, Comparator<? super T> c) {
        for (int i = izq + 1; i <= der; i++) {
            T actual = a[i];
            int j = i - 1;
            while (j >= izq && c.compare(a[j], actual) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = actual;
        }
    }

    private static <T> void intercambiar(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
