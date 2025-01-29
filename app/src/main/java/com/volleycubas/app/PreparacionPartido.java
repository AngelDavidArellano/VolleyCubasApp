package com.volleycubas.app;

import java.util.Map;
import java.util.List;

public class PreparacionPartido {
    private String id;
    private String titulo;
    private String fecha;
    private Map<String, List<String>> sets; // Mapa de arrays para los sets
    private String notas;

    public PreparacionPartido() {
        // Constructor vacío necesario para Firebase
    }

    public PreparacionPartido(String id, String titulo, String fecha, Map<String, List<String>> sets, String notas) {
        this.id = id;
        this.titulo = titulo;
        this.fecha = fecha;
        this.sets = sets;
        this.notas = notas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Map<String, List<String>> getSets() {
        return sets;
    }

    public void setSets(Map<String, List<String>> sets) {
        this.sets = sets;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    @Override
    public String toString() {
        return "PreparacionPartido{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", fecha='" + fecha + '\'' +
                ", sets=" + sets +
                ", notas='" + notas + '\'' +
                '}';
    }
}