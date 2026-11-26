package com.example.blackjack21.data;

import java.util.Map;

/**
 * Estadísticas persistentes. La copia de respaldo se escribe siempre en el
 * mismo lote que el respaldo de la partida (ver {@link GameSession}), de modo
 * que ambas describen el mismo instante.
 */
public final class StatsRepository {

    static final String KEY_STATS = "player_stats";
    static final String KEY_BACKUP = "player_stats_prev";
    static final String KEY_CORRUPT = "player_stats_corrupt";

    /** Estadísticas leídas junto con su texto (null si se crearon vacías). */
    static final class Loaded {
        final PlayerStatistics stats;
        final String text;

        Loaded(PlayerStatistics stats, String text) {
            this.stats = stats;
            this.text = text;
        }
    }

    private final KeyValueStore store;

    public StatsRepository(KeyValueStore store) {
        this.store = store;
    }

    /** Para la pantalla de estadísticas: principal, respaldo o vacías. */
    public PlayerStatistics load() {
        Loaded l = read(KEY_STATS);
        if (l == null) {
            l = read(KEY_BACKUP);
        }
        return l != null ? l.stats : new PlayerStatistics();
    }

    /**
     * Estadísticas a juego con la partida restaurada: si la partida vino del
     * respaldo se usan las estadísticas del respaldo (mismo instante).
     */
    Loaded loadFor(boolean backupGame) {
        String first = backupGame ? KEY_BACKUP : KEY_STATS;
        String second = backupGame ? KEY_STATS : KEY_BACKUP;
        Loaded l = read(first);
        if (l == null) {
            l = read(second);
        }
        return l != null ? l : new Loaded(new PlayerStatistics(), null);
    }

    /** Copia principal ilegible: se aparta antes de sobrescribirla. */
    void quarantineIfCorrupt(Map<String, String> batch) {
        String raw = store.get(KEY_STATS);
        if (raw != null && read(KEY_STATS) == null) {
            batch.put(KEY_CORRUPT, raw);
        }
    }

    void addTo(Map<String, String> batch, String text, String previous, boolean replaceBackup) {
        batch.put(KEY_STATS, text);
        if (replaceBackup) {
            batch.put(KEY_BACKUP, previous);
        }
    }

    private Loaded read(String key) {
        String raw = store.get(key);
        if (raw == null) {
            return null;
        }
        try {
            return new Loaded(GameStateSerializer.readStats(raw), raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
