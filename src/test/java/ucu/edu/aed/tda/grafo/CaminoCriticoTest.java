package ucu.edu.aed.tda.grafo;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.ResultadoCaminoCritico;
import ucu.edu.aed.tda.grafo.model.result.SecuenciaConHolgura;

import java.util.List;

public class CaminoCriticoTest extends TestCase {

    private DirectedGraph<String, WeightedEdge> grafo;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    @Override
    protected void setUp() {
        grafo = new DirectedGraph<>();
    }

    private void vertices(String... nombres) {
        for (String n : nombres) grafo.agregarVertice(n);
    }

    private void tarea(String origen, String destino, double duracion) {
        grafo.agregarArista(origen, destino, new WeightedEdge(duracion));
    }

    private SecuenciaConHolgura<String> buscar(ResultadoCaminoCritico<String> r, List<String> camino) {
        for (SecuenciaConHolgura<String> s : r.getSecuencias()) {
            if (s.getPath().equals(camino)) return s;
        }
        fail("No está la secuencia " + camino);
        return null;
    }

    /** A-3->B-4->D-1->E y A-2->C-6->D: crítico A,C,D,E = 9; la otra = 8, holgura 1. */
    public void testEjemploDeLaExplicacion() {
        vertices("A", "B", "C", "D", "E");
        tarea("A", "B", 3);
        tarea("A", "C", 2);
        tarea("B", "D", 4);
        tarea("C", "D", 6);
        tarea("D", "E", 1);

        ResultadoCaminoCritico<String> r = algoritmos.calcularCaminoCritico(grafo);

        assertEquals(9.0, r.getCostoCritico(), 0.0001);
        assertEquals(List.of("A", "C", "D", "E"), r.getCaminoCritico().getPath());
        assertEquals(2, r.getSecuencias().size());
        assertEquals(0.0, buscar(r, List.of("A", "C", "D", "E")).getHolgura(), 0.0001);
        SecuenciaConHolgura<String> otra = buscar(r, List.of("A", "B", "D", "E"));
        assertEquals(8.0, otra.getCost(), 0.0001);
        assertEquals(1.0, otra.getHolgura(), 0.0001);
    }

    public void testCadenaTodoEsCritico() {
        vertices("A", "B", "C");
        tarea("A", "B", 5);
        tarea("B", "C", 7);

        ResultadoCaminoCritico<String> r = algoritmos.calcularCaminoCritico(grafo);

        assertEquals(12.0, r.getCostoCritico(), 0.0001);
        assertEquals(List.of("A", "B", "C"), r.getCaminoCritico().getPath());
        assertEquals(1, r.getSecuencias().size());
    }

    public void testVariosInicioYFin() {
        vertices("I1", "I2", "M", "F1", "F2");
        tarea("I1", "M", 4);
        tarea("I2", "M", 1);
        tarea("M", "F1", 2);
        tarea("I2", "F2", 10);

        ResultadoCaminoCritico<String> r = algoritmos.calcularCaminoCritico(grafo);

        assertEquals(10.0, r.getCostoCritico(), 0.0001);
        assertEquals(List.of("I2", "F2"), r.getCaminoCritico().getPath());
        assertEquals(3, r.getSecuencias().size());
        assertEquals(4.0, buscar(r, List.of("I1", "M", "F1")).getHolgura(), 0.0001);   // costo 6
        assertEquals(7.0, buscar(r, List.of("I2", "M", "F1")).getHolgura(), 0.0001);   // costo 3
    }

    public void testSecuenciasOrdenadasDeMayorAMenorCosto() {
        vertices("A", "B", "C", "D");
        tarea("A", "B", 1);
        tarea("A", "C", 5);
        tarea("A", "D", 3);

        List<SecuenciaConHolgura<String>> secuencias = algoritmos.calcularCaminoCritico(grafo).getSecuencias();

        assertEquals(5.0, secuencias.get(0).getCost(), 0.0001);
        assertEquals(3.0, secuencias.get(1).getCost(), 0.0001);
        assertEquals(1.0, secuencias.get(2).getCost(), 0.0001);
    }

    public void testUnSoloVertice() {
        vertices("A");
        ResultadoCaminoCritico<String> r = algoritmos.calcularCaminoCritico(grafo);
        assertEquals(0.0, r.getCostoCritico(), 0.0001);
        assertEquals(List.of("A"), r.getCaminoCritico().getPath());
    }

    public void testConCiclosLanzaExcepcion() {
        vertices("A", "B");
        tarea("A", "B", 1);
        tarea("B", "A", 1);
        try {
            algoritmos.calcularCaminoCritico(grafo);
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }

    public void testGrafoVacioLanzaExcepcion() {
        try {
            algoritmos.calcularCaminoCritico(grafo);
            fail("Debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperada) {
            // ok
        }
    }
}
