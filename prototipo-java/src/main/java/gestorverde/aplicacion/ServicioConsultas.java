package gestorverde.aplicacion;

import gestorverde.algoritmos.Busqueda;
import gestorverde.algoritmos.Ordenamiento;
import gestorverde.dominio.Empleado;
import gestorverde.dominio.EventoLote;
import gestorverde.dominio.IntentoRechazado;
import gestorverde.dominio.Lote;
import gestorverde.dominio.MotivoEgreso;
import gestorverde.dominio.NivelOcupacion;
import gestorverde.dominio.Permiso;
import gestorverde.dominio.Ubicacion;
import gestorverde.estructuras.Cola;
import gestorverde.estructuras.ListaEnlazada;
import gestorverde.excepciones.DatosInvalidosException;
import gestorverde.excepciones.GestorVerdeException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.CatalogoDAO;
import gestorverde.persistencia.EventoDAO;
import gestorverde.persistencia.LoteDAO;
import gestorverde.persistencia.Transacciones;
import gestorverde.persistencia.UbicacionDAO;
import java.util.Comparator;

/** Consultas de solo lectura: mapa de ocupación, listados, búsquedas e historia de un lote. */
public class ServicioConsultas extends ServicioBase {

    private final LoteDAO lotes;
    private final UbicacionDAO ubicaciones;
    private final EventoDAO eventos;
    private final CatalogoDAO catalogos;

    public ServicioConsultas(Transacciones tx, AuditoriaDAO auditoria, LoteDAO lotes,
                             UbicacionDAO ubicaciones, EventoDAO eventos, CatalogoDAO catalogos) {
        super(tx, auditoria);
        this.lotes = lotes;
        this.ubicaciones = ubicaciones;
        this.eventos = eventos;
        this.catalogos = catalogos;
    }

    public ListaEnlazada<Empleado> listarEmpleados(Sesion sesion) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        return tx.ejecutar(c -> catalogos.listarEmpleadosActivos(c));
    }

    public ListaEnlazada<MotivoEgreso> listarMotivos(Sesion sesion) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        return tx.ejecutar(c -> catalogos.listarMotivosEgreso(c));
    }

    /** Estructura completa del vivero con capacidad y ocupación calculadas (RF-11). */
    public Ubicacion obtenerEstructura(Sesion sesion) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        return tx.ejecutar(c -> ubicaciones.cargarArbol(c));
    }

    /**
     * Recorre el árbol por niveles (recorrido en anchura con una Cola) y devuelve
     * las ubicaciones receptoras con ocupación alta o completa, ordenadas de
     * mayor a menor porcentaje.
     */
    public ListaEnlazada<Ubicacion> ubicacionesCriticas(Ubicacion raiz) {
        ListaEnlazada<Ubicacion> criticas = new ListaEnlazada<>();
        Cola<Ubicacion> pendientes = new Cola<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            Ubicacion actual = pendientes.desencolar();
            if (actual.admiteLotes()) {
                NivelOcupacion nivel = actual.getNivelOcupacion();
                if (nivel == NivelOcupacion.ALTA || nivel == NivelOcupacion.COMPLETA) {
                    criticas.agregar(actual);
                }
            }
            for (Ubicacion hijo : actual.getHijos()) {
                pendientes.encolar(hijo);
            }
        }
        Comparator<Ubicacion> porPorcentaje =
                Comparator.comparingDouble(Ubicacion::getPorcentajeOcupacion).reversed();
        return Ordenamiento.ordenar(criticas, porPorcentaje);
    }

    /** Lotes con existencia, ordenados por el criterio elegido con quicksort. */
    public ListaEnlazada<Lote> listarLotes(Sesion sesion, CriterioOrden criterio) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        ListaEnlazada<Lote> todos = tx.ejecutar(c -> lotes.listarConExistencia(c));
        return Ordenamiento.ordenar(todos, criterio.getComparador());
    }

    /**
     * Busca un lote por su código con búsqueda binaria: primero ordena por
     * código y luego divide el intervalo a la mitad en cada comparación.
     */
    public Lote buscarPorCodigo(Sesion sesion, String codigo) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        String buscado = Solicitudes.normalizar(codigo);
        if (buscado == null) {
            throw new DatosInvalidosException("debe ingresar un código de lote.");
        }
        Lote[] ordenados = tx.ejecutar(c -> lotes.listarTodos(c)).aArreglo(Lote[]::new);
        Ordenamiento.quickSort(ordenados, Comparator.naturalOrder());
        int posicion = Busqueda.binaria(ordenados, buscado, Lote::getCodigo, Comparator.naturalOrder());
        if (posicion < 0) {
            throw new DatosInvalidosException("no existe el lote " + buscado + ".");
        }
        return ordenados[posicion];
    }

    /** Búsqueda secuencial por texto en la combinación de cultivares (por ejemplo, "lisbon"). */
    public ListaEnlazada<Lote> buscarPorCultivar(Sesion sesion, String texto) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        if (texto == null || texto.isBlank()) {
            throw new DatosInvalidosException("debe ingresar parte del nombre del cultivar.");
        }
        String patron = texto.trim().toLowerCase(ES_AR);
        ListaEnlazada<Lote> todos = tx.ejecutar(c -> lotes.listarConExistencia(c));
        ListaEnlazada<Lote> encontrados =
                Busqueda.secuencial(todos, l -> l.describirCombinacion().toLowerCase(ES_AR).contains(patron));
        return Ordenamiento.ordenar(encontrados, Comparator.naturalOrder());
    }

    /** Historia completa del lote en orden cronológico (línea de tiempo, RF-09). */
    public ListaEnlazada<EventoLote> historia(Sesion sesion, Lote lote) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR);
        return tx.ejecutar(c -> eventos.listarPorLote(c, lote.getId()));
    }

    /** Suma los efectos de la historia: debe coincidir con la existencia registrada (control Q-11 del TP2). */
    public int saldoSegunEventos(ListaEnlazada<EventoLote> historia) {
        int saldo = 0;
        for (EventoLote e : historia) {
            saldo += e.getEfecto();
        }
        return saldo;
    }

    /** Últimos intentos rechazados (RF-02). Requiere permiso de auditoría. */
    public ListaEnlazada<IntentoRechazado> ultimosRechazos(Sesion sesion, int limite) throws GestorVerdeException {
        sesion.exigir(Permiso.CONSULTAR_AUDITORIA);
        return tx.ejecutar(c -> auditoria.ultimosRechazos(c, limite));
    }
}
