package ucu.edu.aed.telecom.resultado;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.TipoEnlace;

/** Las clases de resultado: convenciones, copias defensivas y la tabla de ruteo sobre Floyd. */
public class ResultadosTest extends TestCase {

    private static final double DELTA = 0.0001;

    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    // ---- RutaResultado ----

    public void testRuta() {
        RutaResultado r = new RutaResultado(List.of("A", "B", "C"), 3, 200);
        assertTrue(r.existe());
        assertEquals(List.of("A", "B", "C"), r.getNodos());
        assertEquals(3.0, r.getLatenciaTotal(), DELTA);
        assertEquals(200.0, r.getAnchoBandaCuelloBotella(), DELTA);
    }

    public void testSinRuta() {
        RutaResultado r = RutaResultado.sinRuta();
        assertFalse(r.existe());
        assertTrue(r.getNodos().isEmpty());
        assertEquals(Double.POSITIVE_INFINITY, r.getLatenciaTotal(), 0);
        assertEquals(0.0, r.getAnchoBandaCuelloBotella(), DELTA);
    }

    /** Si después se cambia la lista que se pasó, la ruta no cambia; y la de la ruta no se puede modificar. */
    public void testRutaCopiaLaLista() {
        List<String> nodos = new ArrayList<>(List.of("A", "B"));
        RutaResultado r = new RutaResultado(nodos, 1, 100);
        nodos.add("C");
        assertEquals(List.of("A", "B"), r.getNodos());
        try {
            r.getNodos().add("Z");
            fail("Debía lanzar UnsupportedOperationException");
        } catch (UnsupportedOperationException esperada) {
            // ok
        }
    }

    // ---- TablaRuteo ----

    /** A -> B (1), B -> C (2), A -> C (9), C -> A (4) y D aislado. */
    private TablaRuteo tablaDePrueba() {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        for (String v : List.of("A", "B", "C", "D")) g.agregarVertice(v);
        g.agregarArista("A", "B", new WeightedEdge(1));
        g.agregarArista("B", "C", new WeightedEdge(2));
        g.agregarArista("A", "C", new WeightedEdge(9));
        g.agregarArista("C", "A", new WeightedEdge(4));
        return new TablaRuteo(algoritmos.floyd(g));
    }

    public void testTablaLatenciasYCaminos() {
        TablaRuteo t = tablaDePrueba();
        assertEquals(3.0, t.latencia("A", "C"), DELTA);
        assertEquals(List.of("A", "B", "C"), t.camino("A", "C"));
        assertEquals(6.0, t.latencia("B", "A"), DELTA);
        assertEquals(5.0, t.latencia("C", "B"), DELTA);
        assertEquals(List.of("B", "C", "A"), t.camino("B", "A"));
        assertEquals(0.0, t.latencia("B", "B"), DELTA);
    }

    /** El siguiente salto de A hacia C es B, aunque haya un arco directo A -> C (es más caro). */
    public void testTablaSiguienteSalto() {
        TablaRuteo t = tablaDePrueba();
        assertEquals("B", t.siguienteSalto("A", "C"));
        assertEquals("C", t.siguienteSalto("B", "A"));
        assertEquals("A", t.siguienteSalto("C", "B"));
        assertNull(t.siguienteSalto("A", "A"));
    }

    public void testTablaSinCamino() {
        TablaRuteo t = tablaDePrueba();
        assertEquals(Double.POSITIVE_INFINITY, t.latencia("A", "D"), 0);
        assertTrue(t.camino("D", "A").isEmpty());
        assertNull(t.siguienteSalto("A", "D"));
    }

    public void testTablaNodoInexistente() {
        TablaRuteo t = tablaDePrueba();
        try {
            t.latencia("A", "Z");
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
        try {
            t.siguienteSalto("Z", "A");
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testTablaNodos() {
        assertEquals(List.of("A", "B", "C", "D"), tablaDePrueba().nodos());
    }

    /** Warshall solo dice si hay camino: no sirve para una tabla de latencias. */
    public void testTablaNoAceptaWarshall() {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        g.agregarVertice("A");
        try {
            new TablaRuteo(algoritmos.warshall(g));
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    // ---- AsignacionCDN ----

    public void testAsignacion() {
        Map<String, String> dc = new HashMap<>(Map.of("AP1", "DC1", "DC1", "DC1"));
        Map<String, Double> latencia = new HashMap<>(Map.of("AP1", 7.0, "DC1", 0.0));
        AsignacionCDN a = new AsignacionCDN(dc, latencia);
        dc.put("AP2", "DC1");   // cambiar los mapas originales no afecta a la asignación

        assertEquals("DC1", a.dcDe("AP1"));
        assertEquals(7.0, a.latenciaDe("AP1"), DELTA);
        assertEquals(0.0, a.latenciaDe("DC1"), DELTA);
        assertNull(a.dcDe("AP2"));
        assertEquals(Double.POSITIVE_INFINITY, a.latenciaDe("AP2"), 0);
    }

    // ---- ImpactoFalla y ResultadoCableado ----

    public void testImpactoFallaCopiaYMantieneElOrden() {
        Set<String> incomunicados = new LinkedHashSet<>(List.of("C", "A", "B"));
        ImpactoFalla i = new ImpactoFalla("R1", incomunicados, 900);
        incomunicados.add("Z");
        assertEquals("R1", i.getElementoCaido());
        assertEquals(List.of("C", "A", "B"), new ArrayList<>(i.getNodosIncomunicados()));
        assertEquals(900, i.getUsuariosAfectados());
        try {
            i.getNodosIncomunicados().clear();
            fail("Debía lanzar UnsupportedOperationException");
        } catch (UnsupportedOperationException esperada) {
            // ok
        }
    }

    public void testResultadoCableado() {
        Enlace e = new Enlace("A", "B", TipoEnlace.COBRE, 1, 1, 100, 4);
        List<Enlace> enlaces = new ArrayList<>(List.of(e));
        ResultadoCableado r = new ResultadoCableado(enlaces, 4, false);
        enlaces.clear();
        assertEquals(List.of(e), r.getEnlaces());
        assertEquals(4.0, r.getCostoTotal(), DELTA);
        assertFalse(r.existe());
    }

    public void testRecomendacionYDiametro() {
        RecomendacionDC r = new RecomendacionDC("R7", 1200.5, 300);
        assertEquals("R7", r.getNodoCandidato());
        assertEquals(1200.5, r.getBeneficio(), DELTA);
        assertEquals(300, r.getUsuariosBeneficiados());

        ResultadoDiametro d = new ResultadoDiametro("AP1", "AP9", 42);
        assertEquals("AP1", d.getDesde());
        assertEquals("AP9", d.getHasta());
        assertEquals(42.0, d.getLatencia(), DELTA);
    }
}
