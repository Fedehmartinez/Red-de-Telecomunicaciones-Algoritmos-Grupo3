package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.Ejercicio5.MainEjercicio5;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ejercicio5Test extends TestCase {

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

    private List<Path<String>> caminos(DirectedGraph<String, WeightedEdge> g, String origen, String destino) {
        return algoritmos.obtenerTodosLosCaminos(g.construirComparable(origen), g.construirComparable(destino), g);
    }

    private Path<String> buscar(List<Path<String>> caminos, List<String> camino) {
        for (Path<String> p : caminos) {
            if (p.getPath().equals(camino)) return p;
        }
        fail("No está el camino " + camino);
        return null;
    }

    /** Montevideo -> Porto Alegre en el Cuadro 2: los 18 caminos de la parte 1, en el mismo orden. */
    public void testMontevideoPortoAlegreComoEnLaParte1() {
        List<Path<String>> r = caminos(MainEjercicio5.grafoCuadro2(), "Montevideo", "Porto_Alegre");

        double[] costosEsperados = {6880, 7320, 7200, 7200, 2500, 1450, 1000, 5030, 7580,
                2880, 6180, 1960, 5880, 2400, 2400, 5470, 3320, 2400};
        assertEquals(18, r.size());
        for (int i = 0; i < costosEsperados.length; i++) {
            assertEquals("camino " + (i + 1), costosEsperados[i], r.get(i).getCost());
        }
        assertEquals(List.of("Montevideo", "Porto_Alegre"), r.get(6).getPath());
        assertEquals(List.of("Montevideo", "San_Pablo", "Asuncion", "Curitiba", "Rio_de_Janeiro", "Santos", "Porto_Alegre"),
                r.get(8).getPath());
    }

    public void testNingunCaminoRepiteVertices() {
        for (Path<String> p : caminos(MainEjercicio5.grafoCuadro2(), "Montevideo", "Porto_Alegre")) {
            assertEquals(p.getPath().toString(), p.getPath().size(), new HashSet<>(p.getPath()).size());
        }
    }

    /** A llega a D por A->B->D y A->C->D; el ciclo D->A no genera caminos infinitos. */
    public void testDosCaminosConCiclo() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("A", "C", 5);
        arista("B", "D", 2);
        arista("C", "D", 1);
        arista("D", "A", 4);
        List<Path<String>> r = caminos(grafo, "A", "D");

        assertEquals(2, r.size());
        assertEquals(3.0, buscar(r, List.of("A", "B", "D")).getCost());
        assertEquals(6.0, buscar(r, List.of("A", "C", "D")).getCost());
    }

    /** Un vértice ya usado por otro camino (B) tiene que poder usarse de nuevo: se desmarca al volver. */
    public void testVerticeCompartidoPorVariosCaminos() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("A", "C", 1);
        arista("C", "B", 1);
        arista("B", "D", 1);
        List<Path<String>> r = caminos(grafo, "A", "D");

        Set<List<String>> obtenidos = new HashSet<>();
        for (Path<String> p : r) obtenidos.add(p.getPath());
        assertEquals(Set.of(List.of("A", "B", "D"), List.of("A", "C", "B", "D")), obtenidos);
    }

    public void testDestinoInalcanzableDevuelveListaVacia() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("C", "A", 1);
        assertTrue(caminos(grafo, "A", "C").isEmpty());
    }

    public void testOrigenIgualADestino() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 1);
        List<Path<String>> r = caminos(grafo, "A", "A");

        assertEquals(1, r.size());
        assertEquals(List.of("A"), r.get(0).getPath());
        assertEquals(0.0, r.get(0).getCost());
    }

    public void testVerticeInexistente() {
        vertices("A");
        try {
            caminos(grafo, "A", "Z");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }
}
