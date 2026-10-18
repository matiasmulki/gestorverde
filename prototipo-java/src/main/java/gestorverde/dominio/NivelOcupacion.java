package gestorverde.dominio;

/** Clasificación de la ocupación usada en el mapa del vivero (RF-11). */
public enum NivelOcupacion {
    VACIA, NORMAL, ALTA, COMPLETA, SIN_CAPACIDAD;

    /** Umbral de ocupación alta definido en el diseño de la interfaz (TP2, sección 2.7). */
    public static final double UMBRAL_ALTA = 85.0;

    public static NivelOcupacion clasificar(int capacidad, int ocupacion) {
        if (capacidad <= 0) {
            return SIN_CAPACIDAD;
        } else if (ocupacion == 0) {
            return VACIA;
        } else if (ocupacion >= capacidad) {
            return COMPLETA;
        } else if (100.0 * ocupacion / capacidad >= UMBRAL_ALTA) {
            return ALTA;
        } else {
            return NORMAL;
        }
    }
}
