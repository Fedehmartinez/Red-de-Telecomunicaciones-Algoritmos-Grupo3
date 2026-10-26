package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.ResultadoCableado;

import java.util.Objects;

/**
 * Diseño de cableado: conectar todos los nodos de la red con el menor costo total de instalación.
 * Para planificar oficinas o edificios nuevos se arma una RedTelecom con esos nodos y las conexiones
 * posibles entre ellos, cada una con su costo, y se le pide el cableado mínimo.
 */
public class ServicioCableado {

    private final RedTelecom red;

    public ServicioCableado(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    /**
     * Árbol abarcador de costo mínimo: Kruskal sobre grafoCableado(). Si no se pueden conectar todos,
     * devuelve el bosque de costo mínimo con conexo = false.
     */
    public ResultadoCableado cableadoMinimo() {
        throw new UnsupportedOperationException("Falta implementar");
    }
}
