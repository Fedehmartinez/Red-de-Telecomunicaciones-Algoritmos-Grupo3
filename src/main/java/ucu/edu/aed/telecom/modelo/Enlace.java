package ucu.edu.aed.telecom.modelo;

import java.util.Objects;

// Un cable entre dos nodos (por id). Guarda la latencia en cada sentido, el ancho de banda y el costo.
// La latencia sirve para el ruteo y el costo para el cableado; el ancho de banda se usa para filtrar enlaces.
// Unidades: ms, Mbps y miles de USD. Dos enlaces son iguales si unen el mismo par, sin importar el orden.
public class Enlace {

    private final String a;
    private final String b;
    private final TipoEnlace tipo;
    private final double latenciaAB;
    private final double latenciaBA;
    private final double anchoBanda;
    private final double costo;

    public Enlace(String a, String b, TipoEnlace tipo, double latenciaAB, double latenciaBA, double anchoBanda, double costo) {
        if (a == null || b == null) throw new IllegalArgumentException("El enlace necesita sus dos extremos");
        if (a.equals(b)) throw new IllegalArgumentException("Un enlace no puede unir un nodo consigo mismo: " + a);
        if (tipo == null) throw new IllegalArgumentException("El enlace " + a + "-" + b + " necesita un tipo");
        if (!positivoFinito(latenciaAB) || !positivoFinito(latenciaBA)) {
            throw new IllegalArgumentException("Las latencias del enlace " + a + "-" + b + " tienen que ser positivas");
        }
        if (!tipo.admite(anchoBanda)) {
            throw new IllegalArgumentException("Ancho de banda " + anchoBanda + " fuera del rango de " + tipo
                    + " en el enlace " + a + "-" + b);
        }
        if (!(costo >= 0 && costo < Double.POSITIVE_INFINITY)) {   // también rechaza NaN
            throw new IllegalArgumentException("Costo inválido en el enlace " + a + "-" + b);
        }
        this.a = a;
        this.b = b;
        this.tipo = tipo;
        this.latenciaAB = latenciaAB;
        this.latenciaBA = latenciaBA;
        this.anchoBanda = anchoBanda;
        this.costo = costo;
    }

    private static boolean positivoFinito(double x) {
        return x > 0 && x < Double.POSITIVE_INFINITY;   // NaN da false en las dos comparaciones
    }

    public String getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public TipoEnlace getTipo() {
        return tipo;
    }

    public double getLatenciaAB() {
        return latenciaAB;
    }

    public double getLatenciaBA() {
        return latenciaBA;
    }

    public double getAnchoBanda() {
        return anchoBanda;
    }

    public double getCosto() {
        return costo;
    }

    // Latencia saliendo desde el extremo origen hacia el otro
    public double latenciaDesde(String origen) {
        if (a.equals(origen)) return latenciaAB;
        if (b.equals(origen)) return latenciaBA;
        throw new IllegalArgumentException(origen + " no es extremo del enlace " + a + "-" + b);
    }

    public String otroExtremo(String id) {
        if (a.equals(id)) return b;
        if (b.equals(id)) return a;
        throw new IllegalArgumentException(id + " no es extremo del enlace " + a + "-" + b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enlace otro)) return false;
        return (a.equals(otro.a) && b.equals(otro.b)) || (a.equals(otro.b) && b.equals(otro.a));
    }

    // Mismo hash sin importar el orden de los extremos
    @Override
    public int hashCode() {
        int h1 = Objects.hashCode(a);
        int h2 = Objects.hashCode(b);
        return (h1 ^ h2) + (h1 + h2);
    }

    @Override
    public String toString() {
        return a + "-" + b + " (" + tipo + ")";
    }
}
