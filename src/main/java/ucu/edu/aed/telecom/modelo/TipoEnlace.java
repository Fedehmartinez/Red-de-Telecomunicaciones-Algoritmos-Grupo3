package ucu.edu.aed.telecom.modelo;

/**
 * Medio físico de un enlace. Solo guarda el rango de ancho de banda (en Mbps) que soporta, para validar
 * los datos: el ancho de banda real, la latencia y el costo son de cada enlace.
 */
public enum TipoEnlace {
    FIBRA(100, 100000),
    COBRE(10, 1000),
    RADIOENLACE(10, 2000);

    private final double anchoBandaMinimo;
    private final double anchoBandaMaximo;

    TipoEnlace(double anchoBandaMinimo, double anchoBandaMaximo) {
        this.anchoBandaMinimo = anchoBandaMinimo;
        this.anchoBandaMaximo = anchoBandaMaximo;
    }

    public double getAnchoBandaMinimo() {
        return anchoBandaMinimo;
    }

    public double getAnchoBandaMaximo() {
        return anchoBandaMaximo;
    }

    /** true si el ancho de banda está dentro del rango del medio (bordes incluidos). */
    public boolean admite(double anchoBanda) {
        return anchoBanda >= anchoBandaMinimo && anchoBanda <= anchoBandaMaximo;
    }
}
