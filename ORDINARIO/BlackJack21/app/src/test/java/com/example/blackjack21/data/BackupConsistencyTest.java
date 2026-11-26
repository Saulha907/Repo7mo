package com.example.blackjack21.data;

import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.TEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.engine.BetManager;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.game.GameController;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.RandomCodec;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Shoe;
import com.example.blackjack21.model.ShoeState;
import com.example.blackjack21.model.Suit;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * La copia de respaldo va una escritura por detrás. Si la partida principal se
 * daña justo después de liquidar una ronda, se restaura la copia anterior: las
 * estadísticas deben volver al mismo punto, sin contar resultados que esa
 * partida restaurada todavía no ha producido, y sin repetir apuestas ni pagos.
 */
public class BackupConsistencyTest {

    private final PersistenceTest.MemoryStore store = new PersistenceTest.MemoryStore();

    /** Humano 10+9 = 19 contra 10 + 7 oculta; la siguiente carta es un 10. */
    private static GameController fresh() {
        Rank[] ranks = {TEN, TEN, NINE, SEVEN, TEN};
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < ranks.length; i++) {
            cards.add(Card.of(ranks[i], Suit.values()[i % 4]));
        }
        for (int i = 0; i < 30; i++) {
            cards.add(Card.of(Rank.values()[i % 13], Suit.values()[i % 4]));
        }
        Shoe shoe = Shoe.restore(new ShoeState(6, 0, cards, RandomCodec.encode(new Random(3))));
        return new GameController(shoe, new Random(1), "Tú", "CRUPIER",
                Collections.<String>emptyList(), 0, 1000);
    }

    private GameController open() {
        return new GameSession(store).open(BackupConsistencyTest::fresh, "Tú", "CRUPIER");
    }

    private GameController reopen() {
        return new GameSession(store).open(() -> {
            throw new AssertionError("Se reinició una partida válida");
        }, "Tú", "CRUPIER");
    }

    /** Daña la partida principal (la suma de control deja de cuadrar). */
    private void corruptPrimarySave() {
        String good = store.data.get(GameRepository.KEY_SAVE);
        store.data.put(GameRepository.KEY_SAVE, good.replace("\"pending\"", "\"pendinG\""));
    }

    private static void assertConsistent(GameController c, PlayerStatistics s) {
        int balance = c.getHuman().getBalance();
        assertEquals("Saldo en estadísticas", balance, s.getCurrentBalance());
        if (c.getPhase() == GamePhase.ROUND_FINISHED || c.getPhase() == GamePhase.WAITING_FOR_BETS) {
            assertEquals("Fichas ganadas/perdidas frente al saldo real",
                    balance - 1000, s.getChipsWon() - s.getChipsLost());
        }
    }

    private static int humanEntries(GameController c, BetManager.EntryType type) {
        int n = 0;
        for (BetManager.Entry e : c.getEngine().getBetManager().getLedger()) {
            if (e.playerId.equals(GameController.HUMAN_ID) && e.type == type) {
                n++;
            }
        }
        return n;
    }

    @Test
    public void backupRestoreDoesNotKeepStatsOfAnUnplayedResult() {
        GameController c = open();
        assertTrue(c.addChip(50));
        assertTrue(c.deal());
        assertTrue(c.stand());                 // 19 contra 17: gana 50
        assertEquals(1050, c.getHuman().getBalance());
        assertEquals(1, GameSession.readStatistics(store).getRoundsWon());

        corruptPrimarySave();
        GameController again = reopen();       // copia: turno del jugador, antes de plantarse
        assertTrue(again.isHumanTurn());
        assertEquals("Apuesta cobrada una sola vez", 950, again.getHuman().getBalance());
        assertConsistent(again, GameSession.readStatistics(store));

        assertTrue(again.hit());               // esta vez pide: 19 + 10 = se pasa
        assertEquals(GamePhase.ROUND_FINISHED, again.getPhase());
        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals(1, s.getRoundsPlayed());
        assertEquals(1, s.getRoundsLost());
        assertEquals(0, s.getRoundsWon());
        assertConsistent(again, s);
    }

    @Test
    public void backupRestoreNeverRepeatsBetsPaymentsOrResults() {
        GameController c = open();
        c.addChip(50);
        c.deal();
        c.stand();
        corruptPrimarySave();

        GameController again = reopen();
        assertTrue(again.stand());             // misma decisión: mismo resultado
        assertEquals(1050, again.getHuman().getBalance());
        assertEquals(1, humanEntries(again, BetManager.EntryType.BET));
        assertEquals(1, humanEntries(again, BetManager.EntryType.PAYOUT));
        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals(1, s.getRoundsPlayed());
        assertEquals(1, s.getRoundsWon());
        assertConsistent(again, s);

        // Reabrir de nuevo (copia principal ya sana) no cambia nada.
        GameController third = reopen();
        assertEquals(1050, third.getHuman().getBalance());
        assertEquals(1, GameSession.readStatistics(store).getRoundsPlayed());
    }

    @Test
    public void corruptStatsAloneAreRebuiltFromTheSettledGame() {
        GameController c = open();
        c.addChip(50);
        c.deal();
        c.stand();
        store.data.put(StatsRepository.KEY_STATS, "dañado");

        GameController again = reopen();
        PlayerStatistics s = GameSession.readStatistics(store);
        assertEquals(1, s.getRoundsPlayed());
        assertConsistent(again, s);
    }
}
