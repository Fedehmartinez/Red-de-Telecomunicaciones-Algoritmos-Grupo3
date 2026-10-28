package ucu.edu.aed.telecom.resultado;

import java.util.List;

import ucu.edu.aed.telecom.modelo.Enlace;

/**
 * El cableado más barato: los enlaces a instalar y su costo total.
 * Si conexo es false no se pudieron unir todos los nodos (no hay árbol abarcador): los enlaces son los del
 * bosque de costo mínimo, uno por cada parte que sí se puede conectar.
 */
public class ResultadoCableado {

    private final List<Enlace> enlaces;
    private final double costoTotal;
    private final boolean conexo;

    public ResultadoCableado(List<Enlace> enlaces, double costoTotal, boolean conexo) {
        this.enlaces = List.copyOf(enlaces);
        this.costoTotal = costoTotal;
        this.conexo = conexo;
    }

    /** Los enlaces elegidos (lista inmodificable). */
    public List<Enlace> getEnlaces() {
        return enlaces;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public boolean existe() {
        return conexo;
    }
}
