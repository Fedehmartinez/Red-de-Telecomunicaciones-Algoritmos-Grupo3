package ucu.edu.aed.tda.grafo;


import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.List;
import java.util.function.Consumer;

public interface IDirectedGraphAlgorithms {

    /**
     * ejecuta el algoritmos Dijkstra sobre el grafo pasado y utilizando source como vértice de origen
     */
    <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo);

    /**
     * ejecuta Floyd sobre el grafo pasado, sabiendo que el grafo es weighted
     */
    <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo);

    /**
     * ejecuta Warshall sobre el grafo pasado, sabiendo que el grafo es weighted
     */
    <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo);

    /**
     * Calcula el centro del grafo
     */
    <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo);

    /**
     * Calcula la excentrecidad de un vértice
     */
    <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria);

    /**
     * Retorna todos los caminos posibles para ir de "source" a "target"
     */
    <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo);

    /**
     * Aplica un recorrido en profundidad del grafo y pasa los datos al consumer
     */
    <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer);

    /**
     * Aplica un recorrido en amplitud del grafo y pasa los datos al consumer
     */
    <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer);

    /**
     * Calcula la clasificación topológica del grafo actual
     */
    <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo);

    default <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Consumer<V> consumer) {
        for (V vertex : grafo.vertices()) {
            recorridoEnProfundidad(grafo, grafo.construirComparable(vertex), consumer);
        }
    }


    default <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Consumer<V> consumer) {
        for (V v : grafo.vertices()) {
            recorridoEnAmplitud(grafo, grafo.construirComparable(v), consumer);
        }
    }
}
