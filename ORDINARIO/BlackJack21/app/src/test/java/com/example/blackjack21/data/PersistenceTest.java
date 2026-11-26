package com.example.blackjack21.data;

import static com.example.blackjack21.model.Rank.ACE;
import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FIVE;
import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static com.example.blackjack21.model.Rank.THREE;
import static com.example.blackjack21.model.Rank.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.audio.AudioSettings;
import com.example.blackjack21.bot.BotManager;
import com.example.blackjack21.bot.BotPlayer;
import com.example.blackjack21.engine.BetManager;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.Outcome;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.game.GameController;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.RandomCodec;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Shoe;
import com.example.blackjack21.model.ShoeState;
import com.example.blackjack21.model.Suit;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/** Guardado, recuperación, estadísticas y robustez ante datos dañados. */
public class PersistenceTest {

    private static final String YOU = "Tú";
    private static final String DEALER = "CRUPIER";

    /** Almacén en memoria con escrituras por lotes, como SharedPreferences. */
    static final class MemoryStore implements KeyValueStore {
        final Map<String, String> data;
        int batches;

        MemoryStore() {
            this(new HashMap<>());
        }

        MemoryStore(Map<String, String> backing) {
            data = backing;
        }

        @Override
        public String get(String key) {
            return data.get(key);
        }

