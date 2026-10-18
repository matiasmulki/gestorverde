package gestorverde.dominio;

import java.util.EnumSet;
import java.util.Set;

/** Roles del sistema (tabla rol) con los permisos que otorga cada uno (RF-24). */
public enum Rol {
    ADMINISTRADOR(1, EnumSet.allOf(Permiso.class)),
    ENCARGADO_PRODUCCION(2, EnumSet.of(Permiso.CONSULTAR, Permiso.REGISTRAR_PRODUCCION,
            Permiso.INJERTAR, Permiso.CONSULTAR_AUDITORIA)),
    OPERARIO(3, EnumSet.of(Permiso.CONSULTAR, Permiso.REGISTRAR_PRODUCCION)),
    RESPONSABLE_TECNICO(4, EnumSet.of(Permiso.CONSULTAR, Permiso.CONSULTAR_AUDITORIA)),
    VENTAS(5, EnumSet.of(Permiso.CONSULTAR)),
    GERENCIA(6, EnumSet.of(Permiso.CONSULTAR, Permiso.CONSULTAR_AUDITORIA));

    private final int id;
    private final Set<Permiso> permisos;

    Rol(int id, Set<Permiso> permisos) {
        this.id = id;
        this.permisos = permisos;
    }

    public int getId() {
        return id;
    }

    public boolean otorga(Permiso permiso) {
        return permisos.contains(permiso);
    }

    public static Rol desdeId(int id) {
        for (Rol r : values()) {
            if (r.id == id) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + id);
    }
}
