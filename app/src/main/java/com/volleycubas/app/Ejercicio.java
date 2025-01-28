package com.volleycubas.app;

public class Ejercicio {
    private String id;
    private String creador;
    private String titulo;
    private String tipo;
    private String urlImagen; // URL de la imagen
    private String descripcion; // Descripción del ejercicio
    private long timestamp; // Timestamp de la última actualización de la imagen

    public Ejercicio() {}

    public Ejercicio(String id, String creador, String titulo, String tipo, String urlImagen, String descripcion, long timestamp) {
        this.id = id;
        this.creador = creador;
        this.titulo = titulo;
        this.tipo = tipo;
        this.urlImagen = urlImagen;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
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

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Ejercicio{" +
                "id='" + id + '\'' +
                ", creador='" + creador + '\'' +
                ", titulo='" + titulo + '\'' +
                ", tipo='" + tipo + '\'' +
                ", urlImagen='" + urlImagen + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}