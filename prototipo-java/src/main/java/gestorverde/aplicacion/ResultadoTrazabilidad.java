package gestorverde.aplicacion;

import gestorverde.dominio.Lote;
import gestorverde.dominio.NodoTrazabilidad;
import gestorverde.estructuras.ListaEnlazada;

/** Resultado de CU-07: el lote consultado, sus antecesores y sus derivados. */
public class ResultadoTrazabilidad {

    private final Lote lote;
    private final ListaEnlazada<NodoTrazabilidad> antecesores;
    private final ListaEnlazada<NodoTrazabilidad> derivados;

    public ResultadoTrazabilidad(Lote lote, ListaEnlazada<NodoTrazabilidad> antecesores,
                                 ListaEnlazada<NodoTrazabilidad> derivados) {
        this.lote = lote;
        this.antecesores = antecesores;
        this.derivados = derivados;
    }

    public Lote getLote() { return lote; }
    public ListaEnlazada<NodoTrazabilidad> getAntecesores() { return antecesores; }
    public ListaEnlazada<NodoTrazabilidad> getDerivados() { return derivados; }

    /** CU-07, curso alternativo 3a: lote migrado de planillas, sin antecesores registrados. */
    public boolean sinAntecesoresRegistrados() {
        return antecesores.tamanio() <= 1 && lote.getMaterialOrigen() == null;
    }
}
