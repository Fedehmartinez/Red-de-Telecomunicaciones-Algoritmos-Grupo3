package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.telecom.resultado.ImpactoFalla;
import ucu.edu.aed.telecom.resultado.ResultadoDiametro;
import ucu.edu.aed.telecom.resultado.RutaResultado;

import java.util.List;

// Las cinco consultas propias del sistema; cada una se resuelve con el servicio que corresponde
public class ServicioConsultas {

    private final ServicioRuteo ruteo;
    private final ServicioResiliencia resiliencia;
    private final ServicioCDN cdn;

    public ServicioConsultas(ServicioRuteo ruteo, ServicioResiliencia resiliencia, ServicioCDN cdn) {
        if (ruteo == null) throw new NullPointerException();
        this.ruteo = ruteo;
        if (resiliencia == null) throw new NullPointerException();
        this.resiliencia = resiliencia;
        if (cdn == null) throw new NullPointerException();
        this.cdn = cdn;
    }

    // 1. Ruta de menor latencia que garantiza un ancho de banda mínimo (por ejemplo para video o un backup)
    public RutaResultado rutaConAnchoBandaMinimo(String origen, String destino, double anchoBandaMinimo) {
        return ruteo.rutaConAnchoBandaMinimo(origen, destino, anchoBandaMinimo);
    }

    // 2. Qué pasa si cae un nodo: qué nodos quedan sin data center y cuántos usuarios se afectan
    public ImpactoFalla queOcurreSiCae(String nodo) {
        return resiliencia.impactoFallaNodo(nodo);
    }

    // 3. Ranking de nodos críticos por usuarios afectados, para saber dónde invertir primero en redundancia
    public List<ImpactoFalla> rankingNodosCriticos() {
        return resiliencia.rankingCriticosSimple();
    }

    // 4. Diámetro de la red: el peor caso de latencia entre dos nodos
    public ResultadoDiametro diametro() {
        return ruteo.diametro();
    }

    // 5. Nodos de acceso que superan una latencia máxima respecto a su data center
    public List<String> nodosQueSuperanLatencia(double latenciaMaxima) {
        return cdn.nodosQueSuperanLatencia(latenciaMaxima);
    }
}
