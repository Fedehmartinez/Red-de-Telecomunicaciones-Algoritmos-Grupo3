package ucu.edu.aed;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.utils.GrafoLoader;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        // Uso: Main [aeropuertos] [conexiones]  (por defecto, los archivos _2 del ejercicio 6)
        Path aeropuertos = Path.of(args.length > 0 ? args[0] : "src/main/resources/ut04/aeropuertos_2.txt");
        Path conexiones = Path.of(args.length > 1 ? args[1] : "src/main/resources/ut04/conexiones_2.txt");

        DirectedGraph<String, WeightedEdge> grafo = GrafoLoader.cargar(aeropuertos, conexiones);

        System.out.println("Vértices: " + grafo.cantidadDeVertices() + ", aristas: " + grafo.cantidadDeAristas());
        for (String v : grafo.vertices()) {
            System.out.print(v + " -> ");
            for (Edge<String, WeightedEdge> e : grafo.adyacencias(grafo.construirComparable(v))) {
                System.out.print(e.target() + "(" + e.dato() + ") ");
            }
            System.out.println();
        }

        System.out.println("¿Tiene ciclos? " + (grafo.tieneCiclos() ? "SÍ" : "NO"));
    }
}
