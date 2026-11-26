package com.example.sensoresandroid;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
/** GPS: consulta localización con LocationManager; sin servicios de mapas externos. */
public class GpsActivity extends AppCompatActivity implements LocationListener {
    private TextView datos;
    private LocationManager ubicacion;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_sensor);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.txtTitulo)).setText("GPS");
        ((TextView)findViewById(R.id.txtDescripcion)).setText("Consulta latitud, longitud y precisión. Activa la ubicación del teléfono.");
        datos = findViewById(R.id.txtDatos);
        ((TextView)findViewById(R.id.txtEmulador)).setText("Emulador > ⋮ (Extended Controls) > Location > Single points. Busca coordenadas o selecciona un punto y presiona Set location. Autoriza Ubicación precisa cuando Android lo solicite.");
        ubicacion = (LocationManager)getSystemService(LOCATION_SERVICE);
        if (!tienePermiso()) requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
    }
    /** Los permisos de localización se solicitan en tiempo de ejecución. */
    private boolean tienePermiso() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @Override protected void onResume() { super.onResume(); iniciar(); }
    @SuppressWarnings("MissingPermission")
    private void iniciar() {
        if (!tienePermiso()) { datos.setText("Concede permiso de ubicación para continuar."); return; }
        try {
            // Elegir GPS cuando esté disponible; usar red como respaldo.
            String proveedor = ubicacion.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
            if (!ubicacion.isProviderEnabled(proveedor)) {
                datos.setText("Activa la ubicación del teléfono y vuelve a intentar."); return;
            }
            ubicacion.requestLocationUpdates(proveedor, 1500L, 0f, this);
            Location ultima = ubicacion.getLastKnownLocation(proveedor);
            if (ultima != null) onLocationChanged(ultima);
            else datos.setText("Buscando ubicación… puede tardar al estar en interiores.");
        } catch (Exception e) { datos.setText("No fue posible obtener la ubicación: " + e.getMessage()); }
    }
    @Override public void onLocationChanged(Location l) {
        datos.setText(String.format(Locale.getDefault(),
                "Latitud: %.6f\nLongitud: %.6f\nPrecisión: %.1f m",
                l.getLatitude(), l.getLongitude(), l.getAccuracy()));
    }
    @Override protected void onPause() { super.onPause(); ubicacion.removeUpdates(this); }
    @Override public void onRequestPermissionsResult(int req, String[] p, int[] r) {
        super.onRequestPermissionsResult(req,p,r);
        if (req == 200) iniciar();
    }
}
