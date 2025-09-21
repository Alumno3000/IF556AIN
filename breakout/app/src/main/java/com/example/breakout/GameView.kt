package com.example.breakout

// Importaciones necesarias
import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.breakout.R
import kotlin.math.abs
import kotlin.random.Random

// Clase GameView que extiende SurfaceView y escucha callbacks del SurfaceHolder
class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    // Fondo y pintura
    private val bgColor = Color.BLACK
    private val paint = Paint()

    // Hilo del juego
    private var gameThread: Thread? = null
    @Volatile private var running = false  // Volatile para sincronización de hilos

    // Dimensiones de la pantalla
    private var screenW = 0
    private var screenH = 0

    // Paddle (barra del jugador)
    private var paddle: RectF? = null
    private var paddleWidth = 300f
    private var paddleHeight = 30f
    private var paddleY = 0f

    // Bola
    private var ballRadius = 18f
    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 8f
    private var ballSpeedY = -8f

    // Ladrillos
    data class Brick(var rect: RectF, var alive: Boolean = true)
    private val bricks = mutableListOf<Brick>()
    private val rows = 5
    private val cols = 7
    private var brickGap = 8f
    private var brickHeight = 50f

    // Vidas y puntuación
    private var lives = 3
    private var score = 0
    private var isGameOver = false   // Bandera de Game Over

    // Sonidos
    private var soundPool: SoundPool
    private var sPaddle = 0
    private var sBrick = 0
    private var sWall = 0
    private var sLose = 0

    // Inicialización
    init {
        holder.addCallback(this)  // Se registra el callback para eventos de Surface
        paint.isAntiAlias = true  // Pintura suave

        // Configuración de Audio para el juego
        val attrsAudio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attrsAudio)
            .setMaxStreams(4)  // Máximo 4 sonidos simultáneos
            .build()
    }

    // Se llama cuando el SurfaceView está listo
    override fun surfaceCreated(holder: SurfaceHolder) {
        screenW = width
        screenH = height

        // Inicialización del paddle
        paddleWidth = (screenW * 0.25f).coerceAtLeast(200f)
        paddleHeight = (screenH * 0.02f).coerceAtLeast(24f)
        paddleY = screenH - paddleHeight * 4
        paddle = RectF((screenW - paddleWidth) / 2, paddleY, (screenW + paddleWidth) / 2, paddleY + paddleHeight)

        // Inicialización de la bola
        resetBall()

        // Crear ladrillos
        bricks.clear()
        val totalGapX = (cols + 1) * brickGap
        val brickWidth = (screenW - totalGapX) / cols
        brickHeight = (screenH * 0.06f).coerceAtLeast(36f)
        val startY = screenH * 0.12f
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = brickGap + c * (brickWidth + brickGap)
                val top = startY + r * (brickHeight + brickGap)
                val rect = RectF(left, top, left + brickWidth, top + brickHeight)
                bricks.add(Brick(rect, true))
            }
        }

        // Cargar sonidos
        try { sPaddle = soundPool.load(context, R.raw.paddle_hit, 1) } catch (_: Exception) {}
        try { sBrick = soundPool.load(context, R.raw.brick_break, 1) } catch (_: Exception) {}
        try { sWall = soundPool.load(context, R.raw.wall_hit, 1) } catch (_: Exception) {}
        try { sLose = soundPool.load(context, R.raw.life_lost, 1) } catch (_: Exception) {}

        // Iniciar hilo del juego
        running = true
        gameThread = Thread { gameLoop() }
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        var retry = true
        while (retry) {
            try {
                gameThread?.join()  // Esperar a que el hilo termine
                retry = false
            } catch (_: InterruptedException) {}
        }
        soundPool.release()  // Liberar recursos de sonido
    }

    // Loop principal del juego
    private fun gameLoop() {
        val targetFps = 60
        val targetTime = (1000 / targetFps).toLong()
        while (running) {
            val start = System.currentTimeMillis()

            if (!isGameOver) update()  // Actualiza la lógica del juego
            draw()                      // Dibuja todo en pantalla

            val elapsed = System.currentTimeMillis() - start
            val wait = targetTime - elapsed
            if (wait > 0) {
                try { Thread.sleep(wait) } catch (_: InterruptedException) {}
            }
        }
    }

    // Actualización de la lógica del juego
    private fun update() {
        val paddle = this.paddle ?: return

        // Movimiento de la bola
        ballX += ballSpeedX
        ballY += ballSpeedY

        // Colisión con paredes
        if (ballX - ballRadius < 0) {
            ballX = ballRadius
            ballSpeedX = -ballSpeedX
            playSound(sWall)
        } else if (ballX + ballRadius > screenW) {
            ballX = screenW - ballRadius
            ballSpeedX = -ballSpeedX
            playSound(sWall)
        }

        // Colisión con techo
        if (ballY - ballRadius < 0) {
            ballY = ballRadius
            ballSpeedY = -ballSpeedY
            playSound(sWall)
        }

        // Colisión con paddle
        if (RectF(paddle.left, paddle.top, paddle.right, paddle.bottom).contains(ballX, ballY + ballRadius)) {
            ballY = paddle.top - ballRadius
            val hitPos = (ballX - paddle.left) / paddle.width()
            val angleFactor = (hitPos - 0.5f) * 2f
            ballSpeedX += angleFactor * 6f
            ballSpeedY = -abs(ballSpeedY)
            playSound(sPaddle)
        }

        // Colisión con ladrillos
        for (brick in bricks) {
            if (brick.alive &&
                ballX + ballRadius > brick.rect.left &&
                ballX - ballRadius < brick.rect.right &&
                ballY + ballRadius > brick.rect.top &&
                ballY - ballRadius < brick.rect.bottom) {

                val overlapLeft = ballX + ballRadius - brick.rect.left
                val overlapRight = brick.rect.right - (ballX - ballRadius)
                val overlapTop = ballY + ballRadius - brick.rect.top
                val overlapBottom = brick.rect.bottom - (ballY - ballRadius)

                val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)
                if (minOverlap == overlapLeft || minOverlap == overlapRight) {
                    ballSpeedX = -ballSpeedX
                } else {
                    ballSpeedY = -ballSpeedY
                }
                brick.alive = false
                score += 10
                playSound(sBrick)
                break
            }
        }

        // Si la bola cae
        if (ballY - ballRadius > screenH) {
            lives--
            playSound(sLose)
            if (lives <= 0) {
                isGameOver = true
            } else {
                resetBall()
            }
        }

        // Condición de victoria
        if (bricks.none { it.alive }) {
            initNextLevel()
        }
    }

    // Reinicia la posición de la bola
    private fun resetBall() {
        val paddle = this.paddle ?: return
        paddle.left = (screenW - paddleWidth) / 2
        paddle.right = paddle.left + paddleWidth
        ballX = (screenW / 2).toFloat()
        ballY = paddleY - ballRadius - 10f
        ballSpeedX = 8f * if (Random.nextBoolean()) 1 else -1
        ballSpeedY = -8f
    }

    // Reinicia todo el juego
    private fun restartGame() {
        lives = 3
        score = 0
        isGameOver = false
        for (b in bricks) b.alive = true
        resetBall()
    }

    // Inicializa siguiente nivel aumentando velocidad de la bola
    private fun initNextLevel() {
        for (b in bricks) b.alive = true
        ballSpeedX *= 1.12f
        ballSpeedY *= 1.12f
    }

    // Dibuja todo en pantalla
    private fun draw() {
        val paddle = this.paddle ?: return
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(bgColor)  // Fondo

            if (isGameOver) {
                paint.color = Color.WHITE
                paint.textSize = 100f
                canvas.drawText("GAME OVER", screenW / 4f, screenH / 2f, paint)
                paint.textSize = 60f
                canvas.drawText("Toca para reintentar", screenW / 4f - 50, screenH / 2f + 100, paint)
                return
            }

            // Dibujo paddle
            paint.color = Color.WHITE
            canvas.drawRoundRect(paddle, 12f, 12f, paint)

            // Dibujo bola
            paint.color = Color.YELLOW
            canvas.drawCircle(ballX, ballY, ballRadius, paint)

            // Dibujo ladrillos
            for ((i, brick) in bricks.withIndex()) {
                if (brick.alive) {
                    val hue = (i * 20) % 360
                    paint.color = Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.9f, 0.9f))
                    canvas.drawRect(brick.rect, paint)
                }
            }

            // Dibujo UI
            paint.color = Color.WHITE
            paint.textSize = 48f
            paint.isFakeBoldText = true
            canvas.drawText("Score: $score", 30f, 60f, paint)
            canvas.drawText("Lives: $lives", screenW - 220f, 60f, paint)

        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    // Manejo de eventos táctiles
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val paddle = this.paddle ?: return false
        event ?: return false

        // Reiniciar juego si está en Game Over
        if (isGameOver && event.action == MotionEvent.ACTION_DOWN) {
            restartGame()
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val half = paddle.width() / 2
                var left = x - half
                var right = x + half
                // Limitar paddle dentro de pantalla
                if (left < 0) {
                    left = 0f
                    right = left + paddle.width()
                } else if (right > screenW) {
                    right = screenW.toFloat()
                    left = right - paddle.width()
                }
                paddle.left = left
                paddle.right = right
            }
        }
        return true
    }

    // Pausar el juego
    fun pause() { running = false }

    // Reanudar el juego
    fun resume() {
        if (!running) {
            running = true
            gameThread = Thread { gameLoop() }
            gameThread?.start()
        }
    }

    // Reproducir sonido
    private fun playSound(soundId: Int) {
        if (soundId != 0) soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }
}
