package ucu.edu.aed.telecom.resultado;

/**
 * Dónde conviene poner un data center nuevo: el nodo candidato, su beneficio (usuarios por mejora de latencia,
 * en usuarios·ms) y cuántos usuarios pasan a tener menos latencia.
 */
public class RecomendacionDC {

    private final String nodoCandidato;
    private final double beneficio;
    private final int usuariosBeneficiados;

    public RecomendacionDC(String nodoCandidato, double beneficio, int usuariosBeneficiados) {
        this.nodoCandidato = nodoCandidato;
        this.beneficio = beneficio;
        this.usuariosBeneficiados = usuariosBeneficiados;
    }

    public String getNodoCandidato() {
        return nodoCandidato;
    }

    public double getBeneficio() {
        return beneficio;
    }

    public int getUsuariosBeneficiados() {
        return usuariosBeneficiados;
    }

    @Override
    public String toString() {
        return nodoCandidato + " (beneficio " + beneficio + ", " + usuariosBeneficiados + " usuarios mejoran)";
    }
}
