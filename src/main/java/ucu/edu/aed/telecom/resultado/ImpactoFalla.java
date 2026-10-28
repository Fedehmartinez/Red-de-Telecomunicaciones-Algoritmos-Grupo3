package ucu.edu.aed.telecom.resultado;

import java.util.LinkedHashSet;
import java.util.Set;

// Qué pasa cuando falla un nodo o un enlace: guarda los nodos que quedan incomunicados por esa falla
// (sin contar los que ya lo estaban) y los usuarios afectados, que incluyen los del nodo caído si es de acceso
public class ImpactoFalla {

    private final String elementoCaido;
    private final Set<String> nodosIncomunicados;
    private final int usuariosAfectados;

    public ImpactoFalla(String elementoCaido, Set<String> nodosIncomunicados, int usuariosAfectados) {
        this.elementoCaido = elementoCaido;
        this.nodosIncomunicados = new LinkedHashSet<>(nodosIncomunicados);
        this.usuariosAfectados = usuariosAfectados;
    }

    // Id del nodo caído, o "a-b" si es un enlace
    public String getElementoCaido() {
        return elementoCaido;
    }

    public Set<String> getNodosIncomunicados() {
        return nodosIncomunicados;
    }

    public int getUsuariosAfectados() {
        return usuariosAfectados;
    }

    @Override
    public String toString() {
        return elementoCaido + ": " + usuariosAfectados + " usuarios afectados, incomunicados " + nodosIncomunicados;
    }
}
