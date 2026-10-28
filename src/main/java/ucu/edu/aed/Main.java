package ucu.edu.aed;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ucu.edu.aed.telecom.modelo.CargadorRed;
import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.Nodo;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.modelo.TipoEnlace;
import ucu.edu.aed.telecom.modelo.TipoNodo;
import ucu.edu.aed.telecom.resultado.AsignacionCDN;
import ucu.edu.aed.telecom.resultado.ImpactoFalla;
import ucu.edu.aed.telecom.resultado.ResultadoCableado;
import ucu.edu.aed.telecom.resultado.RutaResultado;
import ucu.edu.aed.telecom.resultado.TablaRuteo;
import ucu.edu.aed.telecom.servicio.ServicioCDN;
import ucu.edu.aed.telecom.servicio.ServicioCableado;
import ucu.edu.aed.telecom.servicio.ServicioConsultas;
import ucu.edu.aed.telecom.servicio.ServicioResiliencia;
import ucu.edu.aed.telecom.servicio.ServicioRuteo;

/**
 * Demostración del sistema con la red de ejemplo: ruteo, cableado, resiliencia, CDN y las cinco consultas.
 * Uso: Main [nodos.csv] [enlaces.csv] (por defecto, los de src/main/resources/telecom). Correr desde la raíz
 * del proyecto.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Path nodos = Path.of(args.length > 0 ? args[0] : "src/main/resources/telecom/nodos.csv");
        Path enlaces = Path.of(args.length > 1 ? args[1] : "src/main/resources/telecom/enlaces.csv");
        RedTelecom red = CargadorRed.cargar(nodos, enlaces);

        ServicioRuteo ruteo = new ServicioRuteo(red);
        ServicioCableado cableado = new ServicioCableado(red);
        ServicioResiliencia resiliencia = new ServicioResiliencia(red);
        ServicioCDN cdn = new ServicioCDN(red);
        ServicioConsultas consultas = new ServicioConsultas(ruteo, resiliencia, cdn);

        mostrarRed(red);
        mostrarRuteo(ruteo, red);
        mostrarCableado(cableado, red);
        mostrarResiliencia(resiliencia, ruteo);
        mostrarCDN(cdn, red);
        mostrarConsultas(consultas);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    /** Latencia en ms, o "sin camino" si es infinita. */
    private static String ms(double latencia) {
        return latencia == Double.POSITIVE_INFINITY ? "sin camino" : String.format(Locale.ROOT, "%.1f ms", latencia);
    }

    private static void mostrarRed(RedTelecom red) {
        titulo("Red de ejemplo");
        int usuarios = 0;
        for (Nodo n : red.nodos()) usuarios += n.getUsuarios();
        System.out.println(red.nodos().size() + " nodos, " + red.enlaces().size() + " enlaces, " + usuarios + " usuarios");
        System.out.println("Data centers: " + red.dataCenters());
    }

    private static void mostrarRuteo(ServicioRuteo ruteo, RedTelecom red) {
        titulo("Ruteo (Dijkstra y Floyd sobre el grafo dirigido de latencias)");
        System.out.println("Menor latencia AP1 -> AP6: " + ruteo.rutaMenorLatencia("AP1", "AP6"));
        System.out.println("Latencia asimétrica: AP3 -> SW2 " + ruteo.rutaMenorLatencia("AP3", "SW2")
                + " / SW2 -> AP3 " + ruteo.rutaMenorLatencia("SW2", "AP3"));

        TablaRuteo tabla = ruteo.tablaDeRuteo();
        System.out.println("Tabla de ruteo de R1 hacia los data centers (Floyd):");
        for (Nodo dc : red.dataCenters()) {
            System.out.println("  hacia " + dc.getId() + ": siguiente salto " + tabla.siguienteSalto("R1", dc.getId())
                    + ", " + ms(tabla.latencia("R1", dc.getId())) + ", camino " + tabla.camino("R1", dc.getId()));
        }
        System.out.println("¿Hay camino de OF5 a DC2? (Warshall) " + (ruteo.hayCamino("OF5", "DC2") ? "sí" : "no"));
    }

    private static void mostrarCableado(ServicioCableado cableado, RedTelecom red) {
        titulo("Cableado mínimo (Kruskal sobre el grafo no dirigido de costos)");
        double costoTodos = 0;
        for (Enlace e : red.enlaces()) costoTodos += e.getCosto();
        ResultadoCableado minimo = cableado.cableadoMinimo();
        System.out.println("Red completa: " + minimo.getEnlaces().size() + " enlaces, costo " + minimo.getCostoTotal()
                + " (todos los enlaces cuestan " + costoTodos + "), conexo: " + (minimo.existe() ? "sí" : "no"));

        // planificación de oficinas nuevas: las oficinas y las conexiones posibles con su costo
        RedTelecom plan = new RedTelecom();
        for (String id : List.of("SEDE", "NORTE", "SUR", "ESTE", "OESTE")) plan.agregarNodo(new Nodo(id, TipoNodo.OFICINA, 100, 0));
        plan.agregarEnlace(new Enlace("SEDE", "NORTE", TipoEnlace.FIBRA, 2, 2, 1000, 40));
        plan.agregarEnlace(new Enlace("SEDE", "SUR", TipoEnlace.FIBRA, 2, 2, 1000, 35));
        plan.agregarEnlace(new Enlace("SEDE", "ESTE", TipoEnlace.FIBRA, 3, 3, 1000, 50));
        plan.agregarEnlace(new Enlace("NORTE", "ESTE", TipoEnlace.COBRE, 4, 4, 500, 15));
        plan.agregarEnlace(new Enlace("SUR", "OESTE", TipoEnlace.RADIOENLACE, 6, 6, 200, 12));
        plan.agregarEnlace(new Enlace("NORTE", "OESTE", TipoEnlace.FIBRA, 5, 5, 1000, 45));
        plan.agregarEnlace(new Enlace("ESTE", "SUR", TipoEnlace.COBRE, 4, 4, 500, 30));
        ResultadoCableado oficinas = new ServicioCableado(plan).cableadoMinimo();
        System.out.println("Oficinas nuevas: instalar " + oficinas.getEnlaces() + ", costo " + oficinas.getCostoTotal());
    }

    private static void mostrarResiliencia(ServicioResiliencia resiliencia, ServicioRuteo ruteo) {
        titulo("Resiliencia (grafo no dirigido sin peso)");
        System.out.println("Equipos críticos (puntos de articulación): " + resiliencia.nodosCriticos());
        System.out.println("Enlaces críticos (puentes): " + resiliencia.enlacesCriticos());
        System.out.println("Si se corta R5-RR1: " + resiliencia.impactoFallaEnlace("R5", "RR1"));
        System.out.println("Si caen los tres data centers, incomunicados: "
                + resiliencia.incomunicados(Set.of("DC1", "DC2", "DC3"), Set.of()).size() + " nodos");

        RutaResultado normal = ruteo.rutaMenorLatencia("AP1", "AP4");
        System.out.println("Ruta AP1 -> AP4: " + normal);
        System.out.println("Ruta alternativa si cae SW2: " + ruteo.rutaAlternativa("AP1", "AP4", Set.of("SW2"), Set.of()));
    }

    private static void mostrarCDN(ServicioCDN cdn, RedTelecom red) {
        titulo("CDN (data center de menor latencia DC -> nodo, con Floyd)");
        AsignacionCDN asignacion = cdn.asignarCDN();
        for (Nodo n : red.nodos()) {
            if (n.getTipo().esDeAcceso()) {
                System.out.println("  " + n.getId() + " (" + n.getUsuarios() + " usuarios): " + asignacion.dcDe(n.getId())
                        + ", " + ms(asignacion.latenciaDe(n.getId())));
            }
        }
        System.out.println("Nuevo data center recomendado (beneficio ponderado por usuarios): " + cdn.recomendarDCSimple());
        System.out.println("  con la versión optimizada (un solo Floyd, Desafío 3): " + cdn.recomendarDC());
        System.out.println("Centro de la red (minimiza la peor latencia desde el nodo): " + cdn.centroParaDC());
    }

    private static void mostrarConsultas(ServicioConsultas consultas) {
        titulo("Consultas");
        System.out.println("1. Ruta SW2 -> SW3 sin mínimo: " + consultas.rutaConAnchoBandaMinimo("SW2", "SW3", 0));
        System.out.println("   con al menos 1000 Mbps: " + consultas.rutaConAnchoBandaMinimo("SW2", "SW3", 1000));
        System.out.println("2. Si cae R5: " + consultas.queOcurreSiCae("R5"));
        System.out.println("3. Ranking de nodos críticos por usuarios afectados:");
        List<ImpactoFalla> ranking = consultas.rankingNodosCriticos();
        for (int i = 0; i < Math.min(5, ranking.size()); i++) {
            ImpactoFalla impacto = ranking.get(i);
            System.out.println("   " + (i + 1) + ") " + impacto.getElementoCaido() + ": " + impacto.getUsuariosAfectados() + " usuarios");
        }
        System.out.println("4. Diámetro de la red: " + consultas.diametro());
        System.out.println("5. Nodos de acceso a más de 30 ms de su data center: " + consultas.nodosQueSuperanLatencia(30));
    }
}
