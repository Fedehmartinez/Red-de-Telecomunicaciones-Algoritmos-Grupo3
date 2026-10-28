package ucu.edu.aed.telecom.modelo;

// Tipo de equipo de la red. Los de acceso (punto de acceso y oficina) son los únicos que atienden usuarios
public enum TipoNodo {
    ROUTER, SWITCH, SERVIDOR, PUNTO_ACCESO, DATA_CENTER, OFICINA;

    public boolean esDeAcceso() {
        return this == PUNTO_ACCESO || this == OFICINA;
    }
}
