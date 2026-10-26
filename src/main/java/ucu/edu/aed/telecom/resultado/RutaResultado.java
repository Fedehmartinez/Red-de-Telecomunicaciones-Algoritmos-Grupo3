package ucu.edu.aed.telecom.resultado;

import java.util.List;

/**
 * Una ruta por la red: los nodos por los que pasa (origen y destino incluidos), cuánto tarda en total y el
 * ancho de banda del enlace más angosto, que es lo máximo que puede pasar por toda la ruta.
 * Convenciones: sin ruta = lista vacía, latencia infinita y cuello 0; ruta de un solo nodo (origen igual a
 * destino) = latencia 0 y cuello infinito, porque no atraviesa ningún enlace.
 */
public class RutaResultado {

    private final List<String> nodos;
    private final double latenciaTotal;
    private final double anchoBandaCuelloBotella;

    public RutaResultado(List<String> nodos, double latenciaTotal, double anchoBandaCuelloBotella) {
        this.nodos = List.copyOf(nodos);
        this.latenciaTotal = latenciaTotal;
        this.anchoBandaCuelloBotella = anchoBandaCuelloBotella;
    }

    public static RutaResultado sinRuta() {
        return new RutaResultado(List.of(), Double.POSITIVE_INFINITY, 0);
    }

    public boolean existe() {
        return !nodos.isEmpty();
    }

    /** Los nodos de la ruta en orden (lista inmodificable). */
    public List<String> getNodos() {
        return nodos;
    }

    public double getLatenciaTotal() {
        return latenciaTotal;
    }

    public double getAnchoBandaCuelloBotella() {
        return anchoBandaCuelloBotella;
    }

    @Override
    public String toString() {
        if (!existe()) return "sin ruta";
        return String.join(" -> ", nodos) + " (latencia " + latenciaTotal + " ms, cuello " + anchoBandaCuelloBotella + " Mbps)";
    }
}
