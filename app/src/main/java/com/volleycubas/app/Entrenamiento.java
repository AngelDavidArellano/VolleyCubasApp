package com.volleycubas.app;

import java.util.List;

public class Entrenamiento {
    private String id;
    private String creador;
    private String titulo;
    private String tipo;
    private String descripcion; // Descripción del entrenamiento
    private String fechaCreacion; // Fecha de creación
    private List<String> ejercicios; // Lista de IDs de los ejercicios

    public Entrenamiento() {}

    public Entrenamiento(String id, String creador, String titulo, String tipo, String descripcion, String fechaCreacion, List<String> ejercicios) {
        this.id = id;
        this.creador = creador;
        this.titulo = titulo;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.ejercicios = ejercicios;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreador() {
        return creador;
    }

    public void setCreador(String creador) {
        this.creador = creador;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<String> getEjercicios() {
        return ejercicios;
    }

    public void setEjercicios(List<String> ejercicios) {
        this.ejercicios = ejercicios;
    }
}
