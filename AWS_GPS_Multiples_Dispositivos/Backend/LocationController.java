package com.example.springboot;

// Importaciones necesarias de Spring y utilidades estándar
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

// Anotación que indica que esta clase es un controlador REST
@RestController
// Ruta base para todas las solicitudes de este controlador
@RequestMapping("/location")
public class LocationController {
    
    // Lista que almacenará las ubicaciones recibidas
    private final List<LocationData> receivedLocations = new ArrayList<>();

    // Método para recibir los datos de ubicación a través de una solicitud POST
    @PostMapping
    public ResponseEntity<String> receiveLocation(@RequestBody LocationData data) {
        
        // Se agrega la ubicación recibida a la lista
        receivedLocations.add(data);
        
        // Imprime los detalles de la ubicación recibida en la consola (solo para depuración)
        System.out.println("📍 Nueva ubicación recibida:");
        System.out.println("Nombre: " + data.getName());  // Nombre de la ubicación
        System.out.println("Código: " + data.getCode());  // Código de la ubicación
        System.out.println("Lat: " + data.getLat() + ", Lng: " + data.getLng());  // Coordenadas geográficas
     
        
        // Responde con un mensaje indicando que los datos fueron recibidos correctamente
        return ResponseEntity.ok("✅ Datos recibidos correctamente");
    }

    // Método para obtener todas las ubicaciones almacenadas a través de una solicitud GET
    @GetMapping
    public List<LocationData> getLocations() {
        // Retorna la lista de ubicaciones recibidas
        return receivedLocations;
    }
}
