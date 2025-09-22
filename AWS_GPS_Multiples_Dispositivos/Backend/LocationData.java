package com.example.springboot;

// Clase que representa los datos de ubicación que se recibirán en el controlador
public class LocationData {
    
    // Latitud de la ubicación
    private double lat;
    
    // Longitud de la ubicación
    private double lng;
    
    // Nombre identificativo de la ubicación o dispositivo
    private String name;
    
    // Código único o identificador de la ubicación o dispositivo
    private String code;

    // ====== Getters y Setters ======

    // Getter y Setter para latitud
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    // Getter y Setter para longitud
    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    // Getter y Setter para nombre
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter y Setter para código
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
