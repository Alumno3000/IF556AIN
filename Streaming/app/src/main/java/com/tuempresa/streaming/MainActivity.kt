package com.tuempresa.streaming

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pedro.library.view.OpenGlView
import com.pedro.library.rtmp.RtmpCamera1
import com.pedro.common.ConnectChecker

class MainActivity : ComponentActivity(), ConnectChecker {

    private var rtmpCamera1: RtmpCamera1? = null
    // TU IP DE AWS
    private val rtmpUrl = "rtmp://18.224.212.208/live/alex"

    // --- CORRECCIÓN IMPORTANTE ---
    // Movemos el estado aquí para que los Callbacks puedan cambiar el botón
    private val _isStreaming = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme {
                StreamingScreen()
            }
        }
    }

    @Composable
    fun StreamingScreen() {
        val context = LocalContext.current

        // Leemos el estado global de la clase
        val isStreaming by remember { _isStreaming }
        var hasPermissions by remember { mutableStateOf(false) }

        // Estados de control de UI
        var isMicOn by remember { mutableStateOf(true) }
        var isVideoOn by remember { mutableStateOf(true) }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val cam = permissions[Manifest.permission.CAMERA] ?: false
            val mic = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            hasPermissions = (cam && mic)
        }

        LaunchedEffect(Unit) {
            val camStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            val micStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            if (camStatus == PackageManager.PERMISSION_GRANTED && micStatus == PackageManager.PERMISSION_GRANTED) {
                hasPermissions = true
            } else {
                launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            // 1. CAPA DE CÁMARA
            if (hasPermissions) {
                AndroidView(
                    factory = { ctx ->
                        OpenGlView(ctx).apply {
                            holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    if (rtmpCamera1 == null) {
                                        rtmpCamera1 = RtmpCamera1(this@apply, this@MainActivity)
                                    }
                                    if (rtmpCamera1?.isStreaming == false) {
                                        rtmpCamera1?.startPreview(1280, 720)
                                    }
                                }
                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
                                    rtmpCamera1?.startPreview(1280, 720)
                                }
                                override fun surfaceDestroyed(holder: SurfaceHolder) {
                                    if (rtmpCamera1?.isStreaming == true) rtmpCamera1?.stopStream()
                                    rtmpCamera1?.stopPreview()
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. CAPA NEGRA (Privacidad)
            if (!isVideoOn) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CÁMARA APAGADA", color = Color.Gray, fontSize = 20.sp)
                }
            }

            // 3. CONTROLES
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                    // CAMBIAR CÁMARA
                    FloatingActionButton(
                        onClick = { try { rtmpCamera1?.switchCamera() } catch (e: Exception) {} },
                        containerColor = Color.DarkGray, contentColor = Color.White
                    ) { Text("🔄", fontSize = 24.sp) }

                    // MICRÓFONO
                    FloatingActionButton(
                        onClick = {
                            isMicOn = !isMicOn
                            if (isMicOn) rtmpCamera1?.enableAudio() else rtmpCamera1?.disableAudio()
                            Toast.makeText(context, if(isMicOn) "Mic ON" else "Mic OFF", Toast.LENGTH_SHORT).show()
                        },
                        containerColor = if (isMicOn) Color.DarkGray else Color.Red,
                        contentColor = Color.White
                    ) { Text(if (isMicOn) "🎤" else "🔇", fontSize = 24.sp) }

                    // VIDEO
                    FloatingActionButton(
                        onClick = {
                            isVideoOn = !isVideoOn
                            if (isVideoOn) rtmpCamera1?.glInterface?.unMuteVideo() else rtmpCamera1?.glInterface?.muteVideo()
                        },
                        containerColor = if (isVideoOn) Color.DarkGray else Color.Red,
                        contentColor = Color.White
                    ) { Text(if (isVideoOn) "📹" else "🚫", fontSize = 24.sp) }
                }

                // --- BOTÓN PRINCIPAL (INICIAR / DETENER) ---
                // Este botón cambia de función según el estado 'isStreaming'
                Button(
                    onClick = {
                        if (!hasPermissions) return@Button

                        if (!isStreaming) {
                            // SI NO ESTÁ TRANSMITIENDO -> INICIA
                            if (rtmpCamera1?.prepareAudio() == true &&
                                rtmpCamera1?.prepareVideo(1280, 720, 30, 2500 * 1024, 0) == true) {
                                rtmpCamera1?.startStream(rtmpUrl)
                            } else {
                                Toast.makeText(context, "Error Hardware", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // SI YA ESTÁ TRANSMITIENDO -> DETIENE
                            rtmpCamera1?.stopStream()
                            rtmpCamera1?.startPreview(1280, 720)
                            _isStreaming.value = false // Forzamos el cambio de estado inmediato
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        // Cambia de color: ROJO si transmite, VERDE si está listo
                        containerColor = if (isStreaming) Color.Red else Color(0xFF00C853)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(60.dp).width(250.dp)
                ) {
                    // Cambia el texto
                    Text(
                        text = if (isStreaming) "DETENER STREAM" else "INICIAR HD",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    // --- CALLBACKS QUE ACTUALIZAN EL BOTÓN ---

    override fun onConnectionStarted(url: String) {
        runOnUiThread { Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show() }
    }

    override fun onConnectionSuccess() {
        runOnUiThread {
            Toast.makeText(this, "✅ EN VIVO", Toast.LENGTH_LONG).show()
            _isStreaming.value = true // ¡AQUÍ EL BOTÓN SE PONE ROJO Y DICE DETENER!
        }
    }

    override fun onConnectionFailed(reason: String) {
        runOnUiThread {
            Toast.makeText(this, "Fallo: $reason", Toast.LENGTH_LONG).show()
            if (rtmpCamera1?.isStreaming == true) {
                rtmpCamera1?.stopStream()
                rtmpCamera1?.startPreview(1280, 720)
            }
            _isStreaming.value = false // Si falla, el botón vuelve a verde
        }
    }

    override fun onDisconnect() {
        runOnUiThread {
            Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()
            _isStreaming.value = false // Al desconectar, botón vuelve a verde
        }
    }

    override fun onNewBitrate(bitrate: Long) {}
    override fun onAuthError() { runOnUiThread { _isStreaming.value = false } }
    override fun onAuthSuccess() {}
}