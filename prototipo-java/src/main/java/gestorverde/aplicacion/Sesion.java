package gestorverde.aplicacion;

import gestorverde.dominio.Permiso;
import gestorverde.dominio.Usuario;
import gestorverde.excepciones.AutenticacionException;
import gestorverde.excepciones.SesionExpiradaException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Sesión de un usuario autenticado. Expira tras 10 minutos sin actividad (RNF-09). */
public class Sesion {

    public static final int MINUTOS_EXPIRACION = 10;

    private final Usuario usuario;
    private final Clock reloj;
    private final Instant inicio;
    private Instant ultimaActividad;

    /** Constructor habitual: usa el reloj del sistema. */
    public Sesion(Usuario usuario) {
        this(usuario, Clock.systemDefaultZone());
    }

    /** Constructor sobrecargado que recibe el reloj; permite probar la expiración sin esperar 10 minutos. */
    public Sesion(Usuario usuario, Clock reloj) {
        this.usuario = usuario;
        this.reloj = reloj;
        this.inicio = reloj.instant();
        this.ultimaActividad = inicio;
    }

    public boolean expirada() {
        return Duration.between(ultimaActividad, reloj.instant()).toMinutes() >= MINUTOS_EXPIRACION;
    }

    /** Verifica vigencia y permiso antes de cada operación. */
    public void exigir(Permiso permiso) throws AutenticacionException {
        if (expirada()) {
            throw new SesionExpiradaException(MINUTOS_EXPIRACION);
        }
        if (!usuario.tienePermiso(permiso)) {
            throw new AutenticacionException("el rol " + usuario.getRol() + " no tiene el permiso " + permiso + ".");
        }
        registrarActividad();
    }

    public void registrarActividad() {
        ultimaActividad = reloj.instant();
    }

    public Usuario getUsuario() { return usuario; }
    public Instant getInicio() { return inicio; }
}
