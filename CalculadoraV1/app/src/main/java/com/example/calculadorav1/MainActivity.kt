
package com.example.calculadorav1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

// Actividad principal que maneja la calculadora
class MainActivity : AppCompatActivity() {

    // Variables para los TextViews que muestran la operación y el resultado
    private lateinit var tvOperation: TextView
    private lateinit var tvResult: TextView

    // Método que se ejecuta cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Establece el layout de la actividad

        // Inicialización de los TextViews
        tvOperation = findViewById(R.id.tvOperation)
        tvResult = findViewById(R.id.tvResult)

        // Lista de botones para los números y operadores básicos
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
        )

        // Configuración de los botones numéricos y operadores
        buttons.forEach { id ->
            // Asigna un listener a cada botón de la lista
            findViewById<Button>(id).setOnClickListener {
                // Agrega el texto del botón presionado al TextView de operación
                tvOperation.append((it as Button).text)
                // Calcula el resultado parcial
                calcularParcial()
            }
        }

        // Botón de punto decimal
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            val currentText = tvOperation.text.toString()
            if (currentText.isEmpty()) {
                // Si no hay nada en la operación, empieza con "0."
                tvOperation.text = "0."
            } else {
                // Obtiene el último número ingresado en la operación (separado por operadores)
                val lastNumber = currentText.split("+", "-", "*", "/").last()
                // Solo agrega el punto si el último número no tiene uno
                if (!lastNumber.contains(".")) {
                    tvOperation.append(".")
                }
            }
        }

        // Botón Clear (C), limpia la operación y el resultado
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tvOperation.text = ""
            tvResult.text = ""
        }

        // Botón Delete (←), elimina el último carácter de la operación
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val text = tvOperation.text.toString()
            if (text.isNotEmpty()) {
                // Elimina el último carácter de la operación
                tvOperation.text = text.dropLast(1)
                // Calcula el resultado parcial nuevamente
                calcularParcial()
            }
        }

        // Botón Percent (%), convierte el valor en porcentaje
        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            val text = tvOperation.text.toString()
            if (text.isNotEmpty()) {
                try {
                    // Intenta convertir el texto de la operación a un número
                    val value = text.toDoubleOrNull()
                    if (value != null) {
                        // Si es un número válido, lo divide entre 100 y muestra el resultado
                        tvOperation.text = (value / 100).toString()
                        tvResult.text = ""
                    }
                } catch (_: Exception) {
                    // Si no es un número válido, no hace nada
                }
            }
        }

        // Botón Equals (=), calcula el resultado final de la operación
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calcularFinal()
        }
    }

    // Método para calcular el resultado parcial (después de cada operación)
    private fun calcularParcial() {
        try {
            // Crea una expresión a partir del texto en el TextView de operación
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            // Evalúa la expresión y muestra el resultado en tvResult
            val result = expression.evaluate()
            tvResult.text = result.toString()
        } catch (_: Exception) {
            // Si ocurre un error (por ejemplo, una expresión inválida), limpia el resultado
            tvResult.text = ""
        }
    }

    // Método para calcular el resultado final y reemplazar la operación por el resultado
    private fun calcularFinal() {
        try {
            // Crea una expresión a partir del texto en el TextView de operación
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            // Evalúa la expresión y obtiene el resultado
            val result = expression.evaluate()
            // Muestra el resultado en tvResult
            tvResult.text = result.toString()
            // Reemplaza el texto de la operación con el resultado final
            tvOperation.text = result.toString()
        } catch (_: Exception) {
            // Si ocurre un error (por ejemplo, una expresión inválida), limpia el resultado
            tvResult.text = ""
        }
    }
}
