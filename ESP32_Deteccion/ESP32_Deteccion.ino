#include <WiFi.h>          // Librería para manejar WiFi en ESP32
#include <WebServer.h>     // Librería para levantar un servidor web en ESP32

// ========================== CONFIGURACIÓN DE RED ==========================
const char* ssid = "Galaxy A14 5G C6FA";    // Nombre de la red WiFi (SSID)
const char* password = "miapellido";        // Contraseña de la red WiFi

WebServer server(80);  // Se crea el servidor web en el puerto 80

// ========================== ESTRUCTURA PARA DISPOSITIVOS ==========================
// Definición de una estructura para almacenar la información de cada dispositivo detectado
struct DeviceInfo {
  String mac;        // Dirección MAC del dispositivo
  String hostname;   // Nombre de la red (SSID)
  String vendor;     // Fabricante del dispositivo (según prefijo MAC)
  String deviceType; // Tipo de dispositivo (teléfono, laptop, router, etc.)
  int rssi;          // Intensidad de señal en dBm
};

const int MAX_DEVICES = 50;         // Máximo número de dispositivos a guardar
DeviceInfo devices[MAX_DEVICES];    // Array para guardar la lista de dispositivos
int deviceCount = 0;                // Contador de dispositivos detectados

// ========================== BASE DE DATOS DE VENDORS ==========================
// Prefijos MAC que permiten identificar fabricantes (OUI)
const char* macVendors[][2] = {
  {"DC:4F:22", "Espressif"},
  {"24:0A:C4", "Xiaomi"},
  {"FC:DB:B3", "Samsung"},
  {"8C:85:90", "Apple"},
  {"18:60:24", "Huawei"},
  {"A4:50:46", "Intel"},
  {"00:50:C2", "Microsoft"},
  {"14:CC:20", "TP-Link"},
  {"C8:3A:35", "Tenda"},
  {"B8:27:EB", "Raspberry Pi"},
  {"52:28:9D", "Samsung"},
  {"A8:6E:84", "TP-Link"},
  {"84:3C:99", "Netgear"},
  {"08:33:ED", "TP-Link"},
  {"1C:9E:CC", "Huawei"},
  {"8A:C2:27", "ASUS"},
  {"BA:15:32", "Honor"},
  {"1C:73:E2", "D-Link"},
  {"86:82:A0", "Motorola"},
  {"44:3B:14", "Samsung"},
  {"EA:FE:DB", "Xiaomi"},
  {"80:D0:4A", "Netgear"},
  {"00:E0:4C", "Realtek"},
  {"A8:29:48", "Apple"},
  {"7C:F1:7E", "D-Link"},
  {"B0:F5:30", "Huawei"},
  {"2C:96:82", "Belkin"},
  {"50:C7:BF", "TP-Link"},
  {"D4:01:45", "Dell"},
  {"54:84:DC", "TP-Link"}
};

// ========================== DETECCIÓN DE TIPOS DE DISPOSITIVOS ==========================
// Función para adivinar qué tipo de dispositivo es en base a MAC, SSID y vendor
String detectDeviceType(String mac, String ssid, String vendor) {
  String macPrefix = mac.substring(0, 8);
  macPrefix.toUpperCase();
  ssid.toUpperCase();

  // Reglas según el SSID (nombre de red)
  if (ssid.indexOf("GALAXY") >= 0 || ssid.indexOf("SAMSUNG") >= 0) return "📱 Teléfono Samsung";
  if (ssid.indexOf("REDMI") >= 0 || ssid.indexOf("XIAOMI") >= 0 || ssid.indexOf("POCO") >= 0) return "📱 Teléfono Xiaomi";
  if (ssid.indexOf("MOTO") >= 0) return "📱 Teléfono Motorola";
  if (ssid.indexOf("IPHONE") >= 0 || ssid.indexOf("IPAD") >= 0) return "📱 Dispositivo Apple";
  if (ssid.indexOf("HUAWEI") >= 0 || ssid.indexOf("HONOR") >= 0) return "📱 Teléfono Huawei";

  // Routers y puntos de acceso
  if (ssid.indexOf("TP-LINK") >= 0 || ssid.indexOf("TENDA") >= 0 || 
      ssid.indexOf("D-LINK") >= 0 || ssid.indexOf("ROUTER") >= 0 ||
      ssid.indexOf("AP_") >= 0 || ssid.indexOf("_AP") >= 0) {
    return "📶 Router/WiFi AP";
  }

  // Identificación por vendor
  if (vendor == "Apple") {
    if (macPrefix == "A8:29:48") return "💻 MacBook";
    return "📱 iPhone/iPad";
  }
  if (vendor == "Samsung") return (ssid.indexOf("TAB") >= 0) ? "📱 Tablet Samsung" : "📱 Teléfono Samsung";
  if (vendor == "Xiaomi") return (ssid.indexOf("TAB") >= 0) ? "📱 Tablet Xiaomi" : "📱 Teléfono Xiaomi";
  if (vendor == "Huawei") return (ssid.indexOf("TAB") >= 0 || ssid.indexOf("MATEPAD") >= 0) ? "📱 Tablet Huawei" : "📱 Teléfono Huawei";
  if (vendor == "Motorola") return "📱 Teléfono Motorola";
  if (vendor == "TP-Link" || vendor == "Tenda" || vendor == "D-Link" || vendor == "Netgear" || vendor == "ASUS") return "📶 Router/WiFi AP";
  if (vendor == "Raspberry Pi") return "🖥️ Raspberry Pi";
  if (vendor == "Dell") return "💻 Laptop Dell";
  if (vendor == "Intel") return "💻 Laptop/PC";

  // Por MAC específica
  if (macPrefix == "A8:6E:84") return "📶 Router/WiFi AP";
  if (macPrefix == "00:E0:4C") return "📶 Punto de Acceso";

  // IoT
  if (ssid.indexOf("ESP") >= 0 || ssid.indexOf("IOT") >= 0 || 
      ssid.indexOf("SMART") >= 0 || vendor == "Espressif") {
    return "🏠 Dispositivo IoT";
  }

  // Por defecto según vendor
  if (vendor != "Desconocido") return "📶 Dispositivo " + vendor;

  // Intento final
  if (ssid.length() <= 15) return "📱 Probable Teléfono";
  return "❓ Tipo Desconocido";
}

