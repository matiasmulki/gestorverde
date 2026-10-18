package gestorverde.dominio;

import gestorverde.excepciones.ReglaNegocioException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Unidad de trazabilidad del vivero: un conjunto de plantas (o de yemas) que
 * comparten origen, cultivares, ubicación y etapa.
 *
 * Es abstracta porque no existe un "lote genérico": todo lote es de
 * portainjerto, de yemas o de planta injertada. Cada subclase define qué
 * combinación de cultivares admite y qué operaciones puede realizar.
 * Los atributos son privados (encapsulamiento) y solo cambian a través de
 * métodos que hacen cumplir las reglas de negocio.
 */
public abstract class Lote implements Identificable, Comparable<Lote> {

    private long id;
    private String codigo;
    private final int idCampania;
    private final Cultivar copa;
    private final Cultivar portainjerto;
    private final MaterialCertificado materialOrigen;
    private Long idUbicacion;
    private String codigoUbicacion;
    private EstadoLote estado;
    private int cantidadActual;
    private final boolean saldoInicial;
    private final LocalDateTime fechaCreacion;

    /** Constructor completo: lo usan las subclases al reconstruir un lote leído de la base. */
    protected Lote(long id, String codigo, int idCampania, Cultivar copa, Cultivar portainjerto,
                   MaterialCertificado materialOrigen, Long idUbicacion, String codigoUbicacion,
                   EstadoLote estado, int cantidadActual, boolean saldoInicial, LocalDateTime fechaCreacion) {
        if (estado == null) {
            throw new IllegalArgumentException("El lote debe tener un estado");
        }
        if (cantidadActual < 0) {
            throw new IllegalArgumentException("La existencia de un lote nunca es negativa (RN-01)");
        }
        this.id = id;
        this.codigo = codigo;
        this.idCampania = idCampania;
        this.copa = copa;
        this.portainjerto = portainjerto;
        this.materialOrigen = materialOrigen;
        this.idUbicacion = idUbicacion;
        this.codigoUbicacion = codigoUbicacion;
        this.estado = estado;
        this.cantidadActual = cantidadActual;
        this.saldoInicial = saldoInicial;
        this.fechaCreacion = fechaCreacion;
    }

    // ------------------------------------------------------------------
    // Métodos abstractos: cada clase de lote los resuelve a su manera
    // ------------------------------------------------------------------

    /** Clase del lote, tal como se guarda en la columna lote.clase. */
    public abstract ClaseLote getClase();

    /** Descripción legible de la combinación de cultivares. */
    public abstract String describirCombinacion();

    /**
     * Crea un lote nuevo de la misma clase y cultivares, vacío, en otra ubicación.
     * Lo usa el traslado parcial (CU-04): cada subclase sabe construir su propio tipo.
     */
    public abstract Lote crearDerivado(Long idUbicacionDestino, String codigoDestino, LocalDateTime fecha);

    // ------------------------------------------------------------------
    // Comportamiento que las subclases pueden redefinir (polimorfismo)
    // ------------------------------------------------------------------

    /** Indica si el lote puede usarse como pie en una injertación. Por defecto, no. */
    public boolean esAptoComoPortainjerto() {
        return false;
    }

    /** Verifica que el lote pueda aportar yemas a la fecha indicada. Por defecto, no puede. */
    public void validarYemasUtilizables(LocalDate fecha) throws ReglaNegocioException {
        throw new ReglaNegocioException("CU-03",
                "El lote " + codigo + " es de clase " + getClase().getDescripcion() + " y no contiene yemas.",
                "Seleccione un lote de clase Yemas.").conDatos(id, idUbicacion, null);
    }

    // ------------------------------------------------------------------
    // Reglas de negocio comunes a todos los lotes
    // ------------------------------------------------------------------

    /** RN-01: ninguna operación puede consumir más que la existencia del lote. */
    public void validarConsumo(int cantidad) throws ReglaNegocioException {
        if (estado.esFinal()) {
            throw new ReglaNegocioException("RN-01",
                    "El lote " + codigo + " está en estado " + estado.getNombre() + " y no admite operaciones.",
                    "Seleccione un lote activo.").conDatos(id, idUbicacion, cantidad);
        }
        if (cantidad > cantidadActual) {
            throw new ReglaNegocioException("RN-01",
                    "Se pretende usar " + miles(cantidad) + " plantas del lote " + codigo
                            + ", pero su existencia es " + miles(cantidadActual) + ".",
                    "Ingrese una cantidad menor o igual a " + miles(cantidadActual) + ".")
                    .conDatos(id, idUbicacion, cantidad);
        }
    }

    /** Valida que el lote pueda ser pie de injertación (precondición de CU-03). */
    public void validarAptoComoPortainjerto() throws ReglaNegocioException {
        if (!esAptoComoPortainjerto()) {
            throw new ReglaNegocioException("CU-03",
                    "El lote " + codigo + " (" + getClase().getDescripcion() + ", " + estado.getNombre()
                            + ") no está apto para injertar.",
                    "Seleccione un lote de portainjerto en desarrollo.").conDatos(id, idUbicacion, null);
        }
    }

    /** Cantidad con separador de miles a la usanza argentina (3.050). */
    protected static String miles(int cantidad) {
        return String.format(Locale.forLanguageTag("es-AR"), "%,d", cantidad);
    }

    /** Indica si consumir la cantidad dejaría al lote sin existencia. */
    public boolean quedaAgotado(int cantidadConsumida) {
        return cantidadActual - cantidadConsumida == 0;
    }

    /** Asigna clave y código después de insertar el lote; solo puede hacerse una vez. */
    public void asignarIdentidad(long id, String codigo) {
        if (this.id != 0) {
            throw new IllegalStateException("El lote " + this.codigo + " ya tiene identidad");
        }
        this.id = id;
        this.codigo = codigo;
    }

    /** Actualiza la existencia en memoria tras un evento (la base la mantiene por trigger). */
    public void aplicarEfecto(int efecto) {
        if (cantidadActual + efecto < 0) {
            throw new IllegalStateException("La existencia no puede quedar negativa");
        }
        cantidadActual += efecto;
    }

    public void cambiarEstado(EstadoLote nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public void reubicar(Long idUbicacion, String codigoUbicacion) {
        this.idUbicacion = idUbicacion;
        this.codigoUbicacion = codigoUbicacion;
    }

    // ------------------------------------------------------------------
    // Getters (no hay setters públicos: el estado se modifica por reglas)
    // ------------------------------------------------------------------
    @Override public long getId() { return id; }
    @Override public String getCodigo() { return codigo; }
    public int getIdCampania() { return idCampania; }
    public Cultivar getCopa() { return copa; }
    public Cultivar getPortainjerto() { return portainjerto; }
    public MaterialCertificado getMaterialOrigen() { return materialOrigen; }
    public Long getIdUbicacion() { return idUbicacion; }
    public String getCodigoUbicacion() { return codigoUbicacion; }
    public EstadoLote getEstado() { return estado; }
    public int getCantidadActual() { return cantidadActual; }
    public boolean esSaldoInicial() { return saldoInicial; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    /** Orden natural: por código de lote (L0001, L0002...). */
    @Override
    public int compareTo(Lote otro) {
        return this.codigo.compareTo(otro.codigo);
    }

    @Override
    public String toString() {
        return codigo + " · " + getClase().getDescripcion() + " · " + describirCombinacion()
                + " · " + cantidadActual + " u. · " + estado.getNombre();
    }
}
