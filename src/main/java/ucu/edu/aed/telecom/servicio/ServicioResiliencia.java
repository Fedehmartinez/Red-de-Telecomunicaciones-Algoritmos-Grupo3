package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.ImpactoFalla;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Qué pasa si algo se rompe, sobre el grafo de resiliencia (no dirigido, sin peso): equipos y enlaces
 * críticos, qué nodos quedan incomunicados y a cuántos usuarios afecta cada falla.
 * Un nodo está incomunicado si no llega a ningún data center que siga andando.
 */
public class ServicioResiliencia {

    private final RedTelecom red;

    public ServicioResiliencia(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    /** Equipos críticos: los puntos de articulación de grafoResiliencia. */
    public Set<String> nodosCriticos() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * Enlaces críticos (puentes). La biblioteca tiene puntos de articulación pero no puentes: se agrega un
     * vértice en el medio de cada enlace, y el enlace es puente si y solo si ese vértice es punto de articulación.
     */
    public Set<Enlace> enlacesCriticos() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Nodos que siguen andando pero no llegan a ningún data center vivo: BEA desde los data centers vivos. */
    public Set<String> incomunicados(Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Impacto de que se caiga un nodo (consulta 2). */
    public ImpactoFalla impactoFallaNodo(String id) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Impacto de que se caiga el enlace entre a y b. */
    public ImpactoFalla impactoFallaEnlace(String a, String b) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * Ranking de nodos críticos por usuarios afectados (consulta 3), versión simple: simula la caída de cada
     * nodo con impactoFallaNodo. De mayor a menor; empate: orden de red.nodos().
     */
    public List<ImpactoFalla> rankingCriticosSimple() {
        throw new UnsupportedOperationException("Falta implementar");
    }
}
