package com.example.demointent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CalculadoraActivity extends AppCompatActivity {

    TextView txtTitulo, txtResultado;
    EditText edtNumero1, edtNumero2;
    Button btnCalcular, btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculadora);

        txtTitulo = findViewById(R.id.txtTituloCalculadora);
        txtResultado = findViewById(R.id.txtResultado);
        edtNumero1 = findViewById(R.id.edtNumero1);
        edtNumero2 = findViewById(R.id.edtNumero2);
        btnCalcular = findViewById(R.id.btnCalcular);
        btnRegresar = findViewById(R.id.btnRegresarCalculadora);

        String titulo = getIntent().getStringExtra("titulo");

        if (titulo != null) {
            txtTitulo.setText(titulo);
        }

        btnCalcular.setOnClickListener(v -> {
            String valor1 = edtNumero1.getText().toString().trim();
            String valor2 = edtNumero2.getText().toString().trim();

            if (valor1.isEmpty() || valor2.isEmpty()) {
                Toast.makeText(this, "Ingresa ambos números", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double numero1 = Double.parseDouble(valor1);
                double numero2 = Double.parseDouble(valor2);
                double resultado = numero1 + numero2;

                txtResultado.setText("Resultado: " + resultado);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingresa números válidos", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegresar.setOnClickListener(v -> finish());
    }
}