        @Override
        public void writeAll(Map<String, String> values) {
            batches++;
            for (Map.Entry<String, String> e : values.entrySet()) {
                if (e.getValue() == null) {
                    data.remove(e.getKey());
                } else {
                    data.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    private final MemoryStore store = new MemoryStore();

    /** Zapato con cartas en orden de extracción (más relleno para no agotarse). */
    private static Shoe stacked(Rank... ranks) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < ranks.length; i++) {
            cards.add(Card.of(ranks[i], Suit.values()[i % Suit.values().length]));
        }
        for (int i = 0; i < 40; i++) {
            cards.add(Card.of(Rank.values()[i % Rank.values().length], Suit.values()[i % 4]));
        }
        return Shoe.restore(new ShoeState(6, 0, cards, RandomCodec.encode(new Random(3))));
    }

    private static Supplier<GameController> solo(int balance, Rank... ranks) {
        return () -> new GameController(stacked(ranks), new Random(1), YOU, DEALER,
                Collections.<String>emptyList(), 0, balance);
    }

    private GameController open(Supplier<GameController> fresh) {
        return new GameSession(store).open(fresh, YOU, DEALER);
    }

    /** Reabre la app: nueva sesión sobre el mismo almacén; la fábrica no debe usarse. */
    private GameController reopen() {
        return new GameSession(store).open(() -> {
            throw new AssertionError("Se reinició una partida válida");
        }, YOU, DEALER);
    }

    private static void bet(GameController c, int amount) {
        assertTrue(c.addChip(amount));
        assertTrue(c.deal());
    }

    private static String describe(GameController c) {
        StringBuilder b = new StringBuilder(c.getPhase().name());
        for (PlayerState p : c.getEngine().getState().getPlayers()) {
            b.append(';').append(p.getId()).append('=').append(p.getBalance());
            for (HandState h : p.getHands()) {
                b.append(h.getCards()).append('/').append(h.getBet()).append('/').append(h.getOutcome());
            }
        }
        b.append(";D=").append(c.getEngine().getDealerVisibleCards());
        return b.toString();
    }

    private static int humanPayouts(GameController c) {
        int n = 0;
        for (BetManager.Entry e : c.getEngine().getBetManager().getLedger()) {
            if (e.playerId.equals(GameController.HUMAN_ID) && e.type == BetManager.EntryType.PAYOUT) {
                n++;
            }
        }
        return n;
    }

    // 1 y 2. Saldo ---------------------------------------------------------------------

    @Test
    public void balanceIsSavedAndRecovered() {
        // Humano 10+9 = 19 contra 10+7 = 17: gana 50.
        GameController c = open(solo(1000, TEN, TEN, NINE, SEVEN));
        bet(c, 50);
        assertTrue(c.stand());
        assertEquals(1050, c.getHuman().getBalance());

        GameController again = reopen();
        assertEquals(1050, again.getHuman().getBalance());
        assertEquals(GamePhase.ROUND_FINISHED, again.getPhase());
    }

    // 3 y 4. Estadísticas ----------------------------------------------------------------

    @Test
    public void statisticsAreSavedAndRecovered() {
        GameController c = open(solo(1000, ACE, NINE, TEN, SEVEN, TEN, TEN, TWO, SEVEN));
        bet(c, 100);                         // blackjack natural: +150
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
        assertTrue(c.startNextRound());
        assertTrue(c.deal());                // repite 100: 10+2 = 12 contra 10+7
        assertTrue(c.stand());               // pierde 100

        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals(2, s.getRoundsPlayed());
        assertEquals(1, s.getRoundsWon());
        assertEquals(1, s.getRoundsLost());
        assertEquals(1, s.getBlackjacks());
        assertEquals(150, s.getChipsWon());
        assertEquals(100, s.getChipsLost());
        assertEquals(150, s.getBiggestWin());
        assertEquals(1050, s.getCurrentBalance());
        assertEquals(50, s.getWinRate());
    }

    // 5. Reabrir una partida -----------------------------------------------------------

    @Test
    public void reopeningKeepsGameAndPendingBet() {
        GameController c = open(solo(1000, TEN, TEN, NINE, SEVEN));
        assertTrue(c.addChip(25));
        assertTrue(c.addChip(5));

        GameController again = reopen();
        assertEquals(30, again.getPendingBet());
        assertEquals(1000, again.getHuman().getBalance());
        assertEquals(c.getGameNumber(), again.getGameNumber());
        assertTrue(again.deal());
        assertEquals(970, again.getHuman().getBalance());
    }

    // 6. Ronda interrumpida -------------------------------------------------------------

    @Test
    public void interruptedRoundContinuesWithSameCards() {
        Rank[] deck = {TEN, TEN, SIX, SEVEN, FIVE, NINE};
        GameController control = solo(1000, deck).get();
        bet(control, 50);
        assertTrue(control.hit());           // 16 + 5 = 21: se cierra la mano

        GameController c = open(solo(1000, deck));
        bet(c, 50);                          // la app se cierra en el turno del jugador
        assertTrue(c.isHumanTurn());

        GameController again = reopen();
        assertTrue("Las acciones legales siguen disponibles", again.canHit());
        assertEquals(950, again.getHuman().getBalance());   // apuesta cobrada una sola vez
        assertEquals(describe(c), describe(again));
        assertTrue(again.hit());             // misma carta que en la partida de control
        assertEquals(describe(control), describe(again));
    }

    // 7. Manos divididas -----------------------------------------------------------------

    @Test
    public void splitHandsAreRestored() {
        // 8-8 contra 10 (oculta 7); cada mano recibe 3 y 2 al jugarse.
        Rank[] deck = {EIGHT, TEN, EIGHT, SEVEN, THREE, TWO, TEN, TEN};
        GameController c = open(solo(1000, deck));
        bet(c, 100);
        assertTrue(c.split());
        assertEquals(2, c.getHumanHands().size());

        GameController again = reopen();
        List<HandState> hands = again.getHumanHands();
        assertEquals(2, hands.size());
        assertEquals(c.getHumanHands().get(0).getCards(), hands.get(0).getCards());
        assertEquals(100, hands.get(1).getBet());
        assertEquals(800, again.getHuman().getBalance());
        assertEquals(describe(c), describe(again));

        // Se termina la ronda en ambas y el resultado coincide.
        while (c.isHumanTurn()) {
            assertTrue(c.stand());
            assertTrue(again.stand());
        }
        assertEquals(describe(c), describe(again));
        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals("Dividir no añade rondas", 1, s.getRoundsPlayed());
        assertEquals(2, s.getHandsWon() + s.getHandsLost() + s.getHandsPushed());
    }

    // 8 y 9. Sin pagos ni estadísticas duplicadas ---------------------------------------------

    @Test
    public void reopeningSettledRoundNeverPaysOrCountsTwice() {
        GameController c = open(solo(1000, TEN, TEN, NINE, SEVEN));
        bet(c, 50);
        c.stand();
        for (int i = 0; i < 3; i++) {
            GameController again = reopen();
            assertEquals(1050, again.getHuman().getBalance());
            assertEquals(1, humanPayouts(again));
        }
        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals(1, s.getRoundsPlayed());
        assertEquals(50, s.getChipsWon());
    }

    @Test
    public void sameRoundIsRecordedOnce() {
        PlayerStatistics s = new PlayerStatistics();
        RoundSummary r = new RoundSummary(1, 3, 40, Collections.singletonList(Outcome.WIN));
        assertTrue(s.recordRound(r));
        assertFalse(s.recordRound(r));
        assertFalse("Una ronda anterior tampoco cuenta",
                s.recordRound(new RoundSummary(1, 2, 10, Collections.singletonList(Outcome.WIN))));
        assertTrue(s.recordRound(new RoundSummary(2, 1, -10, Collections.singletonList(Outcome.LOSE))));
        assertEquals(2, s.getRoundsPlayed());
    }

    // 10 y 11. Reinicio tras Game Over ---------------------------------------------------

    @Test
    public void restartAfterGameOverKeepsHistory() {
        // 10 fichas cada uno: humano 10+6 = 16, Alex 20, crupier 10+9 = 19 → humano sin fichas.
        GameController c = open(() -> new GameController(stacked(TEN, TEN, TEN, SIX, TEN, NINE),
                new Random(1), YOU, DEALER, Arrays.asList("Alex"), 0, 10));
        bet(c, 10);
        c.stand();
        while (c.stepAutomatic()) {
            // turnos de bots, si quedan
        }
        assertTrue(c.isGameOver());
        int firstGame = c.getGameNumber();

        assertTrue(c.restartGame());
        assertEquals(1000, c.getHuman().getBalance());
        assertEquals(1000, c.getBotManager().getBots().get(0).getBalance());
        assertEquals(firstGame + 1, c.getGameNumber());

        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals(1, s.getRestarts());
        assertEquals(1, s.getRoundsPlayed());
        assertEquals(1, s.getRoundsLost());

        GameController again = reopen();
        assertEquals(firstGame + 1, again.getGameNumber());
        assertEquals(1000, again.getHuman().getBalance());
        assertEquals(1, GameSession.readStatistics(store).getRestarts());
    }

    // 12. Datos dañados -----------------------------------------------------------------

    @Test
    public void corruptGameStartsFreshWithoutLosingRawData() {
        store.data.put(GameRepository.KEY_SAVE, "basura{");
        GameController c = open(solo(1000, TEN, TEN, NINE, SEVEN));
        assertNotNull(c);
        assertEquals(1000, c.getHuman().getBalance());
        assertEquals("basura{", store.data.get(GameRepository.KEY_CORRUPT));
    }

    @Test
    public void corruptPrimaryFallsBackToBackup() {
        GameController c = open(solo(1000, TEN, TEN, NINE, SEVEN));
        bet(c, 50);
        c.stand();
        String good = store.data.get(GameRepository.KEY_SAVE);
        // Se altera un carácter: la suma de control lo detecta.
        store.data.put(GameRepository.KEY_SAVE, good.replace("\"pending\"", "\"pendinG\""));
        GameController again = reopen();
        assertTrue(again.getHuman().getBalance() >= 950);
        assertNotNull(GameSession.readStatistics(store));
    }

    @Test
    public void corruptStatsUseBackupOrDefaults() {
        GameController c = open(solo(1000, TEN, TEN, NINE, SEVEN));
        bet(c, 50);
        c.stand();
        store.data.put(StatsRepository.KEY_STATS, "0|{}");
        PlayerStatistics s = GameSession.readStatistics(store);
        assertTrue(s.getRoundsPlayed() >= 0);
        store.data.put(StatsRepository.KEY_BACKUP, "x");
        assertEquals(0, GameSession.readStatistics(store).getRoundsPlayed());
    }

    @Test
    public void serializerRejectsInvalidData() {
        String[] bad = {null, "", "123", "zz|{}", "0|[1,2]"};
        for (String text : bad) {
            try {
                GameStateSerializer.readGame(text);
                throw new AssertionError("Aceptó datos inválidos: " + text);
            } catch (IllegalArgumentException expected) {
                // correcto
            }
        }
    }

    // 13. Preferencias de audio -------------------------------------------------------

    @Test
    public void audioPreferencesAreUntouched() {
        Map<String, String> shared = new HashMap<>();
        Map<String, Object> audioValues = new HashMap<>();
        AudioSettings.Store audioStore = new AudioSettings.Store() {
            @Override
            public boolean getBoolean(String key, boolean def) {
                Object v = audioValues.get(key);
                return v instanceof Boolean ? (Boolean) v : def;
            }

            @Override
            public int getInt(String key, int def) {
                Object v = audioValues.get(key);
                return v instanceof Integer ? (Integer) v : def;
            }

            @Override
            public void putBoolean(String key, boolean value) {
                audioValues.put(key, value);
                shared.put(key, String.valueOf(value));
            }

            @Override
            public void putInt(String key, int value) {
                audioValues.put(key, value);
                shared.put(key, String.valueOf(value));
            }
        };
        AudioSettings audio = new AudioSettings(audioStore);
        audio.setEnabled(false);
        audio.setVolume(35);
        Map<String, String> before = new HashMap<>(shared);

        MemoryStore gameStore = new MemoryStore(shared);
        GameController c = new GameSession(gameStore).open(solo(1000, TEN, TEN, NINE, SEVEN), YOU, DEALER);
        bet(c, 50);
        c.stand();

        for (Map.Entry<String, String> e : before.entrySet()) {
            assertEquals(e.getValue(), shared.get(e.getKey()));
        }
        AudioSettings reloaded = new AudioSettings(audioStore);
        assertFalse(reloaded.isEnabled());
        assertEquals(35, reloaded.getVolume());
    }

    // 14. Bots -------------------------------------------------------------------------

    @Test
    public void botsAreRestoredWithBalancesAndPersonalities() {
        GameController c = open(() -> GameController.createDefault(YOU, DEALER, 99L));
        for (int round = 0; round < 3; round++) {
            assertTrue(c.addChip(25));
            assertTrue(c.deal());
            while (c.isHumanTurn() || c.isInsuranceDecisionPending()) {
                if (c.isInsuranceDecisionPending()) {
                    c.declineInsurance();
                } else {
                    c.stand();
                }
            }
            assertTrue(c.startNextRound());
            c.clearBet();
        }

        GameController again = reopen();
        assertEquals(describe(c), describe(again));
        List<BotPlayer> a = c.getBotManager().getBots();
        List<BotPlayer> b = again.getBotManager().getBots();
        assertEquals(BotManager.DEFAULT_NAMES.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).getId(), b.get(i).getId());
            assertEquals(a.get(i).getBalance(), b.get(i).getBalance());
            assertEquals(a.get(i).getPersonality(), b.get(i).getPersonality());
        }
        // Las apuestas futuras de los bots (generador guardado) coinciden.
        c.addChip(25);
        again.addChip(25);
        c.deal();
        again.deal();
        assertEquals(describe(c), describe(again));
    }

