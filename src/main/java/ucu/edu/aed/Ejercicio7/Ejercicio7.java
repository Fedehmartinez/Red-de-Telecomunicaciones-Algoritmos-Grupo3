package ucu.edu.aed.Ejercicio7;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.DijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

/**
 * Ejercicio 7: Dijkstra desde B (parte 1) y Floyd, excentricidades y centro (parte 2).
 * Las respuestas escritas están en Ejercicio7.txt.
 */
public class Ejercicio7 {

    /** Grafo de la matriz de adyacencias de la parte 1. */
    public static DirectedGraph<String, WeightedEdge> grafoParte1() {
        DirectedGraph<String, WeightedEdge> g = vertices("A", "B", "C", "D", "E");
        arista(g, "A", "B", 1);
        arista(g, "A", "C", 2);
        arista(g, "A", "D", 7);
        arista(g, "B", "A", 7);
        arista(g, "B", "C", 1);
        arista(g, "B", "D", 2);
        arista(g, "C", "D", 3);
        arista(g, "D", "A", 6);
        arista(g, "D", "C", 4);
        arista(g, "D", "E", 4);
        arista(g, "E", "B", 2);
        arista(g, "E", "D", 8);
        return g;
    }

    /** Grafo de la figura 1 de la parte 2. */
    public static DirectedGraph<String, WeightedEdge> grafoParte2() {
        DirectedGraph<String, WeightedEdge> g = vertices("A", "B", "C", "D", "E");
        arista(g, "A", "C", 1);
        arista(g, "A", "D", 4);
        arista(g, "B", "A", 6);
        arista(g, "B", "E", 3);
        arista(g, "C", "B", 2);
        arista(g, "C", "E", 1);
        arista(g, "D", "C", 5);
        arista(g, "E", "A", 3);
        return g;
    }

    private static DirectedGraph<String, WeightedEdge> vertices(String... nombres) {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        for (String n : nombres) g.agregarVertice(n);
        return g;
    }

    private static void arista(DirectedGraph<String, WeightedEdge> g, String origen, String destino, double peso) {
        g.agregarArista(origen, destino, new WeightedEdge(peso));
    }

    /**
     * Parte 1.4: dado el vector de predecesores, arma el camino entre el origen y el destino.
     * Se sigue el vector hacia atrás desde el destino hasta llegar al origen.
     * Devuelve lista vacía si el destino no es alcanzable.
     */
    public static <V> List<V> camino(Map<V, V> predecesores, V origen, V destino) {
        LinkedList<V> camino = new LinkedList<>();
        V actual = destino;
        while (actual != null && !actual.equals(origen)) {
            camino.addFirst(actual);
            actual = predecesores.get(actual);
        }
        if (actual == null) return new ArrayList<>();   // se cortó antes de llegar al origen
        camino.addFirst(origen);
        return camino;
    }

    public static void main(String[] args) {
        DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

        // ---- Parte 1: Dijkstra desde B ----
        DirectedGraph<String, WeightedEdge> g1 = grafoParte1();
        DijkstraResult<String> dijkstra = (DijkstraResult<String>) algoritmos.dijkstra(g1.construirComparable("B"), g1);
        System.out.println("PARTE 1 - Dijkstra desde B");
        for (String v : g1.vertices()) {
            System.out.println("  " + v + ": costo=" + dijkstra.getCost(v)
                    + " predecesor=" + dijkstra.getPredecesor(v) + " camino=" + dijkstra.getPath(v));
        }

        // ---- Parte 2: Floyd ----
        DirectedGraph<String, WeightedEdge> g2 = grafoParte2();
        IFloydWarshallResult<String> floyd = algoritmos.floyd(g2);
        System.out.println("\nPARTE 2 - Floyd (costo y camino de fila a columna)");
        for (String o : g2.vertices()) {
            for (String d : g2.vertices()) {
                if (!o.equals(d)) {
                    System.out.println("  " + o + " -> " + d + ": costo=" + floyd.getCost(o, d) + " camino=" + floyd.getPath(o, d));
                }
            }
        }
        System.out.println("\nCamino de menor costo A -> E: " + floyd.getPath("A", "E") + " costo=" + floyd.getCost("A", "E"));

        System.out.println("\nExcentricidades:");
        for (String v : g2.vertices()) {
            System.out.println("  " + v + ": " + algoritmos.obtenerExcentricidad(g2, g2.construirComparable(v)));
        }
        System.out.println("Centro del grafo: " + algoritmos.obtenerCentroGrafo(g2));
    }
}
