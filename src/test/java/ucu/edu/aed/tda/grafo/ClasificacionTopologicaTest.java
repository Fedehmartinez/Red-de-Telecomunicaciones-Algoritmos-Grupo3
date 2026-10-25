package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.List;

public class ClasificacionTopologicaTest extends TestCase {

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

    /** Verifica el contrato: contiene todos los vértices una vez y para cada arco u->v, u va antes que v. */
    private void assertClasificacionValida(List<String> orden) {
        assertEquals(grafo.cantidadDeVertices(), orden.size());
        assertEquals(grafo.cantidadDeVertices(), orden.stream().distinct().count());
        for (Edge<String, WeightedEdge> e : grafo.aristas()) {
            assertTrue(e + " viola el orden", orden.indexOf(e.source()) < orden.indexOf(e.target()));
        }
    }

    public void testGrafoVacio() {
        assertTrue(algoritmos.calcularClasificacionTopologica(grafo).isEmpty());
    }

    public void testVerticesSinAristas() {
        vertices("A", "B", "C");
        assertClasificacionValida(algoritmos.calcularClasificacionTopologica(grafo));
    }

    public void testCadena() {
        vertices("C", "B", "A");        // insertados en orden "al revés" a propósito
        arista("A", "B");
        arista("B", "C");
        List<String> orden = algoritmos.calcularClasificacionTopologica(grafo);
        assertEquals(List.of("A", "B", "C"), orden);
    }

    public void testDiamante() {
        vertices("D", "C", "B", "A");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("C", "D");
        assertClasificacionValida(algoritmos.calcularClasificacionTopologica(grafo));
    }

    /** Previaturas: C1 -> C2 -> C3 -> C5, C1 -> C4 -> C5. */
    public void testPreviaturas() {
        vertices("C5", "C4", "C3", "C2", "C1");
        arista("C1", "C2");
        arista("C2", "C3");
        arista("C1", "C4");
        arista("C3", "C5");
        arista("C4", "C5");
        assertClasificacionValida(algoritmos.calcularClasificacionTopologica(grafo));
    }

    public void testNoConexo() {
        vertices("B", "A", "D", "C");
        arista("A", "B");
        arista("C", "D");
        assertClasificacionValida(algoritmos.calcularClasificacionTopologica(grafo));
    }

    public void testConCiclosLanzaExcepcion() {
        vertices("A", "B");
        arista("A", "B");
        arista("B", "A");
        try {
            algoritmos.calcularClasificacionTopologica(grafo);
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }
}
