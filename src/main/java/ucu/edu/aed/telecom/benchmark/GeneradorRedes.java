package ucu.edu.aed.telecom.benchmark;

import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Arma redes al azar para los experimentos y los tests (con la misma semilla sale siempre la misma red).
// Los primeros nodos son data centers y el resto puntos de acceso o routers.
// Primero arma un árbol para que la red sea conexa y después agrega enlaces extra.
public final class GeneradorRedes {

    private GeneradorRedes() {
    }

    // Genera una red con cantidadNodos nodos y cantidadNodos - 1 + enlacesExtra enlaces.
    // Tira IllegalArgumentException si los números no tienen sentido.
    public static RedTelecom generar(long semilla, int cantidadNodos, int enlacesExtra, int cantidadDataCenters) {
        if (cantidadNodos < 1) throw new IllegalArgumentException("Tiene que haber al menos un nodo");
        if (cantidadDataCenters < 0 || cantidadDataCenters > cantidadNodos) {
            throw new IllegalArgumentException("Cantidad de data centers inválida: " + cantidadDataCenters);
        }
        long maximoExtra = (long) cantidadNodos * (cantidadNodos - 1) / 2 - (cantidadNodos - 1);
        if (enlacesExtra < 0 || enlacesExtra > maximoExtra) {
            throw new IllegalArgumentException("Con " + cantidadNodos + " nodos entran entre 0 y " + maximoExtra + " enlaces extra");
        }

        Random random = new Random(semilla);
        RedTelecom red = new RedTelecom();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < cantidadDataCenters; i++) {
            red.agregarNodo(new Nodo("DC" + i, TipoNodo.DATA_CENTER, 1000, 0));
            ids.add("DC" + i);
        }
        for (int i = 0; i < cantidadNodos - cantidadDataCenters; i++) {
            String id = "N" + i;
            if (random.nextBoolean()) {
                int capacidad = 100 + random.nextInt(4901);
                red.agregarNodo(new Nodo(id, TipoNodo.PUNTO_ACCESO, capacidad, 1 + random.nextInt(capacidad)));
            } else {
                red.agregarNodo(new Nodo(id, TipoNodo.ROUTER, 100, 0));
            }
            ids.add(id);
        }

        // árbol al azar: así la red es conexa
        List<String> orden = new ArrayList<>(ids);
        for (int i = orden.size(); i > 1; i--) {   // mezcla de Fisher-Yates
            int j = random.nextInt(i);
            String auxiliar = orden.get(i - 1);
            orden.set(i - 1, orden.get(j));
            orden.set(j, auxiliar);
        }
        for (int i = 1; i < orden.size(); i++) {
            red.agregarEnlace(enlaceAlAzar(orden.get(i), orden.get(random.nextInt(i)), random));
        }

        // enlaces de más entre pares que todavía no están unidos
        int agregados = 0;
        while (agregados < enlacesExtra) {
            String a = ids.get(random.nextInt(ids.size()));
            String b = ids.get(random.nextInt(ids.size()));
            if (a.equals(b) || red.enlace(a, b) != null) continue;
            red.agregarEnlace(enlaceAlAzar(a, b, random));
            agregados++;
        }
        return red;
    }

    private static Enlace enlaceAlAzar(String a, String b, Random random) {
        double latenciaAB = 1 + random.nextInt(20);
        double latenciaBA = random.nextDouble() < 0.3 ? 1 + random.nextInt(20) : latenciaAB;   // a veces asimétrico
        return new Enlace(a, b, TipoEnlace.FIBRA, latenciaAB, latenciaBA, 1000, 1 + random.nextInt(100));
    }
}
