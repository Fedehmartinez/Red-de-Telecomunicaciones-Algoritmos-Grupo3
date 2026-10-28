package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.UndirectedGraph;
import ucu.edu.aed.tda.grafo.UndirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.ImpactoFalla;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Qué pasa si algo se rompe: equipos y enlaces críticos, nodos incomunicados y usuarios afectados por cada falla.
// Un nodo está incomunicado si no llega a ningún data center que siga andando. Usa el grafo de resiliencia
public class ServicioResiliencia {

    private final RedTelecom red;
    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();
    private final DirectedGraphAlgorithms recorridos = new DirectedGraphAlgorithms();

    public ServicioResiliencia(RedTelecom red) {
        if (red == null) throw new NullPointerException();
        this.red = red;
    }

    // Equipos críticos: puntos de articulación, los nodos que al caerse parten la red. Costo O(V² + E·V)
    public Set<String> nodosCriticos() {
        Set<String> puntos = algoritmos.puntosDeArticulacion(red.grafoResiliencia(Set.of(), Set.of()));
        Set<String> criticos = new LinkedHashSet<>();
        for (Nodo n : red.nodos()) {
            if (puntos.contains(n.getId())) criticos.add(n.getId());
        }
        return criticos;
    }

    // Enlaces críticos (puentes). La biblioteca no los tiene, así que se pone un vértice en el medio de cada enlace:
    // el enlace es puente si y solo si ese vértice es punto de articulación
    public Set<Enlace> enlacesCriticos() {
        UndirectedGraph<Object, WeightedEdge> subdividido = new UndirectedGraph<>();
        for (Nodo n : red.nodos()) subdividido.agregarVertice(n.getId());
        for (Enlace e : red.enlaces()) {
            subdividido.agregarVertice(e);   // el vértice del medio
            subdividido.agregarArista(e.getA(), e, new WeightedEdge(1));
            subdividido.agregarArista(e, e.getB(), new WeightedEdge(1));
        }

        Set<Object> puntos = algoritmos.puntosDeArticulacion(subdividido);
        Set<Enlace> puentes = new LinkedHashSet<>();
        for (Enlace e : red.enlaces()) {
            if (puntos.contains(e)) puentes.add(e);
        }
        return puentes;
    }

    // Nodos que siguen andando pero no llegan a ningún data center vivo: BEA desde los data centers
    // y los que no se visitan quedan incomunicados
    public Set<String> incomunicados(Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        UndirectedGraph<String, WeightedEdge> grafo = red.grafoResiliencia(nodosCaidos, enlacesCaidos);

        Set<String> alcanzados = new HashSet<>();
        for (Nodo dc : red.dataCenters()) {
            if (nodosCaidos.contains(dc.getId()) || alcanzados.contains(dc.getId())) continue;
            recorridos.recorridoEnAmplitud(grafo, grafo.construirComparable(dc.getId()), alcanzados::add);
        }

        Set<String> resultado = new LinkedHashSet<>();
        for (Nodo n : red.nodos()) {
            if (!nodosCaidos.contains(n.getId()) && !alcanzados.contains(n.getId())) resultado.add(n.getId());
        }
        return resultado;
    }

    // Impacto de que caiga un nodo (consulta 2). Compara los incomunicados antes y después de la falla,
    // así solo cuentan los que quedan así por esta falla
    public ImpactoFalla impactoFallaNodo(String id) {
        Nodo caido = red.nodo(id);
        if (caido == null) throw new IllegalArgumentException("El nodo " + id + " no está en la red");

        Set<String> antes = incomunicados(Set.of(), Set.of());
        Set<String> nuevos = new LinkedHashSet<>(incomunicados(Set.of(id), Set.of()));
        nuevos.removeAll(antes);

        int usuarios = usuarios(nuevos);
        if (!antes.contains(id)) usuarios += caido.getUsuarios();
        return new ImpactoFalla(id, nuevos, usuarios);
    }

    // Impacto de que se corte el enlace entre a y b: nodos que quedan incomunicados por el corte y sus usuarios
    public ImpactoFalla impactoFallaEnlace(String a, String b) {
        Enlace caido = red.enlace(a, b);
        if (caido == null) throw new IllegalArgumentException("No hay enlace entre " + a + " y " + b);

        Set<String> antes = incomunicados(Set.of(), Set.of());
        Set<String> nuevos = new LinkedHashSet<>(incomunicados(Set.of(), Set.of(caido)));
        nuevos.removeAll(antes);
        return new ImpactoFalla(caido.getA() + "-" + caido.getB(), nuevos, usuarios(nuevos));
    }

    // Ranking de nodos críticos por usuarios afectados (consulta 3), versión simple: simula la caída de cada nodo. Costo O(V³ + E·V²)
    public List<ImpactoFalla> rankingCriticosSimple() {
        List<ImpactoFalla> ranking = new ArrayList<>();
        for (Nodo n : red.nodos()) {
            ImpactoFalla impacto = impactoFallaNodo(n.getId());
            if (impacto.getUsuariosAfectados() > 0) ranking.add(impacto);
        }
        ranking.sort((x, y) -> Integer.compare(y.getUsuariosAfectados(), x.getUsuariosAfectados()));
        return ranking;
    }

    private int usuarios(Set<String> ids) {
        int total = 0;
        for (String id : ids) total += red.nodo(id).getUsuarios();
        return total;
    }
}
