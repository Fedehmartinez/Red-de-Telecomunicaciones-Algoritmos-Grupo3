package ucu.edu.aed.Ejercicio3;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.FloydWarshallResult;
import ucu.edu.aed.utils.GrafoLoader;
import ucu.edu.aed.utils.PrettyGrid;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class MainEjercicio3 {

  public static void main(String[] args) throws IOException {
  DirectedGraph<String, WeightedEdge> grafo = GrafoLoader.cargar(Path.of("src/main/resources/ut04/aeropuertos.txt"), Path.of("src/main/resources/ut04/conexiones.txt"));
  DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

  System.out.println("Matriz de costos (VUELE SEGURO):");
  PrettyGrid.printGraphMatrix(grafo, v -> v);

  FloydWarshallResult<String> floyd = algoritmos.floyd(grafo);
  FloydWarshallResult<String> warshall = algoritmos.warshall(grafo);
  List<String> nombres = floyd.getVertices();

  System.out.println("\nFloyd - matriz A (costos mínimos):");
  PrettyGrid.printLabeled(aObjetos(floyd.getCostos()), PrettyGrid.NUMBER, nombres, nombres, "");

  System.out.println("\nFloyd - matriz P (intermedios, - = arco directo):");
  PrettyGrid.printLabeled(intermediosConNombres(floyd.getIntermedios(), nombres), v -> v, nombres, nombres, "");

  System.out.println("\nWarshall - cerradura transitiva (1 = hay camino):");
  PrettyGrid.printLabeled(aObjetos(warshall.getAlcanzable()), PrettyGrid.NUMBER, nombres, nombres, "");

  Scanner sc = new Scanner(System.in);
  int opcion;
    do {
      System.out.println("\n1) Menor distancia entre dos ciudades");
      System.out.println("2) Itinerario de menor costo entre dos ciudades");
      System.out.println("3) Aeropuerto más apropiado para el centro de mantenimiento");
      System.out.println("0) Salir");
      System.out.print("Opción: ");
      try {
        opcion = Integer.parseInt(sc.nextLine().trim());
      } catch (NumberFormatException e) {
        System.out.println("Opción inválida, ingrese un número del 0 al 3.");
        opcion = -1;
        continue;
      }

      if (opcion == 1 || opcion == 2) {
        String origen = pedirCiudad(sc, grafo, "Ciudad origen: ");
        String destino = pedirCiudad(sc, grafo, "Ciudad destino: ");
        if (origen == null || destino == null) {
          System.out.println("Ciudad inexistente. Opciones: " + nombres);
        } else if (!floyd.connected(origen, destino)) {
          System.out.println("No hay forma de ir de " + origen + " a " + destino);
        } else if (opcion == 1) {
          System.out.println("Menor distancia: " + floyd.getCost(origen, destino));
        } else {
          System.out.println("Itinerario: " + String.join(" -> ", floyd.getPath(origen, destino)) + " (costo " + floyd.getCost(origen, destino) + ")");
        }
      } else if (opcion == 3) {
        for (String v : nombres) {
          System.out.println("Excentricidad de " + v + ": " + algoritmos.obtenerExcentricidad(grafo, grafo.construirComparable(v)));
        }
        System.out.println("Aeropuerto más apropiado (centro del grafo): " + algoritmos.obtenerCentroGrafo(grafo));
      }
    } while (opcion != 0);
  }

  private static String pedirCiudad(Scanner sc, DirectedGraph<String, WeightedEdge> grafo, String mensaje) {
    System.out.print(mensaje);
    String nombre = sc.nextLine().trim().replace(' ', '_');
    return grafo.buscarVertice(x -> x.equalsIgnoreCase(nombre) ? 0 : 1);
  }

  private static Double[][] aObjetos(double[][] m) {
    Double[][] res = new Double[m.length][m.length];
    for (int i = 0; i < m.length; i++)
      for (int j = 0; j < m.length; j++) res[i][j] = m[i][j];
    return res;
  }

  private static Double[][] aObjetos(boolean[][] m) {
    Double[][] res = new Double[m.length][m.length];
    for (int i = 0; i < m.length; i++)
      for (int j = 0; j < m.length; j++) res[i][j] = m[i][j] ? 1.0 : 0.0;
    return res;
  }

  private static String[][] intermediosConNombres(int[][] P, List<String> nombres) {
    String[][] res = new String[P.length][P.length];
    for (int i = 0; i < P.length; i++)
      for (int j = 0; j < P.length; j++) res[i][j] = P[i][j] == -1 ? "-" : nombres.get(P[i][j]);
    return res;
  }
}
