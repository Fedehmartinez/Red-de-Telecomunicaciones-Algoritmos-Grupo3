package ucu.edu.aed.tda.grafo;

import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class UndirectedGraphAlgorithms implements IUndirectedGraphAlgorithm {

    /**
     * Prim (versión del teórico que no recorre "V-U" entero): U empieza con el origen y en cada paso
     * se agrega la arista de costo mínimo que une U con el resto, junto con su vértice destino.
     * Si el grafo no es conexo, buscarMin no encuentra arista y se corta: el resultado es el árbol
     * de la componente del origen y los demás vértices quedan sin aristas.
     * Precondición: el origen existe en el grafo.
     */
    @Override
    public <V, D extends WeightedEdge> IUndirectedGraph<V, D> prim(IUndirectedGraph<V, D> graph, Comparable<V> source) {
        V origen = graph.buscarVertice(source);
        if (origen == null) {
            throw new IllegalArgumentException("El vértice origen no existe en el grafo");
        }

        UndirectedGraph<V, D> arbol = new UndirectedGraph<>();
        arbol.agregarVertices(graph.vertices());

        Set<V> enArbol = new LinkedHashSet<>();
        Set<V> resto = new LinkedHashSet<>(graph.vertices());
        enArbol.add(origen);
        resto.remove(origen);

        while (!resto.isEmpty()) {
            Edge<V, D> arista = searchMinEdge(graph, enArbol, resto);
            if (arista == null) break;   // lo que queda en "resto" no se puede alcanzar desde el origen
            arbol.agregarArista(arista.source(), arista.target(), arista.dato());
            resto.remove(arista.target());
            enArbol.add(arista.target());
        }
        return arbol;
    }

    /**
     * buscarMin del teórico: la arista de menor costo con un extremo en enArbol y el otro en fueraDelArbol.
     * En lugar de preguntar por cada par (u,v) se recorren las aristas de cada u, que es lo mismo pero
     * sin buscar aristas que no existen. Devuelve null si no hay ninguna arista que cruce.
     * La arista devuelta tiene source() en enArbol y target() en fueraDelArbol.
     */
    @Override
    public <V, D extends WeightedEdge> Edge<V, D> searchMinEdge(IUndirectedGraph<V, D> graph, Collection<V> enArbol, Collection<V> fueraDelArbol) {
        Set<V> fuera = new HashSet<>(fueraDelArbol);
        Edge<V, D> mejorArista = null;
        double min = Double.POSITIVE_INFINITY;

        for (V u : enArbol) {
            for (Edge<V, D> arista : graph.adyacencias(graph.construirComparable(u))) {
                if (fuera.contains(arista.target()) && arista.dato().getWeight() < min) {
                    min = arista.dato().getWeight();
                    mejorArista = arista;
                }
            }
        }
        return mejorArista;
    }

    /**
     * Kruskal con componentes: se empieza con cada vértice en su propia componente y se toman las aristas
     * de menor a mayor costo. Una arista se agrega si une dos componentes distintas (si no, cerraría un ciclo)
     * y en ese caso las dos componentes pasan a ser una.
     * Se corta al tener n-1 aristas o al quedarse sin aristas: si el grafo no es conexo, el resultado es
     * un bosque con un árbol abarcador de costo mínimo por cada componente.
     */
    @Override
    public <V, D extends WeightedEdge> IUndirectedGraph<V, D> kruskal(IUndirectedGraph<V, D> graph) {
        UndirectedGraph<V, D> arbol = new UndirectedGraph<>();
        arbol.agregarVertices(graph.vertices());

        Map<V, Integer> componente = new HashMap<>();
        int numero = 0;
        for (V v : graph.vertices()) componente.put(v, numero++);

        List<Edge<V, D>> aristas = new ArrayList<>(graph.aristas());
        aristas.sort(Comparator.comparingDouble(e -> e.dato().getWeight()));

        int necesarias = graph.cantidadDeVertices() - 1;
        int agregadas = 0;
        for (Edge<V, D> arista : aristas) {
            if (agregadas == necesarias) break;
            int origen = componente.get(arista.source());
            int destino = componente.get(arista.target());
            if (origen != destino) {
                arbol.agregarArista(arista.source(), arista.target(), arista.dato());
                agregadas++;
                // unir: todo lo que estaba en la componente destino pasa a la componente origen
                for (Map.Entry<V, Integer> entry : componente.entrySet()) {
                    if (entry.getValue() == destino) entry.setValue(origen);
                }
            }
        }
        return arbol;
    }

    /**
     * BEA del teórico: recorre todo el grafo por niveles. Si quedan vértices sin visitar (grafo no conexo)
     * se arranca de nuevo desde el siguiente. Se marca al encolar, así ningún vértice entra dos veces a la cola.
     */
    @Override
    public <V, D> void bea(IUndirectedGraph<V, D> graph, Consumer<V> consumer) {
        Set<V> visitados = new HashSet<>();
        Queue<V> cola = new LinkedList<>();

        for (V v : graph.vertices()) {
            if (visitados.contains(v)) continue;
            visitados.add(v);
            cola.add(v);
            while (!cola.isEmpty()) {
                V x = cola.poll();
                consumer.accept(x);
                for (Edge<V, D> arista : graph.adyacencias(graph.construirComparable(x))) {
                    if (visitados.add(arista.target())) cola.add(arista.target());
                }
            }
        }
    }

    /**
     * Puntos de articulación: búsqueda en profundidad numerando los vértices en orden previo (numBp)
     * y calculando bajo[v] al volver de la recursión, como el mínimo entre:
     * numBp[v], numBp[z] para cada arista de retroceso (v,z) y bajo[y] para cada hijo y.
     * - la raíz es punto de articulación si y solo si tiene dos o más hijos;
     * - otro vértice v lo es si y solo si tiene un hijo w con bajo[w] >= numBp[v].
     * Si el grafo no es conexo se hace una búsqueda desde cada componente.
     */
    public <V, D> Set<V> puntosDeArticulacion(IUndirectedGraph<V, D> graph) {
        Map<V, Integer> numBp = new HashMap<>();
        Map<V, Integer> bajo = new HashMap<>();
        Set<V> puntos = new LinkedHashSet<>();

        for (V raiz : graph.vertices()) {
            if (numBp.containsKey(raiz)) continue;
            numBp.put(raiz, numBp.size() + 1);
            bajo.put(raiz, numBp.get(raiz));

            int hijosDeLaRaiz = 0;
            for (Edge<V, D> arista : graph.adyacencias(graph.construirComparable(raiz))) {
                if (!numBp.containsKey(arista.target())) {
                    hijosDeLaRaiz++;
                    bpfArticulacion(graph, arista.target(), raiz, numBp, bajo, puntos);
                }
            }
            if (hijosDeLaRaiz >= 2) puntos.add(raiz);
        }
        return puntos;
    }

    private <V, D> void bpfArticulacion(IUndirectedGraph<V, D> graph, V actual, V padre,
                                        Map<V, Integer> numBp, Map<V, Integer> bajo, Set<V> puntos) {
        numBp.put(actual, numBp.size() + 1);   // numeración en orden previo, empieza en 1
        bajo.put(actual, numBp.get(actual));

        for (Edge<V, D> arista : graph.adyacencias(graph.construirComparable(actual))) {
            V w = arista.target();
            if (w.equals(padre)) continue;   // es la arista de árbol por la que se llegó
            if (numBp.containsKey(w)) {
                // arista de retroceso
                bajo.put(actual, Math.min(bajo.get(actual), numBp.get(w)));
            } else {
                // w es hijo de actual
                bpfArticulacion(graph, w, actual, numBp, bajo, puntos);
                bajo.put(actual, Math.min(bajo.get(actual), bajo.get(w)));
                if (bajo.get(w) >= numBp.get(actual)) puntos.add(actual);
            }
        }
    }
}
