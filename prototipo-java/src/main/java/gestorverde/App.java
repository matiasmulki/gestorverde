package gestorverde;

import gestorverde.aplicacion.ServicioAutenticacion;
import gestorverde.aplicacion.ServicioConsultas;
import gestorverde.aplicacion.ServicioInjertacion;
import gestorverde.aplicacion.ServicioMerma;
import gestorverde.aplicacion.ServicioMovimiento;
import gestorverde.aplicacion.ServicioTrazabilidad;
import gestorverde.aplicacion.Servicios;
import gestorverde.excepciones.PersistenciaException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.EventoDAO;
import gestorverde.persistencia.LoteDAO;
import gestorverde.persistencia.Transacciones;
import gestorverde.persistencia.UbicacionDAO;
import gestorverde.persistencia.jdbc.AuditoriaDAOJdbc;
import gestorverde.persistencia.jdbc.CatalogoDAOJdbc;
import gestorverde.persistencia.jdbc.ConfiguracionBD;
import gestorverde.persistencia.jdbc.EventoDAOJdbc;
import gestorverde.persistencia.jdbc.LoteDAOJdbc;
import gestorverde.persistencia.jdbc.TrazabilidadDAOJdbc;
import gestorverde.persistencia.jdbc.TransaccionesJdbc;
import gestorverde.persistencia.jdbc.UbicacionDAOJdbc;
import gestorverde.persistencia.jdbc.UsuarioDAOJdbc;
import gestorverde.presentacion.Consola;
import gestorverde.presentacion.MenuPrincipal;
import gestorverde.presentacion.swing.AplicacionSwing;
import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/**
 * Punto de entrada y "raíz de composición": el único lugar donde se crean las
 * implementaciones concretas y se conectan entre sí. El resto del sistema
 * depende solo de interfaces (LoteDAO, Transacciones...).
 *
 * Por defecto abre la interfaz gráfica Swing. Con el argumento --consola (o en
 * un equipo sin entorno gráfico) usa el menú de texto.
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        boolean modoConsola = GraphicsEnvironment.isHeadless()
                || (args.length > 0 && args[0].equalsIgnoreCase("--consola"));
        try {
            ConfiguracionBD configuracion = ConfiguracionBD.cargar();
            Transacciones tx = new TransaccionesJdbc(configuracion);
            tx.verificarConexion();
            Servicios servicios = crearServicios(tx);
            if (modoConsola) {
                new MenuPrincipal(new Consola(System.in, System.out), servicios).iniciar();
            } else {
                AplicacionSwing.iniciar(servicios);
            }
        } catch (PersistenciaException e) {
            String mensaje = "No fue posible iniciar el sistema: " + e.getMensajeParaUsuario()
                    + "\nRevise gestorverde.properties y que MySQL esté en ejecución.";
            if (modoConsola) {
                System.out.println(mensaje);
            } else {
                JOptionPane.showMessageDialog(null, mensaje, "Gestor Verde", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
        } catch (Consola.FinDeEntradaException e) {
            System.out.println("No hay más datos de entrada. Fin del programa.");
        }
    }

    /** Crea los DAO JDBC y los servicios que los usan. */
    public static Servicios crearServicios(Transacciones tx) {
        LoteDAO lotes = new LoteDAOJdbc();
        EventoDAO eventos = new EventoDAOJdbc();
        UbicacionDAO ubicaciones = new UbicacionDAOJdbc();
        AuditoriaDAO auditoria = new AuditoriaDAOJdbc();
        return new Servicios(
                new ServicioAutenticacion(tx, auditoria, new UsuarioDAOJdbc()),
                new ServicioConsultas(tx, auditoria, lotes, ubicaciones, eventos, new CatalogoDAOJdbc()),
                new ServicioInjertacion(tx, auditoria, lotes, eventos, ubicaciones),
                new ServicioMovimiento(tx, auditoria, lotes, eventos, ubicaciones),
                new ServicioMerma(tx, auditoria, lotes, eventos),
                new ServicioTrazabilidad(tx, auditoria, lotes, new TrazabilidadDAOJdbc()));
    }
}
