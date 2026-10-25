package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.List;

public class RecorridoEnProfundidadTest extends TestCase {

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
        algoritmos.recorridoEnProfundidad(grafo, grafo.construirComparable(origen), orden::add);
        return orden;
    }

    private List<String> todoElGrafo() {
        List<String> orden = new ArrayList<>();
        algoritmos.recorridoEnProfundidad(grafo, orden::add);
        return orden;
    }

    /** A -> B, C; B -> D; C -> E: se baja por B hasta D antes de pasar a C. */
    public void testBajaPorUnaRamaAntesDeLaOtra() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "E");
        assertEquals(List.of("A", "B", "D", "C", "E"), desde("A"));
    }

    /** En amplitud sería A, B, C, D; en profundidad D va antes que C. */
    public void testDistintoDeAmplitud() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        assertEquals(List.of("A", "B", "D", "C"), desde("A"));
    }

    public void testCadena() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("B", "C");
        arista("C", "D");
        assertEquals(List.of("A", "B", "C", "D"), desde("A"));
        assertEquals(List.of("C", "D"), desde("C"));
    }

    /** D se alcanza desde B y desde C: se visita una sola vez, por la primera rama. */
    public void testNoRepiteVertices() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "D");
        assertEquals(List.of("A", "B", "D", "C"), desde("A"));
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

    /** A -> B -> C -> A: vuelve al origen y se detiene. */
    public void testConCicloNoSeCuelga() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        assertEquals(List.of("A", "B", "C"), desde("A"));
        assertEquals(List.of("B", "C", "A"), desde("B"));
    }

    public void testLazoNoSeCuelga() {
        vertices("A");
        arista("A", "A");
        assertEquals(List.of("A"), desde("A"));
    }

    public void testUnSoloVertice() {
        vertices("A");
        assertEquals(List.of("A"), desde("A"));
    }

    /** Todo el grafo: A -> B y C -> D no están unidos; se recorren los dos árboles, sin repetir B. */
    public void testTodoElGrafoComoBosque() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("C", "D");
        arista("C", "B");
        assertEquals(List.of("A", "B", "C", "D"), todoElGrafo());
    }

    /** B se alcanza desde A, así que el segundo árbol (C) no lo visita de nuevo. */
    public void testTodoElGrafoNoRepiteVertices() {
        vertices("C", "A", "B");
        arista("C", "B");
        arista("A", "B");
        List<String> orden = todoElGrafo();
        assertEquals(List.of("C", "B", "A"), orden);
        assertEquals(3, orden.stream().distinct().count());
    }

    public void testTodoElGrafoConCiclos() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("B", "A");
        arista("C", "D");
        arista("D", "C");
        assertEquals(List.of("A", "B", "C", "D"), todoElGrafo());
    }

    public void testVerticesSinAristas() {
        vertices("A", "B", "C");
        assertEquals(List.of("A", "B", "C"), todoElGrafo());
    }

    public void testGrafoVacio() {
        assertTrue(todoElGrafo().isEmpty());
        assertTrue(desde("A").isEmpty());
    }
}
