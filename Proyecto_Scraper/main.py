from fastapi import FastAPI, Request
from fastapi.templating import Jinja2Templates
from fastapi.responses import JSONResponse
import sqlite3
import json
from scraper_engine import procesar_investigacion

app = FastAPI()

# Configurar carpeta de plantillas
templates = Jinja2Templates(directory="templates")

# Ruta 1: Página Principal (Frontend)
@app.get("/")
async def home(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})

# Ruta 2: API de Scraping (Recibe la orden)
@app.post("/ejecutar-scraping")
async def scraping_endpoint(payload: dict):
    url = payload.get("url")
    if not url:
        return {"error": "Falta la URL o búsqueda"}
    
    # Llamamos al cerebro
    resultado_json_str = procesar_investigacion(url)
    
    # Convertimos el string JSON de la IA a objeto Python real
    try:
        data = json.loads(resultado_json_str)
    except:
        data = {"error": "La IA no devolvió un JSON válido", "raw": resultado_json_str}
        
    return {"status": "ok", "data": data}

# Ruta 3: Historial (Base de Datos)
@app.get("/obtener-historial")
async def get_historial():
    try:
        conn = sqlite3.connect('historial.db')
        conn.row_factory = sqlite3.Row # Para acceder por nombre de columna
        cursor = conn.cursor()
        # Traemos las últimas 10 investigaciones
        cursor.execute("SELECT * FROM investigaciones ORDER BY id DESC LIMIT 10")
        filas = cursor.fetchall()
        
        resultado = []
        for fila in filas:
            # Parseamos el JSON guardado para sacar categoría y resumen si se necesita
            contenido = json.loads(fila['resultado'])
            resultado.append({
                "tema": fila['query'],
                "categoria": contenido.get('categoria', 'General'),
                "fecha": fila['fecha']
            })
        
        conn.close()
        return resultado
    except Exception as e:
        return [{"tema": "Error cargando historial", "categoria": str(e)}]

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)