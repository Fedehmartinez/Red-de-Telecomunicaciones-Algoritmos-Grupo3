package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

public class Path<V> {
    private final List<V> path;
    private final double cost;

    public Path(List<V> path, double cost) {
        this.path = path;
        this.cost = cost;
    }

    public List<V> getPath() {
        return path;
    }

    public double getCost() {
        return cost;
    }
}
