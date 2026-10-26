package ucu.edu.aed.telecom.modelo;

import junit.framework.TestCase;

public class EnlaceTest extends TestCase {

    private static final double DELTA = 0.0001;

    private void assertInvalido(String a, String b, TipoEnlace tipo, double latAB, double latBA, double ancho, double costo) {
        try {
            new Enlace(a, b, tipo, latAB, latBA, ancho, costo);
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testCrearEnlace() {
        Enlace e = new Enlace("R1", "R2", TipoEnlace.FIBRA, 2, 3, 10000, 50);
        assertEquals("R1", e.getA());
        assertEquals("R2", e.getB());
        assertEquals(TipoEnlace.FIBRA, e.getTipo());
        assertEquals(2.0, e.getLatenciaAB(), DELTA);
        assertEquals(3.0, e.getLatenciaBA(), DELTA);
        assertEquals(10000.0, e.getAnchoBanda(), DELTA);
        assertEquals(50.0, e.getCosto(), DELTA);
    }

    /** La latencia depende del sentido: de R1 a R2 es 2 y de R2 a R1 es 3. */
    public void testLatenciaDesdeCadaExtremo() {
        Enlace e = new Enlace("R1", "R2", TipoEnlace.RADIOENLACE, 2, 3, 100, 5);
        assertEquals(2.0, e.latenciaDesde("R1"), DELTA);
        assertEquals(3.0, e.latenciaDesde("R2"), DELTA);
    }

    public void testLatenciaDesdeUnNodoQueNoEsExtremo() {
        Enlace e = new Enlace("R1", "R2", TipoEnlace.FIBRA, 2, 2, 1000, 5);
        try {
            e.latenciaDesde("R3");
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testOtroExtremo() {
        Enlace e = new Enlace("R1", "R2", TipoEnlace.FIBRA, 2, 2, 1000, 5);
        assertEquals("R2", e.otroExtremo("R1"));
        assertEquals("R1", e.otroExtremo("R2"));
        try {
            e.otroExtremo("R3");
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testExtremosNulosOIguales() {
        assertInvalido(null, "R2", TipoEnlace.FIBRA, 1, 1, 1000, 1);
        assertInvalido("R1", null, TipoEnlace.FIBRA, 1, 1, 1000, 1);
        assertInvalido("R1", "R1", TipoEnlace.FIBRA, 1, 1, 1000, 1);
    }

    public void testTipoNulo() {
        assertInvalido("R1", "R2", null, 1, 1, 1000, 1);
    }

    public void testLatenciasNoPositivas() {
        assertInvalido("R1", "R2", TipoEnlace.FIBRA, 0, 1, 1000, 1);
        assertInvalido("R1", "R2", TipoEnlace.FIBRA, 1, -2, 1000, 1);
        assertInvalido("R1", "R2", TipoEnlace.FIBRA, Double.NaN, 1, 1000, 1);
        assertInvalido("R1", "R2", TipoEnlace.FIBRA, 1, Double.POSITIVE_INFINITY, 1000, 1);
    }

    public void testAnchoDeBandaFueraDelRangoDelTipo() {
        assertInvalido("R1", "R2", TipoEnlace.FIBRA, 1, 1, 99, 1);
        assertInvalido("R1", "R2", TipoEnlace.FIBRA, 1, 1, 100001, 1);
        assertInvalido("R1", "R2", TipoEnlace.COBRE, 1, 1, 2000, 1);
        assertInvalido("R1", "R2", TipoEnlace.RADIOENLACE, 1, 1, 5, 1);
        assertInvalido("R1", "R2", TipoEnlace.RADIOENLACE, 1, 1, Double.NaN, 1);
    }

    /** Los bordes del rango de cada tipo se aceptan. */
    public void testAnchoDeBandaEnLosBordes() {
        for (TipoEnlace tipo : TipoEnlace.values()) {
            assertTrue(tipo.admite(tipo.getAnchoBandaMinimo()));
            assertTrue(tipo.admite(tipo.getAnchoBandaMaximo()));
            new Enlace("R1", "R2", tipo, 1, 1, tipo.getAnchoBandaMinimo(), 1);
            new Enlace("R1", "R2", tipo, 1, 1, tipo.getAnchoBandaMaximo(), 1);
        }
    }

    public void testCosto() {
        assertEquals(0.0, new Enlace("R1", "R2", TipoEnlace.COBRE, 1, 1, 100, 0).getCosto(), DELTA);
        assertInvalido("R1", "R2", TipoEnlace.COBRE, 1, 1, 100, -1);
        assertInvalido("R1", "R2", TipoEnlace.COBRE, 1, 1, 100, Double.NaN);
    }

    /** Hay un solo enlace por par: R1-R2 y R2-R1 son el mismo aunque tengan otros datos. */
    public void testIgualesSinImportarElOrden() {
        Enlace ida = new Enlace("R1", "R2", TipoEnlace.FIBRA, 1, 2, 1000, 10);
        Enlace vuelta = new Enlace("R2", "R1", TipoEnlace.COBRE, 7, 7, 100, 3);
        assertEquals(ida, vuelta);
        assertEquals(ida.hashCode(), vuelta.hashCode());
        assertFalse(ida.equals(new Enlace("R1", "R3", TipoEnlace.FIBRA, 1, 2, 1000, 10)));
    }
}
