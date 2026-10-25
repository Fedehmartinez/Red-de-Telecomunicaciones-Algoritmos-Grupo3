package ucu.edu.aed.utils;


import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.Locale;

public final class PrettyGrid {

    private PrettyGrid() {
    }

    // ==== API genérica ====

    public enum Align {LEFT, RIGHT}

    /**
     * Define cómo formatear (y alinear) una celda de tipo T.
     */
    public interface CellFormat<T> {
        String format(T value);

        default Align align() {
            return Align.RIGHT;
        }
    }

    /**
     * Imprime una matriz genérica T[][] con un formateador de celdas.
     */
    public static <T> void print(T[][] a, CellFormat<? super T> fmt, boolean showIndices) {
        if (a == null) {
            System.out.println("(null)");
            return;
        }
        int rows = a.length;
        int cols = 0;
        for (T[] r : a) cols = Math.max(cols, r == null ? 0 : r.length);

        // Construyo matriz de strings y alineaciones por columna
        String[][] s = new String[rows][cols];
        Align[] colAlign = new Align[cols];

        for (int i = 0; i < rows; i++) {
            T[] r = a[i];
            for (int j = 0; j < cols; j++) {
                T v = (r == null || j >= r.length) ? null : r[j];
                String cell = v == null ? "" : fmt.format(v);
                s[i][j] = cell == null ? "" : normalizeDecimal(cell);
                // primera pasada: si no hay alineación definida, usa la del formateador
                if (colAlign[j] == null) colAlign[j] = fmt.align();
            }
        }
        // Ancho por columna
        int[] colW = new int[cols];
        for (int j = 0; j < cols; j++) {
            int w = 1;
            if (showIndices) w = Math.max(w, ("c" + j).length());
            for (int i = 0; i < rows; i++) w = Math.max(w, s[i][j].length());
            colW[j] = w;
            if (colAlign[j] == null) colAlign[j] = Align.RIGHT;
        }

        StringBuilder out = new StringBuilder();
        int rowLabelW = showIndices ? ("r" + Math.max(0, rows - 1)).length() : 0;

        // Encabezado
        if (showIndices) {
            out.append(repeat(' ', rowLabelW)).append("   ");
            for (int j = 0; j < cols; j++) {
                out.append(pad("c" + j, colW[j], Align.RIGHT));
                out.append(j == cols - 1 ? '\n' : ' ');
            }
        }
        // Filas
        for (int i = 0; i < rows; i++) {
            if (showIndices) {
                out.append(pad("r" + i, rowLabelW, Align.RIGHT)).append(" | ");
            }
            for (int j = 0; j < cols; j++) {
                out.append(pad(s[i][j], colW[j], colAlign[j]));
                out.append(j == cols - 1 ? "" : ' ');
            }
            out.append('\n');
        }
        System.out.print(out.toString());
    }

    // ==== Overloads prácticos ====

    /**
     * Ejemplo de formateador numérico genérico para Float/Double/etc.
     */
    public static final CellFormat<Number> NUMBER = new CellFormat<>() {
        @Override
        public String format(Number v) {
            if (v == null) return "";
            double d = v.doubleValue();
            if (Double.isNaN(d)) return "NaN";
            if (!Double.isFinite(d)) return d > 0 ? "∞" : "-∞";
            // 3 decimales compactos
            String s = String.format(Locale.US, "%.3f", d);
            // recorta ceros finales
            if (s.contains(".")) {
                s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
            return s;
        }

        @Override
        public Align align() {
            return Align.RIGHT;
        }
    };

    // ==== Helpers ====

    private static String pad(String s, int w, Align a) {
        s = s == null ? "" : s;
        int n = w - s.length();
        if (n <= 0) return s;
        String spaces = repeat(' ', n);
        return a == Align.RIGHT ? spaces + s : s + spaces;
    }

    private static String repeat(char c, int n) {
        if (n <= 0) return "";
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) b.append(c);
        return b.toString();
    }

    private static String normalizeDecimal(String s) {
        // Para evitar coma como separador en locales no-US
        if (s == null) return "";
        if (!Locale.getDefault().equals(Locale.US)) return s.replace(',', '.');
        return s;
    }


