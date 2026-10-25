package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.Set;

public class DirectedGraphTest extends TestCase {

    private DirectedGraph<String, WeightedEdge> grafo;

    @Override
    protected void setUp() {
        grafo = new DirectedGraph<>();
    }

    private void vertices(String... nombres) {
        for (String n : nombres) grafo.agregarVertice(n);
    }

    private boolean arista(String origen, String destino, double peso) {
        return grafo.agregarArista(origen, destino, new WeightedEdge(peso));
    }

    public void testAgregarVertice() {
        assertTrue(grafo.agregarVertice("A"));
        assertEquals(1, grafo.cantidadDeVertices());
        assertTrue(grafo.existeVertice(grafo.construirComparable("A")));
    }

    public void testVerticeRepetidoNoSeAgrega() {
        assertTrue(grafo.agregarVertice("A"));
        assertFalse(grafo.agregarVertice("A"));
        assertEquals(1, grafo.cantidadDeVertices());
    }

    public void testAristaConVerticeInexistente() {
        vertices("A");
        assertFalse(arista("A", "Z", 1));
        assertFalse(arista("Z", "A", 1));
        assertEquals(0, grafo.cantidadDeAristas());
    }

    /** La segunda A -> B se rechaza y la primera conserva su peso. */
    public void testAristaRepetidaNoSeAgrega() {
        vertices("A", "B");
        assertTrue(arista("A", "B", 3));
        assertFalse(arista("A", "B", 9));
        assertEquals(1, grafo.cantidadDeAristas());
        assertEquals(3.0, grafo.obtenerArista("A", "B").dato().getWeight(), 0.0001);
    }

    public void testAristaDeUnVerticeAsiMismo() {
        vertices("A");
        assertTrue(arista("A", "A", 2));
        assertTrue(grafo.existeArista(grafo.construirComparable("A"), grafo.construirComparable("A")));
        assertEquals(1, grafo.gradoDeSalida(grafo.construirComparable("A")));
        assertEquals(1, grafo.gradoDeEntrada(grafo.construirComparable("A")));
    }

    public void testAristaTieneSentido() {
        vertices("A", "B");
        arista("A", "B", 1);
        assertTrue(grafo.existeArista(grafo.construirComparable("A"), grafo.construirComparable("B")));
        assertFalse(grafo.existeArista(grafo.construirComparable("B"), grafo.construirComparable("A")));
        assertNull(grafo.obtenerArista("B", "A"));
    }

    public void testAristasOpuestasSonDistintas() {
        vertices("A", "B");
        assertTrue(arista("A", "B", 1));
        assertTrue(arista("B", "A", 5));
        assertEquals(2, grafo.cantidadDeAristas());
        assertEquals(1.0, grafo.obtenerArista("A", "B").dato().getWeight(), 0.0001);
        assertEquals(5.0, grafo.obtenerArista("B", "A").dato().getWeight(), 0.0001);
    }

