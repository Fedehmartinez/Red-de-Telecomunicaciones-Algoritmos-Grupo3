package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PuntosDeArticulacionTest extends TestCase {

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

    private Set<String> puntos() {
        return algoritmos.puntosDeArticulacion(grafo);
    }

    /**
     * Ejemplo del teórico: árbol A-B-D-E y A-C-F-G, con retrocesos D-A, E-B, E-A y G-C.
     * A es raíz con dos hijos; C tiene a F con bajo[F] = 5 >= numBp[C] = 5.
     */
    public void testEjemploDelTeorico() {
        vertices("A", "B", "C", "D", "E", "F", "G");
        arista("A", "B");
        arista("B", "D");
        arista("D", "E");
        arista("A", "C");
        arista("C", "F");
        arista("F", "G");
        arista("A", "D");
        arista("B", "E");
        arista("A", "E");
        arista("C", "G");
        assertEquals(Set.of("A", "C"), puntos());
    }

    /** Grafo de los ejercicios del teórico: solo D separa el triángulo A-B-D del resto. */
    public void testGrafoDelEjercicio() {
        vertices("A", "B", "C", "D", "E", "F", "G");
        arista("A", "B");
        arista("A", "D");
        arista("B", "D");
        arista("D", "E");
        arista("D", "F");
        arista("E", "G");
        arista("E", "F");
        arista("F", "G");
        arista("G", "C");
        arista("F", "C");
        assertEquals(Set.of("D"), puntos());
    }

    /** A es la raíz y tiene un solo hijo: no es punto de articulación. B y C sí. */
    public void testCadena() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("B", "C");
        arista("C", "D");
        assertEquals(Set.of("B", "C"), puntos());
    }

    /** B es la raíz (se inserta primero) y tiene dos hijos. */
    public void testRaizConDosHijos() {
        vertices("B", "A", "C");
        arista("A", "B");
        arista("B", "C");
        assertEquals(Set.of("B"), puntos());
    }

    public void testCicloNoTienePuntos() {
        vertices("A", "B", "C", "D");
        arista("A", "B");
        arista("B", "C");
        arista("C", "D");
        arista("D", "A");
        assertTrue(puntos().isEmpty());
    }

    public void testEstrella() {
        vertices("H", "A", "B", "C");
        arista("H", "A");
        arista("H", "B");
        arista("H", "C");
        assertEquals(Set.of("H"), puntos());
    }

    /** Dos triángulos que comparten el vértice C. */
    public void testDosTriangulosUnidosPorUnVertice() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        arista("C", "D");
        arista("D", "E");
        arista("E", "C");
        assertEquals(Set.of("C"), puntos());
    }

    public void testNoConexo() {
        vertices("A", "B", "C", "X", "Y", "Z");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        arista("X", "Y");
        arista("Y", "Z");
        assertEquals(Set.of("Y"), puntos());
    }

    public void testGrafoVacio() {
        assertTrue(puntos().isEmpty());
    }

    public void testUnSoloVertice() {
        vertices("A");
        assertTrue(puntos().isEmpty());
    }

    public void testUnaSolaArista() {
        vertices("A", "B");
        arista("A", "B");
        assertTrue(puntos().isEmpty());
    }

    /**
     * Compara contra la definición: v es punto de articulación si al sacarlo aumenta
     * la cantidad de componentes conexas. Grafos al azar, con semilla fija.
     */
    public void testCoincideConLaDefinicion() {
        Random random = new Random(42);
        for (int prueba = 0; prueba < 50; prueba++) {
            UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
            int n = 2 + random.nextInt(9);
            for (int i = 0; i < n; i++) g.agregarVertice("v" + i);
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (random.nextDouble() < 0.3) g.agregarArista("v" + i, "v" + j, new WeightedEdge(1));
                }
            }

            Set<String> esperados = new HashSet<>();
            int componentes = componentes(g);
            for (String v : g.vertices()) {
                if (componentes(sinVertice(g, v)) > componentes) esperados.add(v);
            }
            assertEquals("prueba " + prueba + ": " + g.aristas(), esperados, algoritmos.puntosDeArticulacion(g));
        }
    }

    /** Copia del grafo sin el vértice v (ni sus aristas). */
    private static UndirectedGraph<String, WeightedEdge> sinVertice(UndirectedGraph<String, WeightedEdge> g, String v) {
        UndirectedGraph<String, WeightedEdge> copia = new UndirectedGraph<>();
        for (String u : g.vertices()) {
            if (!u.equals(v)) copia.agregarVertice(u);
        }
        for (Edge<String, WeightedEdge> e : g.aristas()) {
            copia.agregarArista(e.source(), e.target(), e.dato());   // si toca a v no se agrega
        }
        return copia;
    }

    private int componentes(UndirectedGraph<String, WeightedEdge> g) {
        Set<String> visitados = new HashSet<>();
        int cantidad = 0;
        for (String v : g.vertices()) {
            if (visitados.contains(v)) continue;
            cantidad++;
            Set<String> alcanzados = new HashSet<>();
            alcanzados.add(v);
            boolean crecio = true;
            while (crecio) {
                crecio = false;
                for (String u : new HashSet<>(alcanzados)) {
                    for (Edge<String, WeightedEdge> e : g.adyacencias(g.construirComparable(u))) {
                        if (alcanzados.add(e.target())) crecio = true;
                    }
                }
            }
            visitados.addAll(alcanzados);
        }
        return cantidad;
    }
}
