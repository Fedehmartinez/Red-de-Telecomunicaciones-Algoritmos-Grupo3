package ucu.edu.aed.telecom.resultado;

// Diámetro de la red: el par de nodos con la mayor latencia mínima entre ellos (infinita si algún par no tiene camino)
public class ResultadoDiametro {

    private final String desde;
    private final String hasta;
    private final double latencia;

    public ResultadoDiametro(String desde, String hasta, double latencia) {
        this.desde = desde;
        this.hasta = hasta;
        this.latencia = latencia;
    }

    public String getDesde() {
        return desde;
    }

    public String getHasta() {
        return hasta;
    }

    public double getLatencia() {
        return latencia;
    }

    @Override
    public String toString() {
        return desde + " -> " + hasta + " (" + latencia + " ms)";
    }
}
