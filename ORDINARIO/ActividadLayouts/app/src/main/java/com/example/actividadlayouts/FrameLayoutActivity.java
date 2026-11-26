
package com.example.actividadlayouts;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Actividad que muestra el ejemplo de FrameLayout
public class FrameLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga el archivo XML correspondiente
        setContentView(R.layout.activity_frame_layout);
    }
}
