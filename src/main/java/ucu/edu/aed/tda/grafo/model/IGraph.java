package ucu.edu.aed.tda.grafo.model;


import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Modela un grafo, donde los vértices son del tipo V, y las aristas guardan el dato D
 */
public interface IGraph<V, D> {
    /**
     * Agrega un vértice, y retorna true si efectivamente lo agrega
     */
    boolean agregarVertice(V vertex);

    /**
     * Operación "Bulk",  agrega muchos vértices en la misma llamada
     */
    default void agregarVertices(Collection<V> vertices) {
        vertices.forEach(this::agregarVertice);
    }


    V buscarVertice(Comparable<V> criterio);

    /**
     * Agrega una arista al grafo, indicando con un booleano si la agregó
     */
    boolean agregarArista(V source, V target, D dato);

    /**
     * Elimina una arista del grafo
     */
    boolean eliminarArista(Comparable<V> source, Comparable<V> target);

    /**
     * remueve un vértice del grafo, retorna true si el vértice fue efectivamente removido
     */
    boolean removerVertice(Comparable<V> criteria);

    /**
     * Conjunto de vértices (preferible devolver vista inmodificable).
     */
    Set<V> vertices();

    /**
     * Conjunto de aristas (preferible vista inmodificable).
     */
    Set<Edge<V, D>> aristas();

    /**
     * ¿Existe el vértice v?
     */
    default boolean existeVertice(Comparable<V> criterio) {
        return vertices().stream().anyMatch(x -> criterio.compareTo(x) == 0);
    }

    /**
     * ¿Existe la arista (u -> v) en un grafo dirigido o (u,v) en uno no dirigido?
     */
    boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria);

    /**
     * Retorna una arista que tiene un origen y destino source y target respectivamente
     */
    Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria);

    default Edge<V, D> obtenerArista(V sourceCriteria, V targetCriteria) {
        return obtenerArista(construirComparable(sourceCriteria), construirComparable(targetCriteria));
    }

    /**
     * Retorna todas las aristas que el vertex tiene como adyacentes.
     * En caso de que sea un grafo no dirigido, el método "source()"
     * referencia al vértice "verticeCriteria"
     */
    List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria);

    /**
     * Retorna la cantidad de vértices
     */
    default int cantidadDeVertices() {
        return vertices().size();
    }

    /**
     * Retorna la cantidad de artists
     */
    default int cantidadDeAristas() {
        return aristas().size();
    }

    /**
     * Retorna true si el grafo es conexo
     */
    boolean esConexo();

    /**
     * vacía el grafo
     */
    void vaciar();


    /**
     * retorna true si el grafo tiene ciclos
     */
    boolean tieneCiclos();


    /** método utilitario para construir un comparable de un vértice cualquier utilizando equals */
    default Comparable<V> construirComparable(V vertice) {
        return x -> x.equals(vertice) ? 0 : 1;
    }
}
