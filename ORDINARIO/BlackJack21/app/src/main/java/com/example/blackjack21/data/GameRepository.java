package com.example.blackjack21.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Partida guardada: copia actual más la de la escritura anterior como respaldo.
 * Los datos ilegibles se apartan (no se pierden). La rotación de copias la
 * coordina {@link GameSession} para que partida y estadísticas de respaldo
 * formen siempre una pareja coherente.
 */
public final class GameRepository {

    static final String KEY_SAVE = "game_save";
    static final String KEY_BACKUP = "game_save_prev";
    static final String KEY_CORRUPT = "game_save_corrupt";

    /** Partida legible junto con su texto y su procedencia. */
    public static final class Loaded {
        public final GameSaveData data;
        final String text;
        /** Viene de la copia de respaldo (la principal no sirvió). */
        public final boolean backup;

        Loaded(GameSaveData data, String text, boolean backup) {
            this.data = data;
            this.text = text;
            this.backup = backup;
        }
    }

    private final KeyValueStore store;

    public GameRepository(KeyValueStore store) {
        this.store = store;
    }

    /** Partidas guardadas legibles: primero la principal, después el respaldo. */
    public List<Loaded> loadCandidates() {
        List<Loaded> out = new ArrayList<>();
        String[] keys = {KEY_SAVE, KEY_BACKUP};
        for (int i = 0; i < keys.length; i++) {
            String raw = store.get(keys[i]);
            if (raw == null) {
                continue;
            }
            try {
                out.add(new Loaded(GameStateSerializer.readGame(raw), raw, i == 1));
            } catch (IllegalArgumentException ignored) {
                // Ilegible: se prueba la siguiente copia.
            }
        }
        return out;
    }

    public boolean hasSavedData() {
        return store.get(KEY_SAVE) != null || store.get(KEY_BACKUP) != null;
    }

    /** Ninguna copia sirvió: se conservan los datos originales aparte y se descartan. */
    public void quarantine(Map<String, String> batch) {
        String raw = store.get(KEY_SAVE);
        if (raw == null) {
            raw = store.get(KEY_BACKUP);
        }
        if (raw != null) {
            batch.put(KEY_CORRUPT, raw);
        }
        batch.put(KEY_SAVE, null);
        batch.put(KEY_BACKUP, null);
    }

    static String serialize(GameSaveData data) {
        return GameStateSerializer.writeGame(data);
    }

    /** Añade al lote la partida actual y, si se indica, la anterior como respaldo. */
    void addTo(Map<String, String> batch, String text, String previous, boolean replaceBackup) {
        batch.put(KEY_SAVE, text);
        if (replaceBackup) {
            batch.put(KEY_BACKUP, previous);
        }
    }
}
