
package com.example.actividadlayouts;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Actividad que muestra RelativeLayout
public class RelativeLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el diseño XML correspondiente
        setContentView(R.layout.activity_relative_layout);
    }
}