    public void testEliminarArista() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 1);
        assertTrue(grafo.eliminarArista(grafo.construirComparable("A"), grafo.construirComparable("B")));
        assertFalse(grafo.existeArista(grafo.construirComparable("A"), grafo.construirComparable("B")));
        assertTrue(grafo.existeArista(grafo.construirComparable("B"), grafo.construirComparable("A")));
        assertEquals(2, grafo.cantidadDeVertices());
    }

    public void testEliminarAristaInexistente() {
        vertices("A", "B");
        assertFalse(grafo.eliminarArista(grafo.construirComparable("A"), grafo.construirComparable("B")));
        assertFalse(grafo.eliminarArista(grafo.construirComparable("Z"), grafo.construirComparable("B")));
    }

    /** B tiene un arco que sale (B -> C) y uno que entra (A -> B): al borrar B desaparecen los dos. */
    public void testRemoverVerticeBorraArcosQueSalenYQueEntran() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("A", "C", 1);
        assertTrue(grafo.removerVertice(grafo.construirComparable("B")));

        assertEquals(2, grafo.cantidadDeVertices());
        assertEquals(1, grafo.cantidadDeAristas());
        assertTrue(grafo.existeArista(grafo.construirComparable("A"), grafo.construirComparable("C")));
        assertEquals(Set.of("C"), grafo.successors(grafo.construirComparable("A")));
        assertTrue(grafo.predecessors(grafo.construirComparable("C")).contains("A"));
        assertFalse(grafo.predecessors(grafo.construirComparable("C")).contains("B"));
    }

    public void testRemoverVerticeInexistente() {
        vertices("A");
        assertFalse(grafo.removerVertice(grafo.construirComparable("Z")));
        assertEquals(1, grafo.cantidadDeVertices());
    }

    /** Si se vuelve a agregar un vértice borrado, no conserva los arcos de antes. */
    public void testVerticeRemovidoYAgregadoDeNuevoVieneSinArcos() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 1);
        grafo.removerVertice(grafo.construirComparable("B"));
        grafo.agregarVertice("B");
        assertEquals(0, grafo.cantidadDeAristas());
        assertEquals(0, grafo.gradoDeEntrada(grafo.construirComparable("B")));
        assertEquals(0, grafo.gradoDeSalida(grafo.construirComparable("A")));
    }

    /** A -> B, A -> C, B -> C, D -> C. */
    public void testSuccessorsYPredecessors() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("A", "C", 1);
        arista("B", "C", 1);
        arista("D", "C", 1);

        assertEquals(Set.of("B", "C"), grafo.successors(grafo.construirComparable("A")));
        assertEquals(Set.of("C"), grafo.successors(grafo.construirComparable("B")));
        assertTrue(grafo.successors(grafo.construirComparable("C")).isEmpty());

        assertEquals(Set.of("A", "B", "D"), grafo.predecessors(grafo.construirComparable("C")));
        assertEquals(Set.of("A"), grafo.predecessors(grafo.construirComparable("B")));
        assertTrue(grafo.predecessors(grafo.construirComparable("A")).isEmpty());
    }

    public void testGradoDeEntradaYDeSalida() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("A", "C", 1);
        arista("B", "C", 1);
        arista("D", "C", 1);

        assertEquals(2, grafo.gradoDeSalida(grafo.construirComparable("A")));
        assertEquals(0, grafo.gradoDeEntrada(grafo.construirComparable("A")));
        assertEquals(1, grafo.gradoDeSalida(grafo.construirComparable("B")));
        assertEquals(1, grafo.gradoDeEntrada(grafo.construirComparable("B")));
        assertEquals(0, grafo.gradoDeSalida(grafo.construirComparable("C")));
        assertEquals(3, grafo.gradoDeEntrada(grafo.construirComparable("C")));
    }

    public void testVerticeInexistenteNoTieneVecinos() {
        vertices("A");
        assertTrue(grafo.successors(grafo.construirComparable("Z")).isEmpty());
        assertTrue(grafo.predecessors(grafo.construirComparable("Z")).isEmpty());
        assertEquals(0, grafo.gradoDeSalida(grafo.construirComparable("Z")));
        assertEquals(0, grafo.gradoDeEntrada(grafo.construirComparable("Z")));
    }

    public void testAristasYCantidadDeAristas() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "C", 2);
        arista("C", "A", 3);

        assertEquals(3, grafo.cantidadDeAristas());
        Set<Edge<String, WeightedEdge>> aristas = grafo.aristas();
        assertEquals(3, aristas.size());
        for (Edge<String, WeightedEdge> e : aristas) {
            assertTrue(e.directed());
            assertTrue(grafo.existeArista(grafo.construirComparable(e.source()), grafo.construirComparable(e.target())));
        }
    }

    public void testCantidadDeAristasDeGrafoSinAristas() {
        vertices("A", "B");
        assertEquals(0, grafo.cantidadDeAristas());
        assertTrue(grafo.aristas().isEmpty());
    }

    public void testAdyacencias() {
        vertices("A", "B", "C");
        arista("A", "B", 4);
        arista("A", "C", 7);
        arista("B", "A", 1);

        assertEquals(2, grafo.adyacencias(grafo.construirComparable("A")).size());
        for (Edge<String, WeightedEdge> e : grafo.adyacencias(grafo.construirComparable("A"))) {
            assertEquals("A", e.source());
        }
        assertEquals(1, grafo.adyacencias(grafo.construirComparable("B")).size());
        assertTrue(grafo.adyacencias(grafo.construirComparable("C")).isEmpty());
    }

    public void testAdyacenciasDeVerticeInexistenteEsVacia() {
        vertices("A", "B");
        arista("A", "B", 1);
        assertTrue(grafo.adyacencias(grafo.construirComparable("Z")).isEmpty());
    }

    public void testBuscarVertice() {
        vertices("A", "B");
        assertEquals("B", grafo.buscarVertice(grafo.construirComparable("B")));
        assertNull(grafo.buscarVertice(grafo.construirComparable("Z")));
    }

    /** Un criterio que no usa equals: busca el vértice por su inicial. */
    public void testBuscarVerticePorCriterio() {
        vertices("Montevideo", "Buenos_Aires");
        assertEquals("Buenos_Aires", grafo.buscarVertice(x -> x.startsWith("B") ? 0 : 1));
    }

    public void testVaciar() {
        vertices("A", "B");
        arista("A", "B", 1);
        grafo.vaciar();
        assertEquals(0, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
        assertNull(grafo.buscarVertice(grafo.construirComparable("A")));
        assertTrue(grafo.agregarVertice("A"));
    }
}
