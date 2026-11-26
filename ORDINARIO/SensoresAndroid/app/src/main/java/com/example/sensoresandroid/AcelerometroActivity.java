package com.example.sensoresandroid;
import android.hardware.Sensor;
import java.util.Locale;
/** Activity independiente para acelerómetro. */
public class AcelerometroActivity extends BaseSensorActivity {
    @Override protected int tipoSensor() { return Sensor.TYPE_ACCELEROMETER; }
    @Override protected String titulo() { return "Acelerómetro"; }
    @Override protected String descripcion() { return "Mueve o inclina el dispositivo. Los valores incluyen gravedad y se miden en m/s²."; }
    // Explica al alumno dónde modificar los sensores virtuales.
    @Override protected String instruccionesEmulador() { return "Emulador > ⋮ > Virtual Sensors > Device Pose. Inclina el modelo 3D; observa X, Y y Z. Los cambios incluyen gravedad."; }
    @Override protected String mostrar(float[] v) {
        // Los tres primeros valores representan los ejes físicos X, Y, Z.
        return String.format(Locale.getDefault(), "X: %.2f m/s²\nY: %.2f m/s²\nZ: %.2f m/s²", v[0], v[1], v[2]);
    }
}
