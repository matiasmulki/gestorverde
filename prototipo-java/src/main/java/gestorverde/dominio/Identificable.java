package gestorverde.dominio;

/**
 * Contrato común de los objetos del dominio que tienen identidad propia:
 * una clave numérica (la de la base de datos) y un código de negocio legible.
 */
public interface Identificable {

    long getId();

    String getCodigo();
}
