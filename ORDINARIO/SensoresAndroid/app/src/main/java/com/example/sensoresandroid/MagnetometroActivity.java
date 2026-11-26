package com.example.sensoresandroid;
import android.hardware.Sensor;
import java.util.Locale;
/** Activity independiente para magnetómetro. */
public class MagnetometroActivity extends BaseSensorActivity {
    @Override protected int tipoSensor() { return Sensor.TYPE_MAGNETIC_FIELD; }
    @Override protected String titulo() { return "Magnetómetro"; }
    @Override protected String descripcion() { return "Detecta el campo magnético ambiental en microteslas (µT)."; }
    // Explica al alumno dónde modificar los sensores virtuales.
    @Override protected String instruccionesEmulador() { return "Emulador > ⋮ > Virtual Sensors > Additional Sensors. Cambia Magnetic field X/Y/Z si tu versión del AVD ofrece estos controles."; }
    @Override protected String mostrar(float[] v) {
        // Los tres primeros valores representan los ejes físicos X, Y, Z.
        return String.format(Locale.getDefault(), "X: %.2f µT\nY: %.2f µT\nZ: %.2f µT", v[0], v[1], v[2]);
    }
}
