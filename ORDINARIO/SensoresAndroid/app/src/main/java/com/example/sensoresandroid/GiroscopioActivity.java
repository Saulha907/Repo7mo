package com.example.sensoresandroid;
import android.hardware.Sensor;
import java.util.Locale;
/** Activity independiente para giroscopio. */
public class GiroscopioActivity extends BaseSensorActivity {
    @Override protected int tipoSensor() { return Sensor.TYPE_GYROSCOPE; }
    @Override protected String titulo() { return "Giroscopio"; }
    @Override protected String descripcion() { return "Gira el dispositivo. Se mide la velocidad angular en radianes por segundo."; }
    // Explica al alumno dónde modificar los sensores virtuales.
    @Override protected String instruccionesEmulador() { return "Emulador > ⋮ > Virtual Sensors > Device Pose. Gira el modelo 3D de forma continua. El giroscopio mide velocidad, por lo que vuelve cerca de cero al detenerse."; }
    @Override protected String mostrar(float[] v) {
        // Los tres primeros valores representan los ejes físicos X, Y, Z.
        return String.format(Locale.getDefault(), "X: %.2f rad/s\nY: %.2f rad/s\nZ: %.2f rad/s", v[0], v[1], v[2]);
    }
}
