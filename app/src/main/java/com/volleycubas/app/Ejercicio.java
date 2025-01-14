package com.volleycubas.app;

public class Ejercicio {
    private String id;
    private String creador;
    private String titulo;
    private String tipo;

    public Ejercicio() {}

    public Ejercicio(String id, String creador, String titulo, String tipo) {
        this.id = id;
        this.creador = creador;
        this.titulo = titulo;
        this.tipo = tipo;
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
}
