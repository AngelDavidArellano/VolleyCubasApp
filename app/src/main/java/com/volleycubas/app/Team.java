package com.volleycubas.app;

import java.util.List;

public class Team {
    private String id;
    private String nombre;
    private int numero_jugadores;
    private String url_imagen;
    private List<Object> historial_partidos;  // Ajusta el tipo según tu estructura
    private List<Object> jugadores;          // Ajusta el tipo según tu estructura
    private List<Object> preparacion_partidos;  // Ajusta el tipo según tu estructura
    private List<Object> alineaciones;        // Ajusta el tipo según tu estructura
    private String temporada_creacion;
    private List<String> entrenadores;       // Lista de IDs de entrenadores
    private String capitan;
    private String liga;
    private String rival_fecha_mejor_racha;
    private int mejor_racha;
    private int puntos_a_favor;
    private int puntos_totales;
    private int sets_a_favor;
    private int sets_totales;
    private int partidos_jugados;
    private int partidos_ganados;
    private long timestamp; // Marca de tiempo de la última actualización


    // Constructor vacío requerido por Firestore
    public Team() {
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumero_jugadores() {
        return numero_jugadores;
    }

    public void setNumero_jugadores(int numero_jugadores) {
        this.numero_jugadores = numero_jugadores;
    }

    public String getUrl_imagen() {
        return url_imagen;
    }

    public void setUrl_imagen(String url_imagen) {
        this.url_imagen = url_imagen;
    }

    public List<Object> getHistorial_partidos() {
        return historial_partidos;
    }

    public void setHistorial_partidos(List<Object> historial_partidos) {
        this.historial_partidos = historial_partidos;
    }

    public List<Object> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<Object> jugadores) {
        this.jugadores = jugadores;
    }

    public List<Object> getPreparacion_partidos() {
        return preparacion_partidos;
    }

    public void setPreparacion_partidos(List<Object> preparacion_partidos) {
        this.preparacion_partidos = preparacion_partidos;
    }

    public List<Object> getAlineaciones() {
        return alineaciones;
    }

    public void setAlineaciones(List<Object> alineaciones) {
        this.alineaciones = alineaciones;
    }

    public String getTemporada_creacion() {
        return temporada_creacion;
    }

    public void setTemporada_creacion(String temporada_creacion) {
        this.temporada_creacion = temporada_creacion;
    }

    public List<String> getEntrenadores() {
        return entrenadores;
    }

    public void setEntrenadores(List<String> entrenadores) {
        this.entrenadores = entrenadores;
    }

    public String getCapitan() {
        return capitan;
    }

    public void setCapitan(String capitan) {
        this.capitan = capitan;
    }

    public String getLiga() {
        return liga;
    }

    public void setLiga(String liga) {
        this.liga = liga;
    }

    public String getRival_fecha_mejor_racha() {
        return rival_fecha_mejor_racha;
    }

    public void setRival_fecha_mejor_racha(String rival_fecha_mejor_racha) {
        this.rival_fecha_mejor_racha = rival_fecha_mejor_racha;
    }

    public int getMejor_racha() {
        return mejor_racha;
    }

    public void setMejor_racha(int mejor_racha) {
        this.mejor_racha = mejor_racha;
    }

    public int getPuntos_a_favor() {
        return puntos_a_favor;
    }

    public void setPuntos_a_favor(int puntos_a_favor) {
        this.puntos_a_favor = puntos_a_favor;
    }

    public int getPuntos_totales() {
        return puntos_totales;
    }

    public void setPuntos_totales(int puntos_totales) {
        this.puntos_totales = puntos_totales;
    }

    public int getSets_a_favor() {
        return sets_a_favor;
    }

    public void setSets_a_favor(int sets_a_favor) {
        this.sets_a_favor = sets_a_favor;
    }

    public int getSets_totales() {
        return sets_totales;
    }

    public void setSets_totales(int sets_totales) {
        this.sets_totales = sets_totales;
    }

    public int getPartidos_jugados() {
        return partidos_jugados;
    }

    public void setPartidos_jugados(int partidos_jugados) {
        this.partidos_jugados = partidos_jugados;
    }

    public int getPartidos_ganados() {
        return partidos_ganados;
    }

    public void setPartidos_ganados(int partidos_ganados) {
        this.partidos_ganados = partidos_ganados;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", numero_jugadores=" + numero_jugadores +
                ", url_imagen='" + url_imagen + '\'' +
                ", historial_partidos=" + historial_partidos +
                ", jugadores=" + jugadores +
                ", preparacion_partidos=" + preparacion_partidos +
                ", alineaciones=" + alineaciones +
                ", temporada_creacion='" + temporada_creacion + '\'' +
                ", entrenadores=" + entrenadores +
                ", capitan='" + capitan + '\'' +
                ", liga='" + liga + '\'' +
                ", rival_fecha_mejor_racha='" + rival_fecha_mejor_racha + '\'' +
                ", mejor_racha=" + mejor_racha +
                ", puntos_a_favor=" + puntos_a_favor +
                ", puntos_totales=" + puntos_totales +
                ", sets_a_favor=" + sets_a_favor +
                ", sets_totales=" + sets_totales +
                ", partidos_jugados=" + partidos_jugados +
                ", partidos_ganados=" + partidos_ganados +
                ", timestamp=" + timestamp +
                '}';
    }

}
