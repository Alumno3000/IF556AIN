package com.example.calculadorav2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.calculadorav2.R
import net.objecthunter.exp4j.ExpressionBuilder

// Actividad principal que maneja la lógica de la calculadora
class MainActivity : AppCompatActivity() {

    // Variables que almacenarán las vistas de los TextViews para mostrar la operación y el resultado
    private lateinit var tvOperation: TextView
    private lateinit var tvResult: TextView

    // Método que se ejecuta cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Establece el layout de la actividad

        // Inicialización de los TextViews a partir de su ID en el layout
        tvOperation = findViewById(R.id.tvOperation)
        tvResult = findViewById(R.id.tvResult)

        // Lista de IDs de los botones numéricos y operadores básicos
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
        )

        // Configuración de los botones numéricos y operadores
        buttons.forEach { id ->
            // Asigna un listener a cada botón para agregar su texto a la operación
            findViewById<Button>(id).setOnClickListener {
                tvOperation.append((it as Button).text)
                // Después de agregar el texto, calcula el resultado parcial
                calcularParcial()
            }
        }

        // Configuración del botón de paréntesis
        findViewById<Button>(R.id.btnParen).setOnClickListener {
            // Llama a la función que maneja la apertura y cierre de paréntesis
            manejarParentesis()
            // Calcula el resultado parcial cada vez que se modifica la operación
            calcularParcial()
        }

        // Configuración del botón de punto decimal
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            val currentText = tvOperation.text.toString()

            if (currentText.isEmpty()) {
                // Si el TextView está vacío, se inicia con "0."
                tvOperation.text = "0."
            } else {
                // Si no está vacío, verifica el último número ingresado y agrega el punto solo si no lo tiene
                val lastNumber = currentText.split("+", "-", "*", "/", "(", ")").last()
                if (!lastNumber.contains(".")) {
                    tvOperation.append(".")
                }
            }
        }

        // Configuración del botón Clear (C) para borrar la operación y el resultado
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tvOperation.text = ""  // Borra la operación
            tvResult.text = ""     // Borra el resultado
        }

        // Configuración del botón Delete (←) para eliminar el último carácter de la operación
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val text = tvOperation.text.toString()
            if (text.isNotEmpty()) {
                // Elimina el último carácter de la operación y recalcula el resultado parcial
                tvOperation.text = text.dropLast(1)
                calcularParcial()
            }
        }

        // Configuración del botón Equals (=) para calcular el resultado final
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calcularFinal()
        }
    }

    // Función que maneja el balance de paréntesis
    private fun manejarParentesis() {
        val texto = tvOperation.text.toString()

        // Contamos la cantidad de paréntesis de apertura "(" y cierre ")"
        val openCount = texto.count { it == '(' }
        val closeCount = texto.count { it == ')' }

        if (texto.isEmpty() || texto.last() in listOf('+', '-', '*', '/', '(')) {
            // Si la operación está vacía o termina con un operador o paréntesis abierto, agregamos "("
            tvOperation.append("(")
        } else if (openCount > closeCount) {
            // Si hay más paréntesis de apertura que de cierre, agregamos ")"
            tvOperation.append(")")
        } else {
            // Si todo está balanceado, agregamos "(" nuevamente
            tvOperation.append("(")
        }
    }

    // Función para calcular el resultado parcial de la operación a medida que el usuario ingresa números
    private fun calcularParcial() {
        try {
            // Utiliza la librería exp4j para evaluar la expresión matemática que está en tvOperation
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            // Calcula el resultado de la expresión
            val result = expression.evaluate()
            // Muestra el resultado parcial en tvResult
            tvResult.text = result.toString()
        } catch (_: Exception) {
            // Si ocurre un error (por ejemplo, la expresión no es válida), se limpia el resultado
            tvResult.text = ""
        }
    }

    // Función para calcular el resultado final cuando el usuario presiona el botón "="
    private fun calcularFinal() {
        try {
            // Evalúa la expresión completa de tvOperation
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            val result = expression.evaluate()
            // Muestra el resultado en tvResult
            tvResult.text = result.toString()
            // Reemplaza la operación con el resultado final
            tvOperation.text = result.toString()
        } catch (_: Exception) {
            // Si la expresión no es válida, se limpia el resultado
            tvResult.text = ""
        }
    }
}
