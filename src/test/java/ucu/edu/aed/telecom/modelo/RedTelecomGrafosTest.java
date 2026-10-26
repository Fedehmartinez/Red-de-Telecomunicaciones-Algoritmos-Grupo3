package ucu.edu.aed.telecom.modelo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.List;
import java.util.Set;

/** grafoRuteo, grafoCableado y grafoResiliencia de RedTelecom. */
public class RedTelecomGrafosTest extends TestCase {

    private static final double DELTA = 0.0001;

    private RedTelecom red;

    @Override
    protected void setUp() {
        red = new RedTelecom();
    }

    private void routers(String... ids) {
        for (String id : ids) red.agregarNodo(new Nodo(id, TipoNodo.ROUTER, 10, 0));
    }

    private Enlace enlace(String a, String b, double latAB, double latBA, double ancho, double costo) {
        Enlace e = new Enlace(a, b, TipoEnlace.FIBRA, latAB, latBA, ancho, costo);
        red.agregarEnlace(e);
        return e;
    }

    private static double peso(DirectedGraph<String, WeightedEdge> g, String origen, String destino) {
        Edge<String, WeightedEdge> arco = g.obtenerArista(origen, destino);
        assertNotNull("falta el arco " + origen + " -> " + destino, arco);
        return arco.dato().getWeight();
    }

    private static boolean tiene(DirectedGraph<String, WeightedEdge> g, String origen, String destino) {
        return g.existeArista(g.construirComparable(origen), g.construirComparable(destino));
    }

    private static boolean tiene(UndirectedGraph<String, WeightedEdge> g, String a, String b) {
        return g.existeArista(g.construirComparable(a), g.construirComparable(b));
    }

    /**
     * Red de prueba: A-B (lat 1 / 5, 1000 Mbps, costo 10), B-C (2 / 2, 200 Mbps, costo 4),
     * A-C (9 / 9, 5000 Mbps, costo 30) y D sin enlaces.
     */
    private void redDePrueba() {
        routers("A", "B", "C", "D");
        enlace("A", "B", 1, 5, 1000, 10);
        enlace("B", "C", 2, 2, 200, 4);
        enlace("A", "C", 9, 9, 5000, 30);
    }

    // ---- ruteo ----

    public void testRuteoTieneTodosLosNodosAunqueNoTenganEnlaces() {
        redDePrueba();
        assertEquals(Set.of("A", "B", "C", "D"), red.grafoRuteo().vertices());
    }

    /** Cada enlace da dos arcos, cada uno con la latencia de su sentido. */
    public void testRuteoUnArcoPorSentidoConSuLatencia() {
        redDePrueba();
        DirectedGraph<String, WeightedEdge> g = red.grafoRuteo();
        assertEquals(6, g.cantidadDeAristas());
        assertEquals(1.0, peso(g, "A", "B"), DELTA);
        assertEquals(5.0, peso(g, "B", "A"), DELTA);
        assertEquals(2.0, peso(g, "B", "C"), DELTA);
        assertEquals(9.0, peso(g, "C", "A"), DELTA);
    }

    /** El enlace se cargó como C-B: el arco C -> B lleva latenciaAB y B -> C latenciaBA. */
    public void testRuteoRespetaElSentidoEnQueSeCargoElEnlace() {
        routers("B", "C");
        enlace("C", "B", 3, 7, 1000, 1);
        DirectedGraph<String, WeightedEdge> g = red.grafoRuteo();
        assertEquals(3.0, peso(g, "C", "B"), DELTA);
        assertEquals(7.0, peso(g, "B", "C"), DELTA);
    }

    /** Con Dijkstra de la biblioteca: de A a C conviene A-B-C (3); de C a A, C-B-A (7) le gana a C-A (9). */
    public void testRuteoConDijkstraUsaLaLatenciaDeCadaSentido() {
        redDePrueba();
        DirectedGraph<String, WeightedEdge> g = red.grafoRuteo();
        DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();
        IDijkstraResult<String> desdeA = algoritmos.dijkstra(g.construirComparable("A"), g);
        IDijkstraResult<String> desdeC = algoritmos.dijkstra(g.construirComparable("C"), g);
        assertEquals(3.0, desdeA.getCost("C"), DELTA);
        assertEquals(List.of("A", "B", "C"), desdeA.getPath("C"));
        assertEquals(7.0, desdeC.getCost("A"), DELTA);
        assertEquals(List.of("C", "B", "A"), desdeC.getPath("A"));
    }

