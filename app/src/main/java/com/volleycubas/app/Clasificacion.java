package com.volleycubas.app;

public class Clasificacion {
    private String id;
    private String equipo;
    private int puntos;
    private int jugados;
    private int ganados;
    private int perdidos;
    private int sets_a_favor;
    private int sets_en_contra;
    private int puntos_a_favor;
    private int puntos_en_contra;

    public String getId() {
        return id;
    }

    public String getEquipo() {
        return equipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getJugados() {
        return jugados;
    }

    public int getGanados() {
        return ganados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public int getSetsAFavor() {
        return sets_a_favor;
    }

    public int getSetsEnContra() {
        return sets_en_contra;
    }

    public int getPuntosAFavor() {
        return puntos_a_favor;
    }

    public int getPuntosEnContra() {
        return puntos_en_contra;
    }
}
