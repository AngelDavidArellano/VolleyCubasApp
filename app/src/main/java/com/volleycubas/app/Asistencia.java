package com.volleycubas.app;

public class Asistencia {
    private String fecha;
    private Boolean asistencia;
    private String tipo;

    public Asistencia(String fecha, Boolean asistencia, String tipo) {
        this.fecha = fecha;
        this.asistencia = asistencia;
        this.tipo = tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public Boolean getAsistencia() {
        return asistencia;
    }

    public String getTipo() {
        return tipo;
    }
}
