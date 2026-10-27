package ucu.edu.aed.telecom.servicio;

import junit.framework.TestCase;
import ucu.edu.aed.telecom.benchmark.GeneradorRedes;
import ucu.edu.aed.telecom.modelo.CargadorRed;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;
import ucu.edu.aed.telecom.resultado.RecomendacionDC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

/**
 * recomendarDC (versión optimizada del Desafío 3): los mismos casos que la simple y, sobre todo, que las dos
 * versiones den exactamente lo mismo en muchas redes distintas.
 */
public class RecomendarDCTest extends TestCase {

    private static final double DELTA = 0.0001;

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

    /** Las dos versiones tienen que dar el mismo candidato, el mismo beneficio y los mismos usuarios. */
    private static void assertMismaRecomendacion(String mensaje, RecomendacionDC simple, RecomendacionDC optimizada) {
        if (simple == null) {
            assertNull(mensaje, optimizada);
            return;
        }
        assertNotNull(mensaje, optimizada);
        assertEquals(mensaje, simple.getNodoCandidato(), optimizada.getNodoCandidato());
        assertEquals(mensaje, simple.getBeneficio(), optimizada.getBeneficio(), DELTA);
        assertEquals(mensaje, simple.getUsuariosBeneficiados(), optimizada.getUsuariosBeneficiados());
    }

    private void assertMismaRecomendacion() {
        assertMismaRecomendacion("", cdn.recomendarDCSimple(), cdn.recomendarDC());
    }

    // ---- los mismos casos que la simple ----

    /**
     * Línea DC - A - B - C (2, 3, 4 ms; 100, 200 y 300 usuarios). Hoy las latencias son A 2, B 5 y C 9.
     * Candidato A: 2×100 + 2×200 + 2×300 = 1200. Candidato B: 5×200 + 5×300 = 2500 (A no mejora).
     * Candidato C: 9×300 + 1×200 = 2900. Gana C con 500 usuarios beneficiados.
     */
    public void testLineaAMano() {
        dataCenters("DC");
        acceso("A", 100);
        acceso("B", 200);
        acceso("C", 300);
        enlace("DC", "A", 2, 2);
        enlace("A", "B", 3, 3);
        enlace("B", "C", 4, 4);
        RecomendacionDC r = cdn.recomendarDC();
        assertEquals("C", r.getNodoCandidato());
        assertEquals(2900.0, r.getBeneficio(), DELTA);
        assertEquals(500, r.getUsuariosBeneficiados());
        assertMismaRecomendacion();
    }

    /** Usa la latencia del candidato hacia los nodos (su fila), no la de los nodos hacia él. */
    public void testUsaLaFilaDelCandidato() {
        dataCenters("DC");
        acceso("P", 100);
        acceso("Q", 100);
        enlace("DC", "P", 10, 1);
        enlace("DC", "Q", 10, 1);
        enlace("P", "Q", 1, 20);
        // con P: P 0 (mejora 10) y Q a 1 (mejora 9) = 1900; con Q: Q 0 (mejora 10) y P a 20 (no mejora) = 1000
        RecomendacionDC r = cdn.recomendarDC();
        assertEquals("P", r.getNodoCandidato());
        assertEquals(1900.0, r.getBeneficio(), DELTA);
        assertEquals(200, r.getUsuariosBeneficiados());
        assertMismaRecomendacion();
    }

    /** Un router no tiene usuarios pero puede ser el mejor lugar: está en el medio de todos. */
    public void testUnRouterPuedeSerElMejorCandidato() {
        dataCenters("DC");
        routers("R");
        acceso("A", 100);
        acceso("B", 100);
        enlace("DC", "R", 20, 20);
        enlace("R", "A", 1, 1);
        enlace("R", "B", 1, 1);
        // con R: A y B pasan de 21 a 1 = 2000 + 2000; con A: A 21 y B 19 = 2100 + 1900 = 4000 → empate, gana R (va antes)
        RecomendacionDC r = cdn.recomendarDC();
        assertEquals("R", r.getNodoCandidato());
        assertEquals(4000.0, r.getBeneficio(), DELTA);
        assertMismaRecomendacion();
    }

    /** Z no llega a ningún data center: no suma beneficio y no cambia la recomendación. */
    public void testIgnoraLosNodosSinDataCenter() {
        dataCenters("DC");
        acceso("A", 100);
        acceso("Z", 1000);
        routers("R");
        enlace("DC", "R", 5, 5);
        enlace("R", "A", 5, 5);
        RecomendacionDC r = cdn.recomendarDC();
        assertEquals("A", r.getNodoCandidato());
        assertEquals(1000.0, r.getBeneficio(), DELTA);
        assertEquals(100, r.getUsuariosBeneficiados());
        assertMismaRecomendacion();
    }

    public void testNuncaRecomiendaUnDataCenterExistente() {
        dataCenters("DC1", "DC2");
        acceso("A", 10);
        enlace("DC1", "A", 5, 5);
        enlace("DC2", "A", 7, 7);
        assertEquals("A", cdn.recomendarDC().getNodoCandidato());
        assertMismaRecomendacion();
    }

    public void testSinCandidatos() {
        dataCenters("DC1", "DC2");
        enlace("DC1", "DC2", 3, 3);
        assertNull(cdn.recomendarDC());
        assertMismaRecomendacion();
    }

