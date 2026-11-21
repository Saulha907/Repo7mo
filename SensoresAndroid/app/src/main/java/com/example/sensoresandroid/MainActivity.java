package com.example.sensoresandroid;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
/** Pantalla inicial que conecta las seis Activities mediante un DrawerLayout. */
public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawerLayout);
        // El botón hamburguesa abre el menú desde la izquierda.
        findViewById(R.id.btnMenu).setOnClickListener(v -> drawer.openDrawer(Gravity.LEFT));
        // Cada opción inicia una Activity independiente.
        enlace(R.id.navAcelerometro, AcelerometroActivity.class);
        enlace(R.id.navPodometro, PodometroActivity.class);
        enlace(R.id.navGiroscopio, GiroscopioActivity.class);
        enlace(R.id.navCamara, CamaraActivity.class);
        enlace(R.id.navGps, GpsActivity.class);
        enlace(R.id.navMagnetometro, MagnetometroActivity.class);
    }
    /** Asocia el botón del menú con la pantalla indicada. */
    private void enlace(int buttonId, Class<?> pantalla) {
        Button boton = findViewById(buttonId);
        boton.setOnClickListener(v -> {
            drawer.closeDrawers();
            startActivity(new Intent(this, pantalla));
        });
    }
}
