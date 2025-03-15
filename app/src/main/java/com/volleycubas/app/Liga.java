package com.volleycubas.app;

public class Liga {
    private String nombre;
    private String grupoId;

    public Liga(String nombre, String grupoId) {
        this.nombre = nombre;
        this.grupoId = grupoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupoId() {
        return grupoId;
    }

    @Override
    public String toString() {
        return nombre; // Esto hará que el Spinner muestre los nombres de las ligas
    }
}
