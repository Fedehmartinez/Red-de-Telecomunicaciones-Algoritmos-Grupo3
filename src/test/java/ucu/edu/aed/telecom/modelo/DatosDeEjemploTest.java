package ucu.edu.aed.telecom.modelo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.DijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Los CSV de src/main/resources/telecom: que cumplan lo que dicen sus comentarios. */
public class DatosDeEjemploTest extends TestCase {

    private static final double DELTA = 0.0001;

    private RedTelecom red;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    @Override
    protected void setUp() throws IOException {
        red = CargadorRed.cargar(java.nio.file.Path.of("src/main/resources/telecom/nodos.csv"),
                java.nio.file.Path.of("src/main/resources/telecom/enlaces.csv"));
    }

    private DijkstraResult<String> dijkstra(String origen, DirectedGraph<String, WeightedEdge> grafo) {
        return (DijkstraResult<String>) algoritmos.dijkstra(grafo.construirComparable(origen), grafo);
    }

    // ---- tamaño y tipos ----

    public void testCantidadDeNodosYEnlaces() {
        assertEquals(24, red.nodos().size());
        assertEquals(42, red.enlaces().size());
    }

    public void testTresDataCenters() {
        Set<String> ids = new HashSet<>();
        for (Nodo n : red.dataCenters()) ids.add(n.getId());
        assertEquals(Set.of("DC1", "DC2", "DC3"), ids);
    }

    public void testHayDeTodosLosTiposDeNodoYDeEnlace() {
        Set<TipoNodo> tiposNodo = new HashSet<>();
        for (Nodo n : red.nodos()) tiposNodo.add(n.getTipo());
        assertEquals(Set.of(TipoNodo.values()), tiposNodo);

        Set<TipoEnlace> tiposEnlace = new HashSet<>();
        for (Enlace e : red.enlaces()) tiposEnlace.add(e.getTipo());
        assertEquals(Set.of(TipoEnlace.values()), tiposEnlace);
    }

    public void testUsuariosSoloEnNodosDeAcceso() {
        for (Nodo n : red.nodos()) {
            if (n.getTipo().esDeAcceso()) {
                assertTrue(n.getId(), n.getUsuarios() >= 100 && n.getUsuarios() <= 5000);
                assertTrue(n.getId(), n.getUsuarios() <= n.getCapacidad());
            } else {
                assertEquals(n.getId(), 0, n.getUsuarios());
            }
        }
    }

    public void testHayUnoOMasServidores() {
        int servidores = 0;
        for (Nodo n : red.nodos()) {
            if (n.getTipo() == TipoNodo.SERVIDOR) servidores++;
        }
        assertTrue(servidores >= 1 && servidores <= 2);
    }

    public void testCostosVariados() {
        Set<Double> costos = new HashSet<>();
        for (Enlace e : red.enlaces()) costos.add(e.getCosto());
        assertTrue(costos.size() >= 20);
    }

    // ---- conectividad ----

    public void testEsConexa() {
        assertTrue(red.grafoResiliencia(Set.of(), Set.of()).esConexo());
    }

    /** Se llega de cualquier nodo a cualquier otro respetando el sentido de los arcos. */
    public void testSeLlegaDeCualquierNodoACualquierOtro() {
        assertTrue(red.grafoRuteo().esConexo());
        FloydWarshallResult<String> warshall = (FloydWarshallResult<String>) algoritmos.warshall(red.grafoRuteo());
        for (String o : warshall.getVertices()) {
            for (String d : warshall.getVertices()) {
                if (!o.equals(d)) assertTrue(o + " -> " + d, warshall.connected(o, d));
            }
        }
    }

    public void testPuntosDeArticulacion() {
        UndirectedGraph<String, WeightedEdge> grafo = red.grafoResiliencia(Set.of(), Set.of());
        assertEquals(Set.of("R5", "RR1", "AP5"), new UndirectedGraphAlgorithms().puntosDeArticulacion(grafo));
    }

    /** Un enlace es puente si al caerse la red deja de ser conexa. */
    public void testPuentes() {
        Set<String> puentes = new LinkedHashSet<>();
        for (Enlace e : red.enlaces()) {
            if (!red.grafoResiliencia(Set.of(), Set.of(e)).esConexo()) puentes.add(e.getA() + "-" + e.getB());
        }
        assertEquals(Set.of("R5-RR1", "RR1-AP5", "RR1-OF5", "AP5-AP6"), puentes);
    }

    // ---- asimetría ----

