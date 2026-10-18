package gestorverde.dominio;

/** Clase de lote: coincide con la columna lote.clase (ENUM de MySQL). */
public enum ClaseLote {
    PORTAINJERTO("Portainjerto"),
    YEMAS("Yemas"),
    INJERTADA("Planta injertada");

    private final String descripcion;

    ClaseLote(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
