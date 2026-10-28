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

// Diseño de cableado: conecta todos los nodos con el menor costo total de instalación.
// Para oficinas nuevas se arma una RedTelecom con esas oficinas y las conexiones posibles
public class ServicioCableado {

    private final RedTelecom red;
    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();

    public ServicioCableado(RedTelecom red) {
        if (red == null) throw new NullPointerException();
        this.red = red;
    }

    // Árbol abarcador de costo mínimo con Kruskal sobre el grafo de cableado. Devuelve los enlaces ordenados por costo.
    // Si no se puede conectar todo devuelve el bosque de costo mínimo y conexo en false
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
