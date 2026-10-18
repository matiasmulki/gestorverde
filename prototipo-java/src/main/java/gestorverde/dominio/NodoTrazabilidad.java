package gestorverde.dominio;

/**
 * Un lote dentro del recorrido de trazabilidad: su nivel, el tipo de vínculo
 * y el código del lote con el que se relaciona en el nivel anterior.
 */
public class NodoTrazabilidad {

    private final Lote lote;
    private final int nivel;
    private final String vinculo;
    private final String codigoRelacionado;

    /** Nodo inicial del recorrido (el lote consultado). */
    public NodoTrazabilidad(Lote lote) {
        this(lote, 0, "LOTE CONSULTADO", null);
    }

    public NodoTrazabilidad(Lote lote, int nivel, String vinculo, String codigoRelacionado) {
        this.lote = lote;
        this.nivel = nivel;
        this.vinculo = vinculo;
        this.codigoRelacionado = codigoRelacionado;
    }

    public Lote getLote() { return lote; }
    public int getNivel() { return nivel; }
    public String getVinculo() { return vinculo; }
    public String getCodigoRelacionado() { return codigoRelacionado; }
}
