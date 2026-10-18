package gestorverde.persistencia.jdbc;

import gestorverde.dominio.IntentoRechazado;
import gestorverde.estructuras.ListaEnlazada;
import gestorverde.excepciones.ReglaNegocioException;
import gestorverde.persistencia.AuditoriaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Implementación JDBC de AuditoriaDAO. */
public class AuditoriaDAOJdbc implements AuditoriaDAO {

    @Override
    public void registrar(Connection c, long idUsuario, String accion, String entidad, Long idEntidad, String detalle)
            throws SQLException {
        String sql = "INSERT INTO auditoria (id_usuario, accion, entidad, id_entidad, detalle) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, entidad);
            JdbcUtil.setLongNulable(ps, 4, idEntidad);
            JdbcUtil.setTextoNulable(ps, 5, JdbcUtil.recortar(detalle, 500));
            ps.executeUpdate();
        }
    }

    @Override
    public void registrarRechazo(Connection c, long idUsuario, String operacion, ReglaNegocioException r)
            throws SQLException {
        String sql = "INSERT INTO intento_rechazado (id_usuario, operacion, regla, id_lote, id_ubicacion, "
                   + "cantidad_pretendida, motivo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setString(2, operacion);
            ps.setString(3, r.getCodigoRegla());
            JdbcUtil.setLongNulable(ps, 4, r.getIdLote());
            JdbcUtil.setLongNulable(ps, 5, r.getIdUbicacion());
            JdbcUtil.setIntNulable(ps, 6, r.getCantidadPretendida());
            ps.setString(7, JdbcUtil.recortar(r.getMessage(), 255));
            ps.executeUpdate();
        }
    }

    @Override
    public ListaEnlazada<IntentoRechazado> ultimosRechazos(Connection c, int limite) throws SQLException {
        String sql = "SELECT i.fecha_hora, us.nombre_usuario, i.operacion, i.regla, l.codigo, "
                   + "       i.cantidad_pretendida, i.motivo "
                   + "  FROM intento_rechazado i "
                   + "  JOIN usuario us ON us.id_usuario = i.id_usuario "
                   + "  LEFT JOIN lote l ON l.id_lote = i.id_lote "
                   + " ORDER BY i.fecha_hora DESC, i.id_intento DESC LIMIT ?";
        ListaEnlazada<IntentoRechazado> intentos = new ListaEnlazada<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    intentos.agregar(new IntentoRechazado(JdbcUtil.getFechaHora(rs, "fecha_hora"),
                            rs.getString("nombre_usuario"), rs.getString("operacion"), rs.getString("regla"),
                            rs.getString("codigo"), JdbcUtil.getIntNulable(rs, "cantidad_pretendida"),
                            rs.getString("motivo")));
                }
            }
        }
        return intentos;
    }
}
