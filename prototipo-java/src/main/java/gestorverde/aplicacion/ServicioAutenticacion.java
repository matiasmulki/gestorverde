package gestorverde.aplicacion;

import gestorverde.excepciones.AutenticacionException;
import gestorverde.excepciones.GestorVerdeException;
import gestorverde.persistencia.AuditoriaDAO;
import gestorverde.persistencia.Transacciones;
import gestorverde.persistencia.UsuarioDAO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Inicio de sesión (CU-16). La clave se verifica con PBKDF2-HMAC-SHA256 y sal
 * aleatoria, el mismo esquema con el que se cargaron los usuarios en 02_datos.sql.
 */
public class ServicioAutenticacion extends ServicioBase {

    static final int ITERACIONES = 120_000;
    private static final int LARGO_CLAVE_BITS = 256;
    /** Mismo mensaje para usuario inexistente y clave incorrecta: no revela cuál falló (CP-13). */
    private static final String MENSAJE_RECHAZO = "usuario o clave incorrectos.";

    private final UsuarioDAO usuarios;

    public ServicioAutenticacion(Transacciones tx, AuditoriaDAO auditoria, UsuarioDAO usuarios) {
        super(tx, auditoria);
        this.usuarios = usuarios;
    }

    public Sesion iniciarSesion(String nombreUsuario, char[] clave) throws GestorVerdeException {
        try {
            if (nombreUsuario == null || nombreUsuario.isBlank() || clave == null || clave.length == 0) {
                throw new AutenticacionException(MENSAJE_RECHAZO);
            }
            String nombre = nombreUsuario.trim().toLowerCase(ES_AR);
            return tx.ejecutar(c -> {
                Optional<UsuarioDAO.Credencial> encontrada = usuarios.buscarCredencial(c, nombre);
                if (encontrada.isEmpty() || !encontrada.get().isActivo()) {
                    throw new AutenticacionException(MENSAJE_RECHAZO);
                }
                UsuarioDAO.Credencial cred = encontrada.get();
                if (!claveCorrecta(clave, cred.getSal(), cred.getHashClave())) {
                    throw new AutenticacionException(MENSAJE_RECHAZO);
                }
                usuarios.registrarAcceso(c, cred.getUsuario().getId());
                auditoria.registrar(c, cred.getUsuario().getId(), "LOGIN", "usuario",
                        cred.getUsuario().getId(), "Inicio de sesión");
                return new Sesion(cred.getUsuario());
            });
        } finally {
            if (clave != null) {
                Arrays.fill(clave, '\0');   // la clave no queda en memoria
            }
        }
    }

    /** Compara en tiempo constante para no dar pistas por la duración de la verificación. */
    static boolean claveCorrecta(char[] clave, String salHex, String hashEsperadoHex) {
        String calculado = calcularHash(clave, salHex);
        return MessageDigest.isEqual(calculado.getBytes(StandardCharsets.US_ASCII),
                hashEsperadoHex.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }

    static String calcularHash(char[] clave, String salHex) {
        PBEKeySpec spec = new PBEKeySpec(clave, HexFormat.of().parseHex(salHex), ITERACIONES, LARGO_CLAVE_BITS);
        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return HexFormat.of().formatHex(fabrica.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Captura múltiple: ambos errores indican un problema de la plataforma, no del usuario.
            throw new IllegalStateException("PBKDF2 no está disponible en esta JVM", e);
        } finally {
            spec.clearPassword();
        }
    }
}
