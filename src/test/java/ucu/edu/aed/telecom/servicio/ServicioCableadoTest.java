package ucu.edu.aed.telecom.servicio;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.telecom.modelo.CargadorRed;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;
import ucu.edu.aed.telecom.resultado.ResultadoCableado;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ServicioCableadoTest extends TestCase {

    private static final double DELTA = 0.0001;

    private RedTelecom red;
    private ServicioCableado cableado;

    @Override
    protected void setUp() {
        red = new RedTelecom();
        cableado = new ServicioCableado(red);
    }

    private void oficinas(String... ids) {
        for (String id : ids) red.agregarNodo(new Nodo(id, TipoNodo.OFICINA, 100, 10));
    }

    /** Conexión posible entre a y b con su costo de instalación (latencia 1 en los dos sentidos). */
    private Enlace conexion(String a, String b, double costo) {
        return conexion(a, b, 1, costo);
    }

    private Enlace conexion(String a, String b, double latencia, double costo) {
        Enlace e = new Enlace(a, b, TipoEnlace.FIBRA, latencia, latencia, 1000, costo);
        assertTrue(red.agregarEnlace(e));
        return e;
    }

    /** Si el resultado tiene el enlace entre a y b (en cualquier orden). */
    private static boolean tiene(ResultadoCableado r, String a, String b) {
        for (Enlace e : r.getEnlaces()) {
            if (e.equals(new Enlace(a, b, TipoEnlace.FIBRA, 1, 1, 1000, 0))) return true;
        }
        return false;
    }

    /**
     * Las oficinas 1..6 con las conexiones posibles del grafo del teórico: el cableado mínimo es
     * 1-3, 4-6, 2-5, 3-6 y 2-3, con costo 15.
     */
    private void oficinasDelTeorico() {
        oficinas("1", "2", "3", "4", "5", "6");
        conexion("1", "2", 6);
        conexion("1", "3", 1);
        conexion("1", "4", 5);
        conexion("2", "3", 5);
        conexion("3", "4", 5);
        conexion("2", "5", 3);
        conexion("3", "5", 6);
        conexion("3", "6", 4);
        conexion("4", "6", 2);
        conexion("5", "6", 6);
    }

    // ---- casos borde ----

    public void testRedVacia() {
        ResultadoCableado r = cableado.cableadoMinimo();
        assertTrue(r.getEnlaces().isEmpty());
        assertEquals(0.0, r.getCostoTotal(), DELTA);
        assertTrue(r.isConexo());
    }

    public void testUnaSolaOficina() {
        oficinas("A");
        ResultadoCableado r = cableado.cableadoMinimo();
        assertTrue(r.getEnlaces().isEmpty());
        assertEquals(0.0, r.getCostoTotal(), DELTA);
        assertTrue(r.isConexo());
    }

    public void testDosOficinasSinConexionPosible() {
        oficinas("A", "B");
        ResultadoCableado r = cableado.cableadoMinimo();
        assertTrue(r.getEnlaces().isEmpty());
        assertFalse(r.isConexo());
    }

    public void testRedQueYaEsUnArbolSeQuedaIgual() {
        oficinas("A", "B", "C", "D");
        conexion("A", "B", 7);
        conexion("B", "C", 3);
        conexion("B", "D", 9);
        ResultadoCableado r = cableado.cableadoMinimo();
        assertEquals(3, r.getEnlaces().size());
        assertEquals(19.0, r.getCostoTotal(), DELTA);
        assertTrue(r.isConexo());
    }

    // ---- ejemplos ----

    public void testOficinasDelTeorico() {
        oficinasDelTeorico();
        ResultadoCableado r = cableado.cableadoMinimo();
        assertEquals(15.0, r.getCostoTotal(), DELTA);
        assertTrue(r.isConexo());
        assertEquals(5, r.getEnlaces().size());
        assertTrue(tiene(r, "1", "3"));
        assertTrue(tiene(r, "4", "6"));
        assertTrue(tiene(r, "2", "5"));
        assertTrue(tiene(r, "3", "6"));
        assertTrue(tiene(r, "2", "3"));
    }

    /** El cableado se elige por costo, no por latencia: A-C es lenta pero la más barata. */
    public void testUsaElCostoYNoLaLatencia() {
        oficinas("A", "B", "C");
        conexion("A", "B", 1, 10);
        conexion("B", "C", 1, 10);
        conexion("A", "C", 50, 1);
        ResultadoCableado r = cableado.cableadoMinimo();
        assertTrue(tiene(r, "A", "C"));
        assertEquals(11.0, r.getCostoTotal(), DELTA);
    }

    public void testEnlacesOrdenadosDeMenorAMayorCosto() {
        oficinasDelTeorico();
        List<Enlace> enlaces = cableado.cableadoMinimo().getEnlaces();
        for (int i = 0; i + 1 < enlaces.size(); i++) {
            assertTrue(enlaces.get(i).getCosto() <= enlaces.get(i + 1).getCosto());
        }
    }

    /** Devuelve los enlaces de la red (con su tipo y sus datos), no copias armadas con el peso del grafo. */
    public void testDevuelveLosEnlacesDeLaRed() {
        oficinas("A", "B");
        Enlace e = new Enlace("A", "B", TipoEnlace.RADIOENLACE, 4, 6, 50, 12);
        red.agregarEnlace(e);
        Enlace elegido = cableado.cableadoMinimo().getEnlaces().get(0);
        assertSame(e, elegido);
        assertEquals(TipoEnlace.RADIOENLACE, elegido.getTipo());
    }

    /** Todas las conexiones cuestan lo mismo: cualquier árbol sirve, pero tiene que ser uno de 3 enlaces. */
    public void testCostosIguales() {
        oficinas("A", "B", "C", "D");
        conexion("A", "B", 2);
        conexion("B", "C", 2);
        conexion("C", "D", 2);
        conexion("D", "A", 2);
        conexion("A", "C", 2);
        ResultadoCableado r = cableado.cableadoMinimo();
        assertEquals(3, r.getEnlaces().size());
        assertEquals(6.0, r.getCostoTotal(), DELTA);
        assertTrue(r.isConexo());
        assertTrue(esArbolDe(r, Set.of("A", "B", "C", "D")));
    }

    /** Dos grupos de oficinas que no se pueden unir: bosque con el mínimo de cada grupo y conexo false. */
    public void testNoSePuedenConectarTodas() {
        oficinas("A", "B", "C", "X", "Y");
        conexion("A", "B", 4);
        conexion("B", "C", 2);
        conexion("A", "C", 1);
        conexion("X", "Y", 8);
        ResultadoCableado r = cableado.cableadoMinimo();
        assertFalse(r.isConexo());
        assertEquals(3, r.getEnlaces().size());
        assertEquals(11.0, r.getCostoTotal(), DELTA);
        assertTrue(tiene(r, "A", "C"));
        assertTrue(tiene(r, "B", "C"));
        assertTrue(tiene(r, "X", "Y"));
    }

    /** Una oficina aislada alcanza para que no sea conexo. */
    public void testUnaOficinaAisladaNoEsConexo() {
        oficinasDelTeorico();
        oficinas("7");
        ResultadoCableado r = cableado.cableadoMinimo();
        assertFalse(r.isConexo());
        assertEquals(15.0, r.getCostoTotal(), DELTA);
    }

    public void testNoModificaLaRed() {
        oficinasDelTeorico();
        cableado.cableadoMinimo();
        assertEquals(6, red.nodos().size());
        assertEquals(10, red.enlaces().size());
    }

    // ---- comparación con Prim ----

    /**
     * En redes conexas al azar (semilla fija) el costo tiene que ser el mismo que el del árbol de Prim de la
     * biblioteca, y el resultado tiene que ser un árbol que toca a todos los nodos.
     */
    public void testMismoCostoQuePrimEnRedesAlAzar() {
        Random random = new Random(7);
        UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();
        for (int prueba = 0; prueba < 40; prueba++) {
            setUp();
            int n = 1 + random.nextInt(12);
            String[] ids = new String[n];
            for (int i = 0; i < n; i++) ids[i] = "N" + i;
            oficinas(ids);
            // primero un árbol al azar (así es conexa) y después enlaces de más
            for (int i = 1; i < n; i++) conexion(ids[i], ids[random.nextInt(i)], 1 + random.nextInt(20));
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (red.enlace(ids[i], ids[j]) == null && random.nextDouble() < 0.3) {
                        conexion(ids[i], ids[j], 1 + random.nextInt(20));
                    }
                }
            }

            ResultadoCableado r = cableado.cableadoMinimo();
            IUndirectedGraph<String, WeightedEdge> prim = algoritmos.prim(red.grafoCableado(), red.grafoCableado().construirComparable("N0"));
            double costoPrim = 0;
            for (Edge<String, WeightedEdge> e : prim.aristas()) costoPrim += e.dato().getWeight();

            assertEquals("prueba " + prueba, costoPrim, r.getCostoTotal(), DELTA);
            assertTrue("prueba " + prueba, r.isConexo());
            assertTrue("prueba " + prueba, esArbolDe(r, Set.of(ids)));
        }
    }

    /** Los enlaces del resultado forman un árbol: n - 1 enlaces, conexo y sin ciclos. */
    private static boolean esArbolDe(ResultadoCableado r, Set<String> ids) {
        UndirectedGraph<String, WeightedEdge> g = new UndirectedGraph<>();
        g.agregarVertices(ids);
        for (Enlace e : r.getEnlaces()) g.agregarArista(e.getA(), e.getB(), new WeightedEdge(e.getCosto()));
        return g.cantidadDeAristas() == ids.size() - 1 && g.esConexo() && !g.tieneCiclos();
    }

    // ---- datos de ejemplo ----

    private static RedTelecom datosDeEjemplo() throws IOException {
        return CargadorRed.cargar(Path.of("src/main/resources/telecom/nodos.csv"), Path.of("src/main/resources/telecom/enlaces.csv"));
    }

    public void testDatosDeEjemplo() throws IOException {
        red = datosDeEjemplo();
        ResultadoCableado r = new ServicioCableado(red).cableadoMinimo();
        assertTrue(r.isConexo());
        assertEquals(red.nodos().size() - 1, r.getEnlaces().size());

        double costoPrim = 0;
        UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();
        for (Edge<String, WeightedEdge> e : algoritmos.prim(red.grafoCableado()).aristas()) costoPrim += e.dato().getWeight();
        assertEquals(costoPrim, r.getCostoTotal(), DELTA);
    }

    /** Un puente es la única forma de llegar a una parte de la red: tiene que estar en cualquier cableado. */
    public void testDatosDeEjemploIncluyeLosPuentes() throws IOException {
        red = datosDeEjemplo();
        ResultadoCableado r = new ServicioCableado(red).cableadoMinimo();
        assertTrue(tiene(r, "R5", "RR1"));
        assertTrue(tiene(r, "RR1", "AP5"));
        assertTrue(tiene(r, "RR1", "OF5"));
        assertTrue(tiene(r, "AP5", "AP6"));
    }
}
