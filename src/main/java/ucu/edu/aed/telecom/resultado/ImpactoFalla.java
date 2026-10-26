package ucu.edu.aed.telecom.resultado;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Qué pasa cuando falla un elemento (un nodo o un enlace):
 * - nodosIncomunicados: los nodos que siguen andando pero, por esta falla, ya no llegan a ningún data center
 *   (no incluye el elemento caído ni los que ya estaban incomunicados antes de la falla);
 * - usuariosAfectados: los usuarios de esos nodos más los del propio nodo caído, si es de acceso.
 */
public class ImpactoFalla {

    private final String elementoCaido;
    private final Set<String> nodosIncomunicados;
    private final int usuariosAfectados;

    public ImpactoFalla(String elementoCaido, Set<String> nodosIncomunicados, int usuariosAfectados) {
        this.elementoCaido = elementoCaido;
        this.nodosIncomunicados = Collections.unmodifiableSet(new LinkedHashSet<>(nodosIncomunicados));
        this.usuariosAfectados = usuariosAfectados;
    }

    /** El id del nodo caído, o "a-b" si es un enlace. */
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
