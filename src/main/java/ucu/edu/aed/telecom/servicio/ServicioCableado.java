package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.ResultadoCableado;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Diseño de cableado: conectar todos los nodos de la red con el menor costo total de instalación.
 * Para planificar oficinas o edificios nuevos se arma una RedTelecom con esos nodos y las conexiones
 * posibles entre ellos, cada una con su costo, y se le pide el cableado mínimo.
 */
public class ServicioCableado {

    private final RedTelecom red;
    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();

    public ServicioCableado(RedTelecom red) {
        if (red == null) throw new NullPointerException();
        this.red = red;
    }

    /**
     * Árbol abarcador de costo mínimo: Kruskal sobre grafoCableado() (no dirigido, peso costo). Cada arista
     * del árbol se vuelve a buscar en la red (O(1)) para devolver el Enlace con todos sus datos. Los enlaces
     * quedan ordenados de menor a mayor costo.
     * Si no se pueden conectar todos, Kruskal devuelve el bosque de costo mínimo (un árbol por cada parte que
     * sí se puede unir) y conexo es false: un árbol de V nodos tiene V - 1 enlaces, un bosque tiene menos.
     * Una red vacía o de un solo nodo ya está conectada (sin enlaces, costo 0).
     * Costo: O(V + E·V) para armar el grafo, O(E log E + V²) de Kruskal y O(V log V) para ordenar el resultado.
     */
    public ResultadoCableado cableadoMinimo() {
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.kruskal(red.grafoCableado());

        List<Enlace> enlaces = new ArrayList<>();
        double costoTotal = 0;
        for (Edge<String, WeightedEdge> arista : arbol.aristas()) {
            Enlace enlace = red.enlace(arista.source(), arista.target());
            enlaces.add(enlace);
            costoTotal += enlace.getCosto();
        }
        enlaces.sort(Comparator.comparingDouble(Enlace::getCosto));

        boolean conexo = red.nodos().size() <= 1 || enlaces.size() == red.nodos().size() - 1;
        return new ResultadoCableado(enlaces, costoTotal, conexo);
    }
}
