package ucu.edu.aed.telecom.modelo;

import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CargadorRedTest extends TestCase {

    private static final double DELTA = 0.0001;

    private static final String ENCABEZADO_NODOS = "id,tipo,capacidad,usuarios\n";
    private static final String ENCABEZADO_ENLACES = "a,b,tipo,latenciaAB,latenciaBA,anchoBanda,costo\n";

    private Path archivoNodos;
    private Path archivoEnlaces;

    @Override
    protected void setUp() throws IOException {
        archivoNodos = Files.createTempFile("nodos", ".csv");
        archivoEnlaces = Files.createTempFile("enlaces", ".csv");
    }

    @Override
    protected void tearDown() throws IOException {
        Files.deleteIfExists(archivoNodos);
        Files.deleteIfExists(archivoEnlaces);
    }

    private RedTelecom cargar(String nodos, String enlaces) throws IOException {
        Files.writeString(archivoNodos, nodos);
        Files.writeString(archivoEnlaces, enlaces);
        return CargadorRed.cargar(archivoNodos, archivoEnlaces);
    }

    /** Verifica que la carga falle y que el mensaje diga el archivo y el número de línea. */
    private void assertFalla(String nodos, String enlaces, Path archivo, int linea) throws IOException {
        try {
            cargar(nodos, enlaces);
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            String mensaje = esperado.getMessage();
            assertTrue(mensaje, mensaje.contains(archivo.getFileName().toString()));
            assertTrue(mensaje, mensaje.contains(", línea " + linea + ":"));
        }
    }

    public void testCargaCorrectaConComentariosYLineasVacias() throws IOException {
        RedTelecom red = cargar(
                "# Nodos de prueba\n"
                        + ENCABEZADO_NODOS
                        + "\n"
                        + "# Núcleo\n"
                        + "R1,ROUTER,100,0\n"
                        + "\n"
                        + "DC1,DATA_CENTER,500,0\n"
                        + "AP1,PUNTO_ACCESO,300,250\n",
                "# Enlaces\n"
                        + ENCABEZADO_ENLACES
                        + "R1,DC1,FIBRA,2,3,10000,40\n"
                        + "\n"
                        + "# Acceso\n"
                        + "R1,AP1,COBRE,4,5,500,6.5\n");

        assertEquals(3, red.nodos().size());
        assertEquals(2, red.enlaces().size());
        assertEquals(TipoNodo.PUNTO_ACCESO, red.nodo("AP1").getTipo());
        assertEquals(300, red.nodo("AP1").getCapacidad());
        assertEquals(250, red.nodo("AP1").getUsuarios());
        assertEquals(1, red.dataCenters().size());

        Enlace e = red.enlace("R1", "DC1");
        assertEquals(TipoEnlace.FIBRA, e.getTipo());
        assertEquals(2.0, e.getLatenciaAB(), DELTA);
        assertEquals(3.0, e.getLatenciaBA(), DELTA);
        assertEquals(10000.0, e.getAnchoBanda(), DELTA);
        assertEquals(40.0, e.getCosto(), DELTA);
        assertEquals(6.5, red.enlace("AP1", "R1").getCosto(), DELTA);
    }

    public void testSoloEncabezado() throws IOException {
        RedTelecom red = cargar(ENCABEZADO_NODOS, ENCABEZADO_ENLACES);
        assertTrue(red.nodos().isEmpty());
        assertTrue(red.enlaces().isEmpty());
    }

    /** El encabezado es la primera línea que no es comentario, aunque no diga lo esperado. */
    public void testSalteaElEncabezadoSeaCualSea() throws IOException {
        RedTelecom red = cargar("cualquier cosa\nR1,ROUTER,100,0\n", "otra cosa\n");
        assertEquals(1, red.nodos().size());
    }

    public void testTipoDeNodoDesconocido() throws IOException {
        assertFalla("# comentario\n" + ENCABEZADO_NODOS + "R1,ROUTER,100,0\nX1,ANTENA,100,0\n",
                ENCABEZADO_ENLACES, archivoNodos, 4);
    }

    public void testTipoDeEnlaceDesconocido() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100,0\nR2,ROUTER,100,0\n",
                ENCABEZADO_ENLACES + "R1,R2,SATELITE,2,2,100,5\n", archivoEnlaces, 2);
    }

    public void testNumeroInvalidoEnNodo() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,cien,0\n", ENCABEZADO_ENLACES, archivoNodos, 2);
        assertFalla(ENCABEZADO_NODOS + "AP1,PUNTO_ACCESO,100,2.5\n", ENCABEZADO_ENLACES, archivoNodos, 2);
    }

    public void testNumeroInvalidoEnEnlace() throws IOException {
        String nodos = ENCABEZADO_NODOS + "R1,ROUTER,100,0\nR2,ROUTER,100,0\n";
        assertFalla(nodos, ENCABEZADO_ENLACES + "\nR1,R2,FIBRA,rapido,2,1000,5\n", archivoEnlaces, 3);
        assertFalla(nodos, ENCABEZADO_ENLACES + "R1,R2,FIBRA,2,2,1000,\n", archivoEnlaces, 2);
    }

    public void testCamposDeMenosEnNodo() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100\n", ENCABEZADO_ENLACES, archivoNodos, 2);
    }

    public void testCamposDeMasEnNodo() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100,0,extra\n", ENCABEZADO_ENLACES, archivoNodos, 2);
    }

    public void testCamposDeMenosEnEnlace() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100,0\nR2,ROUTER,100,0\n",
                ENCABEZADO_ENLACES + "# comentario\nR1,R2,FIBRA,2,2,1000\n", archivoEnlaces, 3);
    }

    public void testNodoRepetido() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100,0\nR2,ROUTER,100,0\nR1,SWITCH,50,0\n",
                ENCABEZADO_ENLACES, archivoNodos, 4);
    }

    public void testEnlaceConExtremoInexistente() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100,0\n",
                ENCABEZADO_ENLACES + "R1,R9,FIBRA,2,2,1000,5\n", archivoEnlaces, 2);
    }

    /** B,A después de A,B es el mismo par: es un enlace repetido. */
    public void testEnlaceRepetidoEscritoAlReves() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "A,ROUTER,100,0\nB,ROUTER,100,0\n",
                ENCABEZADO_ENLACES + "A,B,FIBRA,2,2,1000,5\nB,A,FIBRA,3,3,1000,5\n", archivoEnlaces, 3);
    }

    public void testAnchoDeBandaFueraDelRangoDelTipo() throws IOException {
        String nodos = ENCABEZADO_NODOS + "A,ROUTER,100,0\nB,ROUTER,100,0\n";
        assertFalla(nodos, ENCABEZADO_ENLACES + "A,B,COBRE,2,2,5000,5\n", archivoEnlaces, 2);
        assertFalla(nodos, ENCABEZADO_ENLACES + "A,B,FIBRA,2,2,50,5\n", archivoEnlaces, 2);
    }

    public void testNodoQueNoEsDeAccesoConUsuarios() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "R1,ROUTER,100,10\n", ENCABEZADO_ENLACES, archivoNodos, 2);
    }

    public void testUsuariosMayoresQueLaCapacidad() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "AP1,PUNTO_ACCESO,100,200\n", ENCABEZADO_ENLACES, archivoNodos, 2);
    }

    public void testLatenciaNoPositiva() throws IOException {
        String nodos = ENCABEZADO_NODOS + "A,ROUTER,100,0\nB,ROUTER,100,0\n";
        assertFalla(nodos, ENCABEZADO_ENLACES + "A,B,FIBRA,0,2,1000,5\n", archivoEnlaces, 2);
    }

    public void testEnlaceDeUnNodoConsigoMismo() throws IOException {
        assertFalla(ENCABEZADO_NODOS + "A,ROUTER,100,0\n", ENCABEZADO_ENLACES + "A,A,FIBRA,2,2,1000,5\n",
                archivoEnlaces, 2);
    }

    public void testEnlaceIdenticoRepetido() throws IOException {
        try {
            cargar(ENCABEZADO_NODOS + "A,ROUTER,100,0\nB,ROUTER,100,0\n",
                    ENCABEZADO_ENLACES + "A,B,FIBRA,2,2,1000,5\nA,B,FIBRA,2,2,1000,5\n");
            fail("debía lanzar excepción");
        } catch (IllegalArgumentException esperado) {
            assertTrue(esperado.getMessage(), esperado.getMessage().contains(", línea 3:"));
        }
    }
}
