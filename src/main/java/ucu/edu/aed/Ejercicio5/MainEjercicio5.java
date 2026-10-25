package ucu.edu.aed.Ejercicio5;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.DirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.Path;

/**
 * Ejercicio 5 parte 2: todos los caminos entre Montevideo y Porto Alegre.
 * Usa el mismo grafo que la parte 1 (Cuadro 2). Las respuestas escritas están en Ejercicio5Parte2.txt.
 */
public class MainEjercicio5 {

    /** Grafo del Cuadro 2 (Ejercicio 3), con los adyacentes en el orden de las columnas de la tabla. */
    public static DirectedGraph<String, WeightedEdge> grafoCuadro2() {
        DirectedGraph<String, WeightedEdge> g = vertices("Asuncion", "Buenos_Aires", "Curitiba", "Montevideo",
                "Porto_Alegre", "Rio_de_Janeiro", "San_Pablo", "Santos");
        arista(g, "Asuncion", "Buenos_Aires", 1600);
        arista(g, "Asuncion", "Curitiba", 800);
        arista(g, "Asuncion", "Porto_Alegre", 700);
        arista(g, "Buenos_Aires", "Asuncion", 1600);
        arista(g, "Buenos_Aires", "Montevideo", 200);
        arista(g, "Buenos_Aires", "Porto_Alegre", 1250);
        arista(g, "Curitiba", "Rio_de_Janeiro", 1500);
        arista(g, "Montevideo", "Buenos_Aires", 200);
        arista(g, "Montevideo", "Porto_Alegre", 1000);
        arista(g, "Montevideo", "San_Pablo", 980);
        arista(g, "Montevideo", "Santos", 1200);
        arista(g, "Rio_de_Janeiro", "Curitiba", 1500);
        arista(g, "Rio_de_Janeiro", "San_Pablo", 1800);
        arista(g, "Rio_de_Janeiro", "Santos", 1900);
        arista(g, "San_Pablo", "Asuncion", 1200);
        arista(g, "San_Pablo", "Curitiba", 600);
        arista(g, "San_Pablo", "Montevideo", 2000);
        arista(g, "San_Pablo", "Porto_Alegre", 980);
        arista(g, "San_Pablo", "Rio_de_Janeiro", 1800);
        arista(g, "San_Pablo", "Santos", 220);
        arista(g, "Santos", "Porto_Alegre", 1200);
        arista(g, "Santos", "San_Pablo", 220);
        return g;
    }

    private static DirectedGraph<String, WeightedEdge> vertices(String... nombres) {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        for (String n : nombres) g.agregarVertice(n);
        return g;
    }

    private static void arista(DirectedGraph<String, WeightedEdge> g, String origen, String destino, double peso) {
        g.agregarArista(origen, destino, new WeightedEdge(peso));
    }

    public static void main(String[] args) {
        DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();
        DirectedGraph<String, WeightedEdge> g = grafoCuadro2();

        List<Path<String>> caminos = algoritmos.obtenerTodosLosCaminos(
                g.construirComparable("Montevideo"), g.construirComparable("Porto_Alegre"), g);

        System.out.println("Caminos de Montevideo a Porto_Alegre (en el orden en que los encuentra la BPF):");
        for (int i = 0; i < caminos.size(); i++) {
            Path<String> c = caminos.get(i);
            System.out.println("  " + (i + 1) + ") " + String.join(" -> ", c.getPath()) + "  costo=" + c.getCost());
        }

        List<Path<String>> ordenados = new ArrayList<>(caminos);
        ordenados.sort(Comparator.comparingDouble(Path::getCost));
        System.out.println("\nTotal: " + caminos.size() + " caminos");
        System.out.println("Menor costo: " + String.join(" -> ", ordenados.get(0).getPath())
                + " (" + ordenados.get(0).getCost() + ")");
        Path<String> mayor = ordenados.get(ordenados.size() - 1);
        System.out.println("Mayor costo: " + String.join(" -> ", mayor.getPath()) + " (" + mayor.getCost() + ")");
    }
}
