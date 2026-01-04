import sqlite3
import requests
from bs4 import BeautifulSoup
from openai import OpenAI
import json
from googlesearch import search

# --- CONFIGURACIÓN ---
# ¡IMPORTANTE! Reemplaza esto con tu API Key real de console.groq.com
API_KEY = "TU_API_KEY_DE_GROQ"

client = OpenAI(
    base_url="https://api.groq.com/openai/v1",
    api_key=API_KEY
)

# --- BASE DE DATOS ---
def guardar_en_historial(query, resultado_json):
    try:
        conn = sqlite3.connect('historial.db')
        cursor = conn.cursor()
        cursor.execute('''CREATE TABLE IF NOT EXISTS investigaciones 
                          (id INTEGER PRIMARY KEY AUTOINCREMENT, 
                           query TEXT, 
                           resultado TEXT, 
                           fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP)''')
        cursor.execute("INSERT INTO investigaciones (query, resultado) VALUES (?, ?)", 
                       (query, resultado_json))
        conn.commit()
        conn.close()
    except Exception as e:
        print(f"Error al guardar en BD: {e}")

# --- MOTOR PRINCIPAL ---
def procesar_investigacion(entrada_usuario):
    try:
        # 1. Búsqueda en Google (Simulación humana)
        enlaces = []
        if entrada_usuario.startswith("http"):
            enlaces = [entrada_usuario]
        else:
            print(f"Buscando: {entrada_usuario}...")
            # Limitamos a 4 resultados para velocidad/rendimiento en t3.micro
            for resultado in search(entrada_usuario, num_results=4):
                enlaces.append(resultado)

        # 2. Scraping y Limpieza
        contenido_recolectado = ""
        fuentes_finales = []
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}

        for link in enlaces:
            try:
                r = requests.get(link, headers=headers, timeout=10)
                if r.status_code == 200:
                    soup = BeautifulSoup(r.text, 'html.parser')
                    
                    # Limpieza agresiva de basura HTML
                    for s in soup(["script", "style", "nav", "footer", "header", "aside", "form", "svg"]): 
                        s.extract()
                    
                    # Extraer texto útil (hasta 2000 caracteres por fuente)
                    texto = soup.get_text(separator=' ', strip=True)[:2000] 
                    contenido_recolectado += f"\n--- FUENTE: {link} ---\n{texto}\n"
                    fuentes_finales.append(link)
            except Exception as e:
                print(f"Error leyendo {link}: {e}")
                continue

        # 3. Inteligencia Artificial (Prompt Universal)
        prompt = f"""
        Actúa como un Analista de Datos experto.
        Tu misión es investigar: '{entrada_usuario}'.
        
        Basado ÚNICAMENTE en este contenido extraído:
        {contenido_recolectado[:6000]}

        Genera un JSON válido con esta estructura exacta:
        {{
            "titulo_principal": "Título profesional del hallazgo",
            "resumen_ejecutivo": "Resumen denso y directo de 3 líneas máximo.",
            "datos_clave": [
                {{ "etiqueta": "NOMBRE DEL DATO", "valor": "VALOR DEL DATO" }}
            ],
            "categoria": "Categoría General (ej. Salud, Tecnología, Laboral)",
            "fuentes": {json.dumps(fuentes_finales)}
        }}

        REGLAS PARA 'datos_clave':
        - Si es LABORAL, extrae: [Empresa, Sueldo, Requisitos, Link de Postulación].
        - Si es COMERCIAL, extrae: [Precio, Tienda, Disponibilidad, Características].
        - Si es TRÁMITE, extrae: [Requisitos, Costo, Enlace Oficial].
        - Sé creativo y extrae lo más valioso para el usuario.
        """

        response = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {"role": "system", "content": "Eres un motor de extracción JSON estricto."},
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"}
        )

        resultado_final = response.choices[0].message.content
        
        # 4. Guardar y Retornar
        guardar_en_historial(entrada_usuario, resultado_final)
        return resultado_final

    except Exception as e:
        # Respuesta de error controlada para no romper el frontend
        return json.dumps({
            "titulo_principal": "Error de Procesamiento",
            "resumen_ejecutivo": f"Ocurrió un error técnico: {str(e)}",
            "datos_clave": [{"etiqueta": "ESTADO", "valor": "FALLIDO"}],
            "categoria": "ERROR",
            "fuentes": []
        })