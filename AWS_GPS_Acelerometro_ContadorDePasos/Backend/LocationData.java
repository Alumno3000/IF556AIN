
package com.example.springboot;

// Clase que representa los datos de una ubicación
public class LocationData {

    // Atributos que contienen los datos de la ubicación
    private double lat;  // Latitud de la ubicación
    private double lng;  // Longitud de la ubicación
    private double speedKmh;  // Velocidad en km/h
    private float accelX;  // Aceleración en el eje X
    private float accelY;  // Aceleración en el eje Y
    private float accelZ;  // Aceleración en el eje Z
    private String name;  // Nombre del lugar o ubicación
    private String code;  // Código que identifica la ubicación
    private int steps;  // Pasos estimados

    // Getter y setter para la latitud
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    // Getter y setter para la longitud
    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    // Getter y setter para la velocidad en km/h
    public double getSpeedKmh() {
        return speedKmh;
    }
    public void setSpeedKmh(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    // Getter y setter para la aceleración en el eje X
    public float getAccelX() {
        return accelX;
    }
    public void setAccelX(float accelX) {
        this.accelX = accelX;
    }

    // Getter y setter para la aceleración en el eje Y
    public float getAccelY() {
        return accelY;
    }
    public void setAccelY(float accelY) {
        this.accelY = accelY;
    }

    // Getter y setter para la aceleración en el eje Z
    public float getAccelZ() {
        return accelZ;
    }
    public void setAccelZ(float accelZ) {
        this.accelZ = accelZ;
    }

    // Getter y setter para el nombre de la ubicación
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Getter y setter para el código de la ubicación
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    // Getter y setter para los pasos estimados (nuevo campo)
    public int getSteps() {
        return steps;
    }
    public void setSteps(int steps) {
        this.steps = steps;
    }
}