// ========================== OBTENER VENDOR SEGÚN MAC ==========================
// Función que busca el fabricante (vendor) en la base de datos de prefijos
String getVendorFromMAC(String mac) {
  mac.toUpperCase();
  mac.replace(":", "");
  mac.replace("-", "");

  for (int i = 0; i < sizeof(macVendors) / sizeof(macVendors[0]); i++) {
    String vendorPrefix = String(macVendors[i][0]);
    vendorPrefix.replace(":", "");
    if (mac.startsWith(vendorPrefix)) return macVendors[i][1];
  }
  return "Desconocido";
}

// ========================== ESCANEO DE REDES ==========================
// Escanea las redes WiFi disponibles y llena el array de dispositivos
void scanWiFiNetworks() {
  Serial.println("Escaneando redes WiFi...");
  deviceCount = 0;

  int numNetworks = WiFi.scanNetworks();
  Serial.println("Redes encontradas: " + String(numNetworks));

  for (int i = 0; i < numNetworks && deviceCount < MAX_DEVICES; i++) {
    String mac = WiFi.BSSIDstr(i);          // Dirección MAC del AP detectado
    String ssid = WiFi.SSID(i);             // Nombre de red
    String vendor = getVendorFromMAC(mac);  // Buscar fabricante
    String deviceType = detectDeviceType(mac, ssid, vendor);  // Detección de tipo

    // Guardar en la estructura
    devices[deviceCount].mac = mac;
    devices[deviceCount].hostname = ssid;
    devices[deviceCount].vendor = vendor;
    devices[deviceCount].deviceType = deviceType;
    devices[deviceCount].rssi = WiFi.RSSI(i);

    // Imprimir en consola (debug)
    Serial.println("SSID: " + ssid + " | MAC: " + mac + " | Vendor: " + vendor + " | Tipo: " + deviceType);

    deviceCount++;
  }
}

