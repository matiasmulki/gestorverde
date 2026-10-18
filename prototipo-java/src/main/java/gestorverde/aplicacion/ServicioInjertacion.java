package gestorverde.aplicacion;

import gestorverde.dominio.EstadoLote;
import gestorverde.dominio.EventoLote;
import gestorverde.dominio.Lote;
import gestorverde.dominio.LoteInjertado;
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
 * CU-03 Registrar injertación: consume un lote de portainjerto y uno de yemas
 * y crea el lote de plantas injertadas, con su genealogía, en una sola transacción.
 */
public class ServicioInjertacion extends ServicioOperacion<Solicitudes.Injertacion> {

    private final LoteDAO lotes;
    private final EventoDAO eventos;
    private final UbicacionDAO ubicaciones;

    public ServicioInjertacion(Transacciones tx, AuditoriaDAO auditoria, LoteDAO lotes,
                               EventoDAO eventos, UbicacionDAO ubicaciones) {
        super(tx, auditoria);
        this.lotes = lotes;
        this.eventos = eventos;
        this.ubicaciones = ubicaciones;
    }

    @Override
    protected String getNombreOperacion() {
        return "INJERTACION";
    }

    @Override
    protected Permiso getPermisoRequerido() {
        return Permiso.INJERTAR;
    }

    @Override
    protected void validarDatos(Solicitudes.Injertacion s) throws DatosInvalidosException {
        exigirCodigo(s.codigoPortainjerto(), "el lote de portainjerto");
        exigirCodigo(s.codigoYemas(), "el lote de yemas");
        exigirPositivo(s.cantidad());
        if (s.codigoPortainjerto().equals(s.codigoYemas())) {
            throw new DatosInvalidosException("el portainjerto y las yemas deben ser lotes distintos.");
        }
    }

    @Override
    protected Resultado realizar(Connection c, Sesion sesion, Solicitudes.Injertacion s)
            throws SQLException, GestorVerdeException {
        LocalDateTime fecha = ahora();
        int cantidad = s.cantidad();

        // 1. Bloqueo de los lotes involucrados (evita consumos concurrentes del mismo lote)
        Lote pie = lotes.buscarParaActualizar(c, s.codigoPortainjerto())
                .orElseThrow(() -> new DatosInvalidosException("no existe el lote " + s.codigoPortainjerto() + "."));
        Lote yemas = lotes.buscarParaActualizar(c, s.codigoYemas())
                .orElseThrow(() -> new DatosInvalidosException("no existe el lote " + s.codigoYemas() + "."));

        // 2. Validaciones: las reglas viven en las entidades y se invocan polimórficamente
        pie.validarAptoComoPortainjerto();                 // precondición de CU-03
        pie.validarConsumo(cantidad);                      // RN-01
        yemas.validarYemasUtilizables(fecha.toLocalDate()); // clase YEMAS y RN-03
        yemas.validarConsumo(cantidad);                    // RN-01

        // 3. Destino: por defecto la injertación es "en el lugar" y libera el lugar del pie
        String codigoDestino = (s.codigoDestino() == null) ? pie.getCodigoUbicacion() : s.codigoDestino();
        Ubicacion destino = ubicaciones.buscarParaActualizar(c, codigoDestino)
                .orElseThrow(() -> new DatosInvalidosException("no existe la ubicación " + codigoDestino + "."));
        boolean enElLugar = pie.getIdUbicacion() != null && destino.getId() == pie.getIdUbicacion();
        int liberado = enElLugar ? cantidad : 0;
        try {
            destino.validarRecepcion(cantidad - liberado); // RN-05
        } catch (ReglaNegocioException e) {
            throw e.conDatos(pie.getId(), destino.getId(), cantidad);
        }

        // 4. Escrituras: lote nuevo, tres eventos, dos vínculos de genealogía y auditoría
        Lote nuevo = new LoteInjertado(pie.getIdCampania(), yemas.getCopa(), pie.getPortainjerto(),
                destino.getId(), destino.getCodigo(), fecha);
        lotes.insertar(c, nuevo);

        long usuario = sesion.getUsuario().getId();
        long idAlta = eventos.insertar(c, EventoLote.de(nuevo.getId(), TipoEvento.INJERTACION, fecha, cantidad)
                .destino(destino.getId()).empleado(s.idEmpleado())
                .observacion(liberado > 0 ? "Injertación en el lugar" : "Injertación con traslado").construir(), usuario);
        eventos.insertar(c, EventoLote.de(pie.getId(), TipoEvento.CONSUMO_INJERTACION, fecha, cantidad)
                .origen(pie.getIdUbicacion()).empleado(s.idEmpleado()).relacionadoCon(idAlta).construir(), usuario);
        eventos.insertar(c, EventoLote.de(yemas.getId(), TipoEvento.CONSUMO_INJERTACION, fecha, cantidad)
                .origen(yemas.getIdUbicacion()).empleado(s.idEmpleado()).relacionadoCon(idAlta).construir(), usuario);

        lotes.registrarGenealogia(c, nuevo.getId(), pie.getId(), "PORTAINJERTO", idAlta);   // RN-04
        lotes.registrarGenealogia(c, nuevo.getId(), yemas.getId(), "YEMA", idAlta);

        boolean pieCerrado = pie.quedaAgotado(cantidad);
        if (pieCerrado) {
            lotes.actualizarEstado(c, pie.getId(), EstadoLote.CERRADO);
        }
        if (yemas.quedaAgotado(cantidad)) {
            lotes.actualizarEstado(c, yemas.getId(), EstadoLote.CERRADO);
        }

        String detalle = "Injertación " + pie.getCodigo() + " + " + yemas.getCodigo() + " -> "
                + nuevo.getCodigo() + " (" + cantidad + ")";
        auditoria.registrar(c, usuario, "ALTA", "evento_lote", idAlta, detalle);

        return new Resultado("Injertación registrada",
                "Se creó el lote " + nuevo.getCodigo() + ": " + nuevo.describirCombinacion() + ", "
                        + n(cantidad) + " plantas en " + destino.getCodigo() + ".",
                pie.getCodigo() + ": existencia " + n(pie.getCantidadActual()) + " -> "
                        + n(pie.getCantidadActual() - cantidad) + (pieCerrado ? " (cerrado: consumido)" : "") + ".",
                yemas.getCodigo() + ": existencia " + n(yemas.getCantidadActual()) + " -> "
                        + n(yemas.getCantidadActual() - cantidad) + ".",
                "Genealogía registrada: " + nuevo.getCodigo() + " <- " + pie.getCodigo() + " (portainjerto), "
                        + yemas.getCodigo() + " (yema). Evento de alta n.º " + idAlta + ".");
    }
}
