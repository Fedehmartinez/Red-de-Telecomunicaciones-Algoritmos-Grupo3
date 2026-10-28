package ucu.edu.aed.telecom.modelo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Carga una RedTelecom desde dos CSV: nodos (id,tipo,capacidad,usuarios) y
// enlaces (a,b,tipo,latenciaAB,latenciaBA,anchoBanda,costo).
// Ignora las líneas vacías y las que empiezan con #, y saltea el encabezado.
// Si una línea está mal tira IllegalArgumentException con el archivo y el número de línea.
public final class CargadorRed {

    private static final int CAMPOS_NODO = 4;
    private static final int CAMPOS_ENLACE = 7;

    private CargadorRed() {
    }

    public static RedTelecom cargar(Path nodos, Path enlaces) throws IOException {
        RedTelecom red = new RedTelecom();

        boolean encabezado = true;
        List<String> lineas = Files.readAllLines(nodos);
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (esComentarioOVacia(linea)) continue;
            if (encabezado) {
                encabezado = false;
                continue;
            }
            try {
                if (!red.agregarNodo(leerNodo(linea))) {
                    throw new IllegalArgumentException("nodo repetido");
                }
            } catch (IllegalArgumentException e) {
                throw errorEn(nodos, i + 1, e);
            }
        }

        encabezado = true;
        lineas = Files.readAllLines(enlaces);
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (esComentarioOVacia(linea)) continue;
            if (encabezado) {
                encabezado = false;
                continue;
            }
            try {
                if (!red.agregarEnlace(leerEnlace(linea))) {
                    throw new IllegalArgumentException("enlace repetido o con un extremo que no existe");
                }
            } catch (IllegalArgumentException e) {
                throw errorEn(enlaces, i + 1, e);
            }
        }
        return red;
    }

    private static boolean esComentarioOVacia(String linea) {
        return linea.isBlank() || linea.startsWith("#");
    }

    private static IllegalArgumentException errorEn(Path archivo, int numeroDeLinea, IllegalArgumentException causa) {
        return new IllegalArgumentException(archivo.getFileName() + ", línea " + numeroDeLinea + ": " + causa.getMessage(), causa);
    }

    private static Nodo leerNodo(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length != CAMPOS_NODO) {
            throw new IllegalArgumentException("se esperaban " + CAMPOS_NODO + " campos y hay " + campos.length);
        }
        TipoNodo tipo = leerTipoNodo(campos[1]);
        int capacidad = leerEntero(campos[2], "capacidad");
        int usuarios = leerEntero(campos[3], "usuarios");
        return new Nodo(campos[0], tipo, capacidad, usuarios);
    }

    private static Enlace leerEnlace(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length != CAMPOS_ENLACE) {
            throw new IllegalArgumentException("se esperaban " + CAMPOS_ENLACE + " campos y hay " + campos.length);
        }
        TipoEnlace tipo = leerTipoEnlace(campos[2]);
        double latenciaAB = leerNumero(campos[3], "latenciaAB");
        double latenciaBA = leerNumero(campos[4], "latenciaBA");
        double anchoBanda = leerNumero(campos[5], "anchoBanda");
        double costo = leerNumero(campos[6], "costo");
        return new Enlace(campos[0], campos[1], tipo, latenciaAB, latenciaBA, anchoBanda, costo);
    }

    private static TipoNodo leerTipoNodo(String texto) {
        try {
            return TipoNodo.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo de nodo desconocido: " + texto);
        }
    }

    private static TipoEnlace leerTipoEnlace(String texto) {
        try {
            return TipoEnlace.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo de enlace desconocido: " + texto);
        }
    }

    private static int leerEntero(String texto, String campo) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " inválido: " + texto);
        }
    }

    private static double leerNumero(String texto, String campo) {
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " inválido: " + texto);
        }
    }
}
