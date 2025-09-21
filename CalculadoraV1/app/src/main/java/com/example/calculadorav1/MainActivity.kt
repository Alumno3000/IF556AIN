package com.example.calculadorav1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var tvOperation: TextView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvOperation = findViewById(R.id.tvOperation)
        tvResult = findViewById(R.id.tvResult)

        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
        )

        // Botones numéricos y operadores básicos
        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                tvOperation.append((it as Button).text)
                calcularParcial()
            }
        }

        // Botón de punto decimal
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            val currentText = tvOperation.text.toString()

            if (currentText.isEmpty()) {
                // Si está vacío, empezamos con "0."
                tvOperation.text = "0."
            } else {
                // Obtenemos el último número ingresado (separado por operadores)
                val lastNumber = currentText.split("+", "-", "*", "/").last()

                // Solo agregamos el punto si ese número todavía no lo contiene
                if (!lastNumber.contains(".")) {
                    tvOperation.append(".")
                }
            }
        }

        // Botón Clear (C)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tvOperation.text = ""
            tvResult.text = ""
        }

        // Botón Delete (←)
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val text = tvOperation.text.toString()
            if (text.isNotEmpty()) {
                tvOperation.text = text.dropLast(1)
                calcularParcial()
            }
        }

        // Botón Percent (%)
        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            val text = tvOperation.text.toString()
            if (text.isNotEmpty()) {
                try {
                    // Solo convertir si es un número válido
                    val value = text.toDoubleOrNull()
                    if (value != null) {
                        tvOperation.text = (value / 100).toString()
                        tvResult.text = ""
                    }
                } catch (_: Exception) {
                }
            }
        }

        // Botón Equals (=)
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calcularFinal()
        }
    }


    private fun calcularParcial() {
        try {
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            val result = expression.evaluate()
            tvResult.text = result.toString()
        } catch (_: Exception) {
            tvResult.text = ""
        }
    }

    private fun calcularFinal() {
        try {
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            val result = expression.evaluate()
            tvResult.text = result.toString()
            tvOperation.text = result.toString() // reemplaza operación con resultado
        } catch (_: Exception) {
            tvResult.text = ""
        }
    }
}