    // 15. Zapato ------------------------------------------------------------------------

    @Test
    public void restoredShoeDealsSameFutureCardsIncludingReshuffle() {
        Shoe original = new Shoe(1, new Random(42));
        original.beginRound();
        while (!original.needsShuffle()) {
            original.draw();
        }
        original.endRound();

        Shoe copy = Shoe.restore(original.saveState());
        assertEquals(original.remaining(), copy.remaining());
        // La próxima ronda rehace el zapato: el generador guardado baraja igual.
        original.beginRound();
        copy.beginRound();
        for (int i = 0; i < 40; i++) {
            assertEquals(original.draw(), copy.draw());
        }
    }

    @Test
    public void restoredGameMatchesUninterruptedGameRoundAfterRound() {
        GameController c = open(() -> GameController.createDefault(YOU, DEALER, 7L));
        GameController control = GameController.createDefault(YOU, DEALER, 7L);
        for (int round = 0; round < 15; round++) {
            for (GameController g : Arrays.asList(c, control)) {
                g.clearBet();
                if (g.isGameOver()) {
                    return;
                }
                g.addChip(25);
                g.deal();
                while (g.isHumanTurn() || g.isInsuranceDecisionPending()) {
                    if (g.isInsuranceDecisionPending()) {
                        g.declineInsurance();
                    } else if (g.canHit() && g.getEngine().getCurrentHand().getTotal() < 13) {
                        g.hit();
                    } else {
                        g.stand();
                    }
                }
            }
            c = reopen();                    // se cierra y abre la app en cada ronda
            assertEquals("ronda " + round, describe(control), describe(c));
            c.startNextRound();
            control.startNextRound();
        }
    }

    @Test
    public void testDecksAreNotPersisted() {
        GameController c = new GameController(new com.example.blackjack21.model.CardSource() {
            @Override
            public void beginRound() {
            }

            @Override
            public Card draw() {
                return Card.of(TEN, Suit.values()[0]);
            }

            @Override
            public void endRound() {
            }
        }, new Random(1), YOU, DEALER, Collections.<String>emptyList(), 0, 1000);
        assertNull(c.toSaveData());
        new GameSession(store).attach(c);
        assertNull(store.data.get(GameRepository.KEY_SAVE));
    }
}