// ========================== GENERAR PÁGINA HTML ==========================
// Crea el contenido HTML que mostrará la lista de redes/dispositivos
String generateHTML() {
  String html = "<!DOCTYPE html><html><head>";
  html += "<meta charset='UTF-8'>";
  html += "<meta name='viewport' content='width=device-width, initial-scale=1.0'>";
  html += "<title>Escáner Avanzado - ESP32</title>";
  
  // CSS incrustado
  html += "<style>";
  html += "body { font-family: Arial; background: #f0f0f0; margin: 20px; }";
  html += ".container { max-width: 1400px; margin: auto; background: white; padding: 20px; border-radius: 10px; }";
  html += "table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 13px; }";
  html += "th, td { border: 1px solid #ddd; padding: 10px; }";
  html += "th { background: #2E7D32; color: white; }";
  html += "tr:nth-child(even) { background: #f8f9fa; }";
  html += ".btn { background: #2E7D32; color: white; padding: 10px 15px; border: none; border-radius: 5px; cursor: pointer; margin: 5px; }";
  html += ".btn-refresh { background: #1565C0; }";
  html += ".signal-strong { color: #2E7D32; font-weight: bold; }";
  html += ".signal-medium { color: #F57C00; font-weight: bold; }";
  html += ".signal-weak { color: #C62828; font-weight: bold; }";
  html += ".device-phone { background-color: #E8F5E8; }";
  html += ".device-router { background-color: #E3F2FD; }";
  html += ".device-laptop { background-color: #FFF3E0; }";
  html += ".device-iot { background-color: #F3E5F5; }";
  html += ".device-unknown { background-color: #F5F5F5; }";
  html += "</style></head><body>";

  html += "<div class='container'>";
  html += "<h1>🔍 Escáner Avanzado de Red - ESP32</h1>";

  // Información del ESP32
  html += "<div><strong>ESP32 Info:</strong> IP: " + WiFi.localIP().toString() + 
          " | MAC: " + WiFi.macAddress() + 
          " | Señal: " + String(WiFi.RSSI()) + " dBm | Dispositivos: " + String(deviceCount) + "</div>";

  // Botones
  html += "<div><button class='btn btn-refresh' onclick='location.reload()'>🔄 Actualizar</button>";
  html += "<button class='btn' onclick='scanWiFi()'>📡 Escanear Redes</button></div>";

  html += "<h2>📶 Dispositivos Detectados: " + String(deviceCount) + "</h2>";

  // Si hay dispositivos, mostrar tabla
  if (deviceCount > 0) {
    html += "<table>";
    html += "<tr><th>#</th><th>MAC</th><th>Nombre Red</th><th>Fabricante</th><th>Tipo</th><th>Señal</th><th>Calidad</th></tr>";

    for (int i = 0; i < deviceCount; i++) {
      String signalClass = "signal-strong";
      String signalQuality = "Excelente";
      String rowClass = "device-unknown";

      if (devices[i].rssi < -60) { signalClass = "signal-medium"; signalQuality = "Buena"; }
      if (devices[i].rssi < -70) { signalClass = "signal-weak"; signalQuality = "Débil"; }

      // Asignar colores según el tipo de dispositivo
      if (devices[i].deviceType.indexOf("📱") >= 0) rowClass = "device-phone";
      else if (devices[i].deviceType.indexOf("📶") >= 0) rowClass = "device-router";
      else if (devices[i].deviceType.indexOf("💻") >= 0 || devices[i].deviceType.indexOf("🖥️") >= 0) rowClass = "device-laptop";
      else if (devices[i].deviceType.indexOf("🏠") >= 0) rowClass = "device-iot";

      // Fila de la tabla
      html += "<tr class='" + rowClass + "'>";
      html += "<td><strong>" + String(i + 1) + "</strong></td>";
      html += "<td><code>" + devices[i].mac + "</code></td>";
      html += "<td><strong>" + devices[i].hostname + "</strong></td>";
      html += "<td>" + devices[i].vendor + "</td>";
      html += "<td><strong>" + devices[i].deviceType + "</strong></td>";
      html += "<td class='" + signalClass + "'>" + String(devices[i].rssi) + " dBm</td>";
      html += "<td>" + signalQuality + "</td>";
      html += "</tr>";
    }

    html += "</table>";
  } else {
    html += "<p>No se encontraron redes WiFi.</p>";
  }

  // Script para auto refrescar cada 30 segundos
  html += "<script>";
  html += "function scanWiFi() { window.location.href = '/wifi'; }";
  html += "setTimeout(function() { location.reload(); }, 30000);";
  html += "</script>";

  html += "</div></body></html>";
  return html;
}

// ========================== HANDLERS DEL SERVIDOR ==========================
// Página principal
void handleRoot() {
  server.send(200, "text/html", generateHTML());
}

// Endpoint para ejecutar escaneo
void handleWiFiScan() {
  scanWiFiNetworks();
  server.send(200, "text/html", generateHTML());
}

// ========================== SETUP ==========================
void setup() {
  Serial.begin(115200);

  // Conexión a la red WiFi
  WiFi.begin(ssid, password);
  Serial.print("Conectando a WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("\nConectado! IP: " + WiFi.localIP().toString());

  // Configuración de rutas del servidor
  server.on("/", handleRoot);
  server.on("/wifi", handleWiFiScan);

  server.begin();
  Serial.println("Servidor HTTP iniciado");

  // Primer escaneo automático al inicio
  scanWiFiNetworks();
}

// ========================== LOOP ==========================
// Atender solicitudes web constantemente
void loop() {
  server.handleClient();
  delay(100);
}
