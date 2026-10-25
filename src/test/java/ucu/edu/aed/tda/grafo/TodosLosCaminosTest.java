package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TodosLosCaminosTest extends TestCase {

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

    private List<Path<String>> caminos(String origen, String destino) {
        return algoritmos.obtenerTodosLosCaminos(grafo.construirComparable(origen), grafo.construirComparable(destino), grafo);
    }

    private Set<List<String>> soloLosCaminos(List<Path<String>> caminos) {
        Set<List<String>> res = new HashSet<>();
        for (Path<String> p : caminos) res.add(p.getPath());
        return res;
    }

    private Path<String> buscar(List<Path<String>> caminos, List<String> camino) {
        for (Path<String> p : caminos) {
            if (p.getPath().equals(camino)) return p;
        }
        fail("No está el camino " + camino);
        return null;
    }

    /** Red del Cuadro 2 (Ejercicio 3 / Ejercicio 5). */
    private void grafoCuadro2() {
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
    }

    /** Montevideo -> Porto Alegre en el Cuadro 2: los 18 caminos de la parte 1, en el mismo orden. */
    public void testMontevideoPortoAlegreEnElCuadro2() {
        grafoCuadro2();
        List<Path<String>> r = caminos("Montevideo", "Porto_Alegre");

        double[] costosEsperados = {6880, 7320, 7200, 7200, 2500, 1450, 1000, 5030, 7580,
                2880, 6180, 1960, 5880, 2400, 2400, 5470, 3320, 2400};
        assertEquals(18, r.size());
        for (int i = 0; i < costosEsperados.length; i++) {
            assertEquals("camino " + (i + 1), costosEsperados[i], r.get(i).getCost(), DELTA);
        }
        assertEquals(List.of("Montevideo", "Porto_Alegre"), r.get(6).getPath());
        assertEquals(List.of("Montevideo", "San_Pablo", "Asuncion", "Curitiba", "Rio_de_Janeiro", "Santos", "Porto_Alegre"),
                r.get(8).getPath());
    }

    /** A llega a D por A->B->D y A->C->D; el ciclo D->A no genera caminos infinitos. */
    public void testDosCaminosConCiclo() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("A", "C", 5);
        arista("B", "D", 2);
        arista("C", "D", 1);
        arista("D", "A", 4);
        List<Path<String>> r = caminos("A", "D");

        assertEquals(2, r.size());
        assertEquals(3.0, buscar(r, List.of("A", "B", "D")).getCost(), DELTA);
        assertEquals(6.0, buscar(r, List.of("A", "C", "D")).getCost(), DELTA);
    }

    /** Un ciclo B -> C -> B en el medio: se pasa por él una sola vez, sin dar vueltas. */
    public void testCicloEnElMedioNoGeneraCaminosInfinitos() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("B", "C", 1);
        arista("C", "B", 1);
        arista("C", "D", 1);
        List<Path<String>> r = caminos("A", "D");

        assertEquals(1, r.size());
        assertEquals(List.of("A", "B", "C", "D"), r.get(0).getPath());
    }

    /** Un vértice ya usado por otro camino (B) tiene que poder usarse de nuevo: se desmarca al volver. */
    public void testVerticeCompartidoPorVariosCaminos() {
        vertices("A", "B", "C", "D");
        arista("A", "B", 1);
        arista("A", "C", 1);
        arista("C", "B", 1);
        arista("B", "D", 1);
        List<Path<String>> r = caminos("A", "D");

        assertEquals(2, r.size());
        assertEquals(Set.of(List.of("A", "B", "D"), List.of("A", "C", "B", "D")), soloLosCaminos(r));
    }

    /** Dos caminos que pasan por C pero llegan a él por lugares distintos. */
    public void testDosCaminosPorUnVerticeEnComun() {
        vertices("A", "B", "C", "D", "E");
        arista("A", "B", 1);
        arista("A", "C", 1);
        arista("B", "C", 1);
        arista("C", "D", 1);
        arista("C", "E", 1);
        arista("D", "E", 1);
        List<Path<String>> r = caminos("A", "E");

        assertEquals(Set.of(
                List.of("A", "C", "E"),
                List.of("A", "C", "D", "E"),
                List.of("A", "B", "C", "E"),
                List.of("A", "B", "C", "D", "E")), soloLosCaminos(r));
        assertEquals(4, r.size());
    }

    public void testNingunCaminoRepiteVertices() {
        grafoCuadro2();
        for (Path<String> p : caminos("Montevideo", "Porto_Alegre")) {
            assertEquals(p.getPath().toString(), p.getPath().size(), new HashSet<>(p.getPath()).size());
        }
    }

    public void testTodosLosCaminosEmpiezanEnElOrigenYTerminanEnElDestino() {
        grafoCuadro2();
        for (Path<String> p : caminos("Asuncion", "Santos")) {
            assertEquals("Asuncion", p.getPath().get(0));
            assertEquals("Santos", p.getPath().get(p.getPath().size() - 1));
        }
    }

    public void testDestinoInalcanzableDevuelveListaVacia() {
        vertices("A", "B", "C");
        arista("A", "B", 1);
        arista("C", "A", 1);
        assertTrue(caminos("A", "C").isEmpty());
    }

    public void testRespetaElSentidoDeLosArcos() {
        vertices("A", "B");
        arista("B", "A", 1);
        assertTrue(caminos("A", "B").isEmpty());
        assertEquals(1, caminos("B", "A").size());
    }

    public void testOrigenIgualADestino() {
        vertices("A", "B");
        arista("A", "B", 1);
        arista("B", "A", 1);
        List<Path<String>> r = caminos("A", "A");

        assertEquals(1, r.size());
        assertEquals(List.of("A"), r.get(0).getPath());
        assertEquals(0.0, r.get(0).getCost(), DELTA);
    }

    public void testUnSoloCaminoDirecto() {
        vertices("A", "B");
        arista("A", "B", 7);
        List<Path<String>> r = caminos("A", "B");

        assertEquals(1, r.size());
        assertEquals(List.of("A", "B"), r.get(0).getPath());
        assertEquals(7.0, r.get(0).getCost(), DELTA);
    }

    public void testDestinoInexistente() {
        vertices("A");
        try {
            caminos("A", "Z");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testOrigenInexistente() {
        vertices("A");
        try {
            caminos("Z", "A");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    public void testElCostoDeCadaPathEsLaSumaDeSusArcos() {
        grafoCuadro2();
        for (Path<String> p : caminos("Montevideo", "Porto_Alegre")) {
            double suma = 0;
            List<String> camino = p.getPath();
            for (int i = 0; i + 1 < camino.size(); i++) {
                suma += grafo.obtenerArista(camino.get(i), camino.get(i + 1)).dato().getWeight();
            }
            assertEquals(camino.toString(), suma, p.getCost(), DELTA);
        }
    }
}
