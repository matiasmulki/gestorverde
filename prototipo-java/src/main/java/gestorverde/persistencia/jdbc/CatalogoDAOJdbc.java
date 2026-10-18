package gestorverde.persistencia.jdbc;

import gestorverde.dominio.Empleado;
import gestorverde.dominio.MotivoEgreso;
import gestorverde.estructuras.ListaEnlazada;
import gestorverde.persistencia.CatalogoDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Implementación JDBC de CatalogoDAO. */
public class CatalogoDAOJdbc implements CatalogoDAO {

    @Override
    public ListaEnlazada<Empleado> listarEmpleadosActivos(Connection c) throws SQLException {
        ListaEnlazada<Empleado> empleados = new ListaEnlazada<>();
        String sql = "SELECT id_empleado, legajo, apellido, nombre FROM empleado WHERE activo = TRUE ORDER BY apellido, nombre";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.agregar(new Empleado(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        }
        return empleados;
    }

    @Override
    public ListaEnlazada<MotivoEgreso> listarMotivosEgreso(Connection c) throws SQLException {
        ListaEnlazada<MotivoEgreso> motivos = new ListaEnlazada<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT id_motivo, nombre, etapa FROM motivo_egreso ORDER BY id_motivo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                motivos.agregar(new MotivoEgreso(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return motivos;
    }
}
