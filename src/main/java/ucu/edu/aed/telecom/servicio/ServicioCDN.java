package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.AsignacionCDN;
import ucu.edu.aed.telecom.resultado.RecomendacionDC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Distribución de contenido: cada nodo lo atiende el data center de menor latencia y se recomienda dónde poner uno nuevo.
// La latencia que importa es la del data center hacia el nodo (la fila de Floyd)
public class ServicioCDN {

    private final RedTelecom red;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    public ServicioCDN(RedTelecom red) {
        if (red == null) throw new NullPointerException();
        this.red = red;
    }

    // Asigna cada nodo a un data center de la red. Costo O(V³)
    public AsignacionCDN asignarCDN() {
        Set<String> dataCenters = new LinkedHashSet<>();
        for (Nodo dc : red.dataCenters()) dataCenters.add(dc.getId());
        return asignarCDN(dataCenters);
    }

    // A cada nodo le asigna, entre los data centers dados, el de menor latencia (con Floyd). Si ninguno llega, dcDe es null.
    // Empate: el primero en red.nodos(). Costo O(V³)
    public AsignacionCDN asignarCDN(Set<String> dataCenters) {
        if (dataCenters == null) throw new NullPointerException();
        for (String id : dataCenters) {
            if (red.nodo(id) == null) throw new IllegalArgumentException("El nodo " + id + " no está en la red");
        }

        FloydWarshallResult<String> floyd = algoritmos.floyd(red.grafoRuteo());
        List<String> candidatos = new ArrayList<>();   // los data centers pedidos, en el orden de la red
        for (Nodo n : red.nodos()) {
            if (dataCenters.contains(n.getId())) candidatos.add(n.getId());
        }

        Map<String, String> dcPorNodo = new LinkedHashMap<>();
        Map<String, Double> latenciaPorNodo = new LinkedHashMap<>();
        for (Nodo n : red.nodos()) {
            String mejorDC = null;
            double menor = Double.POSITIVE_INFINITY;
            for (String dc : candidatos) {
                double latencia = floyd.getCost(dc, n.getId());   // fila del DC: del data center hacia el nodo
                if (latencia < menor) {
                    menor = latencia;
                    mejorDC = dc;
                }
            }
            if (mejorDC != null) dcPorNodo.put(n.getId(), mejorDC);
            latenciaPorNodo.put(n.getId(), menor);
        }
        return new AsignacionCDN(dcPorNodo, latenciaPorNodo);
    }

    // Recomienda dónde poner un data center nuevo, versión simple: prueba cada nodo como candidato y vuelve a calcular la asignación.
    // Beneficio = suma de usuarios × mejora de latencia en los nodos de acceso. Costo O(V⁴)
    public RecomendacionDC recomendarDCSimple() {
        Set<String> actuales = new LinkedHashSet<>();
        for (Nodo dc : red.dataCenters()) actuales.add(dc.getId());
        AsignacionCDN actual = asignarCDN(actuales);

        RecomendacionDC mejor = null;
        for (Nodo candidato : red.nodos()) {
            if (candidato.esDataCenter()) continue;

            Set<String> conCandidato = new LinkedHashSet<>(actuales);
            conCandidato.add(candidato.getId());
            AsignacionCDN nueva = asignarCDN(conCandidato);

            double beneficio = 0;
            int usuariosBeneficiados = 0;
            for (Nodo n : red.nodos()) {
                if (!n.getTipo().esDeAcceso()) continue;
                double latenciaActual = actual.latenciaDe(n.getId());
                if (latenciaActual == Double.POSITIVE_INFINITY) continue;
                double mejora = latenciaActual - nueva.latenciaDe(n.getId());
                if (mejora > 0) {
                    beneficio += n.getUsuarios() * mejora;
                    usuariosBeneficiados += n.getUsuarios();
                }
            }
            if (mejor == null || beneficio > mejor.getBeneficio()) {
                mejor = new RecomendacionDC(candidato.getId(), beneficio, usuariosBeneficiados);
            }
        }
        return mejor;
    }

    // Misma recomendación que recomendarDCSimple pero con un solo Floyd (Desafío 3), porque agregar un data center no cambia las latencias.
    // La nueva latencia de un nodo es min(actual, dist(candidato -> nodo)). Costo O(V³)
    public RecomendacionDC recomendarDC() {
        FloydWarshallResult<String> floyd = algoritmos.floyd(red.grafoRuteo());   // una sola vez

        // latencia actual de cada nodo de acceso que hoy llega a algún data center
        List<Nodo> acceso = new ArrayList<>();
        Map<String, Double> actual = new LinkedHashMap<>();
        for (Nodo n : red.nodos()) {
            if (!n.getTipo().esDeAcceso()) continue;
            double menor = Double.POSITIVE_INFINITY;
            for (Nodo dc : red.dataCenters()) menor = Math.min(menor, floyd.getCost(dc.getId(), n.getId()));
            if (menor == Double.POSITIVE_INFINITY) continue;   // hoy no tiene data center: no suma
            acceso.add(n);
            actual.put(n.getId(), menor);
        }

        RecomendacionDC mejor = null;
        for (Nodo candidato : red.nodos()) {
            if (candidato.esDataCenter()) continue;

            double beneficio = 0;
            int usuariosBeneficiados = 0;
            for (Nodo n : acceso) {
                double mejora = actual.get(n.getId()) - floyd.getCost(candidato.getId(), n.getId());   // fila del candidato
                if (mejora > 0) {
                    beneficio += n.getUsuarios() * mejora;
                    usuariosBeneficiados += n.getUsuarios();
                }
            }
            if (mejor == null || beneficio > mejor.getBeneficio()) {
                mejor = new RecomendacionDC(candidato.getId(), beneficio, usuariosBeneficiados);
            }
        }
        return mejor;
    }

    // Nodo que minimiza la mayor latencia que sale de él hacia los demás. Usa el grafo de ruteo invertido
    // porque el centro de la biblioteca mira la columna de la matriz. Costo O(V³)
    public String centroParaDC() {
        DirectedGraph<String, WeightedEdge> invertido = new DirectedGraph<>();
        for (Nodo n : red.nodos()) invertido.agregarVertice(n.getId());
        for (Enlace e : red.enlaces()) {
            invertido.agregarArista(e.getB(), e.getA(), new WeightedEdge(e.getLatenciaAB()));
            invertido.agregarArista(e.getA(), e.getB(), new WeightedEdge(e.getLatenciaBA()));
        }
        return algoritmos.obtenerCentroGrafo(invertido);
    }

    // Nodos de acceso que superan la latencia máxima hasta su data center (o no tienen), de mayor a menor latencia. Costo O(V³)
    public List<String> nodosQueSuperanLatencia(double latenciaMaxima) {
        AsignacionCDN asignacion = asignarCDN();
        List<String> resultado = new ArrayList<>();
        for (Nodo n : red.nodos()) {
            if (n.getTipo().esDeAcceso() && asignacion.latenciaDe(n.getId()) > latenciaMaxima) {
                resultado.add(n.getId());
            }
        }
        resultado.sort((a, b) -> Double.compare(asignacion.latenciaDe(b), asignacion.latenciaDe(a)));
        return resultado;
    }
}
