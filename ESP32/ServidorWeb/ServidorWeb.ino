#include <WiFi.h>
#include <WebServer.h>

// --- Datos de tu red WiFi ---
const char* ssid = "Galaxy A14 5G C6FA";   // Nombre de la red Wi-Fi
const char* pass = "miapellido";           // Contraseña de la red Wi-Fi (asegúrate de tenerla correcta)

// --- Servidor Web en puerto 80 ---
WebServer server(80);  // Crear un servidor web en el puerto 80

// --- Página principal con HTML ---
void handleRoot() {
  // Construcción de la página HTML que se enviará como respuesta
  String html = "<!DOCTYPE html><html>";
  html += "<head>";
  html += "<meta charset='UTF-8'>";
  html += "<title>Servidor ESP32</title>";
  html += "<style>";
  
  // Estilos CSS para la página
  html += "body { background: #f0f8ff; font-family: Arial, sans-serif; text-align: center; margin-top: 50px; }";
  html += "h1 { color: #004080; }";
  html += "p { font-size: 18px; color: #333; }";
  html += "div { background: #ffffff; display: inline-block; padding: 20px; border-radius: 15px; box-shadow: 0 4px 8px rgba(0,0,0,0.2); }";
  
  html += "</style>";
  html += "</head><body>";
  html += "<div>";
  html += "<h1>Hola desde ESP32</h1>";  // Título de la página
  html += "<p><strong>Nombre:</strong> Alex Berrios Thea</p>";  // Información personal
  html += "<p><strong>Código:</strong> 215781</p>";
  html += "<p><strong>Universidad:</strong> UNSAAC</p>";
  html += "</div>";
  html += "</body></html>";

  // Enviar la página HTML al cliente
  server.send(200, "text/html", html);
}

void setup() {
  // Inicializar la comunicación serial
  Serial.begin(115200);
  
  // Intentar conectar al Wi-Fi
  WiFi.begin(ssid, pass);
  Serial.print("Conectando a WiFi");
  
  // Mientras no se establezca la conexión, esperamos y mostramos un punto cada 500 ms
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  // Una vez conectado, imprimir la IP asignada
  Serial.println("\nConectado a la red WiFi!");
  Serial.println("IP asignada: " + WiFi.localIP().toString());

  // Asociar la ruta raíz ("/") con la función handleRoot
  server.on("/", handleRoot);
  
  // Iniciar el servidor web
  server.begin();
  Serial.println("Servidor web iniciado");
}

void loop() {
  // El servidor maneja las solicitudes entrantes de los clientes
  server.handleClient();
}