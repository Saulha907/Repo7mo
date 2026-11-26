package com.example.blackjack21.audio;

/**
 * Preferencias de audio (activación y volumen 0..100 %) sobre un almacén
 * clave-valor. Java puro: en Android el almacén es SharedPreferences.
 */
public final class AudioSettings {

    /** Almacén persistente mínimo. */
    public interface Store {
        boolean getBoolean(String key, boolean defValue);

        int getInt(String key, int defValue);

        void putBoolean(String key, boolean value);

        void putInt(String key, int value);
    }

    static final String KEY_ENABLED = "sound_enabled";
    static final String KEY_VOLUME = "sound_volume";

    public static final boolean DEFAULT_ENABLED = true;
    public static final int DEFAULT_VOLUME = 70;
    public static final int MAX_VOLUME = 100;

    private final Store store;
    private boolean enabled;
    private int volume;

    public AudioSettings(Store store) {
        this.store = store;
        enabled = store.getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
        volume = clamp(store.getInt(KEY_VOLUME, DEFAULT_VOLUME));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            store.putBoolean(KEY_ENABLED, enabled);
        }
    }

    /** Volumen general en porcentaje (0..100). */
    public int getVolume() {
        return volume;
    }

    public void setVolume(int percent) {
        int v = clamp(percent);
        if (volume != v) {
            volume = v;
            store.putInt(KEY_VOLUME, v);
        }
    }

    /** Hay que reproducir algo: activado y con volumen mayor que cero. */
    public boolean isAudible() {
        return enabled && volume > 0;
    }

    static int clamp(int percent) {
        return Math.max(0, Math.min(MAX_VOLUME, percent));
    }
}
