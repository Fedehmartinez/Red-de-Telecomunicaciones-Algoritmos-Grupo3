package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.AsignacionCDN;
import ucu.edu.aed.telecom.resultado.RecomendacionDC;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Distribución de contenido: cada nodo es atendido por el data center con menor latencia, y se recomienda
 * dónde poner un data center nuevo. El contenido va del data center al usuario, así que la latencia que
 * importa es DC -> nodo (la fila de la matriz de Floyd), no nodo -> DC.
 */
public class ServicioCDN {

    private final RedTelecom red;

    public ServicioCDN(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    /** Asignación con los data centers de la red. */
    public AsignacionCDN asignarCDN() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * A cada nodo, el data center (de los dados) con menor latencia DC -> nodo. Calcula Floyd en cada
     * llamada (versión simple). Empate: el que aparece primero en red.nodos().
     */
    public AsignacionCDN asignarCDN(Set<String> dataCenters) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * Versión simple de la recomendación: para cada nodo que no es data center, se lo agrega como uno y se
     * vuelve a llamar a asignarCDN. Beneficio = suma de usuarios × mejora de latencia en los nodos de acceso.
     * null si no hay candidatos.
     */
    public RecomendacionDC recomendarDCSimple() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * Centro para ubicar un data center: el nodo que minimiza la mayor latencia desde él hacia los demás.
     * Se usa obtenerCentroGrafo sobre el grafo de ruteo invertido, porque el centro de la biblioteca mira la
     * columna (los otros hacia v) y en el invertido la columna es la fila del original.
     */
    public String centroParaDC() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Nodos de acceso cuya latencia a su data center supera el máximo (o que no tienen), de mayor a menor. */
    public List<String> nodosQueSuperanLatencia(double latenciaMaxima) {
        throw new UnsupportedOperationException("Falta implementar");
    }
}
