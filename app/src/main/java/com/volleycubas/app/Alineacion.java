package com.volleycubas.app;

import java.util.List;
import java.util.Map;

public class Alineacion {
    private String id;
    private String nombre;
    private String creador;
    private List<Map<String, Object>> rotaciones;

    public Alineacion(String id, String nombre, String creador, List<Map<String, Object>> rotaciones) {
        this.id = id;
        this.nombre = nombre;
        this.creador = creador;
        this.rotaciones = rotaciones;
    }

    // Getters y setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCreador() { return creador; }
    public List<Map<String, Object>> getRotaciones() { return rotaciones; }
}

