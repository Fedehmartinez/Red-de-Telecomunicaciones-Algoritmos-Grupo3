package ucu.edu.aed.telecom.modelo;

import junit.framework.TestCase;

public class NodoTest extends TestCase {

    private void assertInvalido(String id, TipoNodo tipo, int capacidad, int usuarios) {
        try {
            new Nodo(id, tipo, capacidad, usuarios);
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testCrearNodoDeAcceso() {
        Nodo ap = new Nodo("AP1", TipoNodo.PUNTO_ACCESO, 500, 320);
        assertEquals("AP1", ap.getId());
        assertEquals(TipoNodo.PUNTO_ACCESO, ap.getTipo());
        assertEquals(500, ap.getCapacidad());
        assertEquals(320, ap.getUsuarios());
        assertFalse(ap.esDataCenter());
    }

    public void testDataCenter() {
        Nodo dc = new Nodo("DC1", TipoNodo.DATA_CENTER, 400, 0);
        assertTrue(dc.esDataCenter());
        assertEquals(0, dc.getUsuarios());
    }

    public void testSoloPuntoDeAccesoYOficinaSonDeAcceso() {
        for (TipoNodo tipo : TipoNodo.values()) {
            boolean esperado = tipo == TipoNodo.PUNTO_ACCESO || tipo == TipoNodo.OFICINA;
            assertEquals(tipo.toString(), esperado, tipo.esDeAcceso());
        }
    }

    public void testIdNuloOVacio() {
        assertInvalido(null, TipoNodo.ROUTER, 10, 0);
        assertInvalido("", TipoNodo.ROUTER, 10, 0);
        assertInvalido("   ", TipoNodo.ROUTER, 10, 0);
    }

    public void testTipoNulo() {
        assertInvalido("R1", null, 10, 0);
    }

    public void testCapacidadOUsuariosNegativos() {
        assertInvalido("R1", TipoNodo.ROUTER, -1, 0);
        assertInvalido("AP1", TipoNodo.PUNTO_ACCESO, 100, -5);
    }

    /** Un router no atiende usuarios directamente. */
    public void testUsuariosEnNodoQueNoEsDeAcceso() {
        assertInvalido("R1", TipoNodo.ROUTER, 10, 1);
        assertInvalido("DC1", TipoNodo.DATA_CENTER, 10, 100);
    }

    public void testUsuariosMayoresQueLaCapacidad() {
        assertInvalido("OF1", TipoNodo.OFICINA, 100, 101);
    }

    /** El borde: usuarios igual a la capacidad sí se permite. */
    public void testUsuariosIgualALaCapacidad() {
        assertEquals(100, new Nodo("OF1", TipoNodo.OFICINA, 100, 100).getUsuarios());
    }

    /** Fuera de los nodos de acceso la capacidad no limita usuarios (no tienen). */
    public void testCapacidadCeroEnRouter() {
        assertEquals(0, new Nodo("R1", TipoNodo.ROUTER, 0, 0).getCapacidad());
    }

    public void testIgualesPorId() {
        Nodo uno = new Nodo("X", TipoNodo.ROUTER, 10, 0);
        Nodo otro = new Nodo("X", TipoNodo.SWITCH, 99, 0);
        assertEquals(uno, otro);
        assertEquals(uno.hashCode(), otro.hashCode());
        assertFalse(uno.equals(new Nodo("Y", TipoNodo.ROUTER, 10, 0)));
    }
}
