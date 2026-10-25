package ucu.edu.aed.tda.grafo.model.edge;

import java.util.Objects;

public class DirectedEdge<V, D> implements Edge<V, D> {
    private final V source, target;
    private final D dato;

    public DirectedEdge(V source, V target, D dato) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.dato = dato;
    }

    public V source() {
        return source;
    }

    public V target() {
        return target;
    }

    public D dato() {
        return dato;
    }

    public boolean directed() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectedEdge<?, ?> e)) return false;
        return Objects.equals(source, e.source)
                && Objects.equals(target, e.target);
    }

    @Override
    public int hashCode() {
        int h1 = Objects.hashCode(source);
        int h2 = Objects.hashCode(target);
        // Mezcla orden-sensible + marca de dirigido
        return 31 * (31 + h1) + h2; // simple y efectivo
    }

    @Override
    public String toString() {
        return source + " -" + dato + "-> " + target;
    }
}