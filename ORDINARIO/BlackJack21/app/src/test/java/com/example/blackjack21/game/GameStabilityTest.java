package com.example.blackjack21.game;

import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FOUR;
import static com.example.blackjack21.model.Rank.KING;
import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static com.example.blackjack21.model.Rank.THREE;
import static com.example.blackjack21.model.Rank.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.bot.BotPersonality;
import com.example.blackjack21.bot.BotPlayer;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.view.table.model.HandView;
import com.example.blackjack21.view.table.model.SeatInfo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Fin de partida, reinicio, manos divididas y robustez de los controles. */
public class GameStabilityTest {

    /** Mazo para dividir 8-8 hasta 4 manos contra 10 + 7 del crupier. */
    private static final Rank[] SPLIT_DECK = {
            EIGHT, TEN, EIGHT, SEVEN,   // humano 8, crupier 10, humano 8, oculta 7
            EIGHT, EIGHT, EIGHT,        // segunda carta de la mano 0 tras cada división
            TWO, TEN,                   // mano 1: 8+2, doblar recibe 10 → 20
            THREE, KING,                // mano 2: 8+3, pedir recibe K → 21
            NINE                        // mano 3: 8+9 = 17
    };

    private static GameController solo(int balance, Rank... cards) {
        return new GameController(new GameTestSource(cards), new Random(1), "Tú", "CRUPIER",
                Collections.<String>emptyList(), 0, balance);
    }

    private static void bet(GameController c, int amount) {
        c.clearBet();
        int remaining = amount;
        int[] chips = GameController.CHIP_VALUES;
        for (int i = chips.length - 1; i >= 0; i--) {
            while (remaining >= chips[i]) {
                assertTrue(c.addChip(chips[i]));
                remaining -= chips[i];
            }
        }
    }

    private static List<HandView> hands(GameController c) {
        return c.snapshot().player.hands;
    }

    private static int activeIndex(GameController c) {
        List<HandView> h = hands(c);
        for (int i = 0; i < h.size(); i++) {
            if (h.get(i).active) {
                return i;
            }
        }
        return -1;
    }

    // 1. Saldo inferior al mínimo ---------------------------------------------------

    @Test
    public void balanceBelowMinimumBlocksBetting() {
        GameController c = solo(15, TEN, TEN, SIX, NINE);
        bet(c, 10);
        assertTrue(c.deal());
        assertTrue(c.stand()); // 16 contra 19
        assertEquals(5, c.getHuman().getBalance());
        assertTrue(c.isGameOver());
        assertTrue(c.startNextRound());
        assertEquals(GameController.Status.GAME_OVER, c.getStatus());
        assertFalse(c.addChip(5));
        assertFalse(c.canDeal());
        assertFalse(c.deal());
        assertEquals(5, c.getHuman().getBalance());
    }

    // 2-4. Reinicio -------------------------------------------------------------------

    @Test
    public void restartGivesThousandChipsAndCleanLedger() {
        GameController c = solo(15, TEN, TEN, SIX, NINE);
        bet(c, 10);
        c.deal();
        c.stand();
        assertTrue(c.restartGame());
        assertEquals(1000, c.getHuman().getBalance());
        assertEquals(GamePhase.WAITING_FOR_BETS, c.getPhase());
        assertTrue(c.getEngine().getBetManager().getLedger().isEmpty());
        assertEquals(0, c.getPendingBet());
        assertFalse(c.isGameOver());
        assertEquals(2, c.getGameNumber());
        assertFalse(c.restartGame()); // pulsación repetida: ya no hay fin de partida
        assertEquals(1000, c.getHuman().getBalance());
    }

    @Test
    public void restartResetsAllThreeBots() {
        GameController c = new GameController(new GameTestSource(), new Random(3), "Tú", "CRUPIER",
                Arrays.asList("Alex", "Max", "Sam"), 2, 5);
        assertTrue(c.isGameOver());
        assertTrue(c.snapshot().bots.get(0).outOfChips);
        assertTrue(c.restartGame());
        Set<BotPersonality> personalities = EnumSet.noneOf(BotPersonality.class);
        for (BotPlayer bot : c.getBotManager().getBots()) {
            assertEquals(1000, bot.getBalance());
            personalities.add(bot.getPersonality());
        }
        assertEquals(3, personalities.size());
        for (SeatInfo seat : c.snapshot().bots) {
            assertFalse(seat.outOfChips);
        }
    }

    @Test
    public void restartIsOnlyAllowedAtGameOver() {
        // Aún no hay estadísticas persistentes: se comprueba que el reinicio
        // no puede descartar una partida viva y que cuenta las partidas.
        GameController c = solo(1000, TEN, TEN, SIX, NINE);
        assertFalse(c.restartGame());
        bet(c, 10);
        c.deal();
        assertFalse(c.restartGame());
        assertEquals(1, c.getGameNumber());
    }

    // 5-9. Manos divididas ---------------------------------------------------------------

    private static GameController splitGame() {
        GameController c = solo(1000, SPLIT_DECK);
        bet(c, 100);
        assertTrue(c.deal());
        return c;
    }

    @Test
    public void splitIntoTwoHands() {
        GameController c = splitGame();
        assertTrue(c.split());
        assertEquals(2, hands(c).size());
        assertEquals(200, c.snapshot().player.bet);
        assertEquals(100, hands(c).get(0).bet);
        assertEquals(100, hands(c).get(1).bet);
    }

