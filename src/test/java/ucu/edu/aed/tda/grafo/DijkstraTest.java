package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.DijkstraResult;

import java.util.List;

public class DijkstraTest extends TestCase {

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

    private DijkstraResult<String> desde(String origen) {
        return (DijkstraResult<String>) algoritmos.dijkstra(grafo.construirComparable(origen), grafo);
    }

    /** Grafo de la parte 1 del Ejercicio 7. */
    private void grafoParte1() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B", 1);
        arista("A", "C", 2);
        arista("A", "D", 7);
        arista("B", "A", 7);
        arista("B", "C", 1);
        arista("B", "D", 2);
        arista("C", "D", 3);
        arista("D", "A", 6);
        arista("D", "C", 4);
        arista("D", "E", 4);
        arista("E", "B", 2);
        arista("E", "D", 8);
    }

    public void testCostosDesdeB() {
        grafoParte1();
        DijkstraResult<String> r = desde("B");
        assertEquals(0.0, r.getCost("B"), DELTA);
        assertEquals(7.0, r.getCost("A"), DELTA);
        assertEquals(1.0, r.getCost("C"), DELTA);
        assertEquals(2.0, r.getCost("D"), DELTA);
        assertEquals(6.0, r.getCost("E"), DELTA);
    }

    public void testCaminosDesdeB() {
        grafoParte1();
        DijkstraResult<String> r = desde("B");
        assertEquals(List.of("B", "D", "E"), r.getPath("E"));
        assertEquals(List.of("B", "C"), r.getPath("C"));
        assertEquals(List.of("B", "A"), r.getPath("A"));   // el arco directo (7) gana a B -> D -> A (8)
    }

    public void testPredecesoresDesdeB() {
        grafoParte1();
        DijkstraResult<String> r = desde("B");
        assertEquals("D", r.getPredecesor("E"));
        assertEquals("B", r.getPredecesor("A"));
        assertEquals("B", r.getPredecesor("C"));
        assertEquals("B", r.getPredecesor("D"));
        assertNull(r.getPredecesor("B"));
    }

    public void testOrigenGuardadoEnElResultado() {
        grafoParte1();
        assertEquals("B", desde("B").getOrigen());
    }

    public void testDestinoInalcanzable() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        DijkstraResult<String> r = desde("A");
        assertEquals(Double.POSITIVE_INFINITY, r.getCost("C"), 0);
        assertTrue(r.getPath("C").isEmpty());
        assertNull(r.getPredecesor("C"));
    }

    /** B -> A existe pero desde A no hay camino a ningún otro vértice. */
    public void testNoSePuedeRecorrerUnArcoAlReves() {
        vertices("A", "B");
        arista("B", "A", 1);
        DijkstraResult<String> r = desde("A");
        assertEquals(Double.POSITIVE_INFINITY, r.getCost("B"), 0);
        assertTrue(r.getPath("B").isEmpty());
    }

    public void testOrigenInexistente() {
        grafoParte1();
        try {
            desde("Z");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testGrafoVacioOrigenInexistente() {
        try {
            desde("A");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testUnSoloVertice() {
        vertices("A");
        DijkstraResult<String> r = desde("A");
        assertEquals(0.0, r.getCost("A"), DELTA);
        assertEquals(List.of("A"), r.getPath("A"));
    }

    public void testCaminoAlPropioOrigen() {
        grafoParte1();
        DijkstraResult<String> r = desde("C");
        assertEquals(0.0, r.getCost("C"), DELTA);
        assertEquals(List.of("C"), r.getPath("C"));
    }

    /** Hay un ciclo que vuelve al origen: igual el costo al origen es 0. */
    public void testCicloNoMejoraElCostoDelOrigen() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 1);
        DijkstraResult<String> r = desde("A");
        assertEquals(0.0, r.getCost("A"), DELTA);
        assertEquals(List.of("A"), r.getPath("A"));
    }

    /** A -> D directo cuesta 10, pero A -> B -> C -> D cuesta 3. */
    public void testCaminoLargoMasBaratoQueArcoDirecto() {
        vertices("A", "B", "C", "D");
        arista("A", "D", 10);
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("C", "D", 1);
        DijkstraResult<String> r = desde("A");
        assertEquals(3.0, r.getCost("D"), DELTA);
        assertEquals(List.of("A", "B", "C", "D"), r.getPath("D"));
    }

    public void testArcoDirectoMasBaratoQueElRodeo() {
        vertices("A", "B", "C");
        arista("A", "C", 2);
        arista("A", "B", 1);
        arista("B", "C", 5);
        DijkstraResult<String> r = desde("A");
        assertEquals(2.0, r.getCost("C"), DELTA);
        assertEquals(List.of("A", "C"), r.getPath("C"));
    }

    /** A -> B -> D y A -> C -> D cuestan 4: el costo es 4 y el camino es cualquiera de los dos. */
    public void testDosCaminosDeIgualCosto() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("B", "D", 3);
        arista("A", "C", 2);
        arista("C", "D", 2);
        DijkstraResult<String> r = desde("A");
        assertEquals(4.0, r.getCost("D"), DELTA);
        List<String> camino = r.getPath("D");
        assertTrue(camino.toString(),
                camino.equals(List.of("A", "B", "D")) || camino.equals(List.of("A", "C", "D")));
    }

    /** A -> B cuesta 1 pero B -> A cuesta 9. */
    public void testCostosAsimetricos() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 9);
        assertEquals(1.0, desde("A").getCost("B"), DELTA);
        assertEquals(9.0, desde("B").getCost("A"), DELTA);
    }

    /** A -> B cuesta 1 pero para volver hay que dar la vuelta por C. */
    public void testCaminoDeVueltaDistintoAlDeIda() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("B", "C", 2);
        arista("C", "A", 2);
        assertEquals(List.of("A", "B"), desde("A").getPath("B"));
        assertEquals(List.of("B", "C", "A"), desde("B").getPath("A"));
        assertEquals(4.0, desde("B").getCost("A"), DELTA);
    }

    public void testArcosConPesoCero() {
        vertices("A", "B", "C");
        arista("A", "B", 0);
        arista("B", "C", 0);
        arista("A", "C", 5);
        DijkstraResult<String> r = desde("A");
        assertEquals(0.0, r.getCost("C"), DELTA);
        assertEquals(List.of("A", "B", "C"), r.getPath("C"));
    }

    /** El costo de un vértice que no está en el grafo es infinito. */
    public void testCostoDeVerticeQueNoEstaEnElGrafo() {
        vertices("A");
        assertEquals(Double.POSITIVE_INFINITY, desde("A").getCost("Z"), 0);
    }

    /** Cuadro 2 del Ejercicio 3: desde Montevideo, Rio de Janeiro conviene por San Pablo. */
    public void testRedDelCuadro2() {
        vertices("Asuncion", "Buenos_Aires", "Curitiba", "Montevideo", "Porto_Alegre", "Rio_de_Janeiro", "San_Pablo", "Santos");
        arista("Asuncion", "Buenos_Aires", 1600);
        arista("Asuncion", "Curitiba", 800);
        arista("Asuncion", "Porto_Alegre", 700);
        arista("Buenos_Aires", "Asuncion", 1600);
        arista("Buenos_Aires", "Montevideo", 200);
        arista("Buenos_Aires", "Porto_Alegre", 1250);
        arista("Curitiba", "Rio_de_Janeiro", 1500);
        arista("Montevideo", "Buenos_Aires", 200);
        arista("Montevideo", "Porto_Alegre", 1000);
        arista("Montevideo", "San_Pablo", 980);
        arista("Montevideo", "Santos", 1200);
        arista("Rio_de_Janeiro", "Curitiba", 1500);
        arista("Rio_de_Janeiro", "San_Pablo", 1800);
        arista("Rio_de_Janeiro", "Santos", 1900);
        arista("San_Pablo", "Asuncion", 1200);
        arista("San_Pablo", "Curitiba", 600);
        arista("San_Pablo", "Montevideo", 2000);
        arista("San_Pablo", "Porto_Alegre", 980);
        arista("San_Pablo", "Rio_de_Janeiro", 1800);
        arista("San_Pablo", "Santos", 220);
        arista("Santos", "Porto_Alegre", 1200);
        arista("Santos", "San_Pablo", 220);

        DijkstraResult<String> r = desde("Montevideo");
        assertEquals(1000.0, r.getCost("Porto_Alegre"), DELTA);
        assertEquals(List.of("Montevideo", "Porto_Alegre"), r.getPath("Porto_Alegre"));
        assertEquals(1580.0, r.getCost("Curitiba"), DELTA);
        assertEquals(List.of("Montevideo", "San_Pablo", "Curitiba"), r.getPath("Curitiba"));
        assertEquals(2780.0, r.getCost("Rio_de_Janeiro"), DELTA);
        assertEquals(1200.0, r.getCost("Santos"), DELTA);
        assertEquals(200.0, r.getCost("Buenos_Aires"), DELTA);
    }
}
