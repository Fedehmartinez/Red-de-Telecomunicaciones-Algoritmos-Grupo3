package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

public class DirectedGraphTieneCiclosTest extends TestCase {

    private DirectedGraph<String, WeightedEdge> grafo;

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

    public void testGrafoVacioNoTieneCiclos() {
        assertFalse(grafo.tieneCiclos());
    }

    public void testVerticesSinAristasNoTieneCiclos() {
        vertices("A", "B", "C");
        assertFalse(grafo.tieneCiclos());
    }

    public void testCadenaNoTieneCiclos() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        assertFalse(grafo.tieneCiclos());
    }

    /** A llega a C por dos caminos (A->C y A->B->C): C queda visitado pero no es un ciclo. */
    public void testDiamanteNoTieneCiclos() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "D");
        assertFalse(grafo.tieneCiclos());
    }

    public void testAciclicoNoConexoNoTieneCiclos() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("C", "D");
        assertFalse(grafo.tieneCiclos());
    }

    public void testCicloSimpleSiTieneCiclos() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        assertTrue(grafo.tieneCiclos());
    }

    public void testLazoSiTieneCiclos() {
        vertices("A");
        arista("A", "A");
        assertTrue(grafo.tieneCiclos());
    }

    public void testCicloEnComponenteNoInicialSiTieneCiclos() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("C", "D");
        arista("D", "C");
        assertTrue(grafo.tieneCiclos());
    }
}
