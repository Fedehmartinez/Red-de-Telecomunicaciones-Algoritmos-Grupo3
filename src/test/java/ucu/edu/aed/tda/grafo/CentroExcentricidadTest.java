package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

public class CentroExcentricidadTest extends TestCase {

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

    private double excentricidad(String v) {
        return algoritmos.obtenerExcentricidad(grafo, grafo.construirComparable(v));
    }

    /**
     * Grafo de las diapositivas (figura 1 de la parte 2 del Ejercicio 7). Matriz de Floyd:
     * A:[0,3,1,4,2] B:[6,0,7,10,3] C:[4,2,0,8,1] D:[9,7,5,0,6] E:[3,6,4,7,0].
     */
    private void grafoDeLasDiapositivas() {
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

    /** Máximo de cada columna de la matriz: A=9, B=7, C=7, D=10, E=6. */
    public void testExcentricidadesDeLasDiapositivas() {
        grafoDeLasDiapositivas();
        assertEquals(9.0, excentricidad("A"), DELTA);
        assertEquals(7.0, excentricidad("B"), DELTA);
        assertEquals(7.0, excentricidad("C"), DELTA);
        assertEquals(10.0, excentricidad("D"), DELTA);
        assertEquals(6.0, excentricidad("E"), DELTA);
    }

    public void testCentroDeLasDiapositivas() {
        grafoDeLasDiapositivas();
        assertEquals("E", algoritmos.obtenerCentroGrafo(grafo));
    }

    public void testExcentricidadDeVerticeInexistente() {
        grafoDeLasDiapositivas();
        try {
            excentricidad("Z");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testGrafoVacioNoTieneCentro() {
        assertNull(algoritmos.obtenerCentroGrafo(grafo));
    }

    public void testGrafoVacioExcentricidadLanzaExcepcion() {
        try {
            excentricidad("A");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testUnVerticeTieneExcentricidadCero() {
        vertices("A");
        assertEquals(0.0, excentricidad("A"), DELTA);
        assertEquals("A", algoritmos.obtenerCentroGrafo(grafo));
    }

    /** A llega a B, pero B no llega a A: nadie llega a A, así que su excentricidad es infinita. */
    public void testVerticeAlQueNoLlegaNadieTieneExcentricidadInfinita() {
        vertices("A", "B");
        arista("A", "B", 1);
        assertEquals(Double.POSITIVE_INFINITY, excentricidad("A"), 0);
        assertEquals(1.0, excentricidad("B"), DELTA);
    }

    public void testVerticeAisladoTieneExcentricidadInfinita() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "A", 1);
        assertEquals(Double.POSITIVE_INFINITY, excentricidad("C"), 0);
        assertEquals(Double.POSITIVE_INFINITY, excentricidad("A"), 0);
    }

    /** Todas las excentricidades son infinitas: el centro es el primer vértice. */
    public void testSiTodosSonInfinitosElCentroEsElPrimero() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "A", 1);
        assertEquals("A", algoritmos.obtenerCentroGrafo(grafo));
    }

    /**
     * A -> B pesa 1 y B -> A pesa 10. Por columna: ecc(A) = 10 (lo que cuesta llegar desde B) y ecc(B) = 1,
     * y el centro es B. Si se usara la fila saldría al revés: ecc(A) = 1, ecc(B) = 10 y el centro sería A.
     */
    public void testSeUsaLaColumnaYNoLaFila() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 10);
        assertEquals(10.0, excentricidad("A"), DELTA);
        assertEquals(1.0, excentricidad("B"), DELTA);
        assertEquals("B", algoritmos.obtenerCentroGrafo(grafo));
    }

    /**
     * A -> B -> C pesan 1 y C -> A pesa 10. Matriz de Floyd: A:[0,1,2] B:[11,0,1] C:[10,11,0].
     * Por columna: A=11, B=11, C=2 y el centro es C. Por fila serían A=2, B=11, C=11 y el centro sería A.
     */
    public void testSeUsaLaColumnaConTresVertices() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("C", "A", 10);
        assertEquals(11.0, excentricidad("A"), DELTA);
        assertEquals(11.0, excentricidad("B"), DELTA);
        assertEquals(2.0, excentricidad("C"), DELTA);
        assertEquals("C", algoritmos.obtenerCentroGrafo(grafo));
    }

    public void testGrafoSimetricoEnEstrella() {
        vertices("C", "X", "Y", "Z");
        for (String hoja : new String[]{"X", "Y", "Z"}) {
            arista("C", hoja, 2);
            arista(hoja, "C", 2);
        }
        assertEquals(2.0, excentricidad("C"), DELTA);
        assertEquals(4.0, excentricidad("X"), DELTA);
        assertEquals("C", algoritmos.obtenerCentroGrafo(grafo));
    }
}
