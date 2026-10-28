package ucu.edu.aed.telecom.modelo;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class RedTelecomTest extends TestCase {

    private RedTelecom red;

    @Override
    protected void setUp() {
        red = new RedTelecom();
    }

    private void routers(String... ids) {
        for (String id : ids) red.agregarNodo(new Nodo(id, TipoNodo.ROUTER, 10, 0));
    }

    private boolean fibra(String a, String b) {
        return red.agregarEnlace(new Enlace(a, b, TipoEnlace.FIBRA, 1, 1, 1000, 10));
    }

    private static List<String> ids(Iterable<Nodo> nodos) {
        List<String> ids = new ArrayList<>();
        for (Nodo n : nodos) ids.add(n.getId());
        return ids;
    }

    public void testRedVacia() {
        assertTrue(red.nodos().isEmpty());
        assertTrue(red.enlaces().isEmpty());
        assertTrue(red.dataCenters().isEmpty());
        assertNull(red.nodo("R1"));
        assertNull(red.enlace("R1", "R2"));
    }

    public void testAgregarYBuscarNodo() {
        Nodo r1 = new Nodo("R1", TipoNodo.ROUTER, 10, 0);
        assertTrue(red.agregarNodo(r1));
        assertSame(r1, red.nodo("R1"));
        assertNull(red.nodo("R2"));
    }

    /** Otro nodo con el mismo id no se agrega y se conserva el primero. */
    public void testNodoRepetidoNoSeAgrega() {
        Nodo primero = new Nodo("X", TipoNodo.ROUTER, 10, 0);
        red.agregarNodo(primero);
        assertFalse(red.agregarNodo(new Nodo("X", TipoNodo.SWITCH, 99, 0)));
        assertEquals(1, red.nodos().size());
        assertSame(primero, red.nodo("X"));
    }

    public void testNodoNulo() {
        try {
            red.agregarNodo(null);
            fail("Debía lanzar NullPointerException");
        } catch (NullPointerException esperada) {
            // ok
        }
    }

    /** El enlace se encuentra en los dos órdenes. */
    public void testAgregarYBuscarEnlace() {
        routers("R1", "R2");
        Enlace e = new Enlace("R1", "R2", TipoEnlace.FIBRA, 1, 2, 1000, 10);
        assertTrue(red.agregarEnlace(e));
        assertSame(e, red.enlace("R1", "R2"));
        assertSame(e, red.enlace("R2", "R1"));
    }

    public void testEnlaceConExtremoInexistente() {
        routers("R1");
        assertFalse(fibra("R1", "R9"));
        assertFalse(fibra("R9", "R1"));
        assertTrue(red.enlaces().isEmpty());
    }

    public void testEnlaceRepetidoEnCualquierOrden() {
        routers("R1", "R2");
        assertTrue(fibra("R1", "R2"));
        assertFalse(fibra("R1", "R2"));
        assertFalse(fibra("R2", "R1"));
        assertEquals(1, red.enlaces().size());
    }

    public void testEnlaceQueNoExiste() {
        routers("R1", "R2", "R3");
        fibra("R1", "R2");
        assertNull(red.enlace("R1", "R3"));
        assertNull(red.enlace("R1", "R9"));
        assertNull(red.enlace(null, "R1"));
    }

    /** Ids que se ordenan distinto como texto que como número: la clave del par igual tiene que coincidir. */
    public void testParesConIdsParecidos() {
        routers("R1", "R10", "R2");
        fibra("R10", "R2");
        fibra("R1", "R2");
        assertEquals("R10", red.enlace("R2", "R10").getA());
        assertEquals("R1", red.enlace("R2", "R1").getA());
        assertNull(red.enlace("R1", "R10"));
    }

    public void testNodosYEnlacesEnOrdenDeInsercion() {
        routers("C", "A", "B");
        fibra("C", "A");
        fibra("B", "A");
        assertEquals(List.of("C", "A", "B"), ids(red.nodos()));
        List<String> extremos = new ArrayList<>();
        for (Enlace e : red.enlaces()) extremos.add(e.getA() + e.getB());
        assertEquals(List.of("CA", "BA"), extremos);
    }

    public void testDataCentersEnOrden() {
        red.agregarNodo(new Nodo("DC2", TipoNodo.DATA_CENTER, 100, 0));
        red.agregarNodo(new Nodo("R1", TipoNodo.ROUTER, 10, 0));
        red.agregarNodo(new Nodo("AP1", TipoNodo.PUNTO_ACCESO, 100, 50));
        red.agregarNodo(new Nodo("DC1", TipoNodo.DATA_CENTER, 100, 0));
        assertEquals(List.of("DC2", "DC1"), ids(red.dataCenters()));
    }
}
