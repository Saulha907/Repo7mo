package com.example.actividadlayouts;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Actividad que muestra un formulario organizado en filas y columnas
public class TableLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el diseño de TableLayout
        setContentView(R.layout.activity_table_layout);

        // Obtiene los campos y el botón del formulario
        EditText txtNombre = findViewById(R.id.txtNombre);
        EditText txtCorreo = findViewById(R.id.txtCorreo);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        // Comprueba los datos cuando se pulsa Registrar
        btnRegistrar.setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString().trim();
            String correo = txtCorreo.getText().toString().trim();

            if (nombre.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Registro realizado: " + nombre, Toast.LENGTH_LONG).show();
            }
        });
    }
}
