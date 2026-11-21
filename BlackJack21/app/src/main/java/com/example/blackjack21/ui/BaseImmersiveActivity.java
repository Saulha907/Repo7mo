package com.example.blackjack21.ui;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.audio.Sounds;

/**
 * Base de todas las pantallas: pantalla completa inmersiva y zona segura.
 * El layout puede declarar una vista con id {@code safe_area}; recibirá como
 * relleno adicional los recortes de pantalla y las barras visibles.
 */
public abstract class BaseImmersiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Crea (una sola vez) el gestor de audio antes del primer onStart.
        Sounds.get(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View safeArea = findViewById(R.id.safe_area);
        if (safeArea != null) {
            applySafeArea(safeArea);
        }
        hideSystemBars(getWindow());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars(getWindow());
        }
    }

    private static void applySafeArea(View view) {
        final int baseLeft = view.getPaddingLeft();
        final int baseTop = view.getPaddingTop();
        final int baseRight = view.getPaddingRight();
        final int baseBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(baseLeft + safe.left, baseTop + safe.top,
                    baseRight + safe.right, baseBottom + safe.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /** Oculta barras del sistema; reaparecen temporalmente al deslizar. */
    public static void hideSystemBars(@Nullable Window window) {
        if (window == null) {
            return;
        }
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }
}
