package com.volleycubas.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Jugador implements Parcelable, Serializable {
    private String id;
    private String nombre;
    private String posicion;
    private int numero;
    private String notas;
    private int numeroMVPs = 0;
    private Boolean asistencia;

    // Constructor vacío requerido por Firestore
    public Jugador() {}

    public Jugador(String id, String nombre, String posicion, int numero, String notas, int numeroMVPs) {
        this.id = id;
        this.nombre = nombre;
        this.posicion = posicion;
        this.numero = numero;
        this.notas = notas;
        this.numeroMVPs = numeroMVPs;
    }

    public Jugador(String id, String nombre, int numero, String posicion) {
        this.id = id;
        this.nombre = nombre;
        this.numero = numero;
        this.posicion = posicion;
    }

    public Jugador(String id, String nombre, Boolean asistencia) {
        this.id = id;
        this.nombre = nombre;
        this.asistencia = asistencia;
    }

    // Getters y setters
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

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public int getNumeroMVPs() {
        return numeroMVPs;
    }

    public void setNumeroMVPs(int numeroMVPs) {
        this.numeroMVPs = numeroMVPs;
    }

    public Boolean isAsistencia() {
        return asistencia;
    }

    public void setAsistencia(Boolean asistencia) {
        this.asistencia = asistencia;
    }

    // Convertir a Map para Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("nombre", nombre);
        map.put("posicion", posicion);
        map.put("numero", numero);
        map.put("notas", notas);
        map.put("numeroMVPs", numeroMVPs);
        return map;
    }

    // Métodos Parcelable
    protected Jugador(Parcel in) {
        id = in.readString();
        nombre = in.readString();
        numero = in.readInt();
        posicion = in.readString();
        notas = in.readString();
        numeroMVPs = in.readInt();
        byte asistenciaByte = in.readByte();
        asistencia = asistenciaByte == -1 ? null : asistenciaByte == 1;    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nombre);
        dest.writeInt(numero);
        dest.writeString(posicion);
        dest.writeString(notas);
        dest.writeInt(numeroMVPs);
        dest.writeByte(asistencia == null ? -1 : (byte) (asistencia ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Jugador> CREATOR = new Creator<Jugador>() {
        @Override
        public Jugador createFromParcel(Parcel in) {
            return new Jugador(in);
        }

        @Override
        public Jugador[] newArray(int size) {
            return new Jugador[size];
        }
    };
}