    @Test
    public void splitIntoThreeHands() {
        GameController c = splitGame();
        assertTrue(c.split());
        assertTrue(c.split());
        assertEquals(3, hands(c).size());
        assertEquals(700, c.getHuman().getBalance());
    }

    @Test
    public void splitIntoFourHandsAndNoMore() {
        GameController c = splitGame();
        assertTrue(c.split());
        assertTrue(c.split());
        assertTrue(c.split());
        assertEquals(4, hands(c).size());
        assertFalse(c.canSplit());
        assertFalse(c.split()); // pulsación repetida o ilegal
        assertEquals(600, c.getHuman().getBalance());
        for (HandView h : hands(c)) {
            assertEquals(100, h.bet);
        }
    }

    @Test
    public void activeHandMovesAcrossSplitHands() {
        GameController c = splitGame();
        c.split();
        c.split();
        c.split();
        assertEquals(0, activeIndex(c));
        assertTrue(c.stand());
        assertEquals(1, activeIndex(c));
        assertEquals("10", hands(c).get(1).score); // 8 + 2
        assertTrue(c.doubleDown());
        assertEquals(2, activeIndex(c));
        assertTrue(c.hit());                      // 8 + 3 + K = 21: se cierra sola
        assertEquals(3, activeIndex(c));
        assertTrue(c.stand());
        assertEquals(-1, activeIndex(c));
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
    }

    @Test
    public void splitHandsHaveIndependentResults() {
        GameController c = splitGame();
        c.split();
        c.split();
        c.split();
        c.stand();      // 16 contra 17 → pierde
        c.doubleDown(); // 20 con apuesta 200 → gana
        c.hit();        // 21 → gana (no es blackjack natural)
        c.stand();      // 17 → empate
        List<HandView> h = hands(c);
        assertEquals(HandView.Result.LOSE, h.get(0).result);
        assertEquals(Integer.valueOf(-100), h.get(0).net);
        assertEquals(HandView.Result.WIN, h.get(1).result);
        assertEquals(200, h.get(1).bet);
        assertEquals(Integer.valueOf(200), h.get(1).net);
        assertEquals(HandView.Result.WIN, h.get(2).result);
        assertEquals(Integer.valueOf(100), h.get(2).net);
        assertEquals(HandView.Result.PUSH, h.get(3).result);
        assertEquals(Integer.valueOf(0), h.get(3).net);
        assertEquals(Integer.valueOf(200), c.snapshot().player.net);
        assertEquals(1200, c.getHuman().getBalance());
    }

    // 10. Pulsaciones repetidas ---------------------------------------------------------

    @Test
    public void repeatedPressesNeverDuplicate() {
        GameController c = splitGame();
        assertFalse(c.deal());
        assertTrue(c.split());
        int balance = c.getHuman().getBalance();
        assertFalse(c.takeInsurance());
        assertFalse(c.declineInsurance());
        assertFalse(c.startNextRound());
        assertFalse(c.restartGame());
        assertFalse(c.addChip(25));
        assertEquals(balance, c.getHuman().getBalance());
    }

    // 11. Fin de partida tras la ronda ---------------------------------------------------

    @Test
    public void gameOverOnlyAfterRoundFinishes() {
        GameController c = solo(10, TEN, TEN, SIX, NINE);
        bet(c, 10);
        assertTrue(c.deal());
        assertEquals(0, c.getHuman().getBalance());
        assertFalse(c.isGameOver()); // mano todavía activa
        assertTrue(c.stand());
        assertTrue(c.isGameOver());
    }

    @Test
    public void noGameOverWhenRoundIsWon() {
        GameController c = solo(10, TEN, TEN, NINE, SEVEN);
        bet(c, 10);
        c.deal();
        c.stand();
        assertEquals(20, c.getHuman().getBalance());
        assertFalse(c.isGameOver());
    }

    // 12. Bots sin saldo -------------------------------------------------------------------

    @Test
    public void brokeBotKeepsSeatButSitsOut() {
        // Asientos: Alex, humano. Reparto: A, H, crupier, A, H, oculta.
        GameController c = new GameController(
                new GameTestSource(TEN, TEN, TEN, SIX, TEN, NINE, TEN,
                        TEN, TEN, SEVEN, EIGHT, FOUR, THREE),
                new Random(1), "Tú", "CRUPIER", Collections.singletonList("Alex"), 1, 10);
        bet(c, 10);
        assertTrue(c.deal());
        if (c.isHumanTurn()) {
            assertTrue(c.stand()); // 20 contra 19
        }
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
        BotPlayer alex = c.getBotManager().getBots().get(0);
        assertEquals(0, alex.getBalance()); // Alex perdió con 16
        assertEquals(20, c.getHuman().getBalance());

        assertTrue(c.startNextRound());
        SeatInfo seat = c.snapshot().bots.get(0);
        assertTrue(seat.outOfChips);
        assertTrue(seat.sittingOut);
        assertEquals("Alex", seat.name);

        assertTrue(c.deal()); // el humano juega solo
        assertFalse(c.getEngine().getPlayer(alex.getId()).isInRound());
        assertEquals(BotPlayer.Status.SITTING_OUT, alex.getStatus());
        assertNull(c.snapshot().bots.get(0).net);
    }
}
