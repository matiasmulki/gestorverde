package gestorverde.dominio;

import gestorverde.excepciones.ReglaNegocioException;
import java.time.LocalDate;

/** Semilla o yema certificada con su precinto, certificado y vencimiento. */
public class MaterialCertificado {

    private final long id;
    private final String tipo;
    private final String precinto;
    private final String nroCertificado;
    private final String organismo;
    private final LocalDate fechaVencimiento;

    public MaterialCertificado(long id, String tipo, String precinto, String nroCertificado,
                               String organismo, LocalDate fechaVencimiento) {
        this.id = id;
        this.tipo = tipo;
        this.precinto = precinto;
        this.nroCertificado = nroCertificado;
        this.organismo = organismo;
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isAfter(fechaVencimiento);
    }

    /** RN-03: solo se injerta con material cuyo certificado esté vigente a la fecha de la operación. */
    public void verificarVigencia(LocalDate fecha) throws ReglaNegocioException {
        if (!estaVigente(fecha)) {
            throw new ReglaNegocioException("RN-03",
                    "El certificado " + nroCertificado + " (precinto " + precinto + ") venció el " + fechaVencimiento + ".",
                    "Utilice yemas de otro lote con certificado vigente.");
        }
    }

    public long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getPrecinto() { return precinto; }
    public String getNroCertificado() { return nroCertificado; }
    public String getOrganismo() { return organismo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }

    @Override
    public String toString() {
        return tipo + " precinto " + precinto + " · cert. " + nroCertificado + " (" + organismo + ") · vence " + fechaVencimiento;
    }
}
