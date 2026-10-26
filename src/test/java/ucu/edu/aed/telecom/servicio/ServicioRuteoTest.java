package ucu.edu.aed.telecom.servicio;

import junit.framework.TestCase;
import ucu.edu.aed.telecom.modelo.CargadorRed;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;
import ucu.edu.aed.telecom.resultado.ResultadoDiametro;
import ucu.edu.aed.telecom.resultado.RutaResultado;
import ucu.edu.aed.telecom.resultado.TablaRuteo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class ServicioRuteoTest extends TestCase {

    private static final double DELTA = 0.0001;
    private static final double INFINITO = Double.POSITIVE_INFINITY;

    private RedTelecom red;
    private ServicioRuteo ruteo;

    @Override
    protected void setUp() {
        red = new RedTelecom();
        ruteo = new ServicioRuteo(red);
    }

    private void routers(String... ids) {
        for (String id : ids) red.agregarNodo(new Nodo(id, TipoNodo.ROUTER, 10, 0));
    }

    private Enlace enlace(String a, String b, double latAB, double latBA, double ancho) {
        Enlace e = new Enlace(a, b, TipoEnlace.FIBRA, latAB, latBA, ancho, 1);
        red.agregarEnlace(e);
        return e;
    }

    /**
     * Diamante A, B, C, D más E sin enlaces (lat AB/BA, ancho):
     * A-B 2/4 10000, B-D 3/3 5000, A-C 1/1 200, C-D 1/1 150.
     * De A a D conviene pasar por C (2 ms) pero es angosto (cuello 150); por B son 5 ms y 5000 Mbps.
     */
    private void diamante() {
        routers("A", "B", "C", "D", "E");
        enlace("A", "B", 2, 4, 10000);
        enlace("B", "D", 3, 3, 5000);
        enlace("A", "C", 1, 1, 200);
        enlace("C", "D", 1, 1, 150);
    }

    private void assertSinRuta(RutaResultado r) {
        assertFalse(r.existe());
        assertTrue(r.getNodos().isEmpty());
        assertEquals(INFINITO, r.getLatenciaTotal(), 0);
        assertEquals(0.0, r.getAnchoBandaCuelloBotella(), DELTA);
    }

    private void assertMismaRuta(RutaResultado esperada, RutaResultado real) {
        assertEquals(esperada.getNodos(), real.getNodos());
        assertEquals(esperada.getLatenciaTotal(), real.getLatenciaTotal(), DELTA);
        assertEquals(esperada.getAnchoBandaCuelloBotella(), real.getAnchoBandaCuelloBotella(), DELTA);
    }

    // ---- rutaMenorLatencia ----

    public void testRutaSimpleConNodosIntermedios() {
        routers("A", "B", "C", "D");
        enlace("A", "B", 2, 2, 1000);
        enlace("B", "C", 3, 3, 1000);
        enlace("C", "D", 4, 4, 1000);
        RutaResultado r = ruteo.rutaMenorLatencia("A", "D");

        assertEquals(List.of("A", "B", "C", "D"), r.getNodos());
        assertEquals(9.0, r.getLatenciaTotal(), DELTA);
    }

    public void testEligeElCaminoDeMenorLatencia() {
        diamante();
        RutaResultado r = ruteo.rutaMenorLatencia("A", "D");
        assertEquals(List.of("A", "C", "D"), r.getNodos());
        assertEquals(2.0, r.getLatenciaTotal(), DELTA);
    }

    /** A -> B cuesta 2 pero B -> A cuesta 4. */
    public void testLatenciaAsimetrica() {
        diamante();
        assertEquals(2.0, ruteo.rutaMenorLatencia("A", "B").getLatenciaTotal(), DELTA);
        assertEquals(4.0, ruteo.rutaMenorLatencia("B", "A").getLatenciaTotal(), DELTA);
        assertEquals(List.of("A", "B"), ruteo.rutaMenorLatencia("A", "B").getNodos());
    }

    public void testCuelloDeBotellaEsElEnlaceMasAngosto() {
        diamante();
        assertEquals(150.0, ruteo.rutaMenorLatencia("A", "D").getAnchoBandaCuelloBotella(), DELTA);
        assertEquals(10000.0, ruteo.rutaMenorLatencia("A", "B").getAnchoBandaCuelloBotella(), DELTA);
    }

    public void testSinRutaPosible() {
        diamante();
        assertSinRuta(ruteo.rutaMenorLatencia("A", "E"));
        assertSinRuta(ruteo.rutaMenorLatencia("E", "A"));
    }

    /** El origen y el destino son el mismo: ruta de un solo nodo, sin latencia y sin cuello. */
    public void testOrigenIgualADestino() {
        diamante();
        RutaResultado r = ruteo.rutaMenorLatencia("A", "A");
        assertTrue(r.existe());
        assertEquals(List.of("A"), r.getNodos());
        assertEquals(0.0, r.getLatenciaTotal(), DELTA);
        assertEquals(INFINITO, r.getAnchoBandaCuelloBotella(), 0);
    }

    public void testNodoAisladoAEsiMismo() {
        diamante();
        assertEquals(List.of("E"), ruteo.rutaMenorLatencia("E", "E").getNodos());
    }

    public void testIdInexistente() {
        diamante();
        try {
            ruteo.rutaMenorLatencia("A", "Z");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
        try {
            ruteo.rutaMenorLatencia("Z", "A");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
        try {
            ruteo.rutaConAnchoBandaMinimo("Z", "A", 100);
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
        try {
            ruteo.rutaAlternativa("A", "Z", Set.of(), Set.of());
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
        try {
            ruteo.hayCamino("A", "Z");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    // ---- rutaConAnchoBandaMinimo ----

    /** Con 1000 Mbps de mínimo el camino por C (150 y 200 Mbps) no sirve: se va por B. */
    public void testConAnchoMinimoEvitaElEnlaceAngosto() {
        diamante();
        RutaResultado r = ruteo.rutaConAnchoBandaMinimo("A", "D", 1000);
        assertEquals(List.of("A", "B", "D"), r.getNodos());
        assertEquals(5.0, r.getLatenciaTotal(), DELTA);
        assertEquals(5000.0, r.getAnchoBandaCuelloBotella(), DELTA);
    }

    public void testConAnchoMinimoCeroCoincideConMenorLatencia() {
        diamante();
        for (String o : List.of("A", "B", "C", "D", "E")) {
            for (String d : List.of("A", "B", "C", "D", "E")) {
                assertMismaRuta(ruteo.rutaMenorLatencia(o, d), ruteo.rutaConAnchoBandaMinimo(o, d, 0));
            }
        }
    }

    public void testNingunEnlaceDeLaRutaEstaPorDebajoDelMinimo() {
        diamante();
        for (double minimo : new double[]{0, 100, 150, 151, 200, 1000, 5000, 5001}) {
            RutaResultado r = ruteo.rutaConAnchoBandaMinimo("A", "D", minimo);
            if (!r.existe()) continue;
            List<String> nodos = r.getNodos();
            for (int i = 0; i + 1 < nodos.size(); i++) {
                assertTrue("mínimo " + minimo, red.enlace(nodos.get(i), nodos.get(i + 1)).getAnchoBanda() >= minimo);
            }
            assertTrue(r.getAnchoBandaCuelloBotella() >= minimo);
        }
    }

    /** El mínimo se acepta: un enlace de 150 Mbps sirve para un mínimo de 150. */
    public void testElAnchoMinimoExactoSeAcepta() {
        diamante();
        RutaResultado r = ruteo.rutaConAnchoBandaMinimo("A", "D", 150);
        assertEquals(List.of("A", "C", "D"), r.getNodos());
        assertEquals(150.0, r.getAnchoBandaCuelloBotella(), DELTA);
    }

    public void testNingunaRutaCumpleElAnchoMinimo() {
        diamante();
        assertSinRuta(ruteo.rutaConAnchoBandaMinimo("A", "D", 6000));
    }

    public void testConAnchoMinimoOrigenIgualADestino() {
        diamante();
        assertEquals(List.of("A"), ruteo.rutaConAnchoBandaMinimo("A", "A", 999999).getNodos());
    }

    // ---- rutaAlternativa ----

    public void testAlternativaNuncaPasaPorLoCaido() {
        diamante();
        RutaResultado r = ruteo.rutaAlternativa("A", "D", Set.of("C"), Set.of());
        assertEquals(List.of("A", "B", "D"), r.getNodos());
        assertEquals(5.0, r.getLatenciaTotal(), DELTA);
        assertFalse(r.getNodos().contains("C"));
    }

    public void testAlternativaConEnlaceCaido() {
        diamante();
        RutaResultado r = ruteo.rutaAlternativa("A", "D", Set.of(), Set.of(red.enlace("C", "D")));
        assertEquals(List.of("A", "B", "D"), r.getNodos());
    }

    public void testAlternativaSinCaidosCoincideConMenorLatencia() {
        diamante();
        assertMismaRuta(ruteo.rutaMenorLatencia("A", "D"), ruteo.rutaAlternativa("A", "D", Set.of(), Set.of()));
    }

    public void testAlternativaSiCaeElOrigenOElDestinoNoHayRuta() {
        diamante();
        assertSinRuta(ruteo.rutaAlternativa("A", "D", Set.of("A"), Set.of()));
        assertSinRuta(ruteo.rutaAlternativa("A", "D", Set.of("D"), Set.of()));
    }

    /** X - Y - Z: el enlace Y-Z es un puente; si cae, X no llega a Z. */
    public void testAlternativaSiCaeUnPuenteNoHayRuta() {
        routers("X", "Y", "Z");
        enlace("X", "Y", 1, 1, 1000);
        Enlace puente = enlace("Y", "Z", 1, 1, 1000);
        assertEquals(2.0, ruteo.rutaMenorLatencia("X", "Z").getLatenciaTotal(), DELTA);
        assertSinRuta(ruteo.rutaAlternativa("X", "Z", Set.of(), Set.of(puente)));
        assertSinRuta(ruteo.rutaAlternativa("X", "Z", Set.of("Y"), Set.of()));
    }

    public void testAlternativaConTodasLasRutasCaidas() {
        diamante();
        assertSinRuta(ruteo.rutaAlternativa("A", "D", Set.of("B", "C"), Set.of()));
    }

    // ---- tabla de ruteo ----

    public void testTablaDeRuteo() {
        diamante();
        TablaRuteo tabla = ruteo.tablaDeRuteo();
        assertEquals(2.0, tabla.latencia("A", "D"), DELTA);
        assertEquals(4.0, tabla.latencia("B", "A"), DELTA);
        assertEquals(INFINITO, tabla.latencia("A", "E"), 0);
        assertEquals(List.of("A", "C", "D"), tabla.camino("A", "D"));
    }

    public void testSiguienteSalto() {
        diamante();
        TablaRuteo tabla = ruteo.tablaDeRuteo();
        assertEquals("C", tabla.siguienteSalto("A", "D"));
        assertEquals("B", tabla.siguienteSalto("A", "B"));
        assertNull(tabla.siguienteSalto("A", "A"));
        assertNull(tabla.siguienteSalto("A", "E"));
    }

    // ---- hayCamino ----

    public void testHayCamino() {
        diamante();
        assertTrue(ruteo.hayCamino("A", "D"));
        assertTrue(ruteo.hayCamino("D", "A"));
        assertFalse(ruteo.hayCamino("A", "E"));
        assertFalse(ruteo.hayCamino("E", "A"));
    }

    /** Warshall no marca la diagonal si no hay un ciclo, pero un nodo siempre llega a sí mismo. */
    public void testHayCaminoDeUnNodoASiMismo() {
        diamante();
        assertTrue(ruteo.hayCamino("A", "A"));
        assertTrue(ruteo.hayCamino("E", "E"));
    }

    // ---- diámetro ----

    /** Sin E: la latencia mayor es 4 (B -> A y B -> C); gana el primero en el orden de la red. */
    public void testDiametroDeUnaRedChica() {
        routers("A", "B", "C", "D");
        enlace("A", "B", 2, 4, 10000);
        enlace("B", "D", 3, 3, 5000);
        enlace("A", "C", 1, 1, 200);
        enlace("C", "D", 1, 1, 150);
        ResultadoDiametro d = ruteo.diametro();
        assertEquals("B", d.getDesde());
        assertEquals("A", d.getHasta());
        assertEquals(4.0, d.getLatencia(), DELTA);
    }

    public void testDiametroConUnParSinCamino() {
        diamante();
        ResultadoDiametro d = ruteo.diametro();
        assertEquals(INFINITO, d.getLatencia(), 0);
        assertEquals("A", d.getDesde());
        assertEquals("E", d.getHasta());
    }

    public void testDiametroConMenosDeDosNodos() {
        assertNull(ruteo.diametro());
        routers("A");
        assertNull(ruteo.diametro());
    }

    public void testDiametroDeDosNodosAsimetricos() {
        routers("A", "B");
        enlace("A", "B", 3, 8, 1000);
        ResultadoDiametro d = ruteo.diametro();
        assertEquals("B", d.getDesde());
        assertEquals("A", d.getHasta());
        assertEquals(8.0, d.getLatencia(), DELTA);
    }

    // ---- datos de ejemplo ----

    private void cargarDatosDeEjemplo() throws IOException {
        red = CargadorRed.cargar(Path.of("src/main/resources/telecom/nodos.csv"),
                Path.of("src/main/resources/telecom/enlaces.csv"));
        ruteo = new ServicioRuteo(red);
    }

    /** Para todos los pares, Dijkstra y la tabla de Floyd dan la misma latencia. */
    public void testDatosDeEjemploRutaCoincideConLaTabla() throws IOException {
        cargarDatosDeEjemplo();
        TablaRuteo tabla = ruteo.tablaDeRuteo();
        for (Nodo o : red.nodos()) {
            for (Nodo d : red.nodos()) {
                RutaResultado r = ruteo.rutaMenorLatencia(o.getId(), d.getId());
                assertEquals(o + " -> " + d, tabla.latencia(o.getId(), d.getId()), r.getLatenciaTotal(), DELTA);
                assertEquals(o.getId(), r.getNodos().get(0));
                assertEquals(d.getId(), r.getNodos().get(r.getNodos().size() - 1));
            }
        }
    }

    public void testDatosDeEjemploHayCaminoEntreTodos() throws IOException {
        cargarDatosDeEjemplo();
        for (Nodo o : red.nodos()) {
            for (Nodo d : red.nodos()) {
                assertTrue(o + " -> " + d, ruteo.hayCamino(o.getId(), d.getId()));
            }
        }
    }

    public void testDatosDeEjemploRutaConAnchoMinimo() throws IOException {
        cargarDatosDeEjemplo();
        RutaResultado rapida = ruteo.rutaMenorLatencia("SW2", "SW3");
        assertEquals(List.of("SW2", "SW3"), rapida.getNodos());
        assertEquals(50.0, rapida.getAnchoBandaCuelloBotella(), DELTA);

        RutaResultado ancha = ruteo.rutaConAnchoBandaMinimo("SW2", "SW3", 1000);
        assertEquals(List.of("SW2", "R3", "R4", "SW3"), ancha.getNodos());
        assertEquals(13.0, ancha.getLatenciaTotal(), DELTA);
        assertEquals(10000.0, ancha.getAnchoBandaCuelloBotella(), DELTA);
    }

    public void testDatosDeEjemploSiCaeElPuenteLaZonaRuralQuedaSinRuta() throws IOException {
        cargarDatosDeEjemplo();
        assertTrue(ruteo.rutaMenorLatencia("DC2", "AP6").existe());
        assertSinRuta(ruteo.rutaAlternativa("DC2", "AP6", Set.of(), Set.of(red.enlace("R5", "RR1"))));
        assertSinRuta(ruteo.rutaAlternativa("DC2", "AP6", Set.of("R5"), Set.of()));
    }

    /** Si cae un enlace del núcleo hay otra ruta, más lenta, que no lo usa. */
    public void testDatosDeEjemploRutaAlternativaEnElNucleo() throws IOException {
        cargarDatosDeEjemplo();
        Enlace r2r1 = red.enlace("R1", "R2");
        RutaResultado normal = ruteo.rutaMenorLatencia("R2", "R4");
        RutaResultado alternativa = ruteo.rutaAlternativa("R2", "R4", Set.of(), Set.of(red.enlace("R2", "R3"), r2r1));
        assertEquals(10.0, normal.getLatenciaTotal(), DELTA);
        assertTrue(alternativa.existe());
        assertTrue(alternativa.getLatenciaTotal() > normal.getLatenciaTotal());
    }

    public void testDatosDeEjemploDiametro() throws IOException {
        cargarDatosDeEjemplo();
        ResultadoDiametro d = ruteo.diametro();
        TablaRuteo tabla = ruteo.tablaDeRuteo();
        double mayor = 0;
        for (Nodo o : red.nodos()) {
            for (Nodo h : red.nodos()) mayor = Math.max(mayor, tabla.latencia(o.getId(), h.getId()));
        }
        assertEquals(mayor, d.getLatencia(), DELTA);
        assertEquals(mayor, tabla.latencia(d.getDesde(), d.getHasta()), DELTA);
        assertFalse(d.getDesde().equals(d.getHasta()));
        assertTrue(Set.of("AP6", "OF5", "AP5").contains(d.getDesde()) || Set.of("AP6", "OF5", "AP5").contains(d.getHasta()));
    }

    // ---- consultas 1 y 4 a través de ServicioConsultas ----

    public void testConsultasDanLoMismoQueElServicioDeRuteo() throws IOException {
        cargarDatosDeEjemplo();
        ServicioConsultas consultas = new ServicioConsultas(ruteo, new ServicioResiliencia(red), new ServicioCDN(red));

        assertMismaRuta(ruteo.rutaConAnchoBandaMinimo("SW2", "SW3", 1000), consultas.rutaConAnchoBandaMinimo("SW2", "SW3", 1000));
        assertMismaRuta(ruteo.rutaConAnchoBandaMinimo("AP1", "AP6", 100), consultas.rutaConAnchoBandaMinimo("AP1", "AP6", 100));

        ResultadoDiametro esperado = ruteo.diametro();
        ResultadoDiametro real = consultas.diametro();
        assertEquals(esperado.getDesde(), real.getDesde());
        assertEquals(esperado.getHasta(), real.getHasta());
        assertEquals(esperado.getLatencia(), real.getLatencia(), DELTA);
    }
}
