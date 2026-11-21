
package com.example.verthorizon;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText edtDato;
    Button btnMostrar;
    TextView txtResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("CicloVida", "onCreate");
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

        edtDato = findViewById(R.id.edtDato);
        btnMostrar = findViewById(R.id.btnMostrar);
        txtResultado = findViewById(R.id.txtResultado);

        if (savedInstanceState != null) {
            String dato = savedInstanceState.getString("datoGuardado");
            if (dato != null) {
                txtResultado.setText(dato);
            }
        }

        btnMostrar.setOnClickListener(v -> {
            String dato = edtDato.getText().toString();
            txtResultado.setText(dato);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String dato = txtResultado.getText().toString();
        outState.putString("datoGuardado", dato);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("CicloVida", "onPause");
        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("CicloVida", "onDestroy");
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }
}
