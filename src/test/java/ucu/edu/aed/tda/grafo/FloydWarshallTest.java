package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.DijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.List;

public class FloydWarshallTest extends TestCase {

    private static final double DELTA = 0.0001;

    private DirectedGraph<String, WeightedEdge> grafo;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    @Override
    protected void setUp() {
        grafo = new DirectedGraph<>();
    }

    private void vertices(String... nombres) {
        for (String n : nombres) grafo.agregarVertice(n);
    }

    private void arista(String origen, String destino, double peso) {
        grafo.agregarArista(origen, destino, new WeightedEdge(peso));
    }

    /** Grafo de la figura 1 de la parte 2 del Ejercicio 7. */
    private void grafoParte2() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "C", 1);
        arista("A", "D", 4);
        arista("B", "A", 6);
        arista("B", "E", 3);
        arista("C", "B", 2);
        arista("C", "E", 1);
        arista("D", "C", 5);
        arista("E", "A", 3);
    }

    /** Suma los pesos de los arcos del camino. */
    private double costoDelCamino(List<String> camino) {
        double suma = 0;
        for (int i = 0; i + 1 < camino.size(); i++) {
            suma += grafo.obtenerArista(camino.get(i), camino.get(i + 1)).dato().getWeight();
        }
        return suma;
    }

    // ---------- Floyd ----------

    public void testFloydCostos() {
        grafoParte2();
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        double[][] esperado = {
                {0, 3, 1, 4, 2},
                {6, 0, 7, 10, 3},
                {4, 2, 0, 8, 1},
                {9, 7, 5, 0, 6},
                {3, 6, 4, 7, 0}};
        List<String> v = List.of("A", "B", "C", "D", "E");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(v.get(i) + " -> " + v.get(j), esperado[i][j], r.getCost(v.get(i), v.get(j)), DELTA);
            }
        }
    }

    public void testFloydCaminos() {
        grafoParte2();
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertEquals(List.of("A", "C", "E"), r.getPath("A", "E"));
        assertEquals(List.of("A", "C", "B"), r.getPath("A", "B"));
        assertEquals(List.of("D", "C", "E", "A"), r.getPath("D", "A"));
        assertEquals(List.of("A", "D"), r.getPath("A", "D"));
    }

    public void testFloydCaminoAlPropioVertice() {
        grafoParte2();
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertEquals(List.of("A"), r.getPath("A", "A"));
        assertEquals(0.0, r.getCost("A", "A"), DELTA);
        assertTrue(r.connected("A", "A"));
    }

    public void testFloydVerticesNoConectados() {
        vertices("X", "Y", "Z");
        arista("X", "Y", 3);
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);

        assertTrue(r.connected("X", "Y"));
        assertFalse(r.connected("Y", "X"));
        assertFalse(r.connected("X", "Z"));
        assertTrue(r.getPath("Y", "X").isEmpty());
        assertTrue(r.getPath("X", "Z").isEmpty());
        assertEquals(Double.POSITIVE_INFINITY, r.getCost("X", "Z"), 0);
    }

    public void testFloydGrafoVacio() {
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertNotNull(r);
        assertTrue(((FloydWarshallResult<String>) r).getVertices().isEmpty());
    }

    public void testFloydUnVertice() {
        vertices("A");
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertEquals(0.0, r.getCost("A", "A"), DELTA);
        assertTrue(r.connected("A", "A"));
        assertEquals(List.of("A"), r.getPath("A", "A"));
    }

    /** El arco A -> A (peso 5) no puede hacer que la distancia de A a sí mismo sea distinta de 0. */
    public void testFloydIgnoraLazos() {
        vertices("A", "B");
        arista("A", "A", 5);
        arista("A", "B", 2);
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertEquals(0.0, r.getCost("A", "A"), DELTA);
        assertEquals(2.0, r.getCost("A", "B"), DELTA);
    }

    /** A -> C directo cuesta 10; por B cuesta 2. */
    public void testFloydPrefiereElCaminoMasBarato() {
        vertices("A", "B", "C");
        arista("A", "C", 10);
        arista("A", "B", 1);
        arista("B", "C", 1);
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertEquals(2.0, r.getCost("A", "C"), DELTA);
        assertEquals(List.of("A", "B", "C"), r.getPath("A", "C"));
    }

    /** Cadena larga: el camino de A a E pasa por todos los intermedios, en orden. */
    public void testFloydCaminoConVariosIntermedios() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("C", "D", 1);
        arista("D", "E", 1);
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        assertEquals(List.of("A", "B", "C", "D", "E"), r.getPath("A", "E"));
        assertEquals(4.0, r.getCost("A", "E"), DELTA);
    }

    public void testFloydCostoDelCaminoEsLaSumaDeSusArcos() {
        grafoParte2();
        IFloydWarshallResult<String> r = algoritmos.floyd(grafo);
        for (String o : grafo.vertices()) {
            for (String d : grafo.vertices()) {
                List<String> camino = r.getPath(o, d);
                assertEquals(o + " -> " + d + " " + camino, r.getCost(o, d), costoDelCamino(camino), DELTA);
                assertEquals(o, camino.get(0));
                assertEquals(d, camino.get(camino.size() - 1));
            }
        }
    }

    /** 7 vértices con arcos de distintos pesos, ciclos y un vértice (G) al que no llega nadie. */
    public void testFloydCoincideConDijkstraDesdeCadaVertice() {
        vertices("A", "B", "C", "D", "E", "F", "G");
        arista("A", "B", 4);
        arista("A", "C", 1);
        arista("C", "B", 2);
        arista("B", "D", 5);
        arista("C", "D", 8);
        arista("D", "E", 3);
        arista("E", "A", 7);
        arista("E", "F", 1);
        arista("F", "D", 2);
        arista("D", "F", 6);
        arista("G", "A", 1);
        arista("G", "F", 9);

        IFloydWarshallResult<String> floyd = algoritmos.floyd(grafo);
        for (String origen : grafo.vertices()) {
            DijkstraResult<String> dijkstra = (DijkstraResult<String>) algoritmos.dijkstra(grafo.construirComparable(origen), grafo);
            for (String destino : grafo.vertices()) {
                String par = origen + " -> " + destino;
                assertEquals(par, dijkstra.getCost(destino), floyd.getCost(origen, destino), DELTA);
                assertEquals(par, dijkstra.getCost(destino) != Double.POSITIVE_INFINITY, floyd.connected(origen, destino));
                assertEquals(par, dijkstra.getPath(destino).isEmpty(), floyd.getPath(origen, destino).isEmpty());
            }
        }
    }

    // ---------- Warshall ----------

    public void testWarshallCadena() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("C", "D", 1);
        IFloydWarshallResult<String> r = algoritmos.warshall(grafo);

        assertTrue(r.connected("A", "B"));
        assertTrue(r.connected("A", "C"));
        assertTrue(r.connected("A", "D"));
        assertTrue(r.connected("B", "D"));
        assertFalse(r.connected("D", "A"));
        assertFalse(r.connected("C", "B"));
        assertEquals(List.of("A", "B", "C", "D"), r.getPath("A", "D"));
        assertTrue(r.getPath("D", "A").isEmpty());
    }

    public void testWarshallCiclo() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("C", "A", 1);
        IFloydWarshallResult<String> r = algoritmos.warshall(grafo);

        for (String o : grafo.vertices()) {
            for (String d : grafo.vertices()) {
                assertTrue(o + " -> " + d, r.connected(o, d));
            }
        }
        assertEquals(List.of("B", "C", "A"), r.getPath("B", "A"));
    }

    public void testWarshallNoConexo() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("C", "D", 1);
        IFloydWarshallResult<String> r = algoritmos.warshall(grafo);

        assertTrue(r.connected("A", "B"));
        assertTrue(r.connected("C", "D"));
        assertFalse(r.connected("A", "C"));
        assertFalse(r.connected("B", "A"));
        assertFalse(r.connected("D", "B"));
        assertTrue(r.getPath("A", "D").isEmpty());
    }

    public void testWarshallVerticeAislado() {
        vertices("A", "B");
        IFloydWarshallResult<String> r = algoritmos.warshall(grafo);
        assertFalse(r.connected("A", "B"));
        assertFalse(r.connected("A", "A"));
    }

    public void testWarshallNoCalculaCostos() {
        vertices("A", "B");
        arista("A", "B", 1);
        IFloydWarshallResult<String> r = algoritmos.warshall(grafo);
        try {
            r.getCost("A", "B");
            fail("debía lanzar excepción");
        } catch (UnsupportedOperationException esperado) {
            // ok
        }
    }

    /** El camino de Warshall puede no ser el más barato, pero tiene que ser un camino válido. */
    public void testWarshallDevuelveUnCaminoValido() {
        grafoParte2();
        IFloydWarshallResult<String> r = algoritmos.warshall(grafo);
        for (String o : grafo.vertices()) {
            for (String d : grafo.vertices()) {
                if (o.equals(d) || !r.connected(o, d)) continue;
                List<String> camino = r.getPath(o, d);
                assertEquals(o, camino.get(0));
                assertEquals(d, camino.get(camino.size() - 1));
                for (int i = 0; i + 1 < camino.size(); i++) {
                    assertTrue(o + " -> " + d + " " + camino,
                            grafo.existeArista(grafo.construirComparable(camino.get(i)), grafo.construirComparable(camino.get(i + 1))));
                }
            }
        }
    }

    /** Se compara entre vértices distintos: Warshall marca la diagonal solo si hay un ciclo, Floyd siempre. */
    public void testWarshallConectadoCoincideConFloyd() {
        vertices("A", "B", "C", "D", "E", "F");
        arista("A", "B", 2);
        arista("B", "C", 1);
        arista("C", "A", 4);
        arista("C", "D", 3);
        arista("E", "D", 1);
        arista("F", "E", 6);

        IFloydWarshallResult<String> floyd = algoritmos.floyd(grafo);
        IFloydWarshallResult<String> warshall = algoritmos.warshall(grafo);
        for (String o : grafo.vertices()) {
            for (String d : grafo.vertices()) {
                if (o.equals(d)) continue;
                assertEquals(o + " -> " + d, floyd.connected(o, d), warshall.connected(o, d));
            }
        }
    }

    public void testWarshallGrafoVacio() {
        assertNotNull(algoritmos.warshall(grafo));
    }
}
