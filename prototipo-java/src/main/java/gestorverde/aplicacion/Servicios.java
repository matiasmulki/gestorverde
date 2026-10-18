package gestorverde.aplicacion;

/**
 * Agrupa los servicios de la aplicación para entregarlos juntos a la capa de
 * presentación (un record: clase inmutable con constructor y getters generados).
 */
public record Servicios(ServicioAutenticacion autenticacion,
                        ServicioConsultas consultas,
                        ServicioInjertacion injertacion,
                        ServicioMovimiento movimiento,
                        ServicioMerma merma,
                        ServicioTrazabilidad trazabilidad) {
}
