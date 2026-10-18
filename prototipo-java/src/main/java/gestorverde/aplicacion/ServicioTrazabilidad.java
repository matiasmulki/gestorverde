package gestorverde.aplicacion;

import gestorverde.dominio.Lote;
import gestorverde.dominio.NodoTrazabilidad;
import gestorverde.dominio.Permiso;
import gestorverde.dominio.VinculoGenealogia;
import gestorverde.estructuras.Cola;
import gestorverde.estructuras.ListaEnlazada;
import gestorverde.estructuras.Pila;
import gestorverde.excepciones.DatosInvalidosException;
import gestorverde.excepciones.GestorVerdeException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.LoteDAO;
import gestorverde.persistencia.Transacciones;
import gestorverde.persistencia.TrazabilidadDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * CU-07 Consultar trazabilidad.
 * Hacia el origen se recorre la genealogía en profundidad con una Pila, de modo
 * que cada rama llega hasta el material certificado antes de pasar a la siguiente.
 * Hacia adelante se recorre en anchura con una Cola: primero los derivados
 * directos, después los derivados de esos, y así sucesivamente.
 */
public class ServicioTrazabilidad extends ServicioBase {

    private final LoteDAO lotes;
    private final TrazabilidadDAO trazabilidad;

    public ServicioTrazabilidad(Transacciones tx, AuditoriaDAO auditoria, LoteDAO lotes,
                                TrazabilidadDAO trazabilidad) {
        super(tx, auditoria);
        this.lotes = lotes;
        this.trazabilidad = trazabilidad;
    }

    public ResultadoTrazabilidad consultar(Sesion sesion, String codigo) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        String buscado = Solicitudes.normalizar(codigo);
        if (buscado == null) {
            throw new DatosInvalidosException("debe ingresar un código de lote.");
        }
        return tx.ejecutar(c -> {
            Lote lote = lotes.buscarPorCodigo(c, buscado)
                    .orElseThrow(() -> new DatosInvalidosException("no existe el lote " + buscado + "."));
            ListaEnlazada<NodoTrazabilidad> antecesores = recorrerHaciaOrigen(c, lote);
            ListaEnlazada<NodoTrazabilidad> derivados = recorrerHaciaAdelante(c, lote);
            auditoria.registrar(c, sesion.getUsuario().getId(), "CONSULTA", "lote", lote.getId(),
                    "Consulta de trazabilidad de " + lote.getCodigo());
            return new ResultadoTrazabilidad(lote, antecesores, derivados);
        });
    }

    /** Recorrido en profundidad (DFS) iterativo con una pila. */
    private ListaEnlazada<NodoTrazabilidad> recorrerHaciaOrigen(Connection c, Lote inicio) throws SQLException {
        ListaEnlazada<NodoTrazabilidad> resultado = new ListaEnlazada<>();
        Set<Long> visitados = new HashSet<>();
        Pila<NodoTrazabilidad> pila = new Pila<>();
        pila.apilar(new NodoTrazabilidad(inicio));

        while (!pila.estaVacia()) {
            NodoTrazabilidad actual = pila.desapilar();
            if (!visitados.add(actual.getLote().getId())) {
                continue;   // ya visitado: protege ante datos con ciclos
            }
            resultado.agregar(actual);

            VinculoGenealogia[] origenes = trazabilidad.origenesDe(c, actual.getLote().getId())
                    .aArreglo(VinculoGenealogia[]::new);
            // Se apilan en orden inverso para que el primero de la lista sea el primero en procesarse.
            for (int i = origenes.length - 1; i >= 0; i--) {
                Lote origen = lotes.buscarPorId(c, origenes[i].getIdLoteRelacionado())
                        .orElseThrow(() -> new SQLException("Genealogía inconsistente"));
                pila.apilar(new NodoTrazabilidad(origen, actual.getNivel() + 1, origenes[i].getTipo(),
                        actual.getLote().getCodigo()));
            }
        }
        return resultado;
    }

    /** Recorrido en anchura (BFS) con una cola. */
    private ListaEnlazada<NodoTrazabilidad> recorrerHaciaAdelante(Connection c, Lote inicio) throws SQLException {
        ListaEnlazada<NodoTrazabilidad> resultado = new ListaEnlazada<>();
        Set<Long> visitados = new HashSet<>();
        visitados.add(inicio.getId());
        Cola<NodoTrazabilidad> cola = new Cola<>();
        cola.encolar(new NodoTrazabilidad(inicio));

        while (!cola.estaVacia()) {
            NodoTrazabilidad actual = cola.desencolar();
            for (VinculoGenealogia v : trazabilidad.derivadosDe(c, actual.getLote().getId())) {
                if (visitados.add(v.getIdLoteRelacionado())) {
                    Lote derivado = lotes.buscarPorId(c, v.getIdLoteRelacionado())
                            .orElseThrow(() -> new SQLException("Genealogía inconsistente"));
                    NodoTrazabilidad nodo = new NodoTrazabilidad(derivado, actual.getNivel() + 1, v.getTipo(),
                            actual.getLote().getCodigo());
                    resultado.agregar(nodo);
                    cola.encolar(nodo);
                }
            }
        }
        return resultado;
    }
}
