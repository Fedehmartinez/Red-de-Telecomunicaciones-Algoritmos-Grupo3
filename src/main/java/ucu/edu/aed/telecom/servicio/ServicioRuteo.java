package ucu.edu.aed.telecom.servicio;

import ucu.edu.aed.telecom.modelo.Enlace;
import ucu.edu.aed.telecom.modelo.RedTelecom;
import ucu.edu.aed.telecom.resultado.ResultadoDiametro;
import ucu.edu.aed.telecom.resultado.RutaResultado;
import ucu.edu.aed.telecom.resultado.TablaRuteo;

import java.util.Objects;
import java.util.Set;

/**
 * Todo lo de rutas sobre el grafo de ruteo (dirigido, peso latencia): la ruta de menor latencia entre dos
 * nodos, con ancho de banda mínimo o esquivando fallas, la tabla de ruteo completa, si hay camino y el
 * diámetro de la red. Para consultas sueltas se usa Dijkstra; para todos los pares, Floyd.
 * Ids que no son de la red: IllegalArgumentException.
 */
public class ServicioRuteo {

    private final RedTelecom red;

    public ServicioRuteo(RedTelecom red) {
        this.red = Objects.requireNonNull(red);
    }

    /** Ruta de menor latencia de origen a destino: Dijkstra sobre grafoRuteo(). */
    public RutaResultado rutaMenorLatencia(String origen, String destino) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Ruta de menor latencia usando solo enlaces con al menos ese ancho de banda: Dijkstra sobre grafoRuteo(min). */
    public RutaResultado rutaConAnchoBandaMinimo(String origen, String destino, double anchoBandaMinimo) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Ruta de menor latencia sin pasar por los nodos ni enlaces caídos. Si cae el origen o el destino, no hay ruta. */
    public RutaResultado rutaAlternativa(String origen, String destino, Set<String> nodosCaidos, Set<Enlace> enlacesCaidos) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Tabla de ruteo de toda la red: Floyd sobre grafoRuteo(). */
    public TablaRuteo tablaDeRuteo() {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /** Si hay algún camino de origen a destino: Warshall (cerradura transitiva) sobre grafoRuteo(). */
    public boolean hayCamino(String origen, String destino) {
        throw new UnsupportedOperationException("Falta implementar");
    }

    /**
     * Diámetro: el par (origen distinto de destino) con la mayor latencia mínima, con Floyd.
     * null si la red tiene menos de 2 nodos; latencia infinita si algún par no tiene camino.
     */
    public ResultadoDiametro diametro() {
        throw new UnsupportedOperationException("Falta implementar");
    }
}
