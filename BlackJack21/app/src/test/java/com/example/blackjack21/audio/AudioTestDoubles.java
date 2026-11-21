package com.example.blackjack21.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dobles de prueba compartidos por las pruebas de audio. */
final class AudioTestDoubles {

    private AudioTestDoubles() {
    }

    /** Almacén en memoria que imita SharedPreferences. */
    static final class MemoryStore implements AudioSettings.Store {
        final Map<String, Object> values = new HashMap<>();
        int writes;

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object v = values.get(key);
            return v instanceof Boolean ? (Boolean) v : defValue;
        }

        @Override
        public int getInt(String key, int defValue) {
            Object v = values.get(key);
            return v instanceof Integer ? (Integer) v : defValue;
        }

        @Override
        public void putBoolean(String key, boolean value) {
            values.put(key, value);
            writes++;
        }

        @Override
        public void putInt(String key, int value) {
            values.put(key, value);
            writes++;
        }
    }

    /** Backend que registra reproducciones; puede simular efectos no cargados. */
    static final class FakeBackend implements SoundManager.Backend {
        final List<SoundEffect> played = new ArrayList<>();
        final List<Float> volumes = new ArrayList<>();
        boolean available = true;
        boolean throwOnPlay;
        int stops;
        boolean released;

        @Override
        public boolean play(SoundEffect effect, float volume) {
            if (throwOnPlay) {
                throw new IllegalStateException("fallo simulado");
            }
            if (!available || released) {
                return false;
            }
            played.add(effect);
            volumes.add(volume);
            return true;
        }

        @Override
        public void stopAll() {
            stops++;
        }

        @Override
        public void release() {
            released = true;
        }
    }

    static final class ManualClock implements SoundManager.Clock {
        long now = 1_000L;

        @Override
        public long nowMs() {
            return now;
        }
    }

    /** Fábrica que recuerda los backends creados. */
    static final class Factory implements SoundManager.BackendFactory {
        final List<FakeBackend> created = new ArrayList<>();

        @Override
        public SoundManager.Backend create() {
            FakeBackend b = new FakeBackend();
            created.add(b);
            return b;
        }

        FakeBackend last() {
            return created.get(created.size() - 1);
        }
    }
}