    public void testEnlacesAsimetricos() {
        Set<String> asimetricos = new HashSet<>();
        for (Enlace e : red.enlaces()) {
            if (Math.abs(e.getLatenciaAB() - e.getLatenciaBA()) > DELTA) asimetricos.add(e.getA() + "-" + e.getB());
        }
        assertEquals(Set.of("R1-R3", "R4-R5", "AP3-SW2", "RR1-AP5"), asimetricos);

        assertEquals(4.0, red.enlace("R1", "R3").getLatenciaAB(), DELTA);
        assertEquals(7.0, red.enlace("R3", "R1").getLatenciaBA(), DELTA);
        assertEquals(3.0, red.enlace("R4", "R5").getLatenciaAB(), DELTA);
        assertEquals(6.0, red.enlace("R5", "R4").getLatenciaBA(), DELTA);
        assertEquals(2.0, red.enlace("AP3", "SW2").getLatenciaAB(), DELTA);
        assertEquals(5.0, red.enlace("SW2", "AP3").getLatenciaBA(), DELTA);
        assertEquals(25.0, red.enlace("RR1", "AP5").getLatenciaAB(), DELTA);
        assertEquals(35.0, red.enlace("AP5", "RR1").getLatenciaBA(), DELTA);
    }

    /** El grafo de ruteo respeta la asimetría: R1 -> R3 pesa 4 y R3 -> R1 pesa 7. */
    public void testLaAsimetriaLlegaAlGrafoDeRuteo() {
        DirectedGraph<String, WeightedEdge> grafo = red.grafoRuteo();
        assertEquals(4.0, grafo.obtenerArista("R1", "R3").dato().getWeight(), DELTA);
        assertEquals(7.0, grafo.obtenerArista("R3", "R1").dato().getWeight(), DELTA);
    }

    // ---- camino más rápido por un enlace angosto ----

    public void testElCaminoMasRapidoDeSW2ASW3UsaElRadioenlaceAngosto() {
        DijkstraResult<String> r = dijkstra("SW2", red.grafoRuteo());
        assertEquals(3.0, r.getCost("SW3"), DELTA);
        assertEquals(List.of("SW2", "SW3"), r.getPath("SW3"));
        assertEquals(50.0, red.enlace("SW2", "SW3").getAnchoBanda(), DELTA);
    }

    public void testLaAlternativaConAnchoDe1000TieneMasLatencia() {
        DirectedGraph<String, WeightedEdge> grafo = red.grafoRuteo(1000);
        DijkstraResult<String> r = dijkstra("SW2", grafo);
        assertEquals(13.0, r.getCost("SW3"), DELTA);
        assertEquals(List.of("SW2", "R3", "R4", "SW3"), r.getPath("SW3"));
        List<String> camino = r.getPath("SW3");
        for (int i = 0; i + 1 < camino.size(); i++) {
            assertTrue(red.enlace(camino.get(i), camino.get(i + 1)).getAnchoBanda() >= 1000);
        }
    }

    // ---- empate ----

    public void testDosCaminosDeIgualLatenciaDeR2AR4() {
        DirectedGraph<String, WeightedEdge> grafo = red.grafoRuteo();
        assertEquals(10.0, dijkstra("R2", grafo).getCost("R4"), DELTA);
        assertEquals(10.0, dijkstra("R4", grafo).getCost("R2"), DELTA);

        List<List<String>> mejores = new ArrayList<>();
        for (Path<String> p : algoritmos.obtenerTodosLosCaminos(grafo.construirComparable("R2"), grafo.construirComparable("R4"), grafo)) {
            if (Math.abs(p.getCost() - 10.0) < DELTA) mejores.add(p.getPath());
        }
        assertEquals(2, mejores.size());
        assertTrue(mejores.contains(List.of("R2", "R1", "R4")));
        assertTrue(mejores.contains(List.of("R2", "R3", "R4")));
    }

    // ---- zona lejos de los data centers ----

    /** Menor latencia desde alguno de los data centers hasta cada nodo (fila de Floyd). */
    private double latenciaAlDCMasCercano(FloydWarshallResult<String> floyd, String nodo) {
        double menor = Double.POSITIVE_INFINITY;
        for (Nodo dc : red.dataCenters()) menor = Math.min(menor, floyd.getCost(dc.getId(), nodo));
        return menor;
    }

    public void testLaZonaRuralEstaLejosDeLosDataCenters() {
        FloydWarshallResult<String> floyd = (FloydWarshallResult<String>) algoritmos.floyd(red.grafoRuteo());
        assertEquals(66.0, latenciaAlDCMasCercano(floyd, "OF5"), DELTA);
        assertEquals(71.0, latenciaAlDCMasCercano(floyd, "AP5"), DELTA);
        assertEquals(101.0, latenciaAlDCMasCercano(floyd, "AP6"), DELTA);

        Set<String> rural = Set.of("OF5", "AP5", "AP6");
        int usuariosRurales = 0;
        for (Nodo n : red.nodos()) {
            if (!n.getTipo().esDeAcceso()) continue;
            if (rural.contains(n.getId())) {
                usuariosRurales += n.getUsuarios();
            } else {
                assertTrue(n.getId(), latenciaAlDCMasCercano(floyd, n.getId()) <= 12);
            }
        }
        assertEquals(6800, usuariosRurales);
    }
}
