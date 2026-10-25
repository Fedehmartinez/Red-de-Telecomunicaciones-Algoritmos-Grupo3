package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.List;
import java.util.Set;

/** Prim, Kruskal y searchMinEdge. */
public class ArbolAbarcadorTest extends TestCase {

    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();

    private static void arista(UndirectedGraph<String, WeightedEdge> g, String a, String b, double costo) {
        g.agregarArista(a, b, new WeightedEdge(costo));
    }

    /** Grafo del teórico (6 vértices): el árbol abarcador de costo mínimo es 1-3, 4-6, 2-5, 3-6, 2-3 con costo 15. */
    private static UndirectedGraph<String, WeightedEdge> grafoDelTeorico() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("1", "2", "3", "4", "5", "6"));
        arista(g, "1", "2", 6);
        arista(g, "1", "3", 1);
        arista(g, "1", "4", 5);
        arista(g, "2", "3", 5);
        arista(g, "3", "4", 5);
        arista(g, "2", "5", 3);
        arista(g, "3", "5", 6);
        arista(g, "3", "6", 4);
        arista(g, "4", "6", 2);
        arista(g, "5", "6", 6);
        return g;
    }

    /** Grafo de los ejercicios del teórico (A..G): costo mínimo 15 con F-C, B-D, E-G, A-D, G-C, D-F. */
    private static UndirectedGraph<String, WeightedEdge> grafoDelEjercicio() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C", "D", "E", "F", "G"));
        arista(g, "A", "B", 5);
        arista(g, "A", "D", 3);
        arista(g, "B", "D", 2);
        arista(g, "D", "E", 7);
        arista(g, "D", "F", 4);
        arista(g, "E", "G", 2);
        arista(g, "E", "F", 5);
        arista(g, "F", "G", 9);
        arista(g, "G", "C", 3);
        arista(g, "F", "C", 1);
        return g;
    }

    private static double costo(IUndirectedGraph<String, WeightedEdge> g) {
        double total = 0;
        for (Edge<String, WeightedEdge> e : g.aristas()) total += e.dato().getWeight();
        return total;
    }

    private static boolean tiene(IUndirectedGraph<String, WeightedEdge> g, String a, String b) {
        return g.existeArista(g.construirComparable(a), g.construirComparable(b));
    }

    /** Árbol abarcador: mismos vértices que el original, n-1 aristas, conexo y sin ciclos. */
    private static void assertArbolAbarcador(IUndirectedGraph<String, WeightedEdge> arbol, IUndirectedGraph<String, WeightedEdge> original) {
        assertEquals(original.vertices(), arbol.vertices());
        assertEquals(original.cantidadDeVertices() - 1, arbol.cantidadDeAristas());
        assertTrue(arbol.esConexo());
        assertFalse(arbol.tieneCiclos());
    }

    // ---- Prim ----

    public void testPrimGrafoDelTeorico() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelTeorico();
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.prim(g, g.construirComparable("1"));

        assertArbolAbarcador(arbol, g);
        assertEquals(15.0, costo(arbol));
        assertTrue(tiene(arbol, "1", "3"));
        assertTrue(tiene(arbol, "2", "3"));
        assertTrue(tiene(arbol, "2", "5"));
        assertTrue(tiene(arbol, "3", "6"));
        assertTrue(tiene(arbol, "4", "6"));
    }

    /** El costo mínimo no depende del vértice por el que se empieza. */
    public void testPrimDesdeCualquierOrigenDaElMismoCosto() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelEjercicio();
        for (String origen : g.vertices()) {
            IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.prim(g, g.construirComparable(origen));
            assertArbolAbarcador(arbol, g);
            assertEquals("desde " + origen, 15.0, costo(arbol));
        }
    }

    public void testPrimSinOrigen() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelTeorico();
        assertEquals(15.0, costo(algoritmos.prim(g)));
    }

    public void testPrimGrafoVacioDevuelveNull() {
        assertNull(algoritmos.prim(new UndirectedGraph<String, WeightedEdge>()));
    }

    public void testPrimUnSoloVertice() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertice("A");
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.prim(g, g.construirComparable("A"));
        assertEquals(Set.of("A"), arbol.vertices());
        assertEquals(0, arbol.cantidadDeAristas());
    }

    /** No conexo: solo se arma el árbol de la componente del origen; el resto queda sin aristas. */
    public void testPrimNoConexoSoloLaComponenteDelOrigen() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C", "D", "E"));
        arista(g, "A", "B", 1);
        arista(g, "C", "D", 2);
        arista(g, "D", "E", 1);
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.prim(g, g.construirComparable("A"));

        assertEquals(g.vertices(), arbol.vertices());
        assertEquals(1, arbol.cantidadDeAristas());
        assertTrue(tiene(arbol, "A", "B"));
    }

    public void testPrimOrigenInexistente() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelTeorico();
        try {
            algoritmos.prim(g, g.construirComparable("Z"));
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    // ---- Kruskal ----

    public void testKruskalGrafoDelTeorico() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelTeorico();
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.kruskal(g);

        assertArbolAbarcador(arbol, g);
        assertEquals(15.0, costo(arbol));
        assertTrue(tiene(arbol, "1", "3"));
        assertTrue(tiene(arbol, "2", "3"));
        assertTrue(tiene(arbol, "2", "5"));
        assertTrue(tiene(arbol, "3", "6"));
        assertTrue(tiene(arbol, "4", "6"));
    }

    public void testKruskalGrafoDelEjercicio() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelEjercicio();
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.kruskal(g);

        assertArbolAbarcador(arbol, g);
        assertEquals(15.0, costo(arbol));
        assertFalse(tiene(arbol, "F", "G"));   // la más cara, cerraría un ciclo
    }

    public void testKruskalGrafoVacio() {
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.kruskal(new UndirectedGraph<String, WeightedEdge>());
        assertEquals(0, arbol.cantidadDeVertices());
        assertEquals(0, arbol.cantidadDeAristas());
    }

    public void testKruskalUnSoloVertice() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertice("A");
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.kruskal(g);
        assertEquals(Set.of("A"), arbol.vertices());
        assertEquals(0, arbol.cantidadDeAristas());
    }

    /** No conexo: devuelve un bosque, con n - (cantidad de componentes) aristas. */
    public void testKruskalNoConexoDevuelveBosque() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C", "D", "E"));
        arista(g, "A", "B", 1);
        arista(g, "C", "D", 2);
        arista(g, "C", "E", 5);
        arista(g, "D", "E", 1);
        IUndirectedGraph<String, WeightedEdge> bosque = algoritmos.kruskal(g);

        assertEquals(3, bosque.cantidadDeAristas());
        assertEquals(4.0, costo(bosque));
        assertFalse(bosque.esConexo());
        assertFalse(bosque.tieneCiclos());
    }

    // ---- los dos ----

    /** Cuadrado con diagonal, todo con costo 1: hay varios árboles posibles y todos cuestan 3. */
    public void testPesosIguales() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C", "D"));
        arista(g, "A", "B", 1);
        arista(g, "B", "C", 1);
        arista(g, "C", "D", 1);
        arista(g, "D", "A", 1);
        arista(g, "A", "C", 1);

        IUndirectedGraph<String, WeightedEdge> porPrim = algoritmos.prim(g, g.construirComparable("A"));
        IUndirectedGraph<String, WeightedEdge> porKruskal = algoritmos.kruskal(g);
        assertArbolAbarcador(porPrim, g);
        assertArbolAbarcador(porKruskal, g);
        assertEquals(3.0, costo(porPrim));
        assertEquals(3.0, costo(porKruskal));
    }

    /** Si el grafo ya es un árbol, el resultado es el mismo grafo. */
    public void testGrafoQueYaEsArbol() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C", "D"));
        arista(g, "A", "B", 4);
        arista(g, "B", "C", 1);
        arista(g, "B", "D", 9);

        assertEquals(g.aristas(), algoritmos.kruskal(g).aristas());
        assertEquals(g.aristas(), algoritmos.prim(g, g.construirComparable("D")).aristas());
    }

    public void testNoModificanElGrafoOriginal() {
        UndirectedGraph<String, WeightedEdge> g = grafoDelTeorico();
        algoritmos.prim(g, g.construirComparable("1"));
        algoritmos.kruskal(g);
        assertEquals(6, g.cantidadDeVertices());
        assertEquals(10, g.cantidadDeAristas());
    }

    // ---- searchMinEdge ----

    /** A-C está dentro de U y no cuenta; entre las que cruzan, la mínima es C-B. */
    public void testSearchMinEdgeEligeLaMenorQueCruza() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C", "D"));
        arista(g, "A", "C", 1);
        arista(g, "A", "B", 5);
        arista(g, "C", "B", 2);
        arista(g, "C", "D", 7);

        Edge<String, WeightedEdge> e = algoritmos.searchMinEdge(g, List.of("A", "C"), List.of("B", "D"));
        assertEquals("C", e.source());
        assertEquals("B", e.target());
        assertEquals(2.0, e.dato().getWeight());
    }

    public void testSearchMinEdgeSinAristaQueCruce() {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(List.of("A", "B", "C"));
        arista(g, "A", "B", 1);
        assertNull(algoritmos.searchMinEdge(g, List.of("A", "B"), List.of("C")));
    }
}
