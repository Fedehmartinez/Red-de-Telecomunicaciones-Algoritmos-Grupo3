package ucu.edu.aed.telecom.modelo;

import java.util.Objects;

/**
 * Un equipo de la red. Los grafos solo conocen el id; el resto de los datos se busca en RedTelecom.
 * En los nodos de acceso la capacidad es la cantidad máxima de usuarios que pueden atender; en los demás
 * es informativa (por ejemplo, Gbps que maneja el equipo) y no tienen usuarios.
 * Dos nodos son iguales si tienen el mismo id.
 */
public class Nodo {

    private final String id;
    private final TipoNodo tipo;
    private final int capacidad;
    private final int usuarios;

    public Nodo(String id, TipoNodo tipo, int capacidad, int usuarios) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El nodo necesita un id");
        if (tipo == null) throw new IllegalArgumentException("El nodo " + id + " necesita un tipo");
        if (capacidad < 0) throw new IllegalArgumentException("Capacidad negativa en " + id);
        if (usuarios < 0) throw new IllegalArgumentException("Usuarios negativos en " + id);
        if (usuarios > 0 && !tipo.esDeAcceso()) {
            throw new IllegalArgumentException("Solo los nodos de acceso tienen usuarios: " + id + " es " + tipo);
        }
        if (tipo.esDeAcceso() && usuarios > capacidad) {
            throw new IllegalArgumentException("El nodo " + id + " tiene más usuarios que su capacidad");
        }
        this.id = id;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.usuarios = usuarios;
    }

    public String getId() {
        return id;
    }

    public TipoNodo getTipo() {
        return tipo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getUsuarios() {
        return usuarios;
    }

    public boolean esDataCenter() {
        return tipo == TipoNodo.DATA_CENTER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nodo otro)) return false;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id + " (" + tipo + ")";
    }
}
