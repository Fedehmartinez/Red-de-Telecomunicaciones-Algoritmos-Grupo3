package ucu.edu.aed.telecom.benchmark;

import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.RecomendacionDC;
import ucu.edu.aed.telecom.servicio.ServicioCDN;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Experimento del Desafío 3: compara recomendarDCSimple (O(V⁴), un Floyd por candidato) con recomendarDC
 * (O(V³), un solo Floyd) en redes del generador de distintos tamaños.
 * - Las redes son mallas ralas, como una red real: E ≈ 3V (un árbol más 2V enlaces extra), con un data center
 *   cada 25 nodos. La semilla es V, así cada tamaño siempre da la misma red.
 * - Antes de medir se verifica que las dos versiones den lo mismo.
 * - Para cada tamaño: CALENTAMIENTO corridas sin medir de cada versión (para que la JVM compile el código) y
 *   después MEDICIONES corridas medidas con System.nanoTime(), alternando las dos versiones. Se guarda la mediana,
 *   que no se deja llevar por una corrida suelta lenta (por ejemplo, si justo pasa el recolector de basura).
 * Uso: BenchmarkCDN [archivo.csv] [V1 V2 ...]. Por defecto resultados/benchmark_cdn.csv y V = 50, 100, 150, 200, 300, 400.
 */
public final class BenchmarkCDN {

    private static final int CALENTAMIENTO = 3;
    private static final int MEDICIONES = 7;

    private BenchmarkCDN() {
    }

    public static void main(String[] args) throws IOException {
        Path salida = Path.of(args.length > 0 ? args[0] : "resultados/benchmark_cdn.csv");
        int[] tamanios = args.length > 1
                ? Arrays.stream(args, 1, args.length).mapToInt(Integer::parseInt).toArray()
                : new int[]{50, 100, 150, 200, 300, 400};

        // calentamiento general con una red chica, para que los primeros tamaños no midan a la JVM arrancando
        ServicioCDN chica = new ServicioCDN(GeneradorRedes.generar(0, 40, 80, 2));
        for (int i = 0; i < 30; i++) {
            chica.recomendarDCSimple();
            chica.recomendarDC();
        }

        List<String> lineas = new ArrayList<>();
        lineas.add("nodos,enlaces,dataCenters,simple_ms,optimizada_ms,aceleracion");
        System.out.println("nodos  enlaces  DCs   simple (ms)  optimizada (ms)  aceleración");

        for (int v : tamanios) {
            int dataCenters = Math.max(1, v / 25);
            RedTelecom red = GeneradorRedes.generar(v, v, 2 * v, dataCenters);
            ServicioCDN cdn = new ServicioCDN(red);
            verificarQueDanLoMismo(cdn, v);

            for (int i = 0; i < CALENTAMIENTO; i++) {
                cdn.recomendarDCSimple();
                cdn.recomendarDC();
            }
            long[] simple = new long[MEDICIONES];
            long[] optimizada = new long[MEDICIONES];
            for (int i = 0; i < MEDICIONES; i++) {
                long inicio = System.nanoTime();
                cdn.recomendarDCSimple();
                simple[i] = System.nanoTime() - inicio;

                inicio = System.nanoTime();
                cdn.recomendarDC();
                optimizada[i] = System.nanoTime() - inicio;
            }

            double msSimple = mediana(simple) / 1e6;
            double msOptimizada = mediana(optimizada) / 1e6;
            double aceleracion = msSimple / msOptimizada;
            lineas.add(String.format(Locale.ROOT, "%d,%d,%d,%.3f,%.3f,%.1f",
                    v, red.enlaces().size(), dataCenters, msSimple, msOptimizada, aceleracion));
            System.out.println(String.format(Locale.ROOT, "%5d  %7d  %3d  %12.3f  %15.3f  %10.1fx",
                    v, red.enlaces().size(), dataCenters, msSimple, msOptimizada, aceleracion));
        }

        if (salida.getParent() != null) Files.createDirectories(salida.getParent());
        Files.write(salida, lineas);
        System.out.println("Resultados guardados en " + salida);
    }

    /** Si la optimizada no da lo mismo que la simple, medir no tiene sentido. */
    private static void verificarQueDanLoMismo(ServicioCDN cdn, int v) {
        RecomendacionDC simple = cdn.recomendarDCSimple();
        RecomendacionDC optimizada = cdn.recomendarDC();
        boolean iguales = simple.getNodoCandidato().equals(optimizada.getNodoCandidato())
                && simple.getBeneficio() == optimizada.getBeneficio()
                && simple.getUsuariosBeneficiados() == optimizada.getUsuariosBeneficiados();
        if (!iguales) {
            throw new IllegalStateException("Con " + v + " nodos las versiones no coinciden: " + simple + " / " + optimizada);
        }
    }

    private static double mediana(long[] tiempos) {
        long[] ordenados = tiempos.clone();
        Arrays.sort(ordenados);
        int medio = ordenados.length / 2;
        return ordenados.length % 2 == 1 ? ordenados[medio] : (ordenados[medio - 1] + ordenados[medio]) / 2.0;
    }
}
