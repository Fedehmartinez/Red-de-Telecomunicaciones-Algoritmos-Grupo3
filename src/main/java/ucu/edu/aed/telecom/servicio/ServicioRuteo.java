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
import java.util.Objects;
import java.util.Set;

/**
 * Todo lo de rutas sobre el grafo de ruteo (dirigido, peso latencia): la ruta de menor latencia entre dos
 * nodos, con ancho de banda mínimo o esquivando fallas, la tabla de ruteo completa, si hay camino y el
 * diámetro de la red. Para consultas sueltas se usa Dijkstra; para todos los pares, Floyd.
 * Ids que no son de la red: IllegalArgumentException.
 *
 * Costos, con V nodos y E enlaces: armar un grafo de ruteo cuesta O(V + E·V), porque en la biblioteca
 * agregarArista y adyacencias buscan el vértice en O(V). Con eso, Dijkstra queda O(V² + E log V),
 * Floyd y Warshall O(V³).
 */
public class ServicioRuteo {

    private final RedTelecom red;
    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    public ServicioRuteo(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    private void verificar(String id) {
        if (red.nodo(id) == null) throw new IllegalArgumentException("El nodo " + id + " no está en la red");
    }

    /**
     * Dijkstra desde origen sobre el grafo dado; arma la ruta hasta destino con su latencia y su cuello de botella.
     * Si origen o destino no están en el grafo (caídos), o no hay camino, no hay ruta.
     * Costo: O(V² + E log V) más lo que cueste armar el grafo.
     */
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

    /** Menor ancho de banda entre los enlaces de la ruta; infinito si no tiene enlaces (un solo nodo). Costo: O(largo de la ruta). */
    private double cuelloDeBotella(List<String> nodos) {
        double cuello = Double.POSITIVE_INFINITY;
        for (int i = 0; i + 1 < nodos.size(); i++) {
            cuello = Math.min(cuello, red.enlace(nodos.get(i), nodos.get(i + 1)).getAnchoBanda());
        }
        return cuello;
    }

    /**
     * Ruta de menor latencia de origen a destino: Dijkstra sobre grafoRuteo(). Incluye el ancho de banda del
     * enlace más angosto. Costo: O(V + E·V) para armar el grafo más O(V² + E log V) de Dijkstra.
     */
    public RutaResultado rutaMenorLatencia(String origen, String destino) {
        verificar(origen);
        verificar(destino);
        return ruta(red.grafoRuteo(), origen, destino);
    }

    /**
     * Ruta de menor latencia usando solo enlaces con al menos ese ancho de banda: Dijkstra sobre
     * grafoRuteo(min). Dijkstra no puede tener en cuenta una restricción por arista, así que se filtran los
     * enlaces angostos al armar el grafo. Costo: igual que rutaMenorLatencia.
     */
    public RutaResultado rutaConAnchoBandaMinimo(String origen, String destino, double anchoBandaMinimo) {
        verificar(origen);
        verificar(destino);
        return ruta(red.grafoRuteo(anchoBandaMinimo), origen, destino);
    }

    /**
     * Ruta de menor latencia sin pasar por los nodos ni enlaces caídos: Dijkstra sobre
     * grafoRuteo(0, nodosCaidos, enlacesCaidos). Si cae el origen o el destino, no hay ruta.
     * Costo: igual que rutaMenorLatencia.
     */
    public RutaResultado rutaAlternativa(String origen, String destino, Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        verificar(origen);
        verificar(destino);
        return ruta(red.grafoRuteo(0, nodosCaidos, enlacesCaidos), origen, destino);
    }

    /**
     * Tabla de ruteo de toda la red: Floyd sobre grafoRuteo(). Se calcula una vez y después cada consulta a
     * la tabla es de la matriz. Costo: O(V + E·V) para armar el grafo más O(V³) de Floyd.
     */
    public TablaRuteo tablaDeRuteo() {
        return new TablaRuteo(algoritmos.floyd(red.grafoRuteo()));
    }

    /**
     * Si hay algún camino de origen a destino: Warshall (cerradura transitiva) sobre grafoRuteo().
     * Warshall solo marca la diagonal si hay un ciclo, así que origen igual a destino se resuelve aparte:
     * un nodo siempre llega a sí mismo. Costo: O(V + E·V) para armar el grafo más O(V³) de Warshall.
     */
    public boolean hayCamino(String origen, String destino) {
        verificar(origen);
        verificar(destino);
        if (origen.equals(destino)) return true;
        return algoritmos.warshall(red.grafoRuteo()).connected(origen, destino);
    }

    /**
     * Diámetro: el par (origen distinto de destino) con la mayor latencia mínima, con Floyd. Se recorre la
     * matriz de Floyd buscando el mayor valor fuera de la diagonal; si algún par no tiene camino, la
     * latencia es infinita y se devuelve el primero de esos pares. Empate: el primero en el orden de la matriz.
     * null si la red tiene menos de 2 nodos.
     * Costo: O(V + E·V) para armar el grafo, O(V³) de Floyd y O(V²) para recorrer la matriz.
     */
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
