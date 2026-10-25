package ucu.edu.aed.tda.grafo;

import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.DijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;
import ucu.edu.aed.tda.grafo.model.result.ResultadoCaminoCritico;
import ucu.edu.aed.tda.grafo.model.result.SecuenciaConHolgura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class DirectedGraphAlgorithms implements IDirectedGraphAlgorithms {

    /**
     * Clasificación topológica: BPF siguiendo los predecesores (equivale a invertir los arcos)
     * y agregando cada vértice a la lista al salir de la recursión.
     * Precondición: el grafo es acíclico.
     */
    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        if (grafo.tieneCiclos()) {
            throw new IllegalArgumentException("El grafo tiene ciclos: no admite clasificación topológica");
        }

        List<V> resultado = new ArrayList<>();
        Set<V> visitados = new HashSet<>();

        for (V v : grafo.vertices()) {
            if (!visitados.contains(v)) {
                bpfTopologico(grafo, v, visitados, resultado);
            }
        }
        return resultado;
    }

    private <V, D> void bpfTopologico(IDirectedIGraph<V, D> grafo, V actual, Set<V> visitados, List<V> resultado) {
        visitados.add(actual);

        for (V predecesor : grafo.predecessors(grafo.construirComparable(actual))) {
            if (!visitados.contains(predecesor)) {
                bpfTopologico(grafo, predecesor, visitados, resultado);
            }
        }

        resultado.add(actual);   // salida de la recursión: todo lo que lo precede ya está en la lista
    }

    /**
     * Camino crítico: las tareas son los arcos (peso = duración). Procesando los vértices en orden
     * topológico se obtiene el camino más largo hasta cada vértice; después se listan todas las
     * secuencias inicio-fin con su holgura respecto del camino crítico.
     * Precondición: el grafo es acíclico y no vacío.
     */
    public <V, D extends WeightedEdge> ResultadoCaminoCritico<V> calcularCaminoCritico(IDirectedIGraph<V, D> grafo) {
        if (grafo.cantidadDeVertices() == 0) {
            throw new IllegalArgumentException("El grafo está vacío");
        }
        List<V> orden = calcularClasificacionTopologica(grafo);   // lanza excepción si hay ciclos

        // camino más largo hasta cada vértice
        Map<V, Double> costoMasLargo = new HashMap<>();
        Map<V, V> predecesorCritico = new HashMap<>();
        for (V v : orden) costoMasLargo.put(v, 0.0);

        for (V v : orden) {
            for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(v))) {
                double nuevoCosto = costoMasLargo.get(v) + arco.dato().getWeight();
                if (nuevoCosto > costoMasLargo.get(arco.target())) {
                    costoMasLargo.put(arco.target(), nuevoCosto);
                    predecesorCritico.put(arco.target(), v);
                }
            }
        }

        // el vértice final de mayor costo cierra el camino crítico
        double costoCritico = -1;
        V verticeFinal = null;
        for (V v : grafo.vertices()) {
            if (grafo.gradoDeSalida(grafo.construirComparable(v)) == 0 && costoMasLargo.get(v) > costoCritico) {
                costoCritico = costoMasLargo.get(v);
                verticeFinal = v;
            }
        }

        // reconstruir el camino hacia atrás
        LinkedList<V> camino = new LinkedList<>();
        for (V actual = verticeFinal; actual != null; actual = predecesorCritico.get(actual)) {
            camino.addFirst(actual);
        }
        SecuenciaConHolgura<V> critico = new SecuenciaConHolgura<>(camino, costoCritico, 0);

        // todas las secuencias inicio-fin
        List<SecuenciaConHolgura<V>> secuencias = new ArrayList<>();
        for (V inicio : grafo.vertices()) {
            if (grafo.gradoDeEntrada(grafo.construirComparable(inicio)) == 0) {
                List<V> caminoActual = new ArrayList<>();
                caminoActual.add(inicio);
                listarSecuencias(grafo, inicio, caminoActual, 0, costoCritico, secuencias);
            }
        }
        secuencias.sort((a, b) -> Double.compare(b.getCost(), a.getCost()));

        return new ResultadoCaminoCritico<>(critico, secuencias);
    }

    private <V, D extends WeightedEdge> void listarSecuencias(IDirectedIGraph<V, D> grafo, V actual, List<V> caminoActual,
                                                              double costoActual, double costoCritico,
                                                              List<SecuenciaConHolgura<V>> secuencias) {
        List<Edge<V, D>> salientes = grafo.adyacencias(grafo.construirComparable(actual));
        if (salientes.isEmpty()) {
            secuencias.add(new SecuenciaConHolgura<>(new ArrayList<>(caminoActual), costoActual, costoCritico - costoActual));
            return;
        }
        for (Edge<V, D> arco : salientes) {
            caminoActual.add(arco.target());
            listarSecuencias(grafo, arco.target(), caminoActual, costoActual + arco.dato().getWeight(), costoCritico, secuencias);
            caminoActual.remove(caminoActual.size() - 1);   // backtracking
        }
    }

    /**
     * Floyd: A empieza como la matriz de costos (0 en la diagonal, ∞ si no hay arco).
     * En la iteración k se permite pasar por el vértice k: si A[i][k] + A[k][j] < A[i][j]
     * se actualiza A[i][j] y se guarda k en P[i][j] para poder recuperar el camino.
     * Precondición: pesos no negativos.
     */
    @Override
    public <V, D extends WeightedEdge> FloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        Map<V, Integer> indice = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) indice.put(vertices.get(i), i);

        int n = vertices.size();
        double[][] A = new double[n][n];
        int[][] P = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = (i == j) ? 0 : Double.POSITIVE_INFINITY;
                P[i][j] = -1;
            }
            for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(vertices.get(i)))) {
                int j = indice.get(arco.target());
                if (i != j) A[i][j] = arco.dato().getWeight();
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (A[i][k] + A[k][j] < A[i][j]) {
                        A[i][j] = A[i][k] + A[k][j];
                        P[i][j] = k;
                    }
                }
            }
        }
        return FloydWarshallResult.deFloyd(vertices, A, P);
    }

    /**
     * Warshall (cerradura transitiva): A empieza como la matriz de adyacencia booleana.
     * En la iteración k, si todavía no hay camino de i a j pero hay de i a k y de k a j, ahora sí lo hay.
     */
    @Override
    public <V, D extends WeightedEdge> FloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        Map<V, Integer> indice = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) indice.put(vertices.get(i), i);

        int n = vertices.size();
        boolean[][] A = new boolean[n][n];
        int[][] P = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) P[i][j] = -1;
            for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(vertices.get(i)))) {
                A[i][indice.get(arco.target())] = true;
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (!A[i][j] && A[i][k] && A[k][j]) {
                        A[i][j] = true;
                        P[i][j] = k;
                    }
                }
            }
        }
        return FloydWarshallResult.deWarshall(vertices, A, P);
    }

    /**
     * Excentricidad de v: el máximo de la columna de v en la matriz de Floyd,
     * o sea, la mayor de las distancias mínimas desde los otros vértices hacia v.
     */
    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        V v = grafo.buscarVertice(vertexCriteria);
        if (v == null) throw new IllegalArgumentException("El vértice no existe");
        return excentricidad(floyd(grafo), v);
    }

    /** Centro: el vértice de menor excentricidad. Devuelve null si el grafo está vacío. */
    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        FloydWarshallResult<V> floyd = floyd(grafo);   // se calcula una sola vez para todos los vértices
        V centro = null;
        double menor = Double.POSITIVE_INFINITY;
        for (V v : floyd.getVertices()) {
            double e = excentricidad(floyd, v);
            if (centro == null || e < menor) {
                centro = v;
                menor = e;
            }
        }
        return centro;
    }

    private <V> double excentricidad(FloydWarshallResult<V> floyd, V v) {
        double maximo = 0;
        for (V u : floyd.getVertices()) {
            if (!u.equals(v)) maximo = Math.max(maximo, floyd.getCost(u, v));
        }
        return maximo;
    }

    /**
     * BPF desde un vértice: visita solo lo alcanzable desde él.
     */
    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V origen = grafo.buscarVertice(sourceCriteria);
        if (origen == null) return;
        bpf(grafo, origen, new HashSet<>(), consumer);
    }

    /**
     * BPF de todo el grafo (bosque abarcador): empieza por el primer vértice y, si quedan vértices
     * sin visitar, arranca un nuevo árbol desde el siguiente. El conjunto de visitados es uno solo
     * para todo el recorrido, así ningún vértice se visita dos veces.
     */
    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Consumer<V> consumer) {
        Set<V> visitados = new HashSet<>();
        for (V v : grafo.vertices()) {
            if (!visitados.contains(v)) bpf(grafo, v, visitados, consumer);
        }
    }

    private <V, D> void bpf(IGraph<V, D> grafo, V actual, Set<V> visitados, Consumer<V> consumer) {
        visitados.add(actual);
        consumer.accept(actual);
        for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(actual))) {
            if (!visitados.contains(arco.target())) bpf(grafo, arco.target(), visitados, consumer);
        }
    }

    /**
     * Dijkstra: costo mínimo desde source a todos los demás vértices, con vector de predecesores.
     * Precondiciones: source existe en el grafo y los pesos son >= 0.
     */
    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        V origen = grafo.buscarVertice(source);
        if (origen == null) {
            throw new IllegalArgumentException("El vértice origen no existe en el grafo");
        }

        Map<V, Double> costos = new HashMap<>();
        Map<V, V> predecesores = new HashMap<>();
        Set<V> definitivos = new HashSet<>();
        for (V v : grafo.vertices()) costos.put(v, Double.POSITIVE_INFINITY);
        costos.put(origen, 0.0);

        // cola de (vértice, costo); las entradas desactualizadas se descartan al sacarlas
        PriorityQueue<Map.Entry<V, Double>> cola = new PriorityQueue<>(Map.Entry.comparingByValue());
        cola.add(Map.entry(origen, 0.0));

        while (!cola.isEmpty()) {
            V actual = cola.poll().getKey();
            if (!definitivos.add(actual)) continue;

            for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(actual))) {
                double nuevoCosto = costos.get(actual) + arco.dato().getWeight();
                if (nuevoCosto < costos.get(arco.target())) {
                    costos.put(arco.target(), nuevoCosto);
                    predecesores.put(arco.target(), actual);
                    cola.add(Map.entry(arco.target(), nuevoCosto));
                }
            }
        }
        return new DijkstraResult<>(origen, costos, predecesores);
    }

    /**
     * Todos los caminos de source a target: BPF con backtracking. Se mantiene el camino actual y su costo;
     * al llegar al destino se guarda una copia y se vuelve atrás. Al volver de un vértice se lo saca del
     * camino y se lo desmarca, para poder usarlo en otros caminos. Ningún vértice se repite en un camino.
     * Precondiciones: source y target existen en el grafo.
     */
    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        V origen = grafo.buscarVertice(source);
        V destino = grafo.buscarVertice(target);
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("El vértice origen o destino no existe en el grafo");
        }

        List<Path<V>> caminos = new ArrayList<>();
        List<V> caminoActual = new ArrayList<>();
        Set<V> visitados = new HashSet<>();
        caminoActual.add(origen);
        visitados.add(origen);
        bpfTodosLosCaminos(grafo, origen, destino, caminoActual, 0, visitados, caminos);
        return caminos;
    }

    private <V, D extends WeightedEdge> void bpfTodosLosCaminos(IGraph<V, D> grafo, V actual, V destino, List<V> caminoActual,
                                                                double costoActual, Set<V> visitados, List<Path<V>> caminos) {
        if (actual.equals(destino)) {
            caminos.add(new Path<>(new ArrayList<>(caminoActual), costoActual));
            return;   // no se sigue explorando desde el destino
        }
        for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(actual))) {
            V sucesor = arco.target();
            if (!visitados.contains(sucesor)) {
                visitados.add(sucesor);
                caminoActual.add(sucesor);
                bpfTodosLosCaminos(grafo, sucesor, destino, caminoActual, costoActual + arco.dato().getWeight(), visitados, caminos);
                caminoActual.remove(caminoActual.size() - 1);   // backtracking
                visitados.remove(sucesor);
            }
        }
    }

    /**
     * BEA desde un vértice: visita solo lo alcanzable desde él, por niveles.
     */
    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V origen = grafo.buscarVertice(sourceCriteria);
        if (origen == null) return;
        bea(grafo, origen, new HashSet<>(), consumer);
    }

    /**
     * BEA de todo el grafo: igual que en profundidad, si quedan vértices sin visitar se arranca de nuevo
     * desde el siguiente, con un solo conjunto de visitados para todo el recorrido.
     */
    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Consumer<V> consumer) {
        Set<V> visitados = new HashSet<>();
        for (V v : grafo.vertices()) {
            if (!visitados.contains(v)) bea(grafo, v, visitados, consumer);
        }
    }

    private <V, D> void bea(IGraph<V, D> grafo, V origen, Set<V> visitados, Consumer<V> consumer) {
        Queue<V> cola = new LinkedList<>();
        visitados.add(origen);   // se marca al encolar, así ningún vértice entra dos veces a la cola
        cola.add(origen);
        while (!cola.isEmpty()) {
            V actual = cola.poll();
            consumer.accept(actual);
            for (Edge<V, D> arco : grafo.adyacencias(grafo.construirComparable(actual))) {
                if (visitados.add(arco.target())) cola.add(arco.target());
            }
        }
    }
}
