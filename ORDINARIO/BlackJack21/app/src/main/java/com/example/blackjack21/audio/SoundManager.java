package com.example.blackjack21.audio;

import java.util.Arrays;

/**
 * Gestor de efectos (Java puro). Aplica la configuración, evita repeticiones
 * inmediatas del mismo efecto y gestiona la pausa en segundo plano. La
 * reproducción real la hace un {@link Backend} (SoundPool en Android), que se
 * crea a demanda y se libera al pasar la app a segundo plano.
 */
public final class SoundManager {

    /** Reproductor real de efectos ya cargados. */
    public interface Backend {
        /** Reproduce si el efecto está cargado; nunca lanza excepciones. */
        boolean play(SoundEffect effect, float volume);

        /** Detiene los sonidos en curso. */
        void stopAll();

        void release();
    }

    public interface BackendFactory {
        Backend create();
    }

    public interface Clock {
        long nowMs();
    }

    /** Intervalo mínimo entre dos reproducciones del mismo efecto. */
    public static final long MIN_REPEAT_MS = 45L;
    private static final long NEVER = Long.MIN_VALUE;

    private final AudioSettings settings;
    private final BackendFactory factory;
    private final Clock clock;
    private final long[] lastPlayed = new long[SoundEffect.values().length];

    private Backend backend;
    private boolean paused;

    public SoundManager(AudioSettings settings, BackendFactory factory, Clock clock) {
        this.settings = settings;
        this.factory = factory;
        this.clock = clock;
        Arrays.fill(lastPlayed, NEVER);
    }

    // --- Reproducción ----------------------------------------------------------------

    /** Reproduce un efecto si el audio está activo; devuelve si se emitió. */
    public synchronized boolean play(SoundEffect effect) {
        if (effect == null || paused || !settings.isAudible()) {
            return false;
        }
        long now = clock.nowMs();
        long last = lastPlayed[effect.ordinal()];
        if (last != NEVER && now - last < MIN_REPEAT_MS) {
            return false; // duplicado inmediato
        }
        Backend b = ensureBackend();
        if (b == null) {
            return false;
        }
        boolean played;
        try {
            played = b.play(effect, volumeFor(effect));
        } catch (RuntimeException e) {
            played = false;
        }
        if (played) {
            lastPlayed[effect.ordinal()] = now;
        }
        return played;
    }

    /** Volumen final (0..1) de un efecto: curva cuadrática, más natural al oído. */
    public synchronized float volumeFor(SoundEffect effect) {
        if (!settings.isEnabled()) {
            return 0f;
        }
        float master = settings.getVolume() / (float) AudioSettings.MAX_VOLUME;
        return master * master * effect.gain;
    }

    /** Carga los efectos por adelantado para no perder los primeros. */
    public synchronized void preload() {
        if (!paused) {
            ensureBackend();
        }
    }

    // --- Configuración ---------------------------------------------------------------

    public synchronized boolean isEnabled() {
        return settings.isEnabled();
    }

    public synchronized void setEnabled(boolean enabled) {
        settings.setEnabled(enabled);
        if (!enabled) {
            stopActive();
        }
    }

    public synchronized int getVolume() {
        return settings.getVolume();
    }

    public synchronized void setVolume(int percent) {
        settings.setVolume(percent);
        if (settings.getVolume() == 0) {
            stopActive();
        }
    }

    // --- Ciclo de vida ---------------------------------------------------------------

    /** Segundo plano: corta lo que suena, descarta lo pendiente y libera recursos. */
    public synchronized void pause() {
        paused = true;
        Arrays.fill(lastPlayed, NEVER);
        releaseBackend();
    }

    /** Primer plano: vuelve a aceptar efectos nuevos (nunca los anteriores). */
    public synchronized void resume() {
        paused = false;
        Arrays.fill(lastPlayed, NEVER);
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    /** Libera los recursos; se recrean a demanda en el siguiente uso. */
    public synchronized void release() {
        releaseBackend();
    }

    synchronized boolean hasBackend() {
        return backend != null;
    }

    private Backend ensureBackend() {
        if (backend == null) {
            try {
                backend = factory.create();
            } catch (RuntimeException e) {
                backend = null;
            }
        }
        return backend;
    }

    private void stopActive() {
        if (backend != null) {
            try {
                backend.stopAll();
            } catch (RuntimeException ignored) {
                // Sin sonido activo que detener.
            }
        }
    }

    private void releaseBackend() {
        if (backend != null) {
            stopActive();
            try {
                backend.release();
            } catch (RuntimeException ignored) {
                // Ya liberado.
            }
            backend = null;
        }
    }
}
