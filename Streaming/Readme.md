Este proyecto aborda la problemática de la transmisión de video en tiempo real desde dispositivos con recursos limitados. Se ha implementado una arquitectura híbrida que delega el procesamiento pesado a una instancia EC2 en Amazon Web Services (AWS), mientras que el dispositivo Android actúa únicamente como capturador eficiente utilizando el protocolo RTMP.

## 📊 Arquitectura del Sistema
El flujo de datos se basa en una arquitectura diseñada para la eficiencia:
* **Captura (Android):** El dispositivo funciona como capturador optimizado para ingesta de video H.264.
* **Servidor (AWS EC2):** Instancia con Ubuntu 22.04 LTS que gestiona el módulo RTMP de Nginx.
* **Visualización:** Permite la visualización concurrente en múltiples clientes web con reproducción fluida.



## 🛠️ Requisitos e Infraestructura
Para el despliegue es indispensable la siguiente configuración de red en AWS (Security Groups):
* **TCP 1935:** Permitir entrada RTMP (0.0.0.0/0).
* **TCP 80:** Permitir salida Web/HLS (0.0.0.0/0).

## 🚀 Manual de Replicación e Implementación

### 1. Fase de Servidor (AWS)
Acceda a su instancia vía SSH y ejecute los comandos de instalación:

```bash
sudo apt update && sudo apt install -y libnginx-mod-rtmp nginx
sudo mkdir -p /var/www/html/hls
sudo chown -R www-data:www-data /var/www/html/hls
Debe configurar /etc/nginx/nginx.conf habilitando el bloque rtmp con los parámetros hls on, un fragmento de 3s y la ruta de almacenamiento en /var/www/html/hls.
```
### 2. Fase de Aplicación Android
Para compilar exitosamente el proyecto en Android Studio, configure los siguientes archivos:

* **Dependencias (app/build.gradle.kts)**: Agregue la librería especializada para RTMP:

```Kotlin
implementation("com.github.pedroSG94.RootEncoder:library:2.4.4")
```


* **Permisos del Sistema (AndroidManifest.xml)**: Es obligatorio incluir los permisos de cámara, audio e internet para evitar cierres inesperados.

* Configuración de IP (MainActivity.kt): Dentro del código, localice y modifique la variable de conexión con la IP Pública de su servidor AWS:


```Kotlin
private val rtmpUrl = "rtmp://54.23.11.102/live/alex"
```

### **Análisis de Resultados**
* **Evidencia Técnica:** Tras iniciar la transmisión, se verifica en el servidor la creación de archivos de segmento .ts y el manifiesto .m3u8.

* **Prueba de Estrés:** Reproducción fluida confirmada con múltiples navegadores simultáneos, manteniendo la estabilidad de la CPU del móvil.

