package gestorverde.persistencia;

import gestorverde.dominio.Empleado;
import gestorverde.dominio.MotivoEgreso;
import gestorverde.estructuras.ListaEnlazada;
import java.sql.Connection;
import java.sql.SQLException;

/** Catálogos de apoyo para los formularios. */
public interface CatalogoDAO {

    ListaEnlazada<Empleado> listarEmpleadosActivos(Connection c) throws SQLException;

    ListaEnlazada<MotivoEgreso> listarMotivosEgreso(Connection c) throws SQLException;
}
