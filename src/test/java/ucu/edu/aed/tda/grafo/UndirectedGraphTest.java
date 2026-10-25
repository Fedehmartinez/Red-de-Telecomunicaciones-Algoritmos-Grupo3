package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.List;

public class UndirectedGraphTest extends TestCase {

    private UndirectedGraph<String, WeightedEdge> grafo;

    @Override
    protected void setUp() {
        grafo = new UndirectedGraph<>();
    }

    private void vertices(String... nombres) {
        for (String n : nombres) grafo.agregarVertice(n);
    }

    private boolean arista(String a, String b) {
        return grafo.agregarArista(a, b, new WeightedEdge(1));
    }

    private boolean existe(String a, String b) {
        return grafo.existeArista(grafo.construirComparable(a), grafo.construirComparable(b));
    }

    // ---- vértices y aristas ----

    public void testAgregarVerticeRepetido() {
        assertTrue(grafo.agregarVertice("A"));
        assertFalse(grafo.agregarVertice("A"));
        assertEquals(1, grafo.cantidadDeVertices());
    }

    public void testLaAristaSeVeDesdeLosDosExtremos() {
        vertices("A", "B");
        assertTrue(arista("A", "B"));
        assertTrue(existe("A", "B"));
        assertTrue(existe("B", "A"));
    }

    /** adyacencias(v) tiene que devolver las aristas con v como source(), aunque se hayan agregado al revés. */
    public void testAdyacenciasTienenAlVerticeComoOrigen() {
        vertices("A", "B");
        arista("A", "B");
        List<Edge<String, WeightedEdge>> deB = grafo.adyacencias(grafo.construirComparable("B"));
        assertEquals(1, deB.size());
        assertEquals("B", deB.get(0).source());
        assertEquals("A", deB.get(0).target());
    }

    /** Cada arista se guarda dos veces, pero aristas() y cantidadDeAristas() la cuentan una sola vez. */
    public void testAristasNoSeRepiten() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        assertEquals(3, grafo.aristas().size());
        assertEquals(3, grafo.cantidadDeAristas());
    }

    public void testAristaRepetidaOInvertidaNoSeAgrega() {
        vertices("A", "B");
        assertTrue(arista("A", "B"));
        assertFalse(arista("A", "B"));
        assertFalse(arista("B", "A"));
        assertEquals(1, grafo.cantidadDeAristas());
    }

    public void testLazoNoSeAgrega() {
        vertices("A");
        assertFalse(arista("A", "A"));
        assertEquals(0, grafo.cantidadDeAristas());
    }

    public void testAristaConVerticeInexistente() {
        vertices("A");
        assertFalse(arista("A", "Z"));
        assertFalse(arista("Z", "A"));
        assertFalse(existe("A", "Z"));
    }

    public void testEliminarAristaDesdeElOtroExtremo() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        assertTrue(grafo.eliminarArista(grafo.construirComparable("B"), grafo.construirComparable("A")));
        assertFalse(existe("A", "B"));
        assertFalse(existe("B", "A"));
        assertTrue(existe("B", "C"));
        assertFalse(grafo.eliminarArista(grafo.construirComparable("A"), grafo.construirComparable("B")));
    }

    public void testRemoverVerticeBorraSusAristas() {
        vertices("C", "A", "B", "D");
        arista("C", "A");
        arista("C", "B");
        arista("A", "D");
        assertTrue(grafo.removerVertice(grafo.construirComparable("C")));
        assertFalse(grafo.existeVertice(grafo.construirComparable("C")));
        assertEquals(3, grafo.cantidadDeVertices());
        assertEquals(1, grafo.cantidadDeAristas());
        assertTrue(grafo.adyacencias(grafo.construirComparable("B")).isEmpty());
        assertTrue(existe("A", "D"));
    }

    public void testRemoverVerticeInexistente() {
        vertices("A");
        assertFalse(grafo.removerVertice(grafo.construirComparable("Z")));
        assertEquals(1, grafo.cantidadDeVertices());
    }

    public void testAdyacenciasDeVerticeInexistenteEsVacia() {
        assertTrue(grafo.adyacencias(grafo.construirComparable("Z")).isEmpty());
    }

    public void testVaciar() {
        vertices("A", "B");
        arista("A", "B");
        grafo.vaciar();
        assertEquals(0, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
    }

    // ---- esConexo ----

    public void testGrafoVacioEsConexo() {
        assertTrue(grafo.esConexo());
    }

    public void testUnSoloVerticeEsConexo() {
        vertices("A");
        assertTrue(grafo.esConexo());
    }

    public void testVerticeAisladoNoEsConexo() {
        vertices("A", "B", "C");
        arista("A", "B");
        assertFalse(grafo.esConexo());
    }

    public void testDosComponentesNoEsConexo() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("C", "D");
        assertFalse(grafo.esConexo());
    }

    /** En el dirigido A -> B <- C no es conexo; acá las aristas no tienen sentido y sí lo es. */
    public void testNoImportaElSentidoEnQueSeAgregaron() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("C", "B");
        assertTrue(grafo.esConexo());
    }

    // ---- tieneCiclos ----

    public void testGrafoVacioNoTieneCiclos() {
        assertFalse(grafo.tieneCiclos());
    }

    public void testVerticesSinAristasNoTieneCiclos() {
        vertices("A", "B", "C");
        assertFalse(grafo.tieneCiclos());
    }

    /** Desde B se ve a A (su padre) como visitado: no es un ciclo, es la misma arista. */
    public void testCadenaNoTieneCiclos() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        assertFalse(grafo.tieneCiclos());
    }

    public void testArbolNoTieneCiclos() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("A", "C");
        arista("B", "D");
        arista("B", "E");
        assertFalse(grafo.tieneCiclos());
    }

    public void testTrianguloTieneCiclos() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        assertTrue(grafo.tieneCiclos());
    }

    public void testCicloEnComponenteNoInicialTieneCiclos() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("C", "D");
        arista("D", "E");
        arista("E", "C");
        assertTrue(grafo.tieneCiclos());
    }
}
