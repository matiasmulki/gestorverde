package gestorverde.aplicacion;

import gestorverde.dominio.EventoLote;
import gestorverde.dominio.Lote;
import gestorverde.dominio.Permiso;
import gestorverde.dominio.TipoEvento;
import gestorverde.dominio.Ubicacion;
import gestorverde.excepciones.DatosInvalidosException;
import gestorverde.excepciones.GestorVerdeException;
import gestorverde.excepciones.ReglaNegocioException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.EventoDAO;
import gestorverde.persistencia.LoteDAO;
import gestorverde.persistencia.Transacciones;
import gestorverde.persistencia.UbicacionDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * CU-04 Movilizar lote. Si se traslada toda la existencia, el lote cambia de
 * ubicación; si se traslada una parte, nace un lote derivado vinculado por DIVISION (RN-04).
 */
public class ServicioMovimiento extends ServicioOperacion<Solicitudes.Movimiento> {

    private final LoteDAO lotes;
    private final EventoDAO eventos;
    private final UbicacionDAO ubicaciones;

    public ServicioMovimiento(Transacciones tx, AuditoriaDAO auditoria, LoteDAO lotes,
                              EventoDAO eventos, UbicacionDAO ubicaciones) {
        super(tx, auditoria);
        this.lotes = lotes;
        this.eventos = eventos;
        this.ubicaciones = ubicaciones;
    }

    @Override
    protected String getNombreOperacion() {
        return "MOVIMIENTO";
    }

    @Override
    protected Permiso getPermisoRequerido() {
        return Permiso.REGISTRAR_PRODUCCION;
    }

    @Override
    protected void validarDatos(Solicitudes.Movimiento s) throws DatosInvalidosException {
        exigirCodigo(s.codigoLote(), "el lote a movilizar");
        exigirCodigo(s.codigoDestino(), "la ubicación de destino");
        exigirPositivo(s.cantidad());
    }

    @Override
    protected Resultado realizar(Connection c, Sesion sesion, Solicitudes.Movimiento s)
            throws SQLException, GestorVerdeException {
        LocalDateTime fecha = ahora();
        long usuario = sesion.getUsuario().getId();

        Lote lote = lotes.buscarParaActualizar(c, s.codigoLote())
                .orElseThrow(() -> new DatosInvalidosException("no existe el lote " + s.codigoLote() + "."));
        Ubicacion destino = ubicaciones.buscarParaActualizar(c, s.codigoDestino())
                .orElseThrow(() -> new DatosInvalidosException("no existe la ubicación " + s.codigoDestino() + "."));
        if (lote.getIdUbicacion() != null && destino.getId() == lote.getIdUbicacion()) {
            throw new DatosInvalidosException("el lote " + lote.getCodigo() + " ya está en " + destino.getCodigo() + ".");
        }

        lote.validarConsumo(s.cantidad());          // RN-01
        try {
            destino.validarRecepcion(s.cantidad()); // RN-05
        } catch (ReglaNegocioException e) {
            throw e.conDatos(lote.getId(), destino.getId(), s.cantidad());
        }

        if (s.cantidad() == lote.getCantidadActual()) {
            // Traslado total: el lote conserva su identidad y cambia de ubicación
            lotes.actualizarUbicacion(c, lote.getId(), destino.getId());
            long idEvento = eventos.insertar(c, EventoLote.de(lote.getId(), TipoEvento.MOVIMIENTO, fecha, s.cantidad())
                    .origen(lote.getIdUbicacion()).destino(destino.getId()).empleado(s.idEmpleado())
                    .observacion("Traslado total").construir(), usuario);
            auditoria.registrar(c, usuario, "MODIFICACION", "lote", lote.getId(),
                    "Movimiento total de " + lote.getCodigo() + " de " + lote.getCodigoUbicacion()
                            + " a " + destino.getCodigo());
            return new Resultado("Movimiento registrado (traslado total)",
                    lote.getCodigo() + " pasó de " + lote.getCodigoUbicacion() + " a " + destino.getCodigo()
                            + " con sus " + n(s.cantidad()) + " plantas.",
                    "La existencia no varía. Evento n.º " + idEvento + ".");
        }

        // Traslado parcial: salida del lote original y alta de un lote derivado de la misma clase
        long idSalida = eventos.insertar(c, EventoLote.de(lote.getId(), TipoEvento.DIVISION_SALIDA, fecha, s.cantidad())
                .origen(lote.getIdUbicacion()).destino(destino.getId()).empleado(s.idEmpleado())
                .observacion("Traslado parcial").construir(), usuario);
        Lote derivado = lote.crearDerivado(destino.getId(), destino.getCodigo(), fecha);   // llamada polimórfica
        lotes.insertar(c, derivado);
        long idAlta = eventos.insertar(c, EventoLote.de(derivado.getId(), TipoEvento.DIVISION_ALTA, fecha, s.cantidad())
                .origen(lote.getIdUbicacion()).destino(destino.getId()).empleado(s.idEmpleado())
                .relacionadoCon(idSalida).observacion("Lote derivado de " + lote.getCodigo()).construir(), usuario);
        lotes.registrarGenealogia(c, derivado.getId(), lote.getId(), "DIVISION", idAlta);
        auditoria.registrar(c, usuario, "ALTA", "lote", derivado.getId(),
                "División de " + lote.getCodigo() + ": " + s.cantidad() + " a " + destino.getCodigo()
                        + " -> " + derivado.getCodigo());

        return new Resultado("Movimiento registrado (traslado parcial)",
                "Se creó el lote derivado " + derivado.getCodigo() + " (" + derivado.getClase().getDescripcion()
                        + ", " + derivado.describirCombinacion() + ") con " + n(s.cantidad())
                        + " plantas en " + destino.getCodigo() + ".",
                lote.getCodigo() + " queda con " + n(lote.getCantidadActual() - s.cantidad()) + " plantas en "
                        + lote.getCodigoUbicacion() + ".",
                "Vínculo de genealogía: " + derivado.getCodigo() + " <- " + lote.getCodigo() + " (DIVISION).");
    }
}
