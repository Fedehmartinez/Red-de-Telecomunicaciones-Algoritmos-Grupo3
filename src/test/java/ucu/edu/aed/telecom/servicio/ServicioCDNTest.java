package ucu.edu.aed.telecom.servicio;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.telecom.modelo.CargadorRed;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;
import ucu.edu.aed.telecom.resultado.AsignacionCDN;
import ucu.edu.aed.telecom.resultado.RecomendacionDC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class ServicioCDNTest extends TestCase {

    private static final double DELTA = 0.0001;
    private static final double INFINITO = Double.POSITIVE_INFINITY;

    private RedTelecom red;
    private ServicioCDN cdn;

    @Override
    protected void setUp() {
        red = new RedTelecom();
        cdn = new ServicioCDN(red);
    }

    private void dataCenters(String... ids) {
        for (String id : ids) red.agregarNodo(new Nodo(id, TipoNodo.DATA_CENTER, 1000, 0));
    }

    private void routers(String... ids) {
        for (String id : ids) red.agregarNodo(new Nodo(id, TipoNodo.ROUTER, 10, 0));
    }

    private void acceso(String id, int usuarios) {
        red.agregarNodo(new Nodo(id, TipoNodo.PUNTO_ACCESO, 5000, usuarios));
    }

    private void enlace(String a, String b, double latAB, double latBA) {
        red.agregarEnlace(new Enlace(a, b, TipoEnlace.FIBRA, latAB, latBA, 1000, 1));
    }

    /** DC - A - B - C en línea: DC-A 2, A-B 3, B-C 4, con 100, 200 y 300 usuarios. */
    private void linea() {
        dataCenters("DC");
        acceso("A", 100);
        acceso("B", 200);
        acceso("C", 300);
        enlace("DC", "A", 2, 2);
        enlace("A", "B", 3, 3);
        enlace("B", "C", 4, 4);
    }

    // ---- asignarCDN ----

    public void testCadaNodoVaAlDataCenterMasCercano() {
        dataCenters("DC1", "DC2");
        acceso("A", 10);
        acceso("B", 10);
        acceso("C", 10);
        enlace("DC1", "A", 1, 1);
        enlace("A", "B", 2, 2);
        enlace("B", "DC2", 1, 1);
        enlace("B", "C", 3, 3);
        AsignacionCDN a = cdn.asignarCDN();

        assertEquals("DC1", a.dcDe("A"));
        assertEquals(1.0, a.latenciaDe("A"), DELTA);
        assertEquals("DC2", a.dcDe("B"));
        assertEquals(1.0, a.latenciaDe("B"), DELTA);
        assertEquals("DC2", a.dcDe("C"));
        assertEquals(4.0, a.latenciaDe("C"), DELTA);
    }

    public void testUnDataCenterSeAtiendeASiMismo() {
        dataCenters("DC1", "DC2");
        enlace("DC1", "DC2", 3, 3);
        AsignacionCDN a = cdn.asignarCDN();

        assertEquals("DC1", a.dcDe("DC1"));
        assertEquals(0.0, a.latenciaDe("DC1"), DELTA);
        assertEquals("DC2", a.dcDe("DC2"));
        assertEquals(0.0, a.latenciaDe("DC2"), DELTA);
    }

    /**
     * El contenido va del data center al nodo: DC1 -> X pesa 1 y X -> DC1 pesa 10; DC2 -> X pesa 5 y X -> DC2 pesa 2.
     * Mirando la fila, X se atiende desde DC1 (1 ms). Si se mirara la columna elegiría DC2 (2 ms).
     */
    public void testUsaLaFilaYNoLaColumna() {
        dataCenters("DC1", "DC2");
        acceso("X", 50);
        enlace("DC1", "X", 1, 10);
        enlace("DC2", "X", 5, 2);
        AsignacionCDN a = cdn.asignarCDN();

        assertEquals("DC1", a.dcDe("X"));
        assertEquals(1.0, a.latenciaDe("X"), DELTA);
    }

    public void testNodoSinDataCenterAlcanzable() {
        dataCenters("DC1");
        acceso("A", 10);
        acceso("B", 10);
        enlace("DC1", "A", 2, 2);
        AsignacionCDN a = cdn.asignarCDN();

        assertEquals("DC1", a.dcDe("A"));
        assertNull(a.dcDe("B"));
        assertEquals(INFINITO, a.latenciaDe("B"), 0);
    }

    /** A está a 2 ms de los dos: gana el que aparece primero en red.nodos(), no el del Set. */
    public void testEmpateDeDataCenters() {
        dataCenters("DC2", "DC1");
        acceso("A", 10);
        enlace("DC1", "A", 2, 2);
        enlace("DC2", "A", 2, 2);

        assertEquals("DC2", cdn.asignarCDN().dcDe("A"));
        assertEquals("DC2", cdn.asignarCDN(Set.of("DC1", "DC2")).dcDe("A"));
    }

    public void testAsignarConUnConjuntoDeDataCenters() {
        dataCenters("DC1", "DC2");
        acceso("A", 10);
        enlace("DC1", "A", 1, 1);
        enlace("DC2", "A", 5, 5);

        AsignacionCDN soloDC2 = cdn.asignarCDN(Set.of("DC2"));
        assertEquals("DC2", soloDC2.dcDe("A"));
        assertEquals(5.0, soloDC2.latenciaDe("A"), DELTA);
        assertEquals("DC2", soloDC2.dcDe("DC1"));
    }

    /** Un nodo que no es data center puede usarse como tal en asignarCDN(Set). */
    public void testAsignarConUnNodoComoDataCenter() {
        linea();
        AsignacionCDN a = cdn.asignarCDN(Set.of("DC", "C"));
        assertEquals("C", a.dcDe("B"));
        assertEquals(4.0, a.latenciaDe("B"), DELTA);
        assertEquals("DC", a.dcDe("A"));
        assertEquals(0.0, a.latenciaDe("C"), DELTA);
    }

    public void testSinDataCentersNadieTieneAsignacion() {
        acceso("A", 10);
        AsignacionCDN a = cdn.asignarCDN();
        assertNull(a.dcDe("A"));
        assertEquals(INFINITO, a.latenciaDe("A"), 0);
    }

    public void testAsignarConIdInexistente() {
        linea();
        try {
            cdn.asignarCDN(Set.of("Z"));
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            // ok
        }
    }

    // ---- recomendarDCSimple ----

    /**
     * Línea DC - A - B - C (2, 3, 4 ms; 100, 200 y 300 usuarios). Hoy las latencias son A 2, B 5 y C 9.
     * Candidato A: 2×100 + 2×200 + 2×300 = 1200. Candidato B: 5×200 + 5×300 = 2500 (A no mejora).
     * Candidato C: 9×300 + 1×200 = 2900 (B mejora de 5 a 4 y A no mejora). Gana C con 500 usuarios beneficiados.
     */
    public void testRecomendarDCSimpleAMano() {
        linea();
        RecomendacionDC r = cdn.recomendarDCSimple();
        assertEquals("C", r.getNodoCandidato());
        assertEquals(2900.0, r.getBeneficio(), DELTA);
        assertEquals(500, r.getUsuariosBeneficiados());
    }

    public void testRecomendarDCSimpleNuncaRecomiendaUnDataCenterExistente() {
        linea();
        dataCenters("DC2");
        enlace("DC2", "C", 1, 1);
        RecomendacionDC r = cdn.recomendarDCSimple();
        assertFalse(r.getNodoCandidato().equals("DC"));
        assertFalse(r.getNodoCandidato().equals("DC2"));
    }

    /** Z no llega a ningún data center: no suma beneficio y no cambia la recomendación. */
    public void testRecomendarDCSimpleIgnoraLosNodosSinDataCenter() {
        linea();
        acceso("Z", 1000);
        RecomendacionDC r = cdn.recomendarDCSimple();
        assertEquals("C", r.getNodoCandidato());
        assertEquals(2900.0, r.getBeneficio(), DELTA);
        assertEquals(500, r.getUsuariosBeneficiados());
    }

    public void testRecomendarDCSimpleSinCandidatos() {
        dataCenters("DC1", "DC2");
        enlace("DC1", "DC2", 3, 3);
        assertNull(cdn.recomendarDCSimple());
    }

    public void testRecomendarDCSimpleConLaRedVacia() {
        assertNull(cdn.recomendarDCSimple());
    }

    /** Con la asimetría: usar el nodo X como data center solo ayuda en el sentido X -> otros. */
    public void testRecomendarDCSimpleUsaLaLatenciaDelDataCenterAlNodo() {
        dataCenters("DC");
        acceso("P", 100);
        acceso("Q", 100);
        enlace("DC", "P", 10, 1);
        enlace("DC", "Q", 10, 1);
        enlace("P", "Q", 1, 20);
        // Hoy P y Q están a 10. Con P como data center: P 0 (mejora 10) y Q a 1 (mejora 9) = 1900.
        // Con Q como data center: Q 0 (mejora 10) y P a 20 (no mejora) = 1000.
        RecomendacionDC r = cdn.recomendarDCSimple();
        assertEquals("P", r.getNodoCandidato());
        assertEquals(1900.0, r.getBeneficio(), DELTA);
        assertEquals(200, r.getUsuariosBeneficiados());
    }

    // ---- centroParaDC ----

    /**
     * A -> B pesa 1 y B -> A pesa 10. Mirando la fila (de v hacia los demás) el centro es A (1 ms contra 10).
     * El centro de la biblioteca mira la columna y daría B.
     */
    public void testCentroParaDCUsaLaFila() {
        routers("A", "B");
        enlace("A", "B", 1, 10);
        assertEquals("A", cdn.centroParaDC());
        assertEquals("B", new DirectedGraphAlgorithms().obtenerCentroGrafo(red.grafoRuteo()));
    }

    public void testCentroParaDCEnUnaEstrellaSimetrica() {
        routers("C", "X", "Y", "Z");
        enlace("C", "X", 2, 2);
        enlace("C", "Y", 2, 2);
        enlace("C", "Z", 2, 2);
        assertEquals("C", cdn.centroParaDC());
    }

    public void testCentroParaDCConLaRedVacia() {
        assertNull(cdn.centroParaDC());
    }

    // ---- nodosQueSuperanLatencia ----

    /** Latencias: A 2, B 5, C 9; R es un router a 20 ms y Z un nodo sin data center. */
    private void redParaLatencias() {
        dataCenters("DC");
        acceso("A", 10);
        acceso("B", 10);
        acceso("C", 10);
        enlace("DC", "A", 2, 2);
        enlace("DC", "B", 5, 5);
        enlace("DC", "C", 9, 9);
    }

    public void testNodosQueSuperanLatenciaDeMayorAMenor() {
        redParaLatencias();
        assertEquals(List.of("C", "B", "A"), cdn.nodosQueSuperanLatencia(1));
        assertEquals(List.of("C", "B"), cdn.nodosQueSuperanLatencia(2.5));
    }

    /** B está a exactamente 5 ms: con máximo 5 no cuenta. */
    public void testLatenciaIgualAlMaximoNoCuenta() {
        redParaLatencias();
        assertEquals(List.of("C"), cdn.nodosQueSuperanLatencia(5));
        assertEquals(List.of("C", "B"), cdn.nodosQueSuperanLatencia(4.9999));
        assertTrue(cdn.nodosQueSuperanLatencia(9).isEmpty());
    }

    public void testLosNodosSinDataCenterSiempreSuperan() {
        redParaLatencias();
        acceso("Z", 10);
        assertEquals(List.of("Z", "C"), cdn.nodosQueSuperanLatencia(5));
        assertEquals(List.of("Z"), cdn.nodosQueSuperanLatencia(1000));
    }

    public void testSoloCuentanLosNodosDeAcceso() {
        redParaLatencias();
        routers("R");
        enlace("DC", "R", 20, 20);
        assertEquals(List.of("C"), cdn.nodosQueSuperanLatencia(5));
    }

    public void testLatenciaNegativaIncluyeATodosLosDeAcceso() {
        redParaLatencias();
        assertEquals(List.of("C", "B", "A"), cdn.nodosQueSuperanLatencia(-1));
    }

    // ---- datos de ejemplo ----

    private void cargarDatosDeEjemplo() throws IOException {
        red = CargadorRed.cargar(Path.of("src/main/resources/telecom/nodos.csv"),
                Path.of("src/main/resources/telecom/enlaces.csv"));
        cdn = new ServicioCDN(red);
    }

    public void testDatosDeEjemploTodosTienenDataCenter() throws IOException {
        cargarDatosDeEjemplo();
        AsignacionCDN a = cdn.asignarCDN();
        for (Nodo n : red.nodos()) {
            assertNotNull(n.getId(), a.dcDe(n.getId()));
            assertTrue(n.getId(), a.latenciaDe(n.getId()) < INFINITO);
        }
        for (Nodo dc : red.dataCenters()) {
            assertEquals(dc.getId(), a.dcDe(dc.getId()));
            assertEquals(0.0, a.latenciaDe(dc.getId()), DELTA);
        }
        assertEquals("DC2", a.dcDe("AP5"));
        assertEquals(71.0, a.latenciaDe("AP5"), DELTA);
    }

    /** La zona rural (AP6, AP5 y OF5) es la única que pasa de 20 ms de su data center. */
    public void testDatosDeEjemploLaZonaRuralSuperaLaLatencia() throws IOException {
        cargarDatosDeEjemplo();
        assertEquals(List.of("AP6", "AP5", "OF5"), cdn.nodosQueSuperanLatencia(20));
        assertEquals(List.of("AP6", "AP5"), cdn.nodosQueSuperanLatencia(66));
        assertTrue(cdn.nodosQueSuperanLatencia(101).isEmpty());
    }

    /** Un data center nuevo en AP5 baja la latencia de toda la zona rural: 6800 usuarios. */
    public void testDatosDeEjemploRecomiendaUnNodoDeLaZonaRural() throws IOException {
        cargarDatosDeEjemplo();
        RecomendacionDC r = cdn.recomendarDCSimple();
        assertEquals("AP5", r.getNodoCandidato());
        // AP5: 3500×71 + AP6: 2800×71 + OF5: 500×11 (de 66 a 55)
        assertEquals(452800.0, r.getBeneficio(), DELTA);
        assertEquals(6800, r.getUsuariosBeneficiados());
    }

    public void testDatosDeEjemploCentroParaDC() throws IOException {
        cargarDatosDeEjemplo();
        String centro = cdn.centroParaDC();
        assertNotNull(centro);
        assertNotNull(red.nodo(centro));
    }

    // ---- consulta 5 a través de ServicioConsultas ----

    public void testConsultaDeLatenciaDaLoMismoQueElServicioDeCDN() throws IOException {
        cargarDatosDeEjemplo();
        ServicioConsultas consultas = new ServicioConsultas(new ServicioRuteo(red), new ServicioResiliencia(red), cdn);
        for (double maximo : new double[]{0, 12, 20, 50, 66, 100, 500}) {
            assertEquals("máximo " + maximo, cdn.nodosQueSuperanLatencia(maximo), consultas.nodosQueSuperanLatencia(maximo));
        }
    }
}
