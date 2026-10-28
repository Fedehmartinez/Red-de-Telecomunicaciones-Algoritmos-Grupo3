package ucu.edu.aed.telecom.modelo;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La red completa: guarda los nodos y los enlaces una sola vez. A partir de acá se arman los grafos que
 * usa cada servicio, que solo conocen los ids; los datos de cada equipo se buscan acá.
 * Los nodos se guardan por id y los enlaces por el par de extremos (sin orden), así buscar un nodo o el
 * enlace entre dos nodos cuesta O(1).
 *
 * Hay un grafo para cada necesidad, porque cada algoritmo pide algo distinto:
 * - ruteo: dirigido con peso latencia (la latencia puede ser distinta en cada sentido);
 * - cableado: no dirigido con peso costo (instalar cuesta lo mismo en los dos sentidos, y el árbol
 *   abarcador de costo mínimo es de grafos no dirigidos);
 * - resiliencia: no dirigido sin peso (un enlace roto se cae para los dos lados).
 * Los grafos se arman de nuevo en cada llamada y nunca modifican la red. Las fallas son una "vista": se
 * pasan los nodos y enlaces caídos y el grafo se arma sin ellos.
 * Costo de armar un grafo: O(V + E·V), porque agregarArista de la biblioteca busca los vértices en O(V).
 */
public class RedTelecom {

    private final Map<String, Nodo> nodos = new LinkedHashMap<>();
    private final Map<List<String>, Enlace> enlaces = new LinkedHashMap<>();

    /** Clave del par sin orden: el menor de los dos ids primero, así A-B y B-A dan la misma clave. */
    private static List<String> clave(String a, String b) {
        return a.compareTo(b) <= 0 ? List.of(a, b) : List.of(b, a);
    }

    /** Devuelve false si ya hay un nodo con ese id. */
    public boolean agregarNodo(Nodo nodo) {
        if (nodo == null) throw new NullPointerException();
        if (nodos.containsKey(nodo.getId())) return false;
        nodos.put(nodo.getId(), nodo);
        return true;
    }

    /** Devuelve false si falta alguno de los dos extremos o si ya hay un enlace entre ellos (en cualquier orden). */
    public boolean agregarEnlace(Enlace enlace) {
        if (enlace == null) throw new NullPointerException();
        if (!nodos.containsKey(enlace.getA()) || !nodos.containsKey(enlace.getB())) return false;
        List<String> clave = clave(enlace.getA(), enlace.getB());
        if (enlaces.containsKey(clave)) return false;
        enlaces.put(clave, enlace);
        return true;
    }

    /** El nodo con ese id, o null si no existe. */
    public Nodo nodo(String id) {
        return nodos.get(id);
    }

    /** El enlace entre a y b (el orden no importa), o null si no hay. */
    public Enlace enlace(String a, String b) {
        if (a == null || b == null) return null;
        return enlaces.get(clave(a, b));
    }

    /** Todos los nodos, en el orden en que se agregaron (vista inmodificable). */
    public Collection<Nodo> nodos() {
        return Collections.unmodifiableCollection(nodos.values());
    }

    /** Todos los enlaces, en el orden en que se agregaron (vista inmodificable). */
    public Collection<Enlace> enlaces() {
        return Collections.unmodifiableCollection(enlaces.values());
    }

    /** Los data centers, en el orden en que se agregaron. */
    public List<Nodo> dataCenters() {
        List<Nodo> dataCenters = new ArrayList<>();
        for (Nodo nodo : nodos.values()) {
            if (nodo.esDataCenter()) dataCenters.add(nodo);
        }
        return dataCenters;
    }

    // ---- grafos ----

    /** Grafo de ruteo de toda la red, sin fallas ni restricción de ancho de banda. */
    public DirectedGraph<String, WeightedEdge> grafoRuteo() {
        return grafoRuteo(0, Set.of(), Set.of());
    }

    /** Grafo de ruteo solo con los enlaces de al menos ese ancho de banda (en Mbps). */
    public DirectedGraph<String, WeightedEdge> grafoRuteo(double anchoBandaMinimo) {
        return grafoRuteo(anchoBandaMinimo, Set.of(), Set.of());
    }

    /**
     * Grafo de ruteo: dirigido, con peso latencia. Cada enlace a-b da dos arcos, a -> b con latenciaAB
     * y b -> a con latenciaBA. Se dejan afuera los nodos caídos, los enlaces caídos, los enlaces que tocan
     * un nodo caído y los que tienen menos ancho de banda que el mínimo (el mínimo en sí se acepta).
     * Dijkstra no puede tener en cuenta una restricción por arista, por eso el ancho de banda mínimo se
     * resuelve filtrando acá y después se usa Dijkstra normal sobre este grafo.
     * Los ids caídos que no son de la red se ignoran.
     */
    public DirectedGraph<String, WeightedEdge> grafoRuteo(double anchoBandaMinimo, Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        if (Double.isNaN(anchoBandaMinimo)) throw new IllegalArgumentException("Ancho de banda mínimo inválido");
        if (nodosCaidos == null) throw new NullPointerException();
        if (enlacesCaidos == null) throw new NullPointerException();

        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        for (String id : nodos.keySet()) {
            if (!nodosCaidos.contains(id)) grafo.agregarVertice(id);
        }
        for (Enlace e : enlaces.values()) {
            if (!disponible(e, nodosCaidos, enlacesCaidos) || e.getAnchoBanda() < anchoBandaMinimo) continue;
            grafo.agregarArista(e.getA(), e.getB(), new WeightedEdge(e.getLatenciaAB()));
            grafo.agregarArista(e.getB(), e.getA(), new WeightedEdge(e.getLatenciaBA()));
        }
        return grafo;
    }

    /** Grafo de cableado: no dirigido, con peso costo de instalación. Tiene todos los nodos y enlaces. */
    public UndirectedGraph<String, WeightedEdge> grafoCableado() {
        UndirectedGraph<String, WeightedEdge> grafo = new UndirectedGraph<>();
        grafo.agregarVertices(nodos.keySet());
        for (Enlace e : enlaces.values()) {
            grafo.agregarArista(e.getA(), e.getB(), new WeightedEdge(e.getCosto()));
        }
        return grafo;
    }

    /**
     * Grafo de resiliencia: no dirigido y sin peso (todas las aristas pesan 1), sin los nodos ni los enlaces
     * caídos. Es el que se usa para conexidad, incomunicados y puntos de articulación.
     * Los ids caídos que no son de la red se ignoran.
     */
    public UndirectedGraph<String, WeightedEdge> grafoResiliencia(Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        if (nodosCaidos == null) throw new NullPointerException();
        if (enlacesCaidos == null) throw new NullPointerException();

        UndirectedGraph<String, WeightedEdge> grafo = new UndirectedGraph<>();
        for (String id : nodos.keySet()) {
            if (!nodosCaidos.contains(id)) grafo.agregarVertice(id);
        }
        for (Enlace e : enlaces.values()) {
            if (disponible(e, nodosCaidos, enlacesCaidos)) {
                grafo.agregarArista(e.getA(), e.getB(), new WeightedEdge(1));
            }
        }
        return grafo;
    }

    /** Un enlace sigue funcionando si no se cayó él ni ninguno de sus dos extremos. */
    private static boolean disponible(Enlace e, Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        return !enlacesCaidos.contains(e) && !nodosCaidos.contains(e.getA()) && !nodosCaidos.contains(e.getB());
    }
}
