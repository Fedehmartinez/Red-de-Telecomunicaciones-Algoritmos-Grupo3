package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.utils.GrafoLoader;

import java.nio.file.Path;

public class DirectedGraphEsConexoTest extends TestCase {

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

    /** A -> B <- C: unidos si se ignora el sentido, pero de A no hay camino a C. */
    public void testSeRespetaElSentidoDeLosArcos() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("C", "B");
        assertFalse(grafo.esConexo());
    }

    /** A -> B -> C: desde A se llega a todos, pero ninguno vuelve a A. */
    public void testCadenaNoEsConexo() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        assertFalse(grafo.esConexo());
    }

    /** Todos llegan a A pero A no llega a nadie. */
    public void testTodosLleganPeroNoSaleNoEsConexo() {
        vertices("A", "B", "C");
        arista("B", "A");
        arista("C", "A");
        assertFalse(grafo.esConexo());
    }

    public void testCicloEsConexo() {
        vertices("A", "B", "C");
        arista("A", "B");
        arista("B", "C");
        arista("C", "A");
        assertTrue(grafo.esConexo());
    }

    /** Red del Ejercicio 3: Warshall da todo 1, así que hay camino entre cualquier par. */
    public void testRedDelEjercicio3EsConexo() throws Exception {
        assertTrue(GrafoLoader.cargar(Path.of("src/main/resources/ut04/aeropuertos.txt"),
                Path.of("src/main/resources/ut04/conexiones.txt")).esConexo());
    }

    /** Archivos _2 del Ejercicio 6: a Montevideo se llega pero de Montevideo no se sale. */
    public void testRedDelEjercicio6NoEsConexo() throws Exception {
        assertFalse(GrafoLoader.cargar(Path.of("src/main/resources/ut04/aeropuertos_2.txt"),
                Path.of("src/main/resources/ut04/conexiones_2.txt")).esConexo());
    }
}
