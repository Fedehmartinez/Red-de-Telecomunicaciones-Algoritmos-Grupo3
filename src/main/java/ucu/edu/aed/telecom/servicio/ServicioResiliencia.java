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
import java.util.Objects;
import java.util.Set;

/**
 * Qué pasa si algo se rompe, sobre el grafo de resiliencia (no dirigido, sin peso): equipos y enlaces
 * críticos, qué nodos quedan incomunicados y a cuántos usuarios afecta cada falla.
 * Un nodo está incomunicado si no llega a ningún data center que siga andando.
 * Las fallas nunca se aplican a la red: se arma el grafo sin los caídos (ver RedTelecom.grafoResiliencia).
 *
 * Costos, con V nodos y E enlaces: armar el grafo de resiliencia cuesta O(V + E·V), porque agregarArista
 * de la biblioteca busca los vértices en O(V); por lo mismo, un recorrido (BEA o BPF) cuesta O(V² + E) y no
 * O(V + E), ya que cada adyacencias(v) busca v en O(V).
 */
public class ServicioResiliencia {

    private final RedTelecom red;
    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();
    private final DirectedGraphAlgorithms recorridos = new DirectedGraphAlgorithms();

    public ServicioResiliencia(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    /**
     * Equipos críticos: los puntos de articulación de grafoResiliencia sin fallas, o sea los nodos que, si se
     * caen, parten la red en más pedazos. Se devuelven en el orden de red.nodos().
     * Costo: O(V + E·V) para armar el grafo más O(V² + E) de la búsqueda en profundidad.
     */
    public Set<String> nodosCriticos() {
        Set<String> puntos = algoritmos.puntosDeArticulacion(red.grafoResiliencia(Set.of(), Set.of()));
        Set<String> criticos = new LinkedHashSet<>();
        for (Nodo n : red.nodos()) {
            if (puntos.contains(n.getId())) criticos.add(n.getId());
        }
        return criticos;
    }

    /**
     * Enlaces críticos (puentes): los que, si se cortan, parten la red. La biblioteca tiene puntos de
     * articulación pero no puentes, así que se subdivide cada enlace a-b con un vértice en el medio m
     * (a - m - b). El enlace es puente si y solo si m es punto de articulación: m solo toca a a y a b, así que
     * sacar m separa la red si y solo si no había otro camino de a a b.
     * Los vértices de este grafo son de dos clases: los ids de los nodos (String) y, en el medio de cada
     * enlace, el propio Enlace. Así no hace falta inventar ids que podrían chocar con los de la red.
     * Se devuelven en el orden de red.enlaces().
     * Costo: el grafo subdividido tiene V + E vértices y 2E aristas, así que armarlo cuesta O((V + E)·E) y la
     * búsqueda en profundidad O((V + E)²).
     */
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

    /**
     * Nodos que siguen andando pero no llegan a ningún data center vivo: BEA desde los data centers vivos
     * sobre grafoResiliencia(nodosCaidos, enlacesCaidos), y los que no se visitaron están incomunicados.
     * Para no recorrer dos veces la misma parte de la red, solo se arranca una BEA desde un data center que
     * todavía no fue alcanzado (si ya lo alcanzó otro, están en la misma componente y daría lo mismo).
     * Si no queda ningún data center vivo, todos los nodos vivos están incomunicados.
     * Los ids caídos que no son de la red se ignoran. Se devuelven en el orden de red.nodos().
     * La BEA de la biblioteca (recorridoEnAmplitud con origen) recibe cualquier IGraph, así que sirve también
     * para el grafo no dirigido.
     * Costo: O(V + E·V) para armar el grafo más O(V² + E) de las BEA (cada componente se recorre una vez), o sea
     * O(V² + E·V).
     */
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

    /**
     * Impacto de que se caiga un nodo (consulta 2). Se comparan los incomunicados sin fallas y con el nodo caído:
     * cuentan solo los que quedan incomunicados POR esta falla. Los usuarios afectados son los de esos nodos más
     * los del nodo caído (si es de acceso), salvo que ese nodo ya estuviera incomunicado antes: esos usuarios ya
     * no tenían servicio, la falla no les cambia nada.
     * Id que no es de la red: IllegalArgumentException.
     * Costo: dos veces incomunicados, O(V² + E·V).
     */
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

    /**
     * Impacto de que se corte el enlace entre a y b (en cualquier orden): los nodos que quedan incomunicados
     * por el corte y sus usuarios. Solo puede incomunicar algo si el enlace es un puente. El elemento caído se
     * nombra "a-b" con los extremos como están en la red.
     * Si no hay enlace entre a y b: IllegalArgumentException. Costo: dos veces incomunicados, O(V² + E·V).
     */
    public ImpactoFalla impactoFallaEnlace(String a, String b) {
        Enlace caido = red.enlace(a, b);
        if (caido == null) throw new IllegalArgumentException("No hay enlace entre " + a + " y " + b);

        Set<String> antes = incomunicados(Set.of(), Set.of());
        Set<String> nuevos = new LinkedHashSet<>(incomunicados(Set.of(), Set.of(caido)));
        nuevos.removeAll(antes);
        return new ImpactoFalla(caido.getA() + "-" + caido.getB(), nuevos, usuarios(nuevos));
    }

    /**
     * Ranking de nodos críticos por usuarios afectados (consulta 3), versión simple: simula la caída de cada
     * nodo con impactoFallaNodo (la consulta 2 en un for). Quedan solo los nodos cuya caída afecta a algún
     * usuario, de mayor a menor; empate: orden de red.nodos() (el sort de List es estable).
     * Costo: V simulaciones de O(V² + E·V) cada una, o sea O(V³ + E·V²), más O(V log V) para ordenar.
     */
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
