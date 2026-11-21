
package com.example.demointent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ContadorActivity extends AppCompatActivity {

    TextView txtTitulo, txtContador;
    Button btnSumar, btnRegresar;
    int contador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contador);

        txtTitulo = findViewById(R.id.txtTituloContador);
        txtContador = findViewById(R.id.txtContador);
        btnSumar = findViewById(R.id.btnSumar);
        btnRegresar = findViewById(R.id.btnRegresarContador);

        String titulo = getIntent().getStringExtra("titulo");
        contador = getIntent().getIntExtra("numeroInicial", 0);

        if (titulo != null) {
            txtTitulo.setText(titulo);
        }

        if (savedInstanceState != null) {
            contador = savedInstanceState.getInt("contador", contador);
        }

        txtContador.setText(String.valueOf(contador));

        btnSumar.setOnClickListener(v -> {
            contador++;
            txtContador.setText(String.valueOf(contador));
        });

        btnRegresar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("contador", contador);
        super.onSaveInstanceState(outState);
    }
}
