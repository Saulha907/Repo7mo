
package com.example.actividadlayouts;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Actividad que muestra el ejemplo de LinearLayout
public class LinearLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el diseño XML de LinearLayout
        setContentView(R.layout.activity_linear_layout);
    }
}
