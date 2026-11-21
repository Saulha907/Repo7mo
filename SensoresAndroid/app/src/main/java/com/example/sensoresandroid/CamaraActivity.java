package com.example.sensoresandroid;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
/** Abre la app de cámara instalada y enseña la miniatura capturada. */
public class CamaraActivity extends AppCompatActivity {
    private ImageView imagen;
    private TextView estado;
    // El launcher recibe el resultado de la cámara usando la API moderna de Activity Result.
    private final ActivityResultLauncher<Intent> camara = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Bundle extras = resultado.getData().getExtras();
                    Object foto = extras == null ? null : extras.get("data");
                    if (foto instanceof Bitmap) {
                        imagen.setImageBitmap((Bitmap)foto);
                        estado.setText("Foto capturada correctamente (vista previa).");
                    } else estado.setText("La cámara no devolvió una miniatura.");
                } else estado.setText("Captura cancelada.");
            });
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_camera);
        imagen = findViewById(R.id.imagenFoto);
        estado = findViewById(R.id.txtEstado);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> {
            // ACTION_IMAGE_CAPTURE solicita otra aplicación; no requiere CAMERA en esta app.
            Intent accion = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (accion.resolveActivity(getPackageManager()) != null) camara.launch(accion);
            else estado.setText("No hay una aplicación de cámara disponible.");
        });
    }
}
