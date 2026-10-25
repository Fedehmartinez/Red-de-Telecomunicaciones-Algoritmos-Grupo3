package ucu.edu.aed.tda.grafo.model.result;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DijkstraResult<V> implements IDijkstraResult<V> {
    private final V origen;
    private final Map<V, Double> costos;
    private final Map<V, V> predecesores;

    public DijkstraResult(V origen, Map<V, Double> costos, Map<V, V> predecesores) {
        this.origen = origen;
        this.costos = costos;
        this.predecesores = predecesores;
    }

    @Override
    public double getCost(V otherVertex) {
        return costos.getOrDefault(otherVertex, Double.POSITIVE_INFINITY);
    }

    @Override
    public List<V> getPath(V otherVertex) {
        LinkedList<V> camino = new LinkedList<>();
        if (getCost(otherVertex) == Double.POSITIVE_INFINITY) return camino;
        for (V actual = otherVertex; actual != null; actual = predecesores.get(actual)) {
            camino.addFirst(actual);
        }
        return camino;
    }

    public V getOrigen() {
        return origen;
    }


    public V getPredecesor(V v) {
        return predecesores.get(v);
    }
}
