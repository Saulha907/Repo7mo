package com.example.blackjack21.bot;

import static com.example.blackjack21.model.Rank.ACE;
import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FIVE;
import static com.example.blackjack21.model.Rank.KING;
import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.QUEEN;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static com.example.blackjack21.model.Rank.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.engine.BetManager;
import com.example.blackjack21.engine.GameEngine;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Shoe;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BotManagerTest {

    private static final String HUMAN = "human";

    // 1-3. Inicialización --------------------------------------------------------

    @Test
    public void seatsThreeBotsWithIndependentBalances() {
        GameEngine engine = new GameEngine(new Shoe(6, new Random(1)));
        BotManager manager = new BotManager(engine, new Random(1));
        List<BotPlayer> bots = manager.seatDefaultBots(1000);

        assertEquals(3, bots.size());
        assertEquals("Alex", bots.get(0).getName());
        assertEquals("Max", bots.get(1).getName());
        assertEquals("Sam", bots.get(2).getName());
        for (BotPlayer b : bots) {
            assertEquals(1000, b.getBalance());
            assertEquals(BotPlayer.Status.WAITING, b.getStatus());
        }

        manager.placeBets();
        BotPlayer alex = bots.get(0);
        BotPlayer max = bots.get(1);
        assertEquals(1000 - alex.getCurrentBet(), alex.getBalance());
        assertEquals(1000 - max.getCurrentBet(), max.getBalance());
        assertNotEquals(alex.getPlayerState(), max.getPlayerState());
    }

    @Test
    public void personalitiesAreRandomAndVaried() {
        Set<String> orders = new java.util.HashSet<>();
        for (int seed = 0; seed < 20; seed++) {
            GameEngine engine = new GameEngine(new Shoe(6, new Random(seed)));
            BotManager manager = new BotManager(engine, new Random(seed));
            List<BotPlayer> bots = manager.seatDefaultBots(1000);
            Set<BotPersonality> distinct = EnumSet.noneOf(BotPersonality.class);
            StringBuilder order = new StringBuilder();
            for (BotPlayer b : bots) {
                distinct.add(b.getPersonality());
                order.append(b.getPersonality().ordinal());
            }
            assertEquals(3, distinct.size());
            orders.add(order.toString());
        }
        assertTrue("Las asignaciones deben variar entre partidas", orders.size() > 1);

        // Misma semilla → misma asignación.
        BotManager a = new BotManager(new GameEngine(new Shoe()), new Random(7));
        BotManager b = new BotManager(new GameEngine(new Shoe()), new Random(7));
        a.seatDefaultBots(1000);
        b.seatDefaultBots(1000);
        for (int i = 0; i < 3; i++) {
            assertEquals(a.getBots().get(i).getPersonality(), b.getBots().get(i).getPersonality());
        }
    }

    @Test
    public void botWithoutMinimumBetSitsOut() {
        GameEngine engine = new GameEngine(new Shoe(6, new Random(3)));
        BotManager manager = new BotManager(engine, new Random(3));
        BotPlayer poor = manager.seatBot("Alex", 5);
        BotPlayer rich = manager.seatBot("Max", 1000);
        manager.placeBets();
        assertEquals(BotPlayer.Status.SITTING_OUT, poor.getStatus());
        assertFalse(poor.getPlayerState().isInRound());
        assertEquals(5, poor.getBalance());
        assertTrue(rich.getPlayerState().isInRound());
    }

    // 15. Seguro en partida -------------------------------------------------------

    @Test
    public void insuranceWithAceUp() {
        // Asientos: Alex, Max. Reparto: A1, M1, crupier(A), A2, M2, oculta.
        GameEngine engine = new GameEngine(new BotTestSource(
                TEN, NINE, ACE, KING, FIVE, SEVEN));
        BotManager manager = new BotManager(engine, new Random(1));
        BotPlayer alex = manager.seatBot("Alex", 1000);
        BotPlayer max = manager.seatBot("Max", 1000);
        alex.setPersonality(BotPersonality.CONSERVATIVE);
        max.setPersonality(BotPersonality.BALANCED);
        manager.placeBets();
        engine.deal();
        assertEquals(GamePhase.INSURANCE_DECISION, engine.getPhase());

        manager.decideInsurance();
        assertEquals(engine.maxInsuranceFor(alex.getPlayerState()) > 0,
                alex.getPlayerState().getInsuranceBet() > 0); // 20 → asegura
        assertEquals(0, max.getPlayerState().getInsuranceBet()); // 14 → no
        assertEquals(GamePhase.PLAYER_TURN, engine.getPhase());
        assertEquals(Boolean.FALSE, alex.getPlayerState().getInsuranceWon());
    }

    // 16. Sin acceso a la carta oculta ------------------------------------------

    @Test
    public void botsCannotSeeHoleCard() {
        BotAction withSixHole = decisionWithHole(SIX);
        BotAction withNineHole = decisionWithHole(NINE);
        assertEquals(withSixHole, withNineHole);
    }

    private static BotAction decisionWithHole(Rank hole) {
        GameEngine engine = new GameEngine(new BotTestSource(TEN, TEN, SIX, hole, TWO, TWO, TWO));
        BotManager manager = new BotManager(engine, new Random(1));
        BotPlayer bot = manager.seatBot("Alex", 1000);
        bot.setPersonality(BotPersonality.BALANCED);
        manager.placeBets();
        engine.deal();
        DecisionContext c = DecisionContext.forCurrentTurn(engine, bot.getId());
        assertEquals(10, c.dealerUpValue);
        assertEquals(1, engine.getDealerVisibleCards().size());
        return bot.getStrategy().decide(c);
    }

    // 17. Orden de turnos --------------------------------------------------------

    @Test
    public void botsRespectTurnOrderAroundHuman() {
        // Asientos: Alex, Max, humano, Sam. Ninguna mano natural; crupier 10 + 7.
        GameEngine engine = new GameEngine(new BotTestSource(
                TEN, TEN, TEN, TEN, TEN,      // primera carta de cada uno + crupier
                SEVEN, EIGHT, NINE, EIGHT, SEVEN, // segunda carta (+ oculta del crupier)
                TWO, TWO, TWO, TWO, TWO, TWO));
        BotManager manager = new BotManager(engine, new Random(5));
        manager.seatBot("Alex", 1000);
        manager.seatBot("Max", 1000);
        engine.addPlayer(HUMAN, "Tú");
        manager.seatBot("Sam", 1000);
        for (BotPlayer b : manager.getBots()) {
            b.setPersonality(BotPersonality.BALANCED);
        }

        manager.placeBets();
        engine.placeBet(HUMAN, 10);
        engine.deal();

        manager.playPendingTurns();
        assertEquals(GamePhase.PLAYER_TURN, engine.getPhase());
        assertEquals(HUMAN, engine.getCurrentPlayer().getId());
        assertTrue(handFinished(engine, "bot:alex"));
        assertTrue(handFinished(engine, "bot:max"));
        assertFalse(handFinished(engine, "bot:sam"));

        engine.stand(HUMAN);
        assertEquals("bot:sam", engine.getCurrentPlayer().getId());
        manager.playPendingTurns();
        assertEquals(GamePhase.ROUND_FINISHED, engine.getPhase());
    }

    @Test
    public void botsDoNotActOnHumanTurn() {
        GameEngine engine = new GameEngine(new BotTestSource(TEN, TEN, SEVEN, SEVEN, TWO, TWO));
        BotManager manager = new BotManager(engine, new Random(5));
        engine.addPlayer(HUMAN, "Tú");
        manager.seatBot("Alex", 1000);
        engine.placeBet(HUMAN, 10);
        engine.deal();
        assertEquals(0, manager.playPendingTurns());
        assertEquals(HUMAN, engine.getCurrentPlayer().getId());
    }

    private static boolean handFinished(GameEngine engine, String id) {
        for (HandState h : engine.getPlayer(id).getHands()) {
            if (!h.isFinished()) {
                return false;
            }
        }
        return true;
    }

    // 18. Partida completa --------------------------------------------------------

    @Test
    public void fullGameWithThreeBotsAndHuman() {
        GameEngine engine = new GameEngine(new Shoe(6, new Random(2024)));
        BotManager manager = new BotManager(engine, new Random(2024));
        manager.seatBot("Alex", 1000);
        manager.seatBot("Max", 1000);
        engine.addPlayer(HUMAN, "Tú");
        manager.seatBot("Sam", 1000);
        manager.assignPersonalities();

        List<String> ids = new ArrayList<>();
        for (PlayerState p : engine.getState().getPlayers()) {
            ids.add(p.getId());
        }

        for (int round = 0; round < 300; round++) {
            manager.onNewRound();
            manager.placeBets();
            if (engine.getPlayer(HUMAN).getBalance() >= 10) {
                engine.placeBet(HUMAN, 10);
            }
            if (engine.getState().getActivePlayers().isEmpty()) {
                break;
            }
            engine.deal();
            if (engine.getPhase() == GamePhase.INSURANCE_DECISION) {
                manager.decideInsurance();
                if (engine.getPhase() == GamePhase.INSURANCE_DECISION) {
                    engine.declineInsurance(HUMAN);
                }
            }
            int guard = 0;
            while (engine.getPhase() == GamePhase.PLAYER_TURN && guard++ < 100) {
                manager.playPendingTurns();
                if (engine.getPhase() == GamePhase.PLAYER_TURN) {
                    assertEquals(HUMAN, engine.getCurrentPlayer().getId());
                    if (engine.getCurrentHand().getTotal() < 17) {
                        engine.hit(HUMAN);
                    } else {
                        engine.stand(HUMAN);
                    }
                }
            }
            assertEquals(GamePhase.ROUND_FINISHED, engine.getPhase());

            for (String id : ids) {
                PlayerState p = engine.getPlayer(id);
                assertTrue(p.getBalance() >= 0);
                for (HandState h : p.getHands()) {
                    assertTrue(h.isSettled());
                }
            }
            engine.startNextRound();
        }

        // Cada saldo coincide con el inicial más todos sus movimientos registrados.
        BetManager bets = engine.getBetManager();
        for (String id : ids) {
            int sum = 1000;
            for (BetManager.Entry e : bets.getLedger()) {
                if (e.playerId.equals(id)) {
                    sum += e.signedAmount();
                }
            }
            assertEquals(sum, engine.getPlayer(id).getBalance());
        }
        assertTrue(engine.getRoundNumber() > 50);
    }

    @Test
    public void botsHandleSplitsInPlay() {
        // Alex (equilibrado) recibe 8-8 contra un 6 del crupier: debe dividir.
        GameEngine engine = new GameEngine(new BotTestSource(
                EIGHT, SIX, EIGHT, QUEEN, TEN, TEN, TEN, TEN));
        BotManager manager = new BotManager(engine, new Random(9));
        BotPlayer alex = manager.seatBot("Alex", 1000);
        alex.setPersonality(BotPersonality.BALANCED);
        manager.placeBets();
        int bet = alex.getCurrentBet();
        engine.deal();
        manager.playPendingTurns();
        assertEquals(2, alex.getPlayerState().getHands().size());
        assertEquals(GamePhase.ROUND_FINISHED, engine.getPhase());
        assertEquals(2 * bet, alex.getPlayerState().getHands().get(0).getBet()
                + alex.getPlayerState().getHands().get(1).getBet());
    }
}
