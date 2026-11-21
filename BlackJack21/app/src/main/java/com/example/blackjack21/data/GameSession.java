package com.example.blackjack21.data;

import com.example.blackjack21.game.GameController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Une una partida con su almacenamiento (Java puro). Recupera la partida
 * guardada o crea una nueva y, tras cada cambio, guarda partida y
 * estadísticas en una única escritura atómica.
 *
 * <p>Copias de respaldo: en cada escritura, la pareja (partida, estadísticas)
 * de la escritura anterior pasa a respaldo en el mismo lote. Así el respaldo
 * siempre describe un único instante coherente: si hay que recurrir a él, se
 * restauran juntas partida y estadísticas, sin resultados contados que la
 * partida restaurada aún no ha producido. Saldos, apuestas y pagos viven solo
 * en la partida (se reconstruyen repitiendo su diario), por lo que restaurar
 * una copia anterior nunca los duplica.</p>
 */
public final class GameSession {

    private final KeyValueStore store;
    private final GameRepository games;
    private final StatsRepository statsRepo;
    private PlayerStatistics stats;
    private GameController controller;
    private int knownGame;
    private boolean restored;
    /** Textos de la última escritura: serán el respaldo de la siguiente. */
    private String lastGameText;
    private String lastStatsText;

    public GameSession(KeyValueStore store) {
        this.store = store;
        this.games = new GameRepository(store);
        this.statsRepo = new StatsRepository(store);
    }

    /**
     * Recupera la partida guardada si es válida; si no, usa {@code fresh}. No
     * reinicia una partida válida ni reproduce animaciones.
     */
    public GameController open(Supplier<GameController> fresh, String humanName, String dealerName) {
        Map<String, String> batch = new LinkedHashMap<>();
        GameController c = null;
        GameRepository.Loaded used = null;
        for (GameRepository.Loaded candidate : games.loadCandidates()) {
            try {
                c = GameController.restore(candidate.data, humanName, dealerName);
                used = candidate;
                break;
            } catch (IllegalArgumentException ignored) {
                // Copia no reproducible: se prueba la siguiente.
            }
        }
        StatsRepository.Loaded loadedStats = statsRepo.loadFor(used != null && used.backup);
        stats = loadedStats.stats;
        statsRepo.quarantineIfCorrupt(batch);

        restored = used != null;
        if (restored) {
            if (used.backup && store.get(GameRepository.KEY_SAVE) != null) {
                batch.put(GameRepository.KEY_CORRUPT, store.get(GameRepository.KEY_SAVE));
            }
            lastGameText = used.text;
            lastStatsText = loadedStats.text;
        } else {
            if (games.hasSavedData()) {
                games.quarantine(batch);
            }
            lastGameText = null;
            lastStatsText = null;
            c = fresh.get();
            c.continueNumbering(stats.getGamesStarted());
        }
        attach(c, batch);
        return c;
    }

    /** Usa una partida ya creada (p. ej. de depuración); solo se guarda si es guardable. */
    public void attach(GameController c) {
        stats = statsRepo.load();
        attach(c, new LinkedHashMap<>());
    }

    private void attach(GameController c, Map<String, String> batch) {
        controller = c;
        knownGame = c.getGameNumber();
        stats.onGameObserved(knownGame, false);
        c.setChangeObserver(this::save);
        save(batch);
    }

    public void detach() {
        if (controller != null) {
            controller.setChangeObserver(null);
            controller = null;
        }
    }

    /** La partida se recuperó de datos guardados (no es nueva). */
    public boolean wasRestored() {
        return restored;
    }

    public PlayerStatistics getStatistics() {
        return stats;
    }

    /** Guarda el estado actual (llamado tras cada cambio de la partida). */
    public void save() {
        save(new LinkedHashMap<>());
    }

    private void save(Map<String, String> batch) {
        GameController c = controller;
        if (c == null) {
            return;
        }
        GameSaveData data = c.toSaveData();
        if (data == null) {
            return; // partida de prueba: no se persiste ni cuenta
        }
        int game = c.getGameNumber();
        if (game != knownGame) {
            stats.onGameObserved(game, game > knownGame);
            knownGame = game;
        }
        stats.recordRound(RoundSummary.of(c));
        stats.setCurrentBalance(c.getHuman().getBalance());

        String gameText = GameRepository.serialize(data);
        String statsText = GameStateSerializer.writeStats(stats);
        boolean changed = !gameText.equals(lastGameText) || !statsText.equals(lastStatsText);
        if (!changed && batch.isEmpty()) {
            return;
        }
        // La pareja anterior pasa a respaldo (o se borra si aún no hay pareja).
        games.addTo(batch, gameText, lastGameText, changed);
        statsRepo.addTo(batch, statsText, lastStatsText, changed);
        store.writeAll(batch);
        lastGameText = gameText;
        lastStatsText = statsText;
    }

    /** Lectura de estadísticas sin partida abierta (pantalla de estadísticas). */
    public static PlayerStatistics readStatistics(KeyValueStore store) {
        return new StatsRepository(store).load();
    }
}
