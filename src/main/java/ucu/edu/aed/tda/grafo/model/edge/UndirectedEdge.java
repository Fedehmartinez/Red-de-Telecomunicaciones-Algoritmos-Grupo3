package ucu.edu.aed.tda.grafo.model.edge;

import java.util.Objects;

public class UndirectedEdge<V, D> implements Edge<V, D> {
    private final V a, b;
    private final D dato;

    public UndirectedEdge(V u, V v, D dato) {
        this.a = Objects.requireNonNull(u);
        this.b = Objects.requireNonNull(v);
        this.dato = dato;
    }
    public V source() { return a; }    // nombres “source/target” son nominales acá
    public V target() { return b; }
    public D dato()   { return dato; }
    public boolean directed() { return false; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UndirectedEdge<?, ?> e)) return false;
        // Simétrico: (a,b) == (b,a)
        return (Objects.equals(a, e.a) && Objects.equals(b, e.b))
            || (Objects.equals(a, e.b) && Objects.equals(b, e.a));
    }

    @Override public int hashCode() {
        // Orden-insensible: combinar sin dirección
        int h1 = Objects.hashCode(a);
        int h2 = Objects.hashCode(b);
        // Suma + XOR es una combinación clásica para pares no ordenados
        return (h1 ^ h2) + (h1 + h2);
    }

    @Override public String toString() {
        return "{" + a + " -" + dato + "- " + b + "}";
    }
}