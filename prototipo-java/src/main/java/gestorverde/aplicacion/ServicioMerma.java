package gestorverde.aplicacion;

import gestorverde.dominio.EstadoLote;
import gestorverde.dominio.EventoLote;
import gestorverde.dominio.Lote;
import gestorverde.dominio.Permiso;
import gestorverde.dominio.TipoEvento;
import gestorverde.excepciones.DatosInvalidosException;
import gestorverde.excepciones.GestorVerdeException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.EventoDAO;
import gestorverde.persistencia.LoteDAO;
import gestorverde.persistencia.Transacciones;
import java.sql.Connection;
import java.sql.SQLException;

/** CU-06 Registrar merma o baja de plantas con su motivo. */
public class ServicioMerma extends ServicioOperacion<Solicitudes.Merma> {

    private final LoteDAO lotes;
    private final EventoDAO eventos;

    public ServicioMerma(Transacciones tx, AuditoriaDAO auditoria, LoteDAO lotes, EventoDAO eventos) {
        super(tx, auditoria);
        this.lotes = lotes;
        this.eventos = eventos;
    }

    @Override
    protected String getNombreOperacion() {
        return "MERMA";
    }

    @Override
    protected Permiso getPermisoRequerido() {
        return Permiso.REGISTRAR_PRODUCCION;
    }

    @Override
    protected void validarDatos(Solicitudes.Merma s) throws DatosInvalidosException {
        exigirCodigo(s.codigoLote(), "el lote");
        exigirPositivo(s.cantidad());
        if (s.idMotivo() <= 0) {
            throw new DatosInvalidosException("debe elegir un motivo de la lista.");
        }
    }

    @Override
    protected Resultado realizar(Connection c, Sesion sesion, Solicitudes.Merma s)
            throws SQLException, GestorVerdeException {
        Lote lote = lotes.buscarParaActualizar(c, s.codigoLote())
                .orElseThrow(() -> new DatosInvalidosException("no existe el lote " + s.codigoLote() + "."));
        lote.validarConsumo(s.cantidad());   // RN-01

        long usuario = sesion.getUsuario().getId();
        long idEvento = eventos.insertar(c, EventoLote.de(lote.getId(), TipoEvento.MERMA, ahora(), s.cantidad())
                .origen(lote.getIdUbicacion()).motivo(s.idMotivo()).observacion(s.observacion()).construir(), usuario);

        boolean agotado = lote.quedaAgotado(s.cantidad());
        if (agotado) {
            lotes.actualizarEstado(c, lote.getId(), EstadoLote.DESCARTADO);
        }
        auditoria.registrar(c, usuario, "ALTA", "evento_lote", idEvento,
                "Merma de " + s.cantidad() + " en " + lote.getCodigo());

        return new Resultado("Merma registrada",
                lote.getCodigo() + ": existencia " + n(lote.getCantidadActual()) + " -> "
                        + n(lote.getCantidadActual() - s.cantidad()) + (agotado ? " (lote descartado)" : "") + ".",
                "Evento n.º " + idEvento + ". La existencia la actualiza el trigger de la base de datos.");
    }
}
