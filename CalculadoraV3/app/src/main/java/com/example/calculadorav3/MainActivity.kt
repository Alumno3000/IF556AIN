// Paquete actual del proyecto
package com.example.calculadorav3

// Importación de clases necesarias para la interfaz y funcionalidad
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder // Librería para evaluar expresiones matemáticas

// Clase principal de la actividad, que extiende AppCompatActivity
class MainActivity : AppCompatActivity() {

    // Variables de la vista
    private lateinit var tvOperation: EditText   // 🔹 Campo de entrada de operación matemática
    private lateinit var tvResult: TextView      // 🔹 Campo de texto para mostrar el resultado

    // Método que se ejecuta al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Establece el layout XML de la actividad

        // Asigna el EditText para ingresar operaciones
        tvOperation = findViewById(R.id.tvOperation)

        // 🔹 Bloquea el teclado del sistema pero mantiene el cursor visible
        tvOperation.showSoftInputOnFocus = false

        // 🔹 Solución para versiones antiguas de Android (< API 21)
        try {
            val method = EditText::class.java.getMethod(
                "setShowSoftInputOnFocus",  // Nombre del método reflejado
                Boolean::class.javaPrimitiveType // Tipo de parámetro esperado
            )
            method.isAccessible = true     // Permite el acceso al método privado
            method.invoke(tvOperation, false) // Llama al método en el campo tvOperation
        } catch (_: Exception) {} // Ignora cualquier excepción

        // Asigna el TextView para mostrar el resultado
        tvResult = findViewById(R.id.tvResult)

        // Lista de IDs de botones numéricos y operadores básicos
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
        )

        // 🔹 Configura clic para insertar el número u operador en la posición del cursor
        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                insertarTexto((it as Button).text.toString()) // Inserta el texto del botón
                calcularParcial() // Calcula el resultado parcialmente
            }
        }

        // 🔹 Botón para insertar paréntesis () dinámicamente
        findViewById<Button>(R.id.btnParen).setOnClickListener {
            manejarParentesis() // Lógica para decidir si insertar ( o )
            calcularParcial()   // Calcula el resultado parcial
        }

        // 🔹 Botón para insertar punto decimal
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            val currentText = tvOperation.text.toString()
            val lastNumber = currentText.split("+", "-", "*", "/", "(", ")").last()

            if (currentText.isEmpty()) {
                insertarTexto("0.") // Si está vacío, empieza con 0.
            } else if (!lastNumber.contains(".")) {
                insertarTexto(".") // Solo inserta si el número no tiene punto
            }
        }

        // 🔹 Botón para limpiar toda la operación
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tvOperation.setText("") // Borra operación
            tvResult.text = ""      // Borra resultado
        }

        // 🔹 Botón para borrar un carácter (tipo retroceso)
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val cursorPos = tvOperation.selectionStart
            val text = tvOperation.text.toString()
            if (cursorPos > 0) {
                val newText = text.removeRange(cursorPos - 1, cursorPos) // Elimina el carácter anterior
                tvOperation.setText(newText)
                tvOperation.setSelection(cursorPos - 1) // Mueve el cursor una posición atrás
                calcularParcial() // Recalcula resultado parcial
            }
        }

        // 🔹 Botón igual (=) para calcular el resultado final
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            calcularFinal() // Ejecuta cálculo final y muestra resultado
        }
    }

    // 🔹 Inserta texto donde esté el cursor en el EditText
    private fun insertarTexto(valor: String) {
        val cursorPos = tvOperation.selectionStart
        val oldText = tvOperation.text.toString()
        val newText = oldText.substring(0, cursorPos) + valor + oldText.substring(cursorPos)
        tvOperation.setText(newText)
        tvOperation.setSelection(cursorPos + valor.length) // Mueve el cursor después del texto insertado
    }

    // 🔹 Maneja la lógica de inserción de paréntesis (abrir o cerrar)
    private fun manejarParentesis() {
        val texto = tvOperation.text.toString()
        val openCount = texto.count { it == '(' }
        val closeCount = texto.count { it == ')' }

        // Si está vacío o termina con operador, abre paréntesis
        if (texto.isEmpty() || texto.last() in listOf('+', '-', '*', '/', '(')) {
            insertarTexto("(")
        } else if (openCount > closeCount) {
            insertarTexto(")") // Si hay más abiertos que cerrados, cierra
        } else {
            insertarTexto("(") // Si no, abre otro
        }
    }

    // 🔹 Cálculo parcial mientras se escribe la operación
    private fun calcularParcial() {
        try {
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            val result = expression.evaluate()
            tvResult.text = result.toString() // Muestra resultado si es válido
        } catch (_: Exception) {
            tvResult.text = "" // Si hay error (expresión incompleta), no muestra nada
        }
    }

    // 🔹 Cálculo final cuando se presiona "="
    private fun calcularFinal() {
        try {
            val expression = ExpressionBuilder(tvOperation.text.toString()).build()
            val result = expression.evaluate()
            tvResult.text = result.toString()               // Muestra el resultado
            tvOperation.setText(result.toString())          // Reemplaza la operación por el resultado
            tvOperation.setSelection(tvOperation.text.length) // Mueve cursor al final
        } catch (_: Exception) {
            tvResult.text = "" // En caso de error, limpia el resultado
        }
    }
}
