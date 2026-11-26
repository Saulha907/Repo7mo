
package com.example.fragmentado;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    Button btnUno, btnDos, btnTres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUno = findViewById(R.id.btnUno);
        btnDos = findViewById(R.id.btnDos);
        btnTres = findViewById(R.id.btnTres);

        btnUno.setOnClickListener(v -> {
            cambiarFragmento(new FragmentoUno());
        });

        btnDos.setOnClickListener(v -> {
            cambiarFragmento(new FragmentoDos());
        });

        btnTres.setOnClickListener(v -> {
            cambiarFragmento(new FragmentoTres());
        });

        if (savedInstanceState == null) {
            cambiarFragmento(new FragmentoUno());
        }
    }

    private void cambiarFragmento(Fragment fragmento) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmentos, fragmento)
                .commit();
    }
}
