package ucu.edu.aed.telecom.resultado;

import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tabla de ruteo de toda la red: latencia mínima y camino entre todos los pares de nodos.
 * Envuelve el resultado de Floyd, que se calcula una vez; después cada consulta es una búsqueda en la
 * matriz (latencia en O(1), camino en O(largo del camino)).
 */
public class TablaRuteo {

    private final FloydWarshallResult<String> floyd;
    private final Set<String> nodos;

    public TablaRuteo(FloydWarshallResult<String> floyd) {
        if (!floyd.esFloyd()) {
            throw new IllegalArgumentException("La tabla de ruteo necesita el resultado de Floyd (Warshall no tiene latencias)");
        }
        this.floyd = floyd;
        this.nodos = new HashSet<>(floyd.getVertices());
    }

    private void verificar(String id) {
        if (!nodos.contains(id)) throw new IllegalArgumentException("El nodo " + id + " no está en la tabla");
    }

    /** Latencia mínima de origen a destino; infinito si no hay camino. */
    public double latencia(String origen, String destino) {
        verificar(origen);
        verificar(destino);
        return floyd.getCost(origen, destino);
    }

    /** Camino de menor latencia (origen y destino incluidos); vacío si no hay camino. */
    public List<String> camino(String origen, String destino) {
        verificar(origen);
        verificar(destino);
        return floyd.getPath(origen, destino);
    }

    /**
     * El primer nodo después del origen en el camino de menor latencia hacia el destino: es lo que guarda un
     * router en su tabla. null si no hay camino o si origen y destino son el mismo.
     */
    public String siguienteSalto(String origen, String destino) {
        List<String> camino = camino(origen, destino);
        return camino.size() >= 2 ? camino.get(1) : null;
    }

    /** Los nodos de la tabla, en el orden de las filas de la matriz. */
    public List<String> nodos() {
        return Collections.unmodifiableList(floyd.getVertices());
    }
}
