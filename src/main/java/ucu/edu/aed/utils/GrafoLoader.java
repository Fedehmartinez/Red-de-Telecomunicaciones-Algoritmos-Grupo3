package ucu.edu.aed.utils;

import ucu.edu.aed.tda.grafo.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Carga un grafo dirigido desde dos archivos de texto:
 * aeropuertos: un vértice por línea.
 * conexiones: "origen,destino,distancia" por línea.
 */
public final class GrafoLoader {

    private GrafoLoader() {
    }

    public static DirectedGraph<String, WeightedEdge> cargar(Path aeropuertos, Path conexiones) throws IOException {
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();

        for (String linea : Files.readAllLines(aeropuertos)) {
            String nombre = linea.trim();
            if (nombre.isEmpty()) continue;
            grafo.agregarVertice(nombre);
        }

        for (String linea : Files.readAllLines(conexiones)) {
            if (linea.trim().isEmpty()) continue;
            String[] campos = linea.split(",");
            String origen = campos[0].trim();
            String destino = campos[1].trim();
            double distancia = Double.parseDouble(campos[2].trim());

            if (!grafo.agregarArista(origen, destino, new WeightedEdge(distancia))) {
                System.out.println("AVISO: no se pudo agregar la arista " + origen + " -> " + destino);
            }
        }
        return grafo;
    }
}
