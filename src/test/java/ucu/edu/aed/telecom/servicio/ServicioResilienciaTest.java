package ucu.edu.aed.telecom.servicio;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.telecom.modelo.CargadorRed;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;
import ucu.edu.aed.telecom.resultado.ImpactoFalla;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ServicioResilienciaTest extends TestCase {

    private RedTelecom red;
    private ServicioResiliencia resiliencia;

    @Override
    protected void setUp() {
        red = new RedTelecom();
        resiliencia = new ServicioResiliencia(red);
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

    private Enlace enlace(String a, String b) {
        Enlace e = new Enlace(a, b, TipoEnlace.FIBRA, 1, 1, 1000, 1);
        assertTrue(red.agregarEnlace(e));
        return e;
    }

    /** Para comparar conjuntos de enlaces escribiéndolos como "a-b". */
    private static Set<String> nombres(Set<Enlace> enlaces) {
        Set<String> nombres = new HashSet<>();
        for (Enlace e : enlaces) nombres.add(e.getA() + "-" + e.getB());
        return nombres;
    }

    /**
     * DC - R1 - R2 en un triángulo con R3 (DC-R1, R1-R2, R2-R3, R3-R1), y de R2 cuelga una rama
     * R2 - R4 - A (100 usuarios) con B (50 usuarios) colgando de R4.
     *        DC
     *        |
     *        R1 --- R3
     *         \    /
     *           R2
     *           |
     *           R4 --- B
     *           |
     *           A
     * Críticos: R1 (separa al DC), R2 y R4. Puentes: DC-R1, R2-R4, R4-A y R4-B.
     */
    private void redConRama() {
        dataCenters("DC");
        routers("R1", "R2", "R3", "R4");
        acceso("A", 100);
        acceso("B", 50);
        enlace("DC", "R1");
        enlace("R1", "R2");
        enlace("R2", "R3");
        enlace("R3", "R1");
        enlace("R2", "R4");
        enlace("R4", "A");
        enlace("R4", "B");
    }

    // ---- nodosCriticos ----

    public void testCriticosRedVacia() {
        assertTrue(resiliencia.nodosCriticos().isEmpty());
    }

    public void testCriticosUnSoloNodo() {
        dataCenters("DC");
        assertTrue(resiliencia.nodosCriticos().isEmpty());
    }

    public void testCriticosEnUnaLinea() {
        routers("A", "B", "C", "D");
        enlace("A", "B");
        enlace("B", "C");
        enlace("C", "D");
        assertEquals(Set.of("B", "C"), resiliencia.nodosCriticos());
    }

    public void testCriticosEnUnCicloNoHay() {
        routers("A", "B", "C", "D");
        enlace("A", "B");
        enlace("B", "C");
        enlace("C", "D");
        enlace("D", "A");
        assertTrue(resiliencia.nodosCriticos().isEmpty());
    }

    public void testCriticosRedConRama() {
        redConRama();
        assertEquals(Set.of("R1", "R2", "R4"), resiliencia.nodosCriticos());
    }

    /** Se devuelven en el orden de la red y no en el que los encuentra la búsqueda. */
    public void testCriticosEnElOrdenDeLaRed() {
        redConRama();
        assertEquals(List.of("R1", "R2", "R4"), new ArrayList<>(resiliencia.nodosCriticos()));
    }

    /** Red no conexa: cuenta los puntos de articulación de todas las partes. */
    public void testCriticosEnUnaRedNoConexa() {
        routers("A", "B", "C", "X", "Y", "Z");
        enlace("A", "B");
        enlace("B", "C");
        enlace("X", "Y");
        enlace("Y", "Z");
        assertEquals(Set.of("B", "Y"), resiliencia.nodosCriticos());
    }

    // ---- enlacesCriticos ----

    public void testPuentesRedVacia() {
        assertTrue(resiliencia.enlacesCriticos().isEmpty());
    }

    /** Dos nodos y un enlace: cortarlo los separa. */
    public void testPuenteConDosNodos() {
        routers("A", "B");
        enlace("A", "B");
        assertEquals(Set.of("A-B"), nombres(resiliencia.enlacesCriticos()));
    }

    public void testPuentesEnUnCicloNoHay() {
        routers("A", "B", "C");
        enlace("A", "B");
        enlace("B", "C");
        enlace("C", "A");
        assertTrue(resiliencia.enlacesCriticos().isEmpty());
    }

    public void testPuentesRedConRama() {
        redConRama();
        assertEquals(Set.of("DC-R1", "R2-R4", "R4-A", "R4-B"), nombres(resiliencia.enlacesCriticos()));
    }

    /**
     * Dos ciclos unidos por un enlace: el enlace del medio es puente aunque sus dos extremos sean puntos de
     * articulación y los enlaces de los ciclos no (no alcanza con mirar si los extremos son críticos).
     */
    public void testPuenteEntreDosCiclos() {
        routers("A", "B", "C", "X", "Y", "Z");
        enlace("A", "B");
        enlace("B", "C");
        enlace("C", "A");
        enlace("X", "Y");
        enlace("Y", "Z");
        enlace("Z", "X");
        enlace("C", "X");
        assertEquals(Set.of("C-X"), nombres(resiliencia.enlacesCriticos()));
    }

    /** Dos nodos críticos unidos por un enlace que no es puente (hay otro camino entre ellos). */
    public void testExtremosCriticosNoAlcanzanParaSerPuente() {
        routers("A", "B", "C", "D", "E");
        enlace("A", "B");
        enlace("B", "C");
        enlace("C", "A");
        enlace("A", "D");
        enlace("B", "E");
        assertEquals(Set.of("A", "B"), resiliencia.nodosCriticos());
        assertEquals(Set.of("A-D", "B-E"), nombres(resiliencia.enlacesCriticos()));
    }

    /**
     * Contra la definición, en redes al azar con semilla fija: un enlace es puente si y solo si al sacarlo
     * aumenta la cantidad de componentes conexas.
     */
    public void testPuentesCoincidenConLaDefinicion() {
        Random random = new Random(42);
        for (int prueba = 0; prueba < 50; prueba++) {
            setUp();
            int n = 2 + random.nextInt(9);
            for (int i = 0; i < n; i++) routers("v" + i);
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (random.nextDouble() < 0.3) enlace("v" + i, "v" + j);
                }
            }

            Set<Enlace> esperados = new HashSet<>();
            int componentes = componentes(red.grafoResiliencia(Set.of(), Set.of()));
            for (Enlace e : red.enlaces()) {
                if (componentes(red.grafoResiliencia(Set.of(), Set.of(e))) > componentes) esperados.add(e);
            }
            assertEquals("prueba " + prueba + ": " + red.enlaces(), esperados, resiliencia.enlacesCriticos());
        }
    }

    /** Cantidad de componentes conexas: cuántas BEA hay que arrancar para visitar todo. */
    private static int componentes(UndirectedGraph<String, WeightedEdge> g) {
        DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();
        Set<String> visitados = new HashSet<>();
        int componentes = 0;
        for (String v : g.vertices()) {
            if (visitados.contains(v)) continue;
            componentes++;
            algoritmos.recorridoEnAmplitud(g, g.construirComparable(v), visitados::add);
        }
        return componentes;
    }

    // ---- incomunicados ----

    public void testSinFallasNadieIncomunicado() {
        redConRama();
        assertTrue(resiliencia.incomunicados(Set.of(), Set.of()).isEmpty());
    }

    public void testSinDataCentersTodosIncomunicados() {
        routers("A", "B");
        enlace("A", "B");
        assertEquals(Set.of("A", "B"), resiliencia.incomunicados(Set.of(), Set.of()));
    }

    /** Un nodo sin enlaces ya está incomunicado sin que se caiga nada. */
    public void testNodoAisladoEstaIncomunicado() {
        redConRama();
        acceso("Z", 10);
        assertEquals(Set.of("Z"), resiliencia.incomunicados(Set.of(), Set.of()));
    }

    /** El nodo caído no cuenta como incomunicado: no está andando. */
    public void testElCaidoNoCuenta() {
        redConRama();
        assertEquals(Set.of("A", "B"), resiliencia.incomunicados(Set.of("R4"), Set.of()));
    }

    public void testCaeUnNodoCritico() {
        redConRama();
        assertEquals(Set.of("R4", "A", "B"), resiliencia.incomunicados(Set.of("R2"), Set.of()));
    }

    public void testCaeUnNodoNoCritico() {
        redConRama();
        assertTrue(resiliencia.incomunicados(Set.of("R3"), Set.of()).isEmpty());
    }

    public void testCaeElUnicoDataCenter() {
        redConRama();
        assertEquals(Set.of("R1", "R2", "R3", "R4", "A", "B"), resiliencia.incomunicados(Set.of("DC"), Set.of()));
    }

    /** Si cae un data center pero hay otro en la misma parte de la red, nadie queda incomunicado. */
    public void testCaeUnDataCenterConRespaldo() {
        redConRama();
        dataCenters("DC2");
        enlace("DC2", "R3");
        assertTrue(resiliencia.incomunicados(Set.of("DC"), Set.of()).isEmpty());
    }

    /** Cada parte de la red que tiene su propio data center sigue andando aunque no se toquen entre sí. */
    public void testDosPartesConSuDataCenter() {
        dataCenters("DC1", "DC2");
        routers("A", "B", "C");
        enlace("DC1", "A");
        enlace("DC2", "B");
        assertEquals(Set.of("C"), resiliencia.incomunicados(Set.of(), Set.of()));
    }

    public void testEnlaceCaido() {
        redConRama();
        Enlace e = red.enlace("R2", "R4");
        assertEquals(Set.of("R4", "A", "B"), resiliencia.incomunicados(Set.of(), Set.of(e)));
    }

    /** Un enlace del ciclo no deja a nadie afuera. */
    public void testEnlaceCaidoQueNoEsPuente() {
        redConRama();
        assertTrue(resiliencia.incomunicados(Set.of(), Set.of(red.enlace("R1", "R2"))).isEmpty());
    }

    /** Dos fallas juntas: cada una sola no incomunica a R2, juntas sí. */
    public void testDosFallasJuntas() {
        redConRama();
        Set<Enlace> caidos = Set.of(red.enlace("R1", "R2"), red.enlace("R3", "R1"));
        assertEquals(Set.of("R2", "R3", "R4", "A", "B"), resiliencia.incomunicados(Set.of(), caidos));
    }

    public void testIdsCaidosQueNoSonDeLaRedSeIgnoran() {
        redConRama();
        assertTrue(resiliencia.incomunicados(Set.of("NO_EXISTE"), Set.of()).isEmpty());
    }

    public void testIncomunicadosEnElOrdenDeLaRed() {
        redConRama();
        assertEquals(List.of("R1", "R2", "R3", "R4", "A", "B"), new ArrayList<>(resiliencia.incomunicados(Set.of("DC"), Set.of())));
    }

    public void testIncomunicadosNoModificaLaRed() {
        redConRama();
        resiliencia.incomunicados(Set.of("R2"), Set.of(red.enlace("R4", "A")));
        assertEquals(7, red.nodos().size());
        assertEquals(7, red.enlaces().size());
        assertTrue(resiliencia.incomunicados(Set.of(), Set.of()).isEmpty());
    }

    // ---- impactoFallaNodo (consulta 2) ----

    public void testImpactoNodoCritico() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaNodo("R2");
        assertEquals("R2", impacto.getElementoCaido());
        assertEquals(Set.of("R4", "A", "B"), impacto.getNodosIncomunicados());
        assertEquals(150, impacto.getUsuariosAfectados());
    }

    public void testImpactoNodoNoCritico() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaNodo("R3");
        assertTrue(impacto.getNodosIncomunicados().isEmpty());
        assertEquals(0, impacto.getUsuariosAfectados());
    }

    /** Un nodo de acceso que se cae afecta a sus propios usuarios aunque no incomunique a nadie. */
    public void testImpactoNodoDeAccesoCuentaSusUsuarios() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaNodo("A");
        assertTrue(impacto.getNodosIncomunicados().isEmpty());
        assertEquals(100, impacto.getUsuariosAfectados());
    }

    public void testImpactoDelUnicoDataCenter() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaNodo("DC");
        assertEquals(Set.of("R1", "R2", "R3", "R4", "A", "B"), impacto.getNodosIncomunicados());
        assertEquals(150, impacto.getUsuariosAfectados());
    }

    /** Los que ya estaban incomunicados antes de la falla no cuentan: la falla no les cambia nada. */
    public void testImpactoNoCuentaLosQueYaEstabanIncomunicados() {
        redConRama();
        acceso("Z", 70);
        ImpactoFalla impacto = resiliencia.impactoFallaNodo("R2");
        assertEquals(Set.of("R4", "A", "B"), impacto.getNodosIncomunicados());
        assertEquals(150, impacto.getUsuariosAfectados());
    }

    /** Si se cae un nodo que ya estaba incomunicado, sus usuarios ya no tenían servicio: no cuentan. */
    public void testImpactoDeUnNodoQueYaEstabaIncomunicado() {
        redConRama();
        acceso("Z", 70);
        ImpactoFalla impacto = resiliencia.impactoFallaNodo("Z");
        assertTrue(impacto.getNodosIncomunicados().isEmpty());
        assertEquals(0, impacto.getUsuariosAfectados());
    }

    public void testImpactoNodoInexistente() {
        redConRama();
        try {
            resiliencia.impactoFallaNodo("NO_EXISTE");
            fail("Tendría que lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // esperado
        }
    }

    /**
     * Lo que usa la optimización del ranking: en redes al azar (semilla fija), si cae un nodo que no es punto de
     * articulación ni data center, nadie más queda incomunicado y solo se pierden sus propios usuarios.
     */
    public void testNodoQueNoEsCriticoNiDataCenterSoloAfectaASusUsuarios() {
        Random random = new Random(11);
        for (int prueba = 0; prueba < 30; prueba++) {
            setUp();
            int n = 2 + random.nextInt(10);
            for (int i = 0; i < n; i++) {
                double tipo = random.nextDouble();
                if (tipo < 0.2) dataCenters("v" + i);
                else if (tipo < 0.6) routers("v" + i);
                else acceso("v" + i, 1 + random.nextInt(100));
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (random.nextDouble() < 0.3) enlace("v" + i, "v" + j);
                }
            }

            Set<String> criticos = resiliencia.nodosCriticos();
            Set<String> antes = resiliencia.incomunicados(Set.of(), Set.of());
            for (Nodo nodo : red.nodos()) {
                if (criticos.contains(nodo.getId()) || nodo.esDataCenter()) continue;
                ImpactoFalla impacto = resiliencia.impactoFallaNodo(nodo.getId());
                String mensaje = "prueba " + prueba + ", cae " + nodo.getId() + ": " + red.enlaces();
                assertTrue(mensaje, impacto.getNodosIncomunicados().isEmpty());
                int propios = antes.contains(nodo.getId()) ? 0 : nodo.getUsuarios();
                assertEquals(mensaje, propios, impacto.getUsuariosAfectados());
            }
        }
    }

    // ---- impactoFallaEnlace ----

    public void testImpactoEnlacePuente() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaEnlace("R2", "R4");
        assertEquals("R2-R4", impacto.getElementoCaido());
        assertEquals(Set.of("R4", "A", "B"), impacto.getNodosIncomunicados());
        assertEquals(150, impacto.getUsuariosAfectados());
    }

    /** Los extremos se pueden pasar al revés; el nombre queda como está el enlace en la red. */
    public void testImpactoEnlaceConLosExtremosAlReves() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaEnlace("A", "R4");
        assertEquals("R4-A", impacto.getElementoCaido());
        assertEquals(Set.of("A"), impacto.getNodosIncomunicados());
        assertEquals(100, impacto.getUsuariosAfectados());
    }

    public void testImpactoEnlaceQueNoEsPuente() {
        redConRama();
        ImpactoFalla impacto = resiliencia.impactoFallaEnlace("R1", "R2");
        assertTrue(impacto.getNodosIncomunicados().isEmpty());
        assertEquals(0, impacto.getUsuariosAfectados());
    }

    public void testImpactoEnlaceInexistente() {
        redConRama();
        try {
            resiliencia.impactoFallaEnlace("DC", "A");
            fail("Tendría que lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // esperado
        }
    }

    /** Solo un puente puede incomunicar algo: para cada enlace, hay incomunicados solo si es puente. */
    public void testSoloLosPuentesIncomunican() {
        redConRama();
        Set<Enlace> puentes = resiliencia.enlacesCriticos();
        for (Enlace e : red.enlaces()) {
            boolean incomunica = !resiliencia.impactoFallaEnlace(e.getA(), e.getB()).getNodosIncomunicados().isEmpty();
            assertEquals(e.toString(), puentes.contains(e), incomunica);
        }
    }

    // ---- rankingCriticosSimple (consulta 3) ----

    public void testRankingRedVacia() {
        assertTrue(resiliencia.rankingCriticosSimple().isEmpty());
    }

    /** DC 150 (todos), R1 150, R2 150, R4 150, A 100, B 50; R3 no afecta a nadie y no aparece. */
    public void testRankingDeMayorAMenor() {
        redConRama();
        List<String> orden = new ArrayList<>();
        List<Integer> usuarios = new ArrayList<>();
        for (ImpactoFalla i : resiliencia.rankingCriticosSimple()) {
            orden.add(i.getElementoCaido());
            usuarios.add(i.getUsuariosAfectados());
        }
        // empates en 150: orden de la red (DC, R1, R2, R4)
        assertEquals(List.of("DC", "R1", "R2", "R4", "A", "B"), orden);
        assertEquals(List.of(150, 150, 150, 150, 100, 50), usuarios);
    }

    public void testRankingSinUsuariosQuedaVacio() {
        dataCenters("DC");
        routers("A", "B");
        enlace("DC", "A");
        enlace("A", "B");
        assertTrue(resiliencia.rankingCriticosSimple().isEmpty());
    }

    /** Cada entrada del ranking es lo mismo que la consulta 2 para ese nodo. */
    public void testRankingCoincideConImpactoFallaNodo() {
        redConRama();
        for (ImpactoFalla i : resiliencia.rankingCriticosSimple()) {
            ImpactoFalla directo = resiliencia.impactoFallaNodo(i.getElementoCaido());
            assertEquals(directo.getNodosIncomunicados(), i.getNodosIncomunicados());
            assertEquals(directo.getUsuariosAfectados(), i.getUsuariosAfectados());
        }
    }

    // ---- datos de ejemplo ----

    private static RedTelecom datosDeEjemplo() throws IOException {
        return CargadorRed.cargar(Path.of("src/main/resources/telecom/nodos.csv"), Path.of("src/main/resources/telecom/enlaces.csv"));
    }

    public void testDatosDeEjemploCriticosYPuentes() throws IOException {
        ServicioResiliencia r = new ServicioResiliencia(datosDeEjemplo());
        assertEquals(Set.of("R5", "RR1", "AP5"), r.nodosCriticos());
        assertEquals(Set.of("R5-RR1", "RR1-AP5", "RR1-OF5", "AP5-AP6"), nombres(r.enlacesCriticos()));
    }

    public void testDatosDeEjemploSinFallasNadieIncomunicado() throws IOException {
        assertTrue(new ServicioResiliencia(datosDeEjemplo()).incomunicados(Set.of(), Set.of()).isEmpty());
    }

    /** Si cae R5 se pierde toda la zona rural: AP5 3500 + AP6 2800 + OF5 500 = 6800 usuarios. */
    public void testDatosDeEjemploCaeR5() throws IOException {
        ImpactoFalla impacto = new ServicioResiliencia(datosDeEjemplo()).impactoFallaNodo("R5");
        assertEquals(Set.of("RR1", "AP5", "AP6", "OF5"), impacto.getNodosIncomunicados());
        assertEquals(6800, impacto.getUsuariosAfectados());
    }

    /** El núcleo es redundante: si cae un router de ahí o un data center, nadie se queda sin servicio. */
    public void testDatosDeEjemploElNucleoEsRedundante() throws IOException {
        ServicioResiliencia r = new ServicioResiliencia(datosDeEjemplo());
        for (String id : List.of("R1", "R2", "R3", "R4", "DC1", "DC2", "DC3", "SW1", "SW2", "SW3")) {
            assertEquals(id, 0, r.impactoFallaNodo(id).getUsuariosAfectados());
        }
    }

    public void testDatosDeEjemploCaenLosTresDataCenters() throws IOException {
        RedTelecom datos = datosDeEjemplo();
        Set<String> incomunicados = new ServicioResiliencia(datos).incomunicados(Set.of("DC1", "DC2", "DC3"), Set.of());
        assertEquals(datos.nodos().size() - 3, incomunicados.size());
    }

    public void testDatosDeEjemploCorteDelRadioenlaceRural() throws IOException {
        ImpactoFalla impacto = new ServicioResiliencia(datosDeEjemplo()).impactoFallaEnlace("RR1", "R5");
        assertEquals("R5-RR1", impacto.getElementoCaido());
        assertEquals(6800, impacto.getUsuariosAfectados());
    }

    /**
     * Ranking: R5 y RR1 6800 (empate, R5 va primero en la red), AP5 6300 (AP6 + los suyos), AP6 2800, AP2 1200,
     * AP4 900, AP1 800, AP3 600, OF5 500, OF2 400, OF3 250, OF1 150, OF4 120. Los demás no afectan a nadie.
     */
    public void testDatosDeEjemploRanking() throws IOException {
        List<String> orden = new ArrayList<>();
        List<Integer> usuarios = new ArrayList<>();
        for (ImpactoFalla i : new ServicioResiliencia(datosDeEjemplo()).rankingCriticosSimple()) {
            orden.add(i.getElementoCaido());
            usuarios.add(i.getUsuariosAfectados());
        }
        assertEquals(List.of("R5", "RR1", "AP5", "AP6", "AP2", "AP4", "AP1", "AP3", "OF5", "OF2", "OF3", "OF1", "OF4"), orden);
        assertEquals(List.of(6800, 6800, 6300, 2800, 1200, 900, 800, 600, 500, 400, 250, 150, 120), usuarios);
    }

    // ---- consultas 2 y 3 ----

    public void testConsultasDanLoMismoQueElServicioDeResiliencia() throws IOException {
        RedTelecom datos = datosDeEjemplo();
        ServicioResiliencia r = new ServicioResiliencia(datos);
        ServicioConsultas consultas = new ServicioConsultas(new ServicioRuteo(datos), r, new ServicioCDN(datos));

        ImpactoFalla directo = r.impactoFallaNodo("RR1");
        ImpactoFalla consulta = consultas.queOcurreSiCae("RR1");
        assertEquals(directo.getNodosIncomunicados(), consulta.getNodosIncomunicados());
        assertEquals(directo.getUsuariosAfectados(), consulta.getUsuariosAfectados());

        List<ImpactoFalla> rankingDirecto = r.rankingCriticosSimple();
        List<ImpactoFalla> rankingConsulta = consultas.rankingNodosCriticos();
        assertEquals(rankingDirecto.size(), rankingConsulta.size());
        for (int i = 0; i < rankingDirecto.size(); i++) {
            assertEquals(rankingDirecto.get(i).getElementoCaido(), rankingConsulta.get(i).getElementoCaido());
            assertEquals(rankingDirecto.get(i).getUsuariosAfectados(), rankingConsulta.get(i).getUsuariosAfectados());
        }
    }

    public void testConsultaQueOcurreSiCaeConIdInexistente() throws IOException {
        RedTelecom datos = datosDeEjemplo();
        ServicioConsultas consultas = new ServicioConsultas(new ServicioRuteo(datos), new ServicioResiliencia(datos), new ServicioCDN(datos));
        try {
            consultas.queOcurreSiCae("NO_EXISTE");
            fail("Tendría que lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // esperado
        }
    }
}
