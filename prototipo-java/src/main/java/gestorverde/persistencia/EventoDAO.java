package gestorverde.persistencia;

import gestorverde.dominio.EventoLote;
import gestorverde.estructuras.ListaEnlazada;
import java.sql.Connection;
import java.sql.SQLException;

/** Acceso a la historia de eventos. Solo inserta y consulta: los eventos son inmutables (RN-02). */
public interface EventoDAO {

    /** Inserta el evento y devuelve su clave. El trigger de la base actualiza la existencia del lote. */
    long insertar(Connection c, EventoLote evento, long idUsuario) throws SQLException;

    ListaEnlazada<EventoLote> listarPorLote(Connection c, long idLote) throws SQLException;
}
