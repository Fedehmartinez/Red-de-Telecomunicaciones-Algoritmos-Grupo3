package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

/**
 * Una secuencia de tareas (camino de un vértice inicial a uno final) con su costo total
 * y su holgura respecto del camino crítico.
 */
public class SecuenciaConHolgura<V> extends Path<V> {
    private final double holgura;

    public SecuenciaConHolgura(List<V> path, double cost, double holgura) {
        super(path, cost);
        this.holgura = holgura;
    }

    public double getHolgura() {
        return holgura;
    }

    @Override
    public String toString() {
        return getPath() + " costo=" + getCost() + " holgura=" + holgura;
    }
}
