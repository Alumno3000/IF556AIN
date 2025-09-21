package com.example.geoalex

// Importaciones necesarias
import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import java.util.*

/* ---------------------------------------------------------
   ACTIVIDAD PRINCIPAL
   --------------------------------------------------------- */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicia la interfaz con Jetpack Compose
        setContent { GeoApp() }
    }
}

/* ---------------------------------------------------------
   FUNCIÓN PRINCIPAL DE LA APP
   --------------------------------------------------------- */
@Composable
fun GeoApp() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            // Llama a la pantalla principal de ubicación
            LocationScreen()
        }
    }
}

/* ---------------------------------------------------------
   ESTADO DE GEOLOCALIZACIÓN
   --------------------------------------------------------- */
private data class GeoState(
    val lat: Double? = null,          // Latitud
    val lon: Double? = null,          // Longitud
    val accuracyM: Float? = null,     // Precisión en metros
    val lastUpdate: Long? = null,     // Última actualización (timestamp)
    val isGpsEnabled: Boolean = true, // Si el GPS está activado
    val hasPermission: Boolean = false // Si se tienen permisos de ubicación
)

/* ---------------------------------------------------------
   PANTALLA PRINCIPAL DE GEOLOCALIZACIÓN
   --------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen() {
    val context = LocalContext.current

    /* ----------- PERMISOS ----------- */
    // Revisa si el usuario ya dio permiso
    var hasPermission by remember {
        mutableStateOf(
            hasFineOrCoarseLocationPermission(context)
        )
    }

    // Launcher para solicitar permisos en tiempo de ejecución
    val permissionLauncher = rememberLauncherForLocationPermissions(
        onResult = { granted -> hasPermission = granted }
    )

    /* ----------- ESTADO DE GEOLOCALIZACIÓN ----------- */
    var geo by remember {
        mutableStateOf(
            GeoState(
                isGpsEnabled = isLocationEnabled(context),
                hasPermission = hasPermission
            )
        )
    }

    // Revisa estado del GPS cuando la app vuelve al primer plano
    LaunchedEffect(Unit) {
        geo = geo.copy(isGpsEnabled = isLocationEnabled(context))
    }

    /* ----------- FLUJO DE UBICACIONES ----------- */
    val fused by remember { mutableStateOf(LocationServices.getFusedLocationProviderClient(context)) }

    // Si hay permiso, inicia la recolección de ubicaciones
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            locationFlow(fused, context).collectLatest { loc ->
                geo = geo.copy(
                    lat = loc.latitude,
                    lon = loc.longitude,
                    accuracyM = loc.accuracy,
                    lastUpdate = System.currentTimeMillis(),
                    hasPermission = true,
                    isGpsEnabled = isLocationEnabled(context)
                )
            }
        }
    }

    /* ----------- UI ----------- */
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GeoPosición") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Caso 1: No hay permisos
            if (!geo.hasPermission) {
                Text(
                    "Necesitamos permiso de ubicación para mostrar tu geoposición.",
                    textAlign = TextAlign.Center
                )
                Button(onClick = { permissionLauncher() }) { Text("Conceder permiso") }
                return@Column
            }

            // Caso 2: GPS apagado
            if (!geo.isGpsEnabled) {
                Text(
                    "Tu GPS/Ubicación parece estar desactivado. Enciéndelo para obtener coordenadas.",
                    textAlign = TextAlign.Center
                )
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Abrir configuración de ubicación")
                }
            }

            // Datos de ubicación
            Text("Latitud: ${geo.lat?.format(6) ?: "—"}")
            Text("Longitud: ${geo.lon?.format(6) ?: "—"}")
            Text("Precisión (m): ${geo.accuracyM?.let { String.format(Locale.US, "%.1f", it) } ?: "—"}")
            Text("Última actualización: ${geo.lastUpdate?.let { it.toReadableTime() } ?: "—"}")

            // Botones de acción
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Abrir en Google Maps
                Button(
                    enabled = geo.lat != null && geo.lon != null,
                    onClick = {
                        val lat = geo.lat ?: return@Button
                        val lon = geo.lon ?: return@Button
                        openInGoogleMaps(context, lat, lon)
                    }
                ) { Text("Abrir en Google Maps") }

                // Refrescar estado manualmente
                OutlinedButton(
                    onClick = {
                        geo = geo.copy(isGpsEnabled = isLocationEnabled(context))
                        if (!hasPermission) permissionLauncher()
                    }
                ) { Text("Refrescar estado") }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Tips: mantén el teléfono con vista al cielo para mejor precisión. El primer fix puede tardar algunos segundos.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* ---------------------------------------------------------
   HELPERS DE PERMISOS
   --------------------------------------------------------- */

// Lanza el diálogo de permisos de ubicación
@Composable
private fun rememberLauncherForLocationPermissions(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        onResult(granted)
    }
    return {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

// Verifica si ya tenemos permisos de ubicación
private fun hasFineOrCoarseLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED
    return fine || coarse
}

/* ---------------------------------------------------------
   FLUJO DE UBICACIONES
   --------------------------------------------------------- */

// Crea un flujo que emite ubicaciones periódicamente
private fun locationFlow(client: FusedLocationProviderClient, context: Context) = callbackFlow {
    // Verifica permisos antes de iniciar
    if (!hasFineOrCoarseLocationPermission(context)) {
        close()
        return@callbackFlow
    }

    // Configuración de la solicitud de ubicación
    val request = LocationRequest.Builder(2000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateIntervalMillis(1000L)
        .setWaitForAccurateLocation(false)
        .build()

    // Callback para recibir ubicaciones
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { trySend(it).isSuccess }
        }
    }

    // Inicia actualizaciones
    try {
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // Emite también la última ubicación conocida
        client.lastLocation.addOnSuccessListener { loc ->
            loc?.let { trySend(it).isSuccess }
        }
    } catch (e: SecurityException) {
        // Ignorar si no hay permisos
    }

    // Limpieza cuando el flow se cierra
    awaitClose { client.removeLocationUpdates(callback) }
}

/* ---------------------------------------------------------
   UTILIDADES
   --------------------------------------------------------- */

// Revisa si el GPS o red de ubicación está activo
private fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gps = runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
    val net = runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
    return gps || net
}

// Abre las coordenadas en Google Maps
private fun openInGoogleMaps(context: Context, lat: Double, lon: Double) {
    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(Mi%20ubicación)")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) == null) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } else {
        context.startActivity(intent)
    }
}

// Formateo de números decimales
private fun Double.format(digits: Int) =
    "%.${digits}f".format(Locale.US, this)

// Convierte timestamp a fecha legible
private fun Long.toReadableTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss 'del' dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}
