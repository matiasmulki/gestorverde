package gestorverde.dominio;

/** Motivo parametrizable de pérdida de plantas (tabla motivo_egreso). */
public class MotivoEgreso {

    private final int id;
    private final String nombre;
    private final String etapa;

    public MotivoEgreso(int id, String nombre, String etapa) {
        this.id = id;
        this.nombre = nombre;
        this.etapa = etapa;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEtapa() { return etapa; }

    @Override
    public String toString() {
        return nombre + " (" + etapa + ")";
    }
}
