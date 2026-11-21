
package com.example.actividadlayouts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga la pantalla principal
        setContentView(R.layout.activity_main);

        // Obtiene los cuatro botones del XML
        Button btnLinear = findViewById(R.id.btnLinear);
        Button btnRelative = findViewById(R.id.btnRelative);
        Button btnFrame = findViewById(R.id.btnFrame);
        Button btnTable = findViewById(R.id.btnTable);

        // Abre la actividad LinearLayout
        btnLinear.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LinearLayoutActivity.class
            );
            startActivity(intent);
        });

        // Abre la actividad RelativeLayout
        btnRelative.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RelativeLayoutActivity.class
            );
            startActivity(intent);
        });

        // Abre la actividad FrameLayout
        btnFrame.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    FrameLayoutActivity.class
            );
            startActivity(intent);
        });

        // Abre la actividad TableLayout
        btnTable.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    TableLayoutActivity.class
            );
            startActivity(intent);
        });
    }
}
