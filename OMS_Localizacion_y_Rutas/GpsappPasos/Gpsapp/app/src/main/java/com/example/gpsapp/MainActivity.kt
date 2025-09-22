package com.example.gpsapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity(), SensorEventListener {

    // Variables para obtener la ubicación y actualizarla
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val client = OkHttpClient()

    // Variables para manejar el estado del usuario
    private var userName by mutableStateOf("")
    private var userCode by mutableStateOf("")
    private var isSending by mutableStateOf(false)

    // Variables relacionadas con el conteo de pasos
    private var lastLocation: Location? = null
    private var stepsCount = 0
    private val strideLength = 0.75 // Longitud de zancada promedio en metros

    // Variables para manejar los sensores de acelerómetro
    private lateinit var sensorManager: SensorManager
    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización del cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicialización del sensor de acelerómetro
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Configuración de la solicitud de ubicación (actualizaciones cada 5 segundos)
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).setMinUpdateIntervalMillis(3000L).build()

        // Callback para recibir las actualizaciones de la ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Si se está enviando la ubicación, se procesan y envían los datos
                    if (isSending) {
                        updateSteps(location) // Actualiza el contador de pasos
                        sendLocation(location) // Envía la ubicación al servidor
                    }
                }
            }
        }

        // Solicitar permisos de ubicación si no se han concedido
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) Log.e("GPS", "Permiso de ubicación denegado")
            }

        // Verifica si ya se tienen los permisos, si no, los solicita
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Configuración de la UI
        setContent {
            MaterialTheme {
                LocationUI(
                    userName,
                    onNameChange = { userName = it },
                    userCode,
                    onCodeChange = { userCode = it },
                    isSending,
                    onToggleSend = {
                        if (!isSending) startLocationUpdates() else stopLocationUpdates()
                        isSending = !isSending
                    }
                )
            }
        }
    }

    // Función para iniciar la actualización de ubicación
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    // Función para detener la actualización de ubicación
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Función para calcular el número de pasos basados en el movimiento
    private fun updateSteps(location: Location) {
        lastLocation?.let { prev ->
            val distance = prev.distanceTo(location) // Distancia en metros entre ubicaciones
            val steps = (distance / strideLength).toInt() // Calcula pasos a partir de la distancia
            stepsCount += steps // Actualiza el contador de pasos
        }
        lastLocation = location
    }

    // Función para enviar la ubicación, velocidad, aceleración y pasos al servidor
    private fun sendLocation(location: Location) {
        val json = JSONObject().apply {
            put("lat", location.latitude)
            put("lng", location.longitude)
            put("name", userName)
            put("code", userCode)
            put("steps", stepsCount)
            put("speedKmh", location.speed * 3.6) // Convierte velocidad de m/s a km/h
            put("accelX", accelX) // Aceleración en X
            put("accelY", accelY) // Aceleración en Y
            put("accelZ", accelZ) // Aceleración en Z
        }

        // Crear el cuerpo de la solicitud HTTP
        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        // Configurar la solicitud POST
        val request = Request.Builder()
            .url("http://18.224.212.208/location") // URL del servidor
            .post(body) // Enviar datos como cuerpo de la solicitud
            .build()

        // Enviar la solicitud de manera asíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GPS", "Error enviando: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("GPS", "Respuesta: ${response.body?.string()}")
            }
        })
    }

    // Detener actualizaciones de ubicación y sensores cuando se destruye la actividad
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        sensorManager.unregisterListener(this)
    }

    // --- SensorEventListener ---
    // Función que se llama cuando hay un cambio en los sensores (acelerómetro)
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            accelX = it.values[0] // Aceleración en el eje X
            accelY = it.values[1] // Aceleración en el eje Y
            accelZ = it.values[2] // Aceleración en el eje Z
        }
    }

    // Método vacío, pero necesario para implementar SensorEventListener
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

// Composable para la interfaz de usuario (UI)
@Composable
fun LocationUI(
    name: String,
    onNameChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    isSending: Boolean,
    onToggleSend: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Campo para ingresar el nombre del usuario
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Campo para ingresar el código del usuario
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text("Código") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Botón para iniciar o detener el envío de ubicación en tiempo real
        Button(onClick = onToggleSend, modifier = Modifier.fillMaxWidth()) {
            Text(if (isSending) "Detener envío" else "Enviar ubicación en tiempo real")
        }
    }
}
