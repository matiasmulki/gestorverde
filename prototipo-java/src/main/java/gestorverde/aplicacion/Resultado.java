package gestorverde.aplicacion;

import gestorverde.estructuras.ListaEnlazada;

/** Respuesta de una operación confirmada: un título y las líneas de detalle. */
public class Resultado {

    private final String titulo;
    private final ListaEnlazada<String> detalles;

    /** Constructor con parámetros variables (varargs): acepta cualquier cantidad de líneas. */
    public Resultado(String titulo, String... lineas) {
        this.titulo = titulo;
        this.detalles = new ListaEnlazada<>();
        for (String linea : lineas) {
            detalles.agregar(linea);
        }
    }

    public String getTitulo() { return titulo; }
    public ListaEnlazada<String> getDetalles() { return detalles; }
}
