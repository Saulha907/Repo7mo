package com.example.sensoresandroid;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
/** Pantalla de bienvenida: animación de entrada y salto automático al menú. */
public class SplashActivity extends AppCompatActivity {
    private final Handler manejador = new Handler(Looper.getMainLooper());
    private final Runnable continuar = () -> {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Evita regresar al Splash al pulsar Atrás.
    };
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved); setContentView(R.layout.activity_splash);
        TextView logo = findViewById(R.id.txtSplash);
        // Fade-in (opacidad) y escala: el título aparece suavemente.
        logo.setAlpha(0f); logo.setScaleX(.6f); logo.setScaleY(.6f);
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1100).start();
        // Después de 1.8 segundos abrimos el menú principal.
        manejador.postDelayed(continuar, 1800);
    }
    @Override protected void onDestroy() {
        manejador.removeCallbacks(continuar); // Evita tareas pendientes tras cerrar la pantalla.
        super.onDestroy();
    }
}
