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
import java.util.Objects;
import java.util.Set;

/**
 * Distribución de contenido: cada nodo es atendido por el data center con menor latencia, y se recomienda
 * dónde poner un data center nuevo. El contenido va del data center al usuario, así que la latencia que
 * importa es DC -> nodo (la fila de la matriz de Floyd), no nodo -> DC.
 *
 * Costos, con V nodos y E enlaces: armar el grafo de ruteo cuesta O(V + E·V) y Floyd O(V³), así que
 * calcular una asignación cuesta O(V³).
 */
public class ServicioCDN {

    private final RedTelecom red;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    public ServicioCDN(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    /** Asignación con los data centers de la red. Costo: O(V³). */
    public AsignacionCDN asignarCDN() {
        Set<String> dataCenters = new LinkedHashSet<>();
        for (Nodo dc : red.dataCenters()) dataCenters.add(dc.getId());
        return asignarCDN(dataCenters);
    }

    /**
     * A cada nodo, el data center (de los dados) con menor latencia DC -> nodo. Se calcula Floyd sobre
     * grafoRuteo() en cada llamada (versión simple, sin guardar la matriz) y para cada nodo se recorren los
     * data centers mirando la fila de cada uno. Un data center se atiende a sí mismo con latencia 0. Si
     * ningún data center llega a un nodo, dcDe es null y la latencia infinita. Empate: el data center que
     * aparece primero en red.nodos(). Ids que no son de la red: IllegalArgumentException.
     * Costo: O(V³) de Floyd más O(V·D) para elegir, con D la cantidad de data centers.
     */
    public AsignacionCDN asignarCDN(Set<String> dataCenters) {
        Objects.requireNonNull(dataCenters);
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

    /**
     * Versión simple de la recomendación: para cada nodo que no es data center, se lo agrega como uno y se
     * vuelve a llamar a asignarCDN. El beneficio es la suma, sobre los nodos de acceso, de usuarios × (latencia
     * actual - latencia nueva); los usuarios beneficiados son los de los nodos que mejoran estrictamente. Los
     * nodos sin data center alcanzable hoy no suman. Gana el de mayor beneficio (empate: el primero en
     * red.nodos()); null si no hay candidatos. Nunca recomienda un data center que ya existe.
     * Costo: C candidatos × O(V³) de cada asignación, o sea O(V⁴).
     */
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

    /**
     * Centro para ubicar un data center: el nodo que minimiza la mayor latencia desde él hacia los demás.
     * Se usa obtenerCentroGrafo sobre el grafo de ruteo invertido, porque el centro de la biblioteca mira la
     * columna (los otros hacia v) y en el invertido la columna es la fila del original. El invertido tiene,
     * para cada enlace a-b, el arco b -> a con latenciaAB y el arco a -> b con latenciaBA.
     * null si la red está vacía. Costo: O(V + E·V) para armar el grafo más O(V³) de Floyd.
     */
    public String centroParaDC() {
        DirectedGraph<String, WeightedEdge> invertido = new DirectedGraph<>();
        for (Nodo n : red.nodos()) invertido.agregarVertice(n.getId());
        for (Enlace e : red.enlaces()) {
            invertido.agregarArista(e.getB(), e.getA(), new WeightedEdge(e.getLatenciaAB()));
            invertido.agregarArista(e.getA(), e.getB(), new WeightedEdge(e.getLatenciaBA()));
        }
        return algoritmos.obtenerCentroGrafo(invertido);
    }

    /**
     * Nodos de acceso cuya latencia a su data center supera el máximo (o que no tienen data center), de mayor
     * a menor latencia; igual a max no cuenta. Empate: orden de red.nodos(). Costo: O(V³) de la asignación
     * más O(V log V) para ordenar.
     */
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
