package gestorverde.dominio;

import java.util.Objects;

/** Variedad de copa o de portainjerto del catálogo controlado (RF-14). */
public class Cultivar {

    private final int id;
    private final String nombre;

    public Cultivar(int id, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El cultivar debe tener nombre");
        }
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cultivar)) return false;
        return id == ((Cultivar) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
