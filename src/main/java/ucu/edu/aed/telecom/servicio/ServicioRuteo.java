package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.ResultadoDiametro;
import ucu.edu.aed.telecom.resultado.RutaResultado;
import ucu.edu.aed.telecom.resultado.TablaRuteo;

import java.util.List;
import java.util.Set;

// Rutas sobre el grafo de ruteo: ruta de menor latencia (con ancho de banda mínimo o esquivando fallas),
// tabla de ruteo, si hay camino y diámetro. Para consultas sueltas usa Dijkstra y para todos los pares Floyd
public class ServicioRuteo {

    private final RedTelecom red;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    public ServicioRuteo(RedTelecom red) {
        if (red == null) throw new NullPointerException();
        this.red = red;
    }

    private void verificar(String id) {
        if (red.nodo(id) == null) throw new IllegalArgumentException("El nodo " + id + " no está en la red");
    }

    // Corre Dijkstra desde origen sobre el grafo y arma la ruta hasta destino.
    // Si algún extremo no está en el grafo o no hay camino, no hay ruta
    private RutaResultado ruta(DirectedGraph<String, WeightedEdge> grafo, String origen, String destino) {
        if (!grafo.existeVertice(grafo.construirComparable(origen)) || !grafo.existeVertice(grafo.construirComparable(destino))) {
            return RutaResultado.sinRuta();
        }
        IDijkstraResult<String> dijkstra = algoritmos.dijkstra(grafo.construirComparable(origen), grafo);
        double latencia = dijkstra.getCost(destino);
        if (latencia == Double.POSITIVE_INFINITY) return RutaResultado.sinRuta();

        List<String> nodos = dijkstra.getPath(destino);
        return new RutaResultado(nodos, latencia, cuelloDeBotella(nodos));
    }

    // Menor ancho de banda entre los enlaces de la ruta (infinito si no tiene enlaces)
    private double cuelloDeBotella(List<String> nodos) {
        double cuello = Double.POSITIVE_INFINITY;
        for (int i = 0; i + 1 < nodos.size(); i++) {
            cuello = Math.min(cuello, red.enlace(nodos.get(i), nodos.get(i + 1)).getAnchoBanda());
        }
        return cuello;
    }

    // Ruta de menor latencia de origen a destino con Dijkstra
    public RutaResultado rutaMenorLatencia(String origen, String destino) {
        verificar(origen);
        verificar(destino);
        return ruta(red.grafoRuteo(), origen, destino);
    }

    // Ruta de menor latencia usando solo enlaces con al menos ese ancho de banda (los angostos se filtran al armar el grafo)
    public RutaResultado rutaConAnchoBandaMinimo(String origen, String destino, double anchoBandaMinimo) {
        verificar(origen);
        verificar(destino);
        return ruta(red.grafoRuteo(anchoBandaMinimo), origen, destino);
    }

    // Ruta de menor latencia sin pasar por los nodos ni enlaces caídos
    public RutaResultado rutaAlternativa(String origen, String destino, Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        verificar(origen);
        verificar(destino);
        return ruta(red.grafoRuteo(0, nodosCaidos, enlacesCaidos), origen, destino);
    }

    // Tabla de ruteo de toda la red con Floyd. Costo O(V³)
    public TablaRuteo tablaDeRuteo() {
        return new TablaRuteo(algoritmos.floyd(red.grafoRuteo()));
    }

    // Si hay algún camino de origen a destino (Warshall). Un nodo siempre llega a sí mismo. Costo O(V³)
    public boolean hayCamino(String origen, String destino) {
        verificar(origen);
        verificar(destino);
        if (origen.equals(destino)) return true;
        return algoritmos.warshall(red.grafoRuteo()).connected(origen, destino);
    }

    // Diámetro: el par de nodos distintos con la mayor latencia mínima, recorriendo la matriz de Floyd.
    // null si hay menos de 2 nodos
    public ResultadoDiametro diametro() {
        if (red.nodos().size() < 2) return null;

        FloydWarshallResult<String> floyd = algoritmos.floyd(red.grafoRuteo());
        ResultadoDiametro mayor = null;
        for (String origen : floyd.getVertices()) {
            for (String destino : floyd.getVertices()) {
                if (origen.equals(destino)) continue;
                double latencia = floyd.getCost(origen, destino);
                if (mayor == null || latencia > mayor.getLatencia()) {
                    mayor = new ResultadoDiametro(origen, destino, latencia);
                }
            }
        }
        return mayor;
    }
}
