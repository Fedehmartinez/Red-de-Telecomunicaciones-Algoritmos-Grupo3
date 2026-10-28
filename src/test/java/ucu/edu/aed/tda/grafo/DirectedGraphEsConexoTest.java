package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

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
    public void testRedDelEjercicio3EsConexo() {
        vertices("Asuncion", "Buenos_Aires", "Curitiba", "Montevideo", "Porto_Alegre", "Rio_de_Janeiro", "San_Pablo", "Santos");
        arista("Asuncion", "Buenos_Aires");
        arista("Asuncion", "Curitiba");
        arista("Asuncion", "Porto_Alegre");
        arista("Buenos_Aires", "Asuncion");
        arista("Buenos_Aires", "Montevideo");
        arista("Buenos_Aires", "Porto_Alegre");
        arista("Curitiba", "Rio_de_Janeiro");
        arista("Montevideo", "Buenos_Aires");
        arista("Montevideo", "Porto_Alegre");
        arista("Montevideo", "San_Pablo");
        arista("Porto_Alegre", "Montevideo");
        arista("Porto_Alegre", "San_Pablo");
        arista("Porto_Alegre", "Santos");
        arista("Rio_de_Janeiro", "Curitiba");
        arista("Rio_de_Janeiro", "San_Pablo");
        arista("Rio_de_Janeiro", "Santos");
        arista("San_Pablo", "Asuncion");
        arista("San_Pablo", "Curitiba");
        arista("San_Pablo", "Montevideo");
        arista("San_Pablo", "Porto_Alegre");
        arista("San_Pablo", "Rio_de_Janeiro");
        arista("San_Pablo", "Santos");
        arista("Santos", "Porto_Alegre");
        arista("Santos", "San_Pablo");
        assertTrue(grafo.esConexo());
    }

    /** Red del Ejercicio 6: a Montevideo se llega pero de Montevideo no se sale. */
    public void testRedDelEjercicio6NoEsConexo() {
        vertices("Asuncion", "Buenos_Aires", "Curitiba", "Florianopolis", "Montevideo", "Porto_Alegre", "Rio_de_Janeiro", "San_Pablo", "Santos");
        arista("Asuncion", "Buenos_Aires");
        arista("Asuncion", "Curitiba");
        arista("Asuncion", "Porto_Alegre");
        arista("Buenos_Aires", "Montevideo");
        arista("Buenos_Aires", "Porto_Alegre");
        arista("Curitiba", "Montevideo");
        arista("Florianopolis", "Rio_de_Janeiro");
        arista("Florianopolis", "San_Pablo");
        arista("Porto_Alegre", "Montevideo");
        arista("Rio_de_Janeiro", "San_Pablo");
        arista("Rio_de_Janeiro", "Santos");
        arista("San_Pablo", "Florianopolis");
        arista("San_Pablo", "Rio_de_Janeiro");
        arista("San_Pablo", "Santos");
        arista("Santos", "San_Pablo");
        assertFalse(grafo.esConexo());
    }
}
