package ucu.edu.aed.tda.grafo.model.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado de Floyd o de Warshall. Los dos devuelven una matriz n x n y una matriz P de intermedios:
 * P[i][j] = k es el vértice intermedio que mejoró (Floyd) o habilitó (Warshall) el camino de i a j;
 * -1 significa que el camino es el arco directo.
 * Floyd además guarda los costos mínimos; Warshall solo sabe si existe camino, así que getCost no aplica.
 */
public class FloydWarshallResult<V> implements IFloydWarshallResult<V> {

    private final List<V> vertices;
    private final Map<V, Integer> indice = new HashMap<>();
    private final double[][] costos;          // null en Warshall
    private final boolean[][] alcanzable;
    private final int[][] intermedios;

    private FloydWarshallResult(List<V> vertices, double[][] costos, boolean[][] alcanzable, int[][] intermedios) {
        this.vertices = vertices;
        this.costos = costos;
        this.alcanzable = alcanzable;
        this.intermedios = intermedios;
        for (int i = 0; i < vertices.size(); i++) indice.put(vertices.get(i), i);
    }

    /** Resultado de Floyd: hay camino de i a j si su costo mínimo no es infinito. */
    public static <V> FloydWarshallResult<V> deFloyd(List<V> vertices, double[][] costos, int[][] intermedios) {
        int n = vertices.size();
        boolean[][] alcanzable = new boolean[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) alcanzable[i][j] = costos[i][j] != Double.POSITIVE_INFINITY;
        return new FloydWarshallResult<>(vertices, costos, alcanzable, intermedios);
    }

    /** Resultado de Warshall: solo la cerradura transitiva, sin costos. */
    public static <V> FloydWarshallResult<V> deWarshall(List<V> vertices, boolean[][] alcanzable, int[][] intermedios) {
        return new FloydWarshallResult<>(vertices, null, alcanzable, intermedios);
    }

    public boolean esFloyd() {
        return costos != null;
    }

    @Override
    public double getCost(V source, V target) {
        if (!esFloyd()) {
            throw new UnsupportedOperationException("Warshall solo calcula si existe camino; para costos usar Floyd");
        }
        return costos[indice.get(source)][indice.get(target)];
    }

    @Override
    public boolean connected(V source, V target) {
        return alcanzable[indice.get(source)][indice.get(target)];
    }

    /**
     * Camino completo (origen ... destino). Lista vacía si no hay camino.
     * En Floyd es el de menor costo; en Warshall es un camino cualquiera.
     */
    @Override
    public List<V> getPath(V source, V target) {
        if (!connected(source, target)) return Collections.emptyList();
        List<V> camino = new ArrayList<>();
        camino.add(source);
        // en Floyd ir de un vértice a sí mismo cuesta 0 y el camino es él solo;
        // en Warshall, si A[i][i] es verdadero es porque hay un ciclo que vuelve a i
        if (source.equals(target) && esFloyd()) return camino;
        agregarIntermedios(indice.get(source), indice.get(target), camino);
        camino.add(target);
        return camino;
    }

    private void agregarIntermedios(int i, int j, List<V> camino) {
        int k = intermedios[i][j];
        if (k == -1) return;                 // arco directo, no hay nada en el medio
        agregarIntermedios(i, k, camino);
        camino.add(vertices.get(k));
        agregarIntermedios(k, j, camino);
    }

    public List<V> getVertices() {
        return vertices;
    }

    /** Matriz A de costos mínimos (solo Floyd, null en Warshall). */
    public double[][] getCostos() {
        return costos;
    }

    /** Cerradura transitiva: true si hay camino de i a j. */
    public boolean[][] getAlcanzable() {
        return alcanzable;
    }

    public int[][] getIntermedios() {
        return intermedios;
    }
}
