
package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnContador, btnCalculadora, btnTemperatura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnContador = findViewById(R.id.btnContador);
        btnCalculadora = findViewById(R.id.btnCalculadora);
        btnTemperatura = findViewById(R.id.btnTemperatura);

        btnContador.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContadorActivity.class);
            intent.putExtra("titulo", "Contador");
            intent.putExtra("numeroInicial", 0);
            startActivity(intent);
        });

        btnCalculadora.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalculadoraActivity.class);
            intent.putExtra("titulo", "Calculadora de sumas");
            startActivity(intent);
        });

        btnTemperatura.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TemperaturaActivity.class);
            intent.putExtra("titulo", "Conversor de temperatura");
            intent.putExtra("unidad", "Celsius a Fahrenheit");
            startActivity(intent);
        });
    }
}
