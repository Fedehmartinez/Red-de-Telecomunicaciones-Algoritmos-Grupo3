package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.Ejercicio7.Ejercicio7;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.DijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio7Test extends TestCase {

    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    public void testDijkstraDesdeB() {
        DirectedGraph<String, WeightedEdge> g = Ejercicio7.grafoParte1();
        DijkstraResult<String> r = (DijkstraResult<String>) algoritmos.dijkstra(g.construirComparable("B"), g);

        assertEquals(0.0, r.getCost("B"));
        assertEquals(7.0, r.getCost("A"));
        assertEquals(1.0, r.getCost("C"));
        assertEquals(2.0, r.getCost("D"));
        assertEquals(6.0, r.getCost("E"));
        assertEquals(List.of("B", "D", "E"), r.getPath("E"));
        assertEquals("D", r.getPredecesor("E"));
        assertNull(r.getPredecesor("B"));
    }

    public void testDijkstraVerticeInalcanzable() {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        g.agregarVertice("X");
        g.agregarVertice("Y");
        DijkstraResult<String> r = (DijkstraResult<String>) algoritmos.dijkstra(g.construirComparable("X"), g);

        assertEquals(Double.POSITIVE_INFINITY, r.getCost("Y"));
        assertTrue(r.getPath("Y").isEmpty());
    }

    public void testDijkstraOrigenInexistente() {
        DirectedGraph<String, WeightedEdge> g = Ejercicio7.grafoParte1();
        try {
            algoritmos.dijkstra(g.construirComparable("Z"), g);
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testCaminoDesdeVectorDePredecesores() {
        Map<String, String> pred = new HashMap<>();
        pred.put("A", "B");
        pred.put("C", "B");
        pred.put("D", "B");
        pred.put("E", "D");

        assertEquals(List.of("B", "D", "E"), Ejercicio7.camino(pred, "B", "E"));
        assertEquals(List.of("B"), Ejercicio7.camino(pred, "B", "B"));
        assertTrue(Ejercicio7.camino(pred, "B", "Z").isEmpty());
    }

    public void testFloydCaminoAE() {
        IFloydWarshallResult<String> r = algoritmos.floyd(Ejercicio7.grafoParte2());

        assertEquals(2.0, r.getCost("A", "E"));
        assertEquals(List.of("A", "C", "E"), r.getPath("A", "E"));
        assertEquals(List.of("A"), r.getPath("A", "A"));
    }

    public void testFloydNoConectados() {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        g.agregarVertice("X");
        g.agregarVertice("Y");
        g.agregarArista("X", "Y", new WeightedEdge(3));
        IFloydWarshallResult<String> r = algoritmos.floyd(g);

        assertTrue(r.connected("X", "Y"));
        assertFalse(r.connected("Y", "X"));
        assertTrue(r.getPath("Y", "X").isEmpty());
    }

    public void testExcentricidadesYCentro() {
        DirectedGraph<String, WeightedEdge> g = Ejercicio7.grafoParte2();
        double[] esperado = {9, 7, 7, 10, 6};   // máximo de cada columna de la matriz de Floyd (ver Ejercicio7.txt)
        String[] vertices = {"A", "B", "C", "D", "E"};
        for (int i = 0; i < vertices.length; i++) {
            assertEquals(vertices[i], esperado[i], algoritmos.obtenerExcentricidad(g, g.construirComparable(vertices[i])));
        }
        assertEquals("E", algoritmos.obtenerCentroGrafo(g));
    }
}
