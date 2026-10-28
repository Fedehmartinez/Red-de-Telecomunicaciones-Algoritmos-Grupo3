package ucu.edu.aed.telecom.modelo;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Guarda todos los nodos y enlaces de la red una sola vez, y a partir de ahí arma los grafos
// que usa cada servicio (ruteo, cableado y resiliencia). Las fallas no se aplican a la red:
// se arma el grafo sin los nodos y enlaces caídos.
public class RedTelecom {

    private final Map<String, Nodo> nodos = new LinkedHashMap<>();
    private final Map<List<String>, Enlace> enlaces = new LinkedHashMap<>();

    // Clave del par sin orden: A-B y B-A dan la misma
    private static List<String> clave(String a, String b) {
        return a.compareTo(b) <= 0 ? List.of(a, b) : List.of(b, a);
    }

    // Agrega un nodo. Devuelve false si ya hay uno con ese id
    public boolean agregarNodo(Nodo nodo) {
        if (nodo == null) throw new NullPointerException();
        if (nodos.containsKey(nodo.getId())) return false;
        nodos.put(nodo.getId(), nodo);
        return true;
    }

    // Agrega un enlace. Devuelve false si falta un extremo o si ya hay un enlace entre esos nodos
    public boolean agregarEnlace(Enlace enlace) {
        if (enlace == null) throw new NullPointerException();
        if (!nodos.containsKey(enlace.getA()) || !nodos.containsKey(enlace.getB())) return false;
        List<String> clave = clave(enlace.getA(), enlace.getB());
        if (enlaces.containsKey(clave)) return false;
        enlaces.put(clave, enlace);
        return true;
    }

    // Devuelve el nodo con ese id, o null si no existe
    public Nodo nodo(String id) {
        return nodos.get(id);
    }

    // Devuelve el enlace entre a y b (en cualquier orden), o null si no hay
    public Enlace enlace(String a, String b) {
        if (a == null || b == null) return null;
        return enlaces.get(clave(a, b));
    }

    // Todos los nodos, en el orden en que se agregaron
    public Collection<Nodo> nodos() {
        return nodos.values();
    }

    // Todos los enlaces, en el orden en que se agregaron
    public Collection<Enlace> enlaces() {
        return enlaces.values();
    }

    // Los data centers, en el orden en que se agregaron
    public List<Nodo> dataCenters() {
        List<Nodo> dataCenters = new ArrayList<>();
        for (Nodo nodo : nodos.values()) {
            if (nodo.esDataCenter()) dataCenters.add(nodo);
        }
        return dataCenters;
    }

    // ---- grafos ----

    // Grafo de ruteo de toda la red, sin fallas ni mínimo de ancho de banda
    public DirectedGraph<String, WeightedEdge> grafoRuteo() {
        return grafoRuteo(0, Set.of(), Set.of());
    }

    // Grafo de ruteo solo con los enlaces de al menos ese ancho de banda (Mbps)
    public DirectedGraph<String, WeightedEdge> grafoRuteo(double anchoBandaMinimo) {
        return grafoRuteo(anchoBandaMinimo, Set.of(), Set.of());
    }

    // Grafo de ruteo: dirigido y con la latencia como peso. Cada enlace da dos arcos, uno por sentido.
    // Deja afuera los nodos y enlaces caídos y los enlaces con menos ancho de banda que el mínimo
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

    // Grafo de cableado: no dirigido y con el costo de instalación como peso
    public UndirectedGraph<String, WeightedEdge> grafoCableado() {
        UndirectedGraph<String, WeightedEdge> grafo = new UndirectedGraph<>();
        grafo.agregarVertices(nodos.keySet());
        for (Enlace e : enlaces.values()) {
            grafo.agregarArista(e.getA(), e.getB(), new WeightedEdge(e.getCosto()));
        }
        return grafo;
    }

    // Grafo de resiliencia: no dirigido y sin peso (todas las aristas pesan 1), sin lo que está caído
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

    // Un enlace funciona si no se cayó él ni ninguno de sus dos extremos
    private static boolean disponible(Enlace e, Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        return !enlacesCaidos.contains(e) && !nodosCaidos.contains(e.getA()) && !nodosCaidos.contains(e.getB());
    }
}
