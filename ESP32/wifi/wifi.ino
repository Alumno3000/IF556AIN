#include <WiFi.h>

// Definir las credenciales de la red Wi-Fi
const char* ssid = "Galaxy A14 5G C6FA";  // Nombre de la red Wi-Fi
const char* pass = "miapellido";          // Contraseña de la red Wi-Fi (recuerda cerrarla con comillas)

void setup() {
  // Inicializar la comunicación serial para poder ver los mensajes de debug
  Serial.begin(115200);
  
  // Iniciar la conexión Wi-Fi con las credenciales especificadas
  WiFi.begin(ssid, pass);
  
  // Imprimir mensaje de "Conectando" para indicar que está intentando la conexión
  Serial.print("Conectando");
  
  // Esperar hasta que se establezca la conexión Wi-Fi
  // El ciclo sigue ejecutándose mientras el ESP32 no esté conectado
  while (WiFi.status() != WL_CONNECTED) { 
    delay(500);           // Espera medio segundo antes de intentar de nuevo
    Serial.print(".");    // Imprime un punto para mostrar el progreso de la conexión
  }
  
  // Una vez conectado, imprime la dirección IP asignada al ESP32
  Serial.println("\nConectado! IP: " + WiFi.localIP().toString());
}

void loop() {
  // El código no hace nada en el loop, ya que solo se conecta a la red Wi-Fi
}
