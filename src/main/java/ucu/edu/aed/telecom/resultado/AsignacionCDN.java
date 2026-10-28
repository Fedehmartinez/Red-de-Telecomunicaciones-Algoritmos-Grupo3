package ucu.edu.aed.telecom.resultado;

import java.util.LinkedHashMap;
import java.util.Map;

// Qué data center atiende a cada nodo y con qué latencia (del data center hacia el nodo)
public class AsignacionCDN {

    private final Map<String, String> dcPorNodo;
    private final Map<String, Double> latenciaPorNodo;

    public AsignacionCDN(Map<String, String> dcPorNodo, Map<String, Double> latenciaPorNodo) {
        this.dcPorNodo = new LinkedHashMap<>(dcPorNodo);
        this.latenciaPorNodo = new LinkedHashMap<>(latenciaPorNodo);
    }

    // El data center que atiende al nodo, o null si no tiene
    public String dcDe(String nodo) {
        return dcPorNodo.get(nodo);
    }

    // Latencia desde su data center hasta el nodo, o infinito si no tiene
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
