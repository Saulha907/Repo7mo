
package com.example.calculadorasumas

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val num1 = findViewById<EditText>(R.id.num1)
        val num2 = findViewById<EditText>(R.id.num2)
        val btnSumar = findViewById<Button>(R.id.btnSumar)
        val txtResultado = findViewById<TextView>(R.id.txtResultado)

        btnSumar.setOnClickListener {

            val numero1 = num1.text.toString().toDoubleOrNull()
            val numero2 = num2.text.toString().toDoubleOrNull()

            if (numero1 != null && numero2 != null) {

                val suma = numero1 + numero2

                txtResultado.text = "RESULTADO: $suma"

            } else {

                Toast.makeText(
                    this,
                    "Ingresa dos números válidos",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
}
