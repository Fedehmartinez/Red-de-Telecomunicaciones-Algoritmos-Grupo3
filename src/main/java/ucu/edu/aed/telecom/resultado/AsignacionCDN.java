package ucu.edu.aed.telecom.resultado;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Qué data center atiende a cada nodo y con qué latencia (del data center hacia el nodo).
 * Un nodo al que no llega ningún data center no tiene asignación: dcDe da null y latenciaDe infinito.
 */
public class AsignacionCDN {

    private final Map<String, String> dcPorNodo;
    private final Map<String, Double> latenciaPorNodo;

    public AsignacionCDN(Map<String, String> dcPorNodo, Map<String, Double> latenciaPorNodo) {
        this.dcPorNodo = Collections.unmodifiableMap(new LinkedHashMap<>(dcPorNodo));
        this.latenciaPorNodo = Collections.unmodifiableMap(new LinkedHashMap<>(latenciaPorNodo));
    }

    /** El data center que atiende al nodo, o null si no tiene. */
    public String dcDe(String nodo) {
        return dcPorNodo.get(nodo);
    }

    /** Latencia desde su data center hasta el nodo, o infinito si no tiene. */
    public double latenciaDe(String nodo) {
        return latenciaPorNodo.getOrDefault(nodo, Double.POSITIVE_INFINITY);
    }

    public Map<String, String> getDcPorNodo() {
        return dcPorNodo;
    }

    public Map<String, Double> getLatenciaPorNodo() {
        return latenciaPorNodo;
    }
}
