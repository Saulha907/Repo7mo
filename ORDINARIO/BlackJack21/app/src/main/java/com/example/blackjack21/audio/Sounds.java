package com.example.blackjack21.audio;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Punto de acceso Android al único {@link SoundManager} de la aplicación.
 * Guarda solo el contexto de la aplicación (sin fugas de Activity) y sigue el
 * ciclo de vida de todas las pantallas para pausar en segundo plano.
 */
public final class Sounds {

    private static final String PREFS = "audio_prefs";

    private static SoundManager instance;

    private Sounds() {
    }

    /** Debe llamarse desde {@code onCreate} de la primera pantalla (BaseImmersiveActivity). */
    public static synchronized SoundManager get(Context context) {
        if (instance == null) {
            Application app = (Application) context.getApplicationContext();
            AudioSettings settings = new AudioSettings(new PrefsStore(
                    app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)));
            instance = new SoundManager(settings, () -> new SoundPoolBackend(app),
                    SystemClock::uptimeMillis);
            app.registerActivityLifecycleCallbacks(new LifecycleCallbacks(new AudioLifecycle(instance)));
        }
        return instance;
    }

    /** Reproduce un efecto; atajo para las pantallas. */
    public static void play(Context context, SoundEffect effect) {
        get(context).play(effect);
    }

    /** Preferencias de audio persistidas con apply() (escritura asíncrona). */
    private static final class PrefsStore implements AudioSettings.Store {
        private final SharedPreferences prefs;

        PrefsStore(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return prefs.getBoolean(key, defValue);
        }

        @Override
        public int getInt(String key, int defValue) {
            return prefs.getInt(key, defValue);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            prefs.edit().putBoolean(key, value).apply();
        }

        @Override
        public void putInt(String key, int value) {
            prefs.edit().putInt(key, value).apply();
        }
    }

    private static final class LifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        private final AudioLifecycle lifecycle;

        LifecycleCallbacks(AudioLifecycle lifecycle) {
            this.lifecycle = lifecycle;
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            lifecycle.onScreenStarted();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            lifecycle.onScreenStopped(activity.isChangingConfigurations());
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle state) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle state) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    }
}
