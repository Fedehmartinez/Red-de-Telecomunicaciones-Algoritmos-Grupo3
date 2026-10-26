package ucu.edu.aed.telecom.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * La red completa: guarda los nodos y los enlaces una sola vez. A partir de acá se arman los grafos que
 * usa cada servicio, que solo conocen los ids; los datos de cada equipo se buscan acá.
 * Los nodos se guardan por id y los enlaces por el par de extremos (sin orden), así buscar un nodo o el
 * enlace entre dos nodos cuesta O(1).
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
        Objects.requireNonNull(nodo);
        if (nodos.containsKey(nodo.getId())) return false;
        nodos.put(nodo.getId(), nodo);
        return true;
    }

    /** Devuelve false si falta alguno de los dos extremos o si ya hay un enlace entre ellos (en cualquier orden). */
    public boolean agregarEnlace(Enlace enlace) {
        Objects.requireNonNull(enlace);
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
}
