// Importamos las clases necesarias de Spring Framework para manejar las respuestas HTTP
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Importamos clases para gestionar listas
import java.util.ArrayList;
import java.util.List;

// Se define un controlador REST para manejar las solicitudes relacionadas con las ubicaciones
@RestController
@RequestMapping("/location")  // Define la ruta base para las solicitudes (por ejemplo, /location)
public class LocationController {

    // Se crea una lista para almacenar los datos de ubicación recibidos
    private final List<LocationData> receivedLocations = new ArrayList<>();

    // Endpoint para recibir datos de ubicación mediante una solicitud POST
    @PostMapping
    public ResponseEntity<String> receiveLocation(@RequestBody LocationData data) {
        // Añadimos los datos de la ubicación a la lista
        receivedLocations.add(data);

        // Imprimimos en la consola los detalles de la ubicación recibida
        System.out.println("📍 Nueva ubicación recibida:");
        System.out.println("Nombre: " + data.getName());  // Nombre del lugar
        System.out.println("Código: " + data.getCode());  // Código de la ubicación
        System.out.println("Lat: " + data.getLat() + ", Lng: " + data.getLng());  // Coordenadas geográficas
        System.out.println("Velocidad: " + data.getSpeedKmh() + " km/h");  // Velocidad en km/h
        System.out.println("Accel X: " + data.getAccelX());  // Aceleración en el eje X
        System.out.println("Accel Y: " + data.getAccelY());  // Aceleración en el eje Y
        System.out.println("Accel Z: " + data.getAccelZ());  // Aceleración en el eje Z
        System.out.println("Pasos estimados: " + data.getSteps());  // Pasos estimados de la persona

        // Retornamos una respuesta indicando que los datos fueron recibidos correctamente
        return ResponseEntity.ok("✅ Datos recibidos correctamente");
    }

    // Endpoint para obtener la lista de ubicaciones recibidas mediante una solicitud GET
    @GetMapping
    public List<LocationData> getLocations() {
        // Retorna la lista de ubicaciones recibidas
        return receivedLocations;
    }
}

