package com.example.sensoresandroid;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
/**
 * Lógica compartida para los sensores de movimiento y campo magnético.
 * Cada subclase es una Activity independiente, como solicita la actividad.
 */
public abstract class BaseSensorActivity extends AppCompatActivity implements SensorEventListener {
    protected TextView datos;
    private SensorManager administrador;
    private Sensor sensor;
    /** Subclases definen qué tipo de sensor deben leer. */
    protected abstract int tipoSensor();
    protected abstract String titulo();
    protected abstract String descripcion();
    /** Instrucciones específicas para los controles del emulador. */
    protected String instruccionesEmulador() { return "Emulador > ⋮ (Extended Controls) > Virtual Sensors."; }
    /** Convierte los valores del sensor en texto entendible. */
    protected abstract String mostrar(float[] valores);
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_sensor);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.txtTitulo)).setText(titulo());
        ((TextView)findViewById(R.id.txtDescripcion)).setText(descripcion());
        datos = findViewById(R.id.txtDatos);
        ((TextView)findViewById(R.id.txtEmulador)).setText(instruccionesEmulador());
        // Android entrega el servicio centralizado de sensores.
        administrador = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = administrador.getDefaultSensor(tipoSensor());
        if (sensor == null) datos.setText("Sensor no disponible en este AVD. Comprueba el hardware del emulador o usa otro dispositivo virtual compatible.");
    }
    /** Puede sobrescribirse para controlar permisos antes de registrar el sensor. */
    protected boolean puedeEscuchar() { return true; }
    @Override protected void onResume() {
        super.onResume();
        // Registramos el listener solamente mientras la pantalla está visible.
        registrarSensor();
    }
    @Override protected void onPause() {
        super.onPause();
        // Detener las lecturas reduce consumo de batería.
        dejarDeEscuchar();
    }
    /** Registra una escucha sin llamar artificialmente a métodos del ciclo de vida. */
    protected void registrarSensor() {
        if (sensor != null && puedeEscuchar()) administrador.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    /** Suspende las lecturas cuando usamos el modo demostración o dejamos la pantalla. */
    protected void dejarDeEscuchar() { administrador.unregisterListener(this); }
    @Override public void onSensorChanged(SensorEvent evento) {
        // El AVD inyecta las lecturas virtuales en la API SensorManager,
        // igual que lo haría el hardware de un teléfono físico.
        if (evento.sensor.getType() == tipoSensor()) datos.setText(mostrar(evento.values));
    }
    @Override public void onAccuracyChanged(Sensor s, int exactitud) {
        // No se necesita reaccionar a cambios de precisión en este ejemplo.
    }
}
