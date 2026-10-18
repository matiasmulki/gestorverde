package gestorverde.persistencia;

import gestorverde.dominio.IntentoRechazado;
import gestorverde.estructuras.ListaEnlazada;
import gestorverde.excepciones.ReglaNegocioException;
import java.sql.Connection;
import java.sql.SQLException;

/** Registro inalterable de actividad (RF-25) y de intentos rechazados (RF-02). */
public interface AuditoriaDAO {

    void registrar(Connection c, long idUsuario, String accion, String entidad, Long idEntidad, String detalle)
            throws SQLException;

    void registrarRechazo(Connection c, long idUsuario, String operacion, ReglaNegocioException rechazo)
            throws SQLException;

    ListaEnlazada<IntentoRechazado> ultimosRechazos(Connection c, int limite) throws SQLException;
}
