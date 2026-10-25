package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

/**
 * Interfaz destinada para modelar el resultado de ejecutar floyd y warshall
 */
public interface IFloydWarshallResult<V> {


    /**
     * Retorna el camino para ir de source a target
     */
    List<V> getPath(V source, V target);

    /**
     * retorna el costo asociado para ir de source a target
     */
    double getCost(V source, V target);

    /**
     * retorna true si existe conectividad entre source y target
     */
    boolean connected(V source, V target);
}
