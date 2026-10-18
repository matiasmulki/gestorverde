package gestorverde.dominio;

/** Arista del grafo de genealogía: el lote relacionado y el tipo de vínculo. */
public class VinculoGenealogia {

    private final long idLoteRelacionado;
    private final String tipo;

    public VinculoGenealogia(long idLoteRelacionado, String tipo) {
        this.idLoteRelacionado = idLoteRelacionado;
        this.tipo = tipo;
    }

    public long getIdLoteRelacionado() { return idLoteRelacionado; }
    public String getTipo() { return tipo; }
}
