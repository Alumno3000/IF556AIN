package com.example.breakout

// Importa la clase GameView que será la vista principal del juego
import GameView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// La actividad principal del juego, hereda de AppCompatActivity
class MainActivity : AppCompatActivity() {

    // Declaración de la variable gameView que contendrá la vista del juego
    // "lateinit" indica que se inicializará más adelante, no en la declaración
    private lateinit var gameView: GameView

    // Método que se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa la vista del juego
        gameView = GameView(this)

        // Configura la vista del juego como el contenido principal de la actividad
        setContentView(gameView)
    }

    // Método que se llama cuando la actividad entra en pausa (ej. usuario cambia de app)
    override fun onPause() {
        super.onPause()
        // Pausa el juego para detener animaciones y evitar que siga corriendo en segundo plano
        gameView.pause()
    }

    // Método que se llama cuando la actividad se reanuda
    override fun onResume() {
        super.onResume()
        // Reanuda el juego para continuar con la animación y lógica
        gameView.resume()
    }
}