    public void testRedVacia() {
        assertNull(cdn.recomendarDC());
        assertMismaRecomendacion();
    }

    /** Sin data centers nadie suma: los dos dan el primer candidato de la red con beneficio 0. */
    public void testSinDataCenters() {
        acceso("B", 10);
        acceso("A", 10);
        enlace("A", "B", 1, 1);
        RecomendacionDC r = cdn.recomendarDC();
        assertEquals("B", r.getNodoCandidato());
        assertEquals(0.0, r.getBeneficio(), DELTA);
        assertEquals(0, r.getUsuariosBeneficiados());
        assertMismaRecomendacion();
    }

    /**
     * Empate: B y A están igual de lejos del DC y tienen los mismos usuarios, así que dan el mismo beneficio.
     * Gana el primero en el orden de la red (B, que se agregó antes), no el primero por nombre. Este caso
     * faltaba también para la versión simple.
     */
    public void testEmpateGanaElPrimeroDeLaRed() {
        dataCenters("DC");
        acceso("B", 50);
        acceso("A", 50);
        enlace("DC", "B", 4, 4);
        enlace("DC", "A", 4, 4);
        assertEquals("B", cdn.recomendarDCSimple().getNodoCandidato());
        assertEquals("B", cdn.recomendarDC().getNodoCandidato());
        assertMismaRecomendacion();
    }

    /** Un nodo que ya está pegado a su DC no mejora: la mejora tiene que ser estricta para contar usuarios. */
    public void testSinMejoraNoCuentanUsuarios() {
        dataCenters("DC");
        acceso("A", 100);
        acceso("B", 1000);
        enlace("DC", "A", 1, 1);
        enlace("A", "B", 1, 1);
        // con A: A pasa de 1 a 0 y B de 2 a 1 = 100 + 1000 = 1100
        // con B: B pasa de 2 a 0 = 2000; A queda a 1 desde B, igual que hoy: no mejora y sus 100 usuarios no cuentan
        RecomendacionDC r = cdn.recomendarDC();
        assertEquals("B", r.getNodoCandidato());
        assertEquals(2000.0, r.getBeneficio(), DELTA);
        assertEquals(1000, r.getUsuariosBeneficiados());
        assertMismaRecomendacion();
    }

    public void testNoModificaLaRed() {
        dataCenters("DC");
        acceso("A", 100);
        enlace("DC", "A", 1, 1);
        cdn.recomendarDC();
        assertEquals(1, red.dataCenters().size());
        assertEquals(2, red.nodos().size());
    }

    // ---- equivalencia con la simple ----

    public void testDatosDeEjemploIgualQueLaSimple() throws IOException {
        RedTelecom datos = CargadorRed.cargar(Path.of("src/main/resources/telecom/nodos.csv"), Path.of("src/main/resources/telecom/enlaces.csv"));
        ServicioCDN s = new ServicioCDN(datos);
        RecomendacionDC optimizada = s.recomendarDC();
        assertEquals("AP5", optimizada.getNodoCandidato());
        assertEquals(6800, optimizada.getUsuariosBeneficiados());
        assertMismaRecomendacion("datos de ejemplo", s.recomendarDCSimple(), optimizada);
    }

    /** Redes del generador (siempre conexas): árboles y mallas, con 1 a 4 data centers. */
    public void testIgualQueLaSimpleEnRedesDelGenerador() {
        Random random = new Random(2026);
        for (int prueba = 0; prueba < 40; prueba++) {
            int nodos = 2 + random.nextInt(25);
            int maximoExtra = nodos * (nodos - 1) / 2 - (nodos - 1);
            int extra = random.nextInt(Math.min(maximoExtra, 2 * nodos) + 1);
            int dataCenters = 1 + random.nextInt(Math.min(4, nodos));
            ServicioCDN s = new ServicioCDN(GeneradorRedes.generar(prueba, nodos, extra, dataCenters));
            String mensaje = "prueba " + prueba + " (" + nodos + " nodos, " + extra + " extra, " + dataCenters + " DCs)";
            assertMismaRecomendacion(mensaje, s.recomendarDCSimple(), s.recomendarDC());
        }
    }

    /**
     * Redes armadas a mano al azar, que pueden no ser conexas (nodos que no llegan a ningún DC), con latencias
     * asimétricas y a veces sin data centers.
     */
    public void testIgualQueLaSimpleEnRedesNoConexas() {
        Random random = new Random(99);
        for (int prueba = 0; prueba < 40; prueba++) {
            setUp();
            int n = 1 + random.nextInt(12);
            for (int i = 0; i < n; i++) {
                double tipo = random.nextDouble();
                if (tipo < 0.15) dataCenters("v" + i);
                else if (tipo < 0.45) routers("v" + i);
                else acceso("v" + i, 1 + random.nextInt(500));
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (random.nextDouble() < 0.25) enlace("v" + i, "v" + j, 1 + random.nextInt(15), 1 + random.nextInt(15));
                }
            }
            assertMismaRecomendacion("prueba " + prueba + ": " + red.enlaces(), cdn.recomendarDCSimple(), cdn.recomendarDC());
        }
    }
}
