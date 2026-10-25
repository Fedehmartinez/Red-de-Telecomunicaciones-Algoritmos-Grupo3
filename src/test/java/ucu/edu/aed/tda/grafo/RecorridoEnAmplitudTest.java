package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.List;

public class RecorridoEnAmplitudTest extends TestCase {

    private DirectedGraph<String, WeightedEdge> grafo;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    @Override
    protected void setUp() {
        grafo = new DirectedGraph<>();
    }

    private void vertices(String... nombres) {
        for (String n : nombres) grafo.agregarVertice(n);
    }

    private void arista(String origen, String destino) {
        grafo.agregarArista(origen, destino, new WeightedEdge(1));
    }

    private List<String> desde(String origen) {
        List<String> orden = new ArrayList<>();
        algoritmos.recorridoEnAmplitud(grafo, grafo.construirComparable(origen), orden::add);
        return orden;
    }

    /** A -> B, C; B -> D; C -> E: primero el nivel 1 (B, C) y después el nivel 2 (D, E). */
    public void testVisitaPorNiveles() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "E");
        assertEquals(List.of("A", "B", "C", "D", "E"), desde("A"));
    }

    /** En profundidad sería A, B, D, C; en amplitud C va antes que D. */
    public void testDistintoDeProfundidad() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        assertEquals(List.of("A", "B", "C", "D"), desde("A"));
    }

    /** D se alcanza desde B y desde C, y hay un ciclo C -> A: cada vértice se visita una sola vez. */
    public void testNoRepiteVertices() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "D");
        arista("C", "A");
        assertEquals(List.of("A", "B", "C", "D"), desde("A"));
    }

    public void testSoloVisitaLoAlcanzable() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("C", "A");
        assertEquals(List.of("A", "B"), desde("A"));
    }

    public void testOrigenInexistenteNoVisitaNada() {
        vertices("A");
        assertTrue(desde("Z").isEmpty());
    }

    /** Todo el grafo: A -> B y C -> D no están unidos; se recorren los dos, sin repetir B. */
    public void testTodoElGrafo() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("C", "D");
        arista("C", "B");
        List<String> orden = new ArrayList<>();
        algoritmos.recorridoEnAmplitud(grafo, orden::add);
        assertEquals(List.of("A", "B", "C", "D"), orden);
    }
}
