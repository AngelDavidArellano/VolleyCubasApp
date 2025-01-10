package com.volleycubas.app;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Partido implements Serializable {
    private String id; // Identificador único del partido
    private String rival; // Nombre del rival
    private String fase; // Fase del torneo
    private int sets; // Número de sets (3 o 5)
    private Date fechaInicio; // Fecha de inicio del partido
    private int setsAFavor; // Sets ganados por el equipo
    private int setsEnContra; // Sets ganados por el rival
    private StringBuilder puntosSecuencia; // Secuencia de puntos en formato "AABBXABAAABBXABA"
    private String fecha; // Fecha del partido
    private String notas; // Notas del partido

    // Constructor
    public Partido(String id, String rival, String fase, int sets) {
        this.id = id;
        this.rival = rival;
        this.fase = fase;
        this.sets = sets;
        this.fechaInicio = new Date(); // Fecha actual al crear el partido
        this.setsAFavor = 0; // Inicialmente ningún set ganado
        this.setsEnContra = 0; // Inicialmente ningún set perdido
        this.puntosSecuencia = new StringBuilder(); // Cadena vacía para los puntos
        this.fecha = null; // Fecha será asignada al finalizar el partido
        this.notas = notas;
    }

    public Partido() {

    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRival() { return rival; }
    public void setRival(String rival) { this.rival = rival; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public Date getFechaInicio() { return fechaInicio; }

    public int getSetsAFavor() { return setsAFavor; }
    public void setSetsAFavor(int setsAFavor) { this.setsAFavor = setsAFavor; }

    public int getSetsEnContra() { return setsEnContra; }
    public void setSetsEnContra(int setsEnContra) { this.setsEnContra = setsEnContra; }

    public String getPuntosSecuencia() { return puntosSecuencia.toString(); }
    public void setPuntosSecuencia(String puntosSecuencia) { this.puntosSecuencia = new StringBuilder(puntosSecuencia); }

    // Métodos para manejar puntos
    public void agregarPuntoAFavor() {
        puntosSecuencia.append("A");
    }

    public void agregarPuntoEnContra() {
        puntosSecuencia.append("B");
    }

    public void agregarTimeoutAFavor() {
        puntosSecuencia.append("T");
    }
    public void agregarTimeoutEnContra() {
        puntosSecuencia.append("P");
    }

    public void eliminarUltimoPunto() {
        if (puntosSecuencia.length() > 0) {
            char ultimoCaracter = puntosSecuencia.charAt(puntosSecuencia.length() - 1);
            puntosSecuencia.deleteCharAt(puntosSecuencia.length() - 1); // Eliminar último carácter
            Log.d("Partido", "Eliminado el último punto: " + ultimoCaracter);
        } else {
            Log.d("Partido", "No hay acciones para eliminar.");
        }
    }

    public void eliminarUltimoTimeout() {
        if (puntosSecuencia.length() > 0) {
            char ultimoCaracter = puntosSecuencia.charAt(puntosSecuencia.length() - 1);
            if (ultimoCaracter == 'T' || ultimoCaracter == 'P') { // Verificar si es un timeout
                puntosSecuencia.deleteCharAt(puntosSecuencia.length() - 1);
                Log.d("Partido", "Eliminado el último timeout: " + ultimoCaracter);
            } else {
                Log.d("Partido", "El último carácter no es un timeout.");
            }
        } else {
            Log.d("Partido", "No hay acciones para eliminar.");
        }
    }


    public void finalizarSet() {
        puntosSecuencia.append("X"); // Delimitador de sets
    }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public static Partido fromMap(Map<String, Object> map) {
        if (map == null) {
            Log.e("Partido", "El mapa recibido es nulo. No se puede crear el objeto Partido.");
            return null;
        }

        Partido partido = new Partido();
        partido.setId((String) map.get("id"));
        partido.setRival((String) map.get("rival"));
        partido.setFase((String) map.get("fase"));
        partido.setSets(map.containsKey("sets") ? ((Long) map.get("sets")).intValue() : 0);

        String puntosPorSet = (String) map.get("puntosPorSet");
        partido.puntosSecuencia = new StringBuilder(puntosPorSet != null ? puntosPorSet : "");

        partido.setSetsAFavor(map.containsKey("setsAFavor") ? ((Long) map.get("setsAFavor")).intValue() : 0);
        partido.setSetsEnContra(map.containsKey("setsEnContra") ? ((Long) map.get("setsEnContra")).intValue() : 0);

        partido.setFecha((String) map.get("fecha"));
        partido.setNotas((String) map.get("notas"));
        return partido;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("rival", rival);
        map.put("fase", fase);
        map.put("sets", sets);
        map.put("puntosPorSet", puntosSecuencia.toString());
        map.put("setsAFavor", setsAFavor);
        map.put("setsEnContra", setsEnContra);
        map.put("fecha", fecha);
        map.put("notas", notas);
        return map;
    }

}
