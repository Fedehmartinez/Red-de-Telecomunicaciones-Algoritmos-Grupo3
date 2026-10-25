package ucu.edu.aed.Ejercicio4;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.utils.GrafoLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainEjercicio4 {

  public static void main(String[] args) throws IOException
  {
    DirectedGraph<String, WeightedEdge> grafo = 
    GrafoLoader.cargar(Path.of("src/main/resources/ut04/aeropuertos_ej4.txt"), Path.of("src/main/resources/ut04/conexiones_ej4.txt"));
    DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    System.out.println("Lista de adyacencias:");
    for (String v : grafo.vertices()) {
      System.out.print(v + " -> ");
      for (Edge<String, WeightedEdge> e : grafo.adyacencias(grafo.construirComparable(v))) {
        System.out.print(e.target() + "(" + e.dato() + ")  ");
      }
      System.out.println();
    }

    List<String> orden = new ArrayList<>();
    algoritmos.recorridoEnProfundidad(grafo, orden::add);
    System.out.println("\nRecorrido en profundidad: " + String.join(", ", orden));
  }
}
