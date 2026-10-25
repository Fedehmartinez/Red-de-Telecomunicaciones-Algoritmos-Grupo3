package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

/**
 * Resultado del análisis de camino crítico de un proyecto.
 */
public class ResultadoCaminoCritico<V> {
    private final SecuenciaConHolgura<V> caminoCritico;
    private final List<SecuenciaConHolgura<V>> secuencias;

    public ResultadoCaminoCritico(SecuenciaConHolgura<V> caminoCritico, List<SecuenciaConHolgura<V>> secuencias) {
        this.caminoCritico = caminoCritico;
        this.secuencias = secuencias;
    }

    /** El camino crítico (holgura 0). */
    public SecuenciaConHolgura<V> getCaminoCritico() {
        return caminoCritico;
    }

    public double getCostoCritico() {
        return caminoCritico.getCost();
    }

    /** Todas las secuencias inicio-fin (incluida la crítica), de mayor a menor costo. */
    public List<SecuenciaConHolgura<V>> getSecuencias() {
        return secuencias;
    }
}
