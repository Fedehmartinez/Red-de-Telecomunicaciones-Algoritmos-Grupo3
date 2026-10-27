package ucu.edu.aed.telecom.benchmark;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GeneradorRedesTest extends TestCase {

    private static UndirectedGraph<String, WeightedEdge> grafo(RedTelecom red) {
        return red.grafoResiliencia(Set.of(), Set.of());
    }

    public void testCantidadDeNodosYEnlaces() {
        RedTelecom red = GeneradorRedes.generar(1, 30, 45, 3);
        assertEquals(30, red.nodos().size());
        assertEquals(29 + 45, red.enlaces().size());
        assertEquals(3, red.dataCenters().size());
    }

    public void testSiempreEsConexa() {
        for (int semilla = 0; semilla < 20; semilla++) {
            assertTrue("semilla " + semilla, grafo(GeneradorRedes.generar(semilla, 25, semilla, 2)).esConexo());
        }
    }

    /** Sin enlaces extra es un árbol: conexo, V - 1 enlaces y sin ciclos. */
    public void testSinExtraEsUnArbol() {
        UndirectedGraph<String, WeightedEdge> g = grafo(GeneradorRedes.generar(5, 40, 0, 2));
        assertEquals(39, g.cantidadDeAristas());
        assertTrue(g.esConexo());
        assertFalse(g.tieneCiclos());
    }

    public void testConExtraTieneCiclos() {
        assertTrue(grafo(GeneradorRedes.generar(5, 40, 10, 2)).tieneCiclos());
    }

    /** Con el máximo de extra queda la red completa: un enlace entre cada par. */
    public void testMaximoDeExtraEsLaRedCompleta() {
        RedTelecom red = GeneradorRedes.generar(3, 6, 15 - 5, 1);
        assertEquals(15, red.enlaces().size());
    }

    public void testMismaSemillaMismaRed() {
        RedTelecom r1 = GeneradorRedes.generar(42, 20, 15, 2);
        RedTelecom r2 = GeneradorRedes.generar(42, 20, 15, 2);
        assertEquals(descripcion(r1), descripcion(r2));
    }

    public void testDistintaSemillaOtraRed() {
        assertFalse(descripcion(GeneradorRedes.generar(1, 20, 15, 2)).equals(descripcion(GeneradorRedes.generar(2, 20, 15, 2))));
    }

    /** Todo lo que define a la red, para comparar dos redes. */
    private static List<String> descripcion(RedTelecom red) {
        List<String> d = new ArrayList<>();
        for (Nodo n : red.nodos()) d.add(n.getId() + " " + n.getTipo() + " " + n.getUsuarios());
        for (Enlace e : red.enlaces()) d.add(e.getA() + "-" + e.getB() + " " + e.getLatenciaAB() + " " + e.getLatenciaBA() + " " + e.getCosto());
        return d;
    }

    public void testHayNodosDeAccesoConUsuariosYRouters() {
        RedTelecom red = GeneradorRedes.generar(7, 50, 20, 3);
        int acceso = 0;
        int routers = 0;
        for (Nodo n : red.nodos()) {
            if (n.getTipo().esDeAcceso()) {
                acceso++;
                assertTrue(n.getUsuarios() > 0);
            } else {
                assertEquals(0, n.getUsuarios());
                if (!n.esDataCenter()) routers++;
            }
        }
        assertTrue(acceso > 0);
        assertTrue(routers > 0);
    }

    /** Algunos enlaces tienen distinta latencia en cada sentido, pero no todos. */
    public void testHayEnlacesAsimetricosYSimetricos() {
        int asimetricos = 0;
        int total = 0;
        for (Iterator<Enlace> it = GeneradorRedes.generar(9, 60, 60, 2).enlaces().iterator(); it.hasNext(); total++) {
            Enlace e = it.next();
            if (e.getLatenciaAB() != e.getLatenciaBA()) asimetricos++;
        }
        assertTrue(asimetricos > 0);
        assertTrue(asimetricos < total);
    }

    public void testUnSoloNodo() {
        RedTelecom red = GeneradorRedes.generar(1, 1, 0, 1);
        assertEquals(1, red.nodos().size());
        assertTrue(red.enlaces().isEmpty());
    }

    public void testSinDataCenters() {
        assertTrue(GeneradorRedes.generar(1, 10, 3, 0).dataCenters().isEmpty());
    }

    public void testParametrosInvalidos() {
        assertInvalido(0, 0, 0);     // sin nodos
        assertInvalido(5, -1, 1);    // extra negativo
        assertInvalido(5, 7, 1);     // con 5 nodos entran 10 enlaces: 4 del árbol + 6 extra
        assertInvalido(5, 0, 6);     // más data centers que nodos
        assertInvalido(5, 0, -1);
    }

    private static void assertInvalido(int nodos, int extra, int dataCenters) {
        try {
            GeneradorRedes.generar(1, nodos, extra, dataCenters);
            fail("Tendría que lanzar IllegalArgumentException: " + nodos + ", " + extra + ", " + dataCenters);
        } catch (IllegalArgumentException e) {
            // esperado
        }
    }
}
