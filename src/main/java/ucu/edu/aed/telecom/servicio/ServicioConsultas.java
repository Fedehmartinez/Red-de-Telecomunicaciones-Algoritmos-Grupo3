package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.telecom.resultado.ImpactoFalla;
import ucu.edu.aed.telecom.resultado.ResultadoDiametro;
import ucu.edu.aed.telecom.resultado.RutaResultado;

import java.util.List;
import java.util.Objects;

/**
 * Las cinco consultas propias del sistema. Ninguna muestra datos guardados tal cual: todas resuelven algo
 * con los grafos y los algoritmos de la biblioteca, a través del servicio que corresponde.
 */
public class ServicioConsultas {

    private final ServicioRuteo ruteo;
    private final ServicioResiliencia resiliencia;
    private final ServicioCDN cdn;

    public ServicioConsultas(ServicioRuteo ruteo, ServicioResiliencia resiliencia, ServicioCDN cdn) {
        this.ruteo = Objects.requireNonNull(ruteo);
        this.resiliencia = Objects.requireNonNull(resiliencia);
        this.cdn = Objects.requireNonNull(cdn);
    }

    /**
     * 1. Ruta de menor latencia que garantiza un ancho de banda mínimo (por ejemplo, para video o un backup
     * grande): el camino más rápido no sirve si pasa por un enlace angosto. Se resuelve con el servicio de ruteo.
     */
    public RutaResultado rutaConAnchoBandaMinimo(String origen, String destino, double anchoBandaMinimo) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * 2. Qué pasa si cae un nodo: qué nodos quedan sin data center y cuántos usuarios se ven afectados.
     * Se resuelve con el servicio de resiliencia.
     */
    public ImpactoFalla queOcurreSiCae(String nodo) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * 3. Ranking de nodos críticos por usuarios afectados: dónde conviene invertir primero en redundancia.
     * Se resuelve con el servicio de resiliencia.
     */
    public List<ImpactoFalla> rankingNodosCriticos() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** 4. Diámetro de la red: el peor caso de latencia entre dos nodos. Se resuelve con el servicio de ruteo. */
    public ResultadoDiametro diametro() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * 5. Nodos de acceso que superan una latencia máxima respecto a su data center (no cumplen el servicio).
     * Se resuelve con el servicio de CDN.
     */
    public List<String> nodosQueSuperanLatencia(double latenciaMaxima) {
        throw new UnsupportedOperationException("Falta implementar");
    }
}