    /** B-C tiene 200 Mbps: con mínimo 500 se va; con mínimo 200 (el borde) se queda. */
    public void testRuteoConAnchoDeBandaMinimo() {
        redDePrueba();
        DirectedGraph<String, WeightedEdge> con500 = red.grafoRuteo(500);
        assertFalse(tiene(con500, "B", "C"));
        assertFalse(tiene(con500, "C", "B"));
        assertTrue(tiene(con500, "A", "B"));
        assertTrue(tiene(con500, "A", "C"));
        assertEquals(4, con500.cantidadDeAristas());
        assertEquals(4, con500.cantidadDeVertices());   // los nodos quedan aunque se queden sin enlaces

        assertTrue(tiene(red.grafoRuteo(200), "B", "C"));
        assertEquals(6, red.grafoRuteo(0).cantidadDeAristas());
    }

    public void testRuteoConAnchoMinimoMayorQueTodos() {
        redDePrueba();
        assertEquals(0, red.grafoRuteo(1000000).cantidadDeAristas());
    }

    public void testRuteoAnchoMinimoInvalido() {
        redDePrueba();
        try {
            red.grafoRuteo(Double.NaN);
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testRuteoSinNodoCaido() {
        redDePrueba();
        DirectedGraph<String, WeightedEdge> g = red.grafoRuteo(0, Set.of("B"), Set.of());
        assertEquals(Set.of("A", "C", "D"), g.vertices());
        assertEquals(2, g.cantidadDeAristas());   // solo queda A-C en los dos sentidos
        assertTrue(tiene(g, "A", "C"));
    }

    /** Un enlace caído se va en los dos sentidos, aunque se lo pase con los extremos al revés. */
    public void testRuteoSinEnlaceCaido() {
        redDePrueba();
        Enlace alReves = new Enlace("C", "A", TipoEnlace.COBRE, 1, 1, 100, 0);
        DirectedGraph<String, WeightedEdge> g = red.grafoRuteo(0, Set.of(), Set.of(alReves));
        assertFalse(tiene(g, "A", "C"));
        assertFalse(tiene(g, "C", "A"));
        assertEquals(4, g.cantidadDeAristas());
        assertEquals(4, g.cantidadDeVertices());
    }

    public void testRuteoIdsCaidosQueNoExistenSeIgnoran() {
        redDePrueba();
        DirectedGraph<String, WeightedEdge> g = red.grafoRuteo(0, Set.of("Z"), Set.of());
        assertEquals(4, g.cantidadDeVertices());
        assertEquals(6, g.cantidadDeAristas());
    }

    /** Simular fallas no toca la red: después el grafo completo sigue igual. */
    public void testLasFallasNoModificanLaRed() {
        redDePrueba();
        red.grafoRuteo(0, Set.of("A", "B"), Set.of(red.enlace("B", "C")));
        red.grafoResiliencia(Set.of("C"), Set.of(red.enlace("A", "B")));
        assertEquals(4, red.nodos().size());
        assertEquals(3, red.enlaces().size());
        assertEquals(6, red.grafoRuteo().cantidadDeAristas());
        assertEquals(3, red.grafoResiliencia(Set.of(), Set.of()).cantidadDeAristas());
    }

    /** Cada llamada arma un grafo nuevo: cambiar uno no cambia el siguiente. */
    public void testCadaLlamadaDevuelveUnGrafoNuevo() {
        redDePrueba();
        DirectedGraph<String, WeightedEdge> primero = red.grafoRuteo();
        primero.removerVertice(primero.construirComparable("A"));
        assertNotSame(primero, red.grafoRuteo());
        assertEquals(4, red.grafoRuteo().cantidadDeVertices());
    }

    public void testFallasNulas() {
        redDePrueba();
        try {
            red.grafoRuteo(0, null, Set.of());
            fail("Debía lanzar NullPointerException");
        } catch (NullPointerException esperada) {
            // ok
        }
        try {
            red.grafoResiliencia(Set.of(), null);
            fail("Debía lanzar NullPointerException");
        } catch (NullPointerException esperada) {
            // ok
        }
    }

    public void testRedVacia() {
        assertEquals(0, red.grafoRuteo().cantidadDeVertices());
        assertEquals(0, red.grafoCableado().cantidadDeVertices());
        assertEquals(0, red.grafoResiliencia(Set.of(), Set.of()).cantidadDeVertices());
    }

    // ---- cableado ----

    public void testCableadoNoDirigidoConPesoCosto() {
        redDePrueba();
        UndirectedGraph<String, WeightedEdge> g = red.grafoCableado();
        assertEquals(Set.of("A", "B", "C", "D"), g.vertices());
        assertEquals(3, g.cantidadDeAristas());
        assertTrue(tiene(g, "B", "A"));
        assertEquals(10.0, g.obtenerArista("A", "B").dato().getWeight(), DELTA);
        assertEquals(4.0, g.obtenerArista("C", "B").dato().getWeight(), DELTA);
        assertEquals(30.0, g.obtenerArista("A", "C").dato().getWeight(), DELTA);
    }

    /** Con Kruskal de la biblioteca: se dejan A-B (10) y B-C (4); A-C (30) cerraría un ciclo. */
    public void testCableadoConKruskal() {
        redDePrueba();
        UndirectedGraph<String, WeightedEdge> g = red.grafoCableado();
        IUndirectedGraph<String, WeightedEdge> arbol = new UndirectedGraphAlgorithms().kruskal(g);
        assertTrue(arbol.existeArista(arbol.construirComparable("A"), arbol.construirComparable("B")));
        assertTrue(arbol.existeArista(arbol.construirComparable("B"), arbol.construirComparable("C")));
        assertFalse(arbol.existeArista(arbol.construirComparable("A"), arbol.construirComparable("C")));
    }

    // ---- resiliencia ----

    public void testResilienciaSinPeso() {
        redDePrueba();
        UndirectedGraph<String, WeightedEdge> g = red.grafoResiliencia(Set.of(), Set.of());
        assertEquals(Set.of("A", "B", "C", "D"), g.vertices());
        assertEquals(3, g.cantidadDeAristas());
        for (Edge<String, WeightedEdge> e : g.aristas()) {
            assertEquals(e.toString(), 1.0, e.dato().getWeight(), DELTA);
        }
    }

    /** Cadena A-B-C: si cae B, A y C quedan aislados. */
    public void testResilienciaSinNodoCaido() {
        routers("A", "B", "C");
        enlace("A", "B", 1, 1, 1000, 1);
        enlace("B", "C", 1, 1, 1000, 1);
        assertTrue(red.grafoResiliencia(Set.of(), Set.of()).esConexo());

        UndirectedGraph<String, WeightedEdge> g = red.grafoResiliencia(Set.of("B"), Set.of());
        assertEquals(Set.of("A", "C"), g.vertices());
        assertEquals(0, g.cantidadDeAristas());
        assertFalse(g.esConexo());
    }

    public void testResilienciaSinEnlaceCaido() {
        redDePrueba();
        UndirectedGraph<String, WeightedEdge> g = red.grafoResiliencia(Set.of(), Set.of(red.enlace("A", "B")));
        assertFalse(tiene(g, "A", "B"));
        assertTrue(tiene(g, "B", "C"));
        assertTrue(tiene(g, "A", "C"));
        assertEquals(4, g.cantidadDeVertices());
    }

    /** Con la biblioteca: en la cadena A-B-C el único punto de articulación es B. */
    public void testResilienciaConPuntosDeArticulacion() {
        routers("A", "B", "C");
        enlace("A", "B", 1, 1, 1000, 1);
        enlace("B", "C", 1, 1, 1000, 1);
        UndirectedGraph<String, WeightedEdge> g = red.grafoResiliencia(Set.of(), Set.of());
        assertEquals(Set.of("B"), new UndirectedGraphAlgorithms().puntosDeArticulacion(g));
    }
}