    // === NUEVO: impresión con etiquetas personalizadas ===
    public static <T> void printLabeled(
            T[][] a,
            CellFormat<? super T> fmt,
            java.util.List<String> colLabels,
            java.util.List<String> rowLabels,
            String cornerLabel
    ) {
        if (a == null) {
            System.out.println("(null)");
            return;
        }

        final int rows = a.length;
        int cols = 0;
        for (T[] r : a) cols = Math.max(cols, r == null ? 0 : r.length);

        // Normalizar labels a tamaño real
        java.util.List<String> colL = colLabels != null ? colLabels : java.util.Collections.emptyList();
        java.util.List<String> rowL = rowLabels != null ? rowLabels : java.util.Collections.emptyList();
        String corner = cornerLabel == null ? "" : cornerLabel;

        if (colL.size() < cols) {
            // autocompletar
            java.util.ArrayList<String> tmp = new java.util.ArrayList<>(colL);
            for (int j = colL.size(); j < cols; j++) tmp.add("c" + j);
            colL = tmp;
        }
        if (rowL.size() < rows) {
            java.util.ArrayList<String> tmp = new java.util.ArrayList<>(rowL);
            for (int i = rowL.size(); i < rows; i++) tmp.add("r" + i);
            rowL = tmp;
        }

        // Construcción de celdas como strings
        String[][] s = new String[rows][cols];
        Align[] colAlign = new Align[cols];

        for (int i = 0; i < rows; i++) {
            T[] r = a[i];
            for (int j = 0; j < cols; j++) {
                T v = (r == null || j >= r.length) ? null : r[j];
                String cell = v == null ? "" : fmt.format(v);
                s[i][j] = cell == null ? "" : normalizeDecimal(cell);
                if (colAlign[j] == null) colAlign[j] = fmt.align();
            }
        }

        // Ancho por columna considerando encabezados
        int[] colW = new int[cols];
        for (int j = 0; j < cols; j++) {
            int w = Math.max(1, colL.get(j).length());
            for (int i = 0; i < rows; i++) w = Math.max(w, s[i][j].length());
            colW[j] = w;
            if (colAlign[j] == null) colAlign[j] = Align.RIGHT;
        }

        // Ancho de columna para labels de fila (la primera "columna fantasma")
        int rowLabelW = 0;
        for (int i = 0; i < rows; i++) rowLabelW = Math.max(rowLabelW, rowL.get(i).length());
        rowLabelW = Math.max(rowLabelW, corner.length());

        StringBuilder out = new StringBuilder();

        // Encabezado
        out.append(pad(corner, rowLabelW, Align.RIGHT)).append("   ");
        for (int j = 0; j < cols; j++) {
            out.append(pad(colL.get(j), colW[j], Align.RIGHT));
            out.append(j == cols - 1 ? '\n' : ' ');
        }

        // Filas
        for (int i = 0; i < rows; i++) {
            out.append(pad(rowL.get(i), rowLabelW, Align.RIGHT)).append(" | ");
            for (int j = 0; j < cols; j++) {
                out.append(pad(s[i][j], colW[j], colAlign[j]));
                out.append(j == cols - 1 ? "" : ' ');
            }
            out.append('\n');
        }

        System.out.print(out.toString());
    }

    public static <T> void printLabeled(
            T[][] a,
            CellFormat<? super T> fmt,
            java.util.List<String> colLabels,
            java.util.List<String> rowLabels
    ) {
        printLabeled(a, fmt, colLabels, rowLabels, "");
    }

    public static <V, D> void printGraphMatrix(
            IGraph<V, D> graph,
            java.util.function.Function<V, String> vertexLabel,
            java.util.Comparator<V> ordering,
            String diagonalMark,
            String noEdgeMark,
            CellFormat<? super String> cellFmt
    ) {
        // ordenar vértices si nos dan un comparator
        java.util.ArrayList<V> vs = new java.util.ArrayList<>(graph.vertices());
        if (ordering != null) vs.sort(ordering);

        int n = vs.size();
        String[][] cells = new String[n][n];

        for (int i = 0; i < n; i++) {
            V src = vs.get(i);
            var sc = graph.construirComparable(src);
            for (int j = 0; j < n; j++) {
                V trg = vs.get(j);
                if (i == j) {
                    cells[i][j] = diagonalMark;
                    continue;
                }
                Edge<V, D> e = graph.obtenerArista(sc, graph.construirComparable(trg));
                cells[i][j] = (e == null) ? noEdgeMark : String.valueOf(e.dato());
            }
        }

        // labels de filas/columnas desde el mapper provisto
        java.util.List<String> labels = vs.stream()
                .map(v -> vertexLabel == null ? String.valueOf(v) : vertexLabel.apply(v))
                .toList();

        // Imprimir con labels personalizados
        printLabeled(cells,
                cellFmt != null ? cellFmt : Object::toString,
                labels,
                labels,
                ""); // esquina
    }

    // === NUEVO: overload cómodo con defaults razonables ===
    public static <V, D> void printGraphMatrix(IGraph<V, D> graph) {
        PrettyGrid.printGraphMatrix(graph, v -> "V[" + v + "]");
    }

    public static <V, D> void printGraphMatrix(
            IGraph<V, D> graph,
            java.util.function.Function<V, String> vertexLabel
    ) {
        printGraphMatrix(
                graph,
                vertexLabel,
                null,        // ordering
                "·",         // diagonal
                "-",         // no edge
                Object::toString
        );
    }


}