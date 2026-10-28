package ucu.edu.aed.telecom.modelo;

// Medio físico del enlace. Guarda el rango de ancho de banda (Mbps) que soporta, para validar los datos
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

    // true si el ancho de banda entra en el rango del medio (bordes incluidos)
    public boolean admite(double anchoBanda) {
        return anchoBanda >= anchoBandaMinimo && anchoBanda <= anchoBandaMaximo;
    }
}
