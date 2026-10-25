package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.List;

public class BeaNoDirigidoTest extends TestCase {

    private UndirectedGraph<String, WeightedEdge> grafo;
    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();

    @Override
    protected void setUp() {
        grafo = new UndirectedGraph<>();
    }

    private void vertices(String... nombres) {
        for (String n : nombres) grafo.agregarVertice(n);
    }

    private void arista(String a, String b) {
        grafo.agregarArista(a, b, new WeightedEdge(1));
    }

    private List<String> recorrido() {
        List<String> orden = new ArrayList<>();
        algoritmos.bea(grafo, orden::add);
        return orden;
    }

    /** A - B, C; B - D; C - E: primero el nivel 1 (B, C) y después el nivel 2 (D, E). */
    public void testVisitaPorNiveles() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "E");
        assertEquals(List.of("A", "B", "C", "D", "E"), recorrido());
    }

    /** D se alcanza desde B y desde C (hay un ciclo): se visita una sola vez. */
    public void testNoRepiteVertices() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "D");
        assertEquals(List.of("A", "B", "C", "D"), recorrido());
    }

    /** Las aristas se recorren en los dos sentidos: desde C se llega a A aunque se agregó A-B, B-C. */
    public void testRecorreEnLosDosSentidos() {
        vertices("C", "B", "A");
        arista("A", "B");
        arista("B", "C");
        assertEquals(List.of("C", "B", "A"), recorrido());
    }

    public void testRecorreTodasLasComponentes() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("C", "D");
        assertEquals(List.of("A", "B", "C", "D", "E"), recorrido());
    }

    public void testGrafoVacio() {
        assertTrue(recorrido().isEmpty());
    }

    public void testUnSoloVertice() {
        vertices("A");
        assertEquals(List.of("A"), recorrido());
    }
}
