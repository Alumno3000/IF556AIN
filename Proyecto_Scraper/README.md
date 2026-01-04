# 🤖 Cloud AI Smart Scraper

![Python](https://img.shields.io/badge/Python-3.8%2B-blue)
![AWS](https://img.shields.io/badge/AWS-EC2-orange)
![FastAPI](https://img.shields.io/badge/FastAPI-0.68%2B-green)
![AI](https://img.shields.io/badge/AI-Llama%203.3-purple)
![License](https://img.shields.io/badge/License-MIT-grey)

**Agente de Investigación Universal** desplegado en la nube. Este sistema automatiza la búsqueda de información web, utilizando Inteligencia Artificial para extraer, limpiar y estructurar datos de cualquier dominio (laboral, médico, comercial, etc.) en tiempo real.

---

## 🚀 Características Principales

- **🧠 Inteligencia Semántica Universal:** A diferencia de los scrapers tradicionales, no usa selectores CSS rígidos. Integra **Llama 3.3 (vía Groq)** para "leer" el contenido HTML y entender qué datos son relevantes (precios, requisitos, fechas) según el contexto de la búsqueda.
- **☁️ Arquitectura Cloud:** Optimizado para ejecutarse en instancias ligeras de **AWS EC2 (t3.micro)**, gestionando eficientemente la memoria RAM.
- **⚡ Backend Asíncrono:** Construido con **FastAPI**, permite procesar múltiples investigaciones simultáneas sin bloquear el servidor.
- **📱 Interfaz Mobile-First:** Frontend desarrollado con **Tailwind CSS**, permitiendo la activación y monitoreo remoto desde dispositivos móviles Android.
- **💾 Auditoría y Persistencia:** Sistema de historial integrado con **SQLite** para almacenar y consultar investigaciones pasadas.
- **📥 Exportación de Datos:** Generación automática de reportes estructurados descargables en formato **JSON**.

---

## 🛠️ Stack Tecnológico

- **Backend:** Python, FastAPI, Uvicorn.
- **IA & NLP:** Groq API (Llama 3.3-70B), BeautifulSoup4.
- **Infraestructura:** AWS EC2 (Ubuntu 24.04).
- **Frontend:** HTML5, Jinja2, Tailwind CSS via CDN.
- **Base de Datos:** SQLite3.

---

## ⚙️ Instalación y Despliegue

Sigue estos pasos para replicar el proyecto en un entorno local o en la nube (AWS):

### 1. Clonar el Repositorio

git clone [https://github.com/Alumno3000/IF556AIN.git](https://github.com/Alumno3000/IF556AIN.git)
cd IF556AIN/Proyecto_Scraper

2. Configurar el Entorno Virtual
Es recomendable usar un entorno aislado para las dependencias.

sudo apt update && sudo apt install python3-venv -y
python3 -m venv venv
source venv/bin/activate

3. Instalar Dependencias


pip install fastapi uvicorn requests beautifulsoup4 openai googlesearch-python jinja2
4. Configuración Crítica (API Key)
El sistema requiere una clave de API gratuita de Groq Cloud para funcionar.

Abre el archivo scraper_engine.py.

Localiza la variable API_KEY y pega tu clave:

Python

API_KEY = "gsk_TU_CLAVE_AQUI..."
5. Estructura de Directorios
Asegúrate de que el archivo index.html se encuentre dentro de la carpeta templates/ para que FastAPI lo reconozca.

mkdir -p templates
mv index.html templates/ 2>/dev/null || true

▶️ Ejecución
Modo Desarrollo
Bash

python main.py
