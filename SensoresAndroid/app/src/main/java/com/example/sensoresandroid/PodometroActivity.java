package com.example.sensoresandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * PODÓMETRO DEL EMULADOR.
 * TYPE_STEP_COUNTER requiere un sensor de pasos que algunos AVD no exponen.
 * La app muestra datos verdaderos del sensor si existe y, opcionalmente,
 * dispone de un simulador MANUAL etiquetado para hacer la práctica en clase.
 */
public class PodometroActivity extends BaseSensorActivity {
    private float baseSensor = -1f; // Primera lectura de pasos acumulados.
    private int pasosManual = 0;    // Pasos de demostración (nunca son datos físicos).
    private boolean modoManual = false;
    private Button sumar, reiniciar, cambiarModo;

    @Override protected int tipoSensor() { return Sensor.TYPE_STEP_COUNTER; }
    @Override protected String titulo() { return "Podómetro"; }
    @Override protected String descripcion() {
        return "Intenta leer el contador de pasos del AVD. Si tu emulador no lo ofrece, activa el modo DEMOSTRACIÓN manual.";
    }
    @Override protected String instruccionesEmulador() {
        return "Muchos emuladores NO incluyen TYPE_STEP_COUNTER ni una opción de simular pasos en Extended Controls. "
            + "Aquí se distingue siempre entre lectura del sensor y simulación manual.";
    }
    @Override protected String mostrar(float[] v) {
        // Android entrega los pasos acumulados desde el último reinicio del dispositivo.
        if (baseSensor < 0) baseSensor = v[0];
        return "MODO SENSOR (datos del dispositivo)\nPasos en sesión: "
            + Math.max(0, (int)(v[0] - baseSensor)) + "\nTotal desde reinicio: " + (int)v[0];
    }
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        // Los tres botones están en el layout común, ocultos en otros sensores.
        sumar = findViewById(R.id.btnSumarPaso);
        reiniciar = findViewById(R.id.btnReiniciarPasos);
        cambiarModo = findViewById(R.id.btnModoPasos);
        cambiarModo.setVisibility(View.VISIBLE);
        cambiarModo.setOnClickListener(v -> {
            modoManual = !modoManual;
            sumar.setVisibility(modoManual ? View.VISIBLE : View.GONE);
            reiniciar.setVisibility(modoManual ? View.VISIBLE : View.GONE);
            cambiarModo.setText(modoManual ? "Volver a lectura del sensor" : "Activar DEMOSTRACIÓN manual");
            if (modoManual) {
                // Dejamos de escuchar el sensor para evitar mezclar orígenes.
                dejarDeEscuchar();
                mostrarManual();
            } else {
                datos.setText("Esperando pasos del sensor físico/virtual...");
                registrarSensor();
            }
        });
        sumar.setOnClickListener(v -> { pasosManual++; mostrarManual(); });
        reiniciar.setOnClickListener(v -> { pasosManual = 0; mostrarManual(); });
        // Android 10+ necesita autorización del usuario para registrar pasos reales.
        if (Build.VERSION.SDK_INT >= 29 && !permisoConcedido()) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        }
    }
    private void mostrarManual() {
        datos.setText("MODO DEMOSTRACIÓN MANUAL (NO es un sensor)\nPasos simulados: " + pasosManual);
    }
    private boolean permisoConcedido() {
        return Build.VERSION.SDK_INT < 29 || checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED;
    }
    @Override protected boolean puedeEscuchar() {
        if (modoManual) return false;
        if (!permisoConcedido()) datos.setText("Autoriza Actividad física para escuchar el podómetro.");
        return permisoConcedido();
    }
    @Override public void onRequestPermissionsResult(int req, String[] permisos, int[] resultados) {
        super.onRequestPermissionsResult(req, permisos, resultados);
        if (req == 100) {
            if (permisoConcedido()) registrarSensor();
            else datos.setText("Permiso denegado. Puedes utilizar el modo DEMOSTRACIÓN manual.");
        }
    }
}
