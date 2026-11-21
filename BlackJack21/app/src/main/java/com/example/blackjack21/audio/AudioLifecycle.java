package com.example.blackjack21.audio;

/**
 * Cuenta las pantallas visibles de la app: al quedar ninguna pausa el audio
 * y al volver a haber alguna lo reanuda. Los cambios de configuración no
 * cuentan como paso a segundo plano. Java puro.
 */
public final class AudioLifecycle {

    private final SoundManager manager;
    private int started;

    public AudioLifecycle(SoundManager manager) {
        this.manager = manager;
    }

    public void onScreenStarted() {
        if (started++ == 0) {
            manager.resume();
            manager.preload();
        }
    }

    public void onScreenStopped(boolean changingConfigurations) {
        if (started > 0) {
            started--;
        }
        if (started == 0 && !changingConfigurations) {
            manager.pause();
        }
    }

    public int getStartedScreens() {
        return started;
    }
}
