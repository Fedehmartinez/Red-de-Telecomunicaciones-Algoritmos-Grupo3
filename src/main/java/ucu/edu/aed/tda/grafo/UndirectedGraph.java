package ucu.edu.aed.tda.grafo;

import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.UndirectedEdge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Grafo no dirigido con lista de adyacencias. Como en el teórico, una arista (v,w) se guarda
 * como dos: una en la lista de v y otra en la lista de w. En la lista de cada vértice la arista
 * queda con ese vértice como source(), que es lo que pide adyacencias().
 * No se permiten lazos ni más de una arista entre el mismo par de vértices.
 */
public class UndirectedGraph<V, D> implements IUndirectedGraph<V, D> {

    private final Map<V, List<Edge<V, D>>> adyacencias = new LinkedHashMap<>();

    @Override
    public boolean agregarVertice(V vertex) {
        if (adyacencias.containsKey(vertex)) return false;
        adyacencias.put(vertex, new ArrayList<>());
        return true;
    }

    @Override
    public V buscarVertice(Comparable<V> criterio) {
        for (V v : adyacencias.keySet()) {
            if (criterio.compareTo(v) == 0) return v;
        }
        return null;
    }

    @Override
    public boolean agregarArista(V source, V target, D dato) {
        if (!adyacencias.containsKey(source) || !adyacencias.containsKey(target)) return false;
        if (source.equals(target)) return false;
        if (existeArista(construirComparable(source), construirComparable(target))) return false;
        adyacencias.get(source).add(new UndirectedEdge<>(source, target, dato));
        adyacencias.get(target).add(new UndirectedEdge<>(target, source, dato));
        return true;
    }

    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        Edge<V, D> arista = obtenerArista(source, target);
        if (arista == null) return false;
        V s = arista.source();
        V t = arista.target();
        adyacencias.get(s).removeIf(e -> e.target().equals(t));
        adyacencias.get(t).removeIf(e -> e.target().equals(s));
        return true;
    }

    /** Solo hay que limpiar las listas de los vecinos de v: nadie más tiene aristas hacia v. */
    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V v = buscarVertice(criteria);
        if (v == null) return false;
        for (Edge<V, D> e : adyacencias.remove(v)) {
            adyacencias.get(e.target()).removeIf(x -> x.target().equals(v));
        }
        return true;
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adyacencias.keySet());
    }

    /** Cada arista está dos veces en las listas; el equals simétrico de UndirectedEdge deja una sola. */
    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> todas = new LinkedHashSet<>();
        adyacencias.values().forEach(todas::addAll);
        return Collections.unmodifiableSet(todas);
    }

    @Override
    public boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        return obtenerArista(sourceCriteria, targetCriteria) != null;
    }

    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        V s = buscarVertice(sourceCriteria);
        if (s == null) return null;
        for (Edge<V, D> e : adyacencias.get(s)) {
            if (targetCriteria.compareTo(e.target()) == 0) return e;
        }
        return null;
    }

    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V v = buscarVertice(verticeCriteria);
        if (v == null) return Collections.emptyList();
        return Collections.unmodifiableList(adyacencias.get(v));
    }

    @Override
    public void vaciar() {
        adyacencias.clear();
    }

    /**
     * Conexo: desde un vértice cualquiera se llega a todos. En un no dirigido alcanza con un solo
     * recorrido, porque si se llega de v a w también se llega de w a v. Un grafo vacío se considera conexo.
     */
    @Override
    public boolean esConexo() {
        if (adyacencias.isEmpty()) return true;

        V inicio = adyacencias.keySet().iterator().next();
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        visitados.add(inicio);
        pendientes.push(inicio);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.pop();
            for (Edge<V, D> e : adyacencias.get(actual)) {
                if (visitados.add(e.target())) pendientes.push(e.target());
            }
        }
        return visitados.size() == adyacencias.size();
    }

    /**
     * Búsqueda en profundidad: en un no dirigido solo hay arcos de árbol y de retroceso.
     * Si desde un vértice se llega a uno ya visitado que no es su padre, es un arco de retroceso y hay ciclo.
     * La arista hacia el padre no cuenta: es la misma arista por la que se llegó.
     */
    @Override
    public boolean tieneCiclos() {
        Set<V> visitados = new HashSet<>();
        for (V v : adyacencias.keySet()) {
            if (!visitados.contains(v) && bpfTieneCiclo(v, null, visitados)) return true;
        }
        return false;
    }

    private boolean bpfTieneCiclo(V actual, V padre, Set<V> visitados) {
        visitados.add(actual);
        for (Edge<V, D> arista : adyacencias.get(actual)) {
            V vecino = arista.target();
            if (vecino.equals(padre)) continue;
            if (visitados.contains(vecino)) return true;   // arco de retroceso
            if (bpfTieneCiclo(vecino, actual, visitados)) return true;
        }
        return false;
    }
}
