// ESP32 Scanner WiFi + BLE + Envío SSE + API PHP

#include <WiFi.h>
#include <HTTPClient.h>
#include <BLEDevice.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>

// DATOS WIFI
const char* WIFI_SSID = "Deliz";
const char* WIFI_PASSWORD = "23456789";

// API servidor
const char* SERVER_URL = "http://54.152.244.20/save_measurement.php";

BLEScan* pBLEScan;

float dist(float rssi) {
  float rssiRef = -59;
  float n = 2.0;
  return pow(10, (rssiRef - rssi) / (10 * n));
}

void connectWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
}

void setup() {
  Serial.begin(115200);
  connectWiFi();

  BLEDevice::init("ESP32-SCANNER");
  pBLEScan = BLEDevice::getScan();
  pBLEScan->setActiveScan(true);
}

void loop() {
  connectWiFi();

  BLEScanResults results = pBLEScan->start(5);
  int count = results.getCount();

  String json = "{";
  json += "\"device_id\":\"esp32-scanner-1\",";
  json += "\"ble\":[";

  for (int i = 0; i < count; i++) {
    BLEAdvertisedDevice dev = results.getDevice(i);

    json += "{";
    json += "\"name\":\"" + String(dev.getName().c_str()) + "\",";
    json += "\"mac\":\"" + dev.getAddress().toString().c_str() + "\",";
    json += "\"rssi\":" + String(dev.getRSSI()) + ",";
    json += "\"dist\":" + String(dist(dev.getRSSI()));
    json += "}";

    if (i < count - 1) json += ",";
  }

  json += "]}";

  HTTPClient http;
  http.begin(SERVER_URL);
  http.addHeader("Content-Type", "application/json");
  http.POST(json);
  http.end();

  pBLEScan->clearResults();
  delay(2000);
}

