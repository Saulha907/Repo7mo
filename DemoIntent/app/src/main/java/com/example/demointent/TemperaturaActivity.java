
package com.example.demointent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class TemperaturaActivity extends AppCompatActivity {

    TextView txtTitulo, txtUnidad, txtResultado;
    EditText edtCelsius;
    Button btnConvertir, btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperatura);

        txtTitulo = findViewById(R.id.txtTituloTemperatura);
        txtUnidad = findViewById(R.id.txtUnidad);
        txtResultado = findViewById(R.id.txtResultadoTemperatura);
        edtCelsius = findViewById(R.id.edtCelsius);
        btnConvertir = findViewById(R.id.btnConvertir);
        btnRegresar = findViewById(R.id.btnRegresarTemperatura);

        String titulo = getIntent().getStringExtra("titulo");
        String unidad = getIntent().getStringExtra("unidad");

        if (titulo != null) {
            txtTitulo.setText(titulo);
        }

        if (unidad != null) {
            txtUnidad.setText(unidad);
        }

        btnConvertir.setOnClickListener(v -> {
            String valor = edtCelsius.getText().toString().trim();

            if (valor.isEmpty()) {
                Toast.makeText(this, "Ingresa una temperatura", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double celsius = Double.parseDouble(valor);
                double fahrenheit = (celsius * 9 / 5) + 32;

                txtResultado.setText(String.format(
                        Locale.getDefault(),
                        "Resultado: %.2f °F",
                        fahrenheit
                ));

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ingresa un número válido", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegresar.setOnClickListener(v -> finish());
    }
}
