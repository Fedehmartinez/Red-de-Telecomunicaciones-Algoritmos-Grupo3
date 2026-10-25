package ucu.edu.aed.tda.grafo;


import ucu.edu.aed.tda.grafo.model.IGraph;

import java.util.Set;

public interface IDirectedIGraph<V, D> extends IGraph<V, D> {

    /**
     * Sucesores (vecinos alcanzables por aristas salientes) de v.
     */
    Set<V> successors(Comparable<V> criteria);

    /**
     * Predecesores (vecinos con aristas entrantes) de v.
     */
    Set<V> predecessors(Comparable<V> criteria);

    /**
     * Calcula la cantidad de vértices que salen de "v"
     */
    default int gradoDeSalida(Comparable<V> criteria) {
        return successors(criteria).size();
    }

    /**
     * Calcula la cantidad de vertices que "apuntan" a v
     */
    default int gradoDeEntrada(Comparable<V> criteria) {
        return predecessors(criteria).size();
    }

}
