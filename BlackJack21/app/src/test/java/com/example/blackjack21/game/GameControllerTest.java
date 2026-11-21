package com.example.blackjack21.game;

import static com.example.blackjack21.model.Rank.ACE;
import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FIVE;
import static com.example.blackjack21.model.Rank.KING;
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

import com.example.blackjack21.engine.BetManager;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Shoe;
import com.example.blackjack21.model.Suit;
import com.example.blackjack21.view.paint.SuitPaths;
import com.example.blackjack21.view.table.model.HandView;
import com.example.blackjack21.view.table.model.SeatInfo;
import com.example.blackjack21.view.table.model.TableCard;
import com.example.blackjack21.view.table.model.TableSnapshot;

import org.junit.Test;

import java.util.Collections;
import java.util.Random;

/**
 * Pruebas del coordinador sin Android. Con un solo jugador humano el orden de
 * reparto es: humano, crupier (visible), humano, crupier (oculta), resto.
 */
public class GameControllerTest {

    private static GameController solo(Rank... cards) {
        return solo(1000, cards);
    }

    private static GameController solo(int balance, Rank... cards) {
        return new GameController(new GameTestSource(cards), new Random(1), "Tú", "CRUPIER",
                Collections.<String>emptyList(), 0, balance);
    }

    private static GameController betAndDeal(int bet, Rank... cards) {
        GameController c = solo(cards);
        addBet(c, bet);
        assertTrue(c.deal());
        return c;
    }

    private static void addBet(GameController c, int bet) {
        int remaining = bet;
        for (int i = GameController.CHIP_VALUES.length - 1; i >= 0; i--) {
            int v = GameController.CHIP_VALUES[i];
            while (remaining >= v) {
                assertTrue(c.addChip(v));
                remaining -= v;
            }
        }
        assertEquals(0, remaining);
    }

    private static SeatInfo player(GameController c) {
        return c.snapshot().player;
    }

    // 1-3. Conversión ----------------------------------------------------------------

    @Test
    public void convertsCards() {
        TableCard ace = TableSnapshotMapper.toTableCard(Card.of(Rank.ACE, Suit.HEARTS));
        assertEquals("A", ace.rank);
        assertEquals(SuitPaths.HEART, ace.suit);
        assertFalse(ace.faceDown);
        TableCard ten = TableSnapshotMapper.toTableCard(Card.of(Rank.TEN, Suit.CLUBS));
        assertEquals("10", ten.rank);
        assertEquals(SuitPaths.CLUB, ten.suit);
        TableCard king = TableSnapshotMapper.toTableCard(Card.of(Rank.KING, Suit.SPADES));
        assertTrue(king.isCourt());
        assertEquals(SuitPaths.SPADE, king.suit);
        assertEquals(SuitPaths.DIAMOND,
                TableSnapshotMapper.toTableCard(Card.of(Rank.TWO, Suit.DIAMONDS)).suit);
    }

    @Test
    public void convertsHands() {
        GameController c = betAndDeal(10, ACE, TEN, SIX, SEVEN);
        HandView hand = player(c).hands.get(0);
        assertEquals(2, hand.cards.size());
        assertEquals("A", hand.cards.get(0).rank);
        assertEquals("6", hand.cards.get(1).rank);
        assertEquals("7/17", hand.score);
        assertEquals(10, hand.bet);
        assertTrue(hand.active);
        assertEquals("17", TableSnapshotMapper.formatScore(17, false));
        assertEquals("21", TableSnapshotMapper.formatScore(21, true));
    }

    @Test
    public void dealerHoleCardStaysHidden() {
        GameController c = betAndDeal(10, TEN, TEN, EIGHT, SEVEN);
        HandView dealer = c.snapshot().dealer.hands.get(0);
        assertEquals(2, dealer.cards.size());
        assertFalse(dealer.cards.get(0).faceDown);
        assertTrue(dealer.cards.get(1).faceDown);
        assertEquals("", dealer.cards.get(1).rank);
        assertEquals("10", dealer.score); // solo la carta visible

        assertTrue(c.stand());
        dealer = c.snapshot().dealer.hands.get(0);
        assertFalse(dealer.cards.get(1).faceDown);
        assertEquals("7", dealer.cards.get(1).rank);
        assertEquals("17", dealer.score);
    }

    // 4. Apuestas ---------------------------------------------------------------------

    @Test
    public void betIsAccumulatedCorrectedAndDeductedOnce() {
        GameController c = solo(1000, TEN, TEN, EIGHT, SEVEN);
        assertTrue(c.addChip(5));
        assertFalse(c.canDeal()); // por debajo del mínimo
        assertTrue(c.addChip(25));
        assertTrue(c.addChip(100));
        assertEquals(130, c.getPendingBet());
        assertEquals(130, player(c).bet);
        assertEquals(1000, c.getHuman().getBalance()); // aún no se descuenta

        assertTrue(c.clearBet());
        assertEquals(0, c.getPendingBet());
        assertFalse(c.clearBet());

        assertTrue(c.addChip(500));
        assertTrue(c.addChip(500));
        assertFalse(c.addChip(5)); // superaría el saldo
        assertEquals(1000, c.getPendingBet());

        assertTrue(c.deal());
        assertEquals(0, c.getHuman().getBalance());
        assertEquals(1000, player(c).bet);
        long bets = c.getEngine().getBetManager().getLedger().stream()
                .filter(e -> e.type == BetManager.EntryType.BET).count();
        assertEquals(1, bets);
    }

    // 5-9. Acciones -------------------------------------------------------------------

    @Test
    public void dealStartsPlayerTurn() {
        GameController c = betAndDeal(50, TEN, TEN, EIGHT, SEVEN);
        assertEquals(GamePhase.PLAYER_TURN, c.getPhase());
        assertTrue(c.isHumanTurn());
        assertEquals(GameController.Status.YOUR_TURN, c.getStatus());
        assertTrue(player(c).active);
        assertFalse(c.canChangeBet());
    }

    @Test
    public void hitAddsRealCard() {
        GameController c = betAndDeal(10, FIVE, TEN, SIX, SEVEN, THREE);
        assertTrue(c.hit());
        HandView hand = player(c).hands.get(0);
        assertEquals(3, hand.cards.size());
        assertEquals("3", hand.cards.get(2).rank);
        assertEquals("14", hand.score);
    }

    @Test
    public void standFinishesRound() {
        GameController c = betAndDeal(100, TEN, TEN, NINE, SEVEN);
        assertTrue(c.stand());
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
        assertEquals(HandView.Result.WIN, player(c).hands.get(0).result);
    }

    @Test
    public void doubleDownDoublesBetAndEndsHand() {
        GameController c = betAndDeal(100, FIVE, TEN, SIX, SEVEN, TEN);
        assertTrue(c.canDouble());
        assertTrue(c.doubleDown());
        SeatInfo p = player(c);
        assertEquals(3, p.hands.get(0).cards.size());
        assertEquals(200, p.hands.get(0).bet);
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
        assertEquals(1200, c.getHuman().getBalance());
        assertFalse(c.doubleDown());
    }

    @Test
    public void splitShowsSeparateHandsWithActiveMarker() {
        GameController c = betAndDeal(100, EIGHT, TEN, EIGHT, SEVEN, THREE, TWO);
        assertTrue(c.canSplit());
        assertTrue(c.split());
        SeatInfo p = player(c);
        assertEquals(2, p.hands.size());
        assertTrue(p.hands.get(0).active);
        assertFalse(p.hands.get(1).active);
        assertEquals(200, p.bet);
        assertEquals(800, c.getHuman().getBalance());

        assertTrue(c.stand());
        p = player(c);
        assertFalse(p.hands.get(0).active);
        assertTrue(p.hands.get(1).active);
    }

    // 10. Seguro -------------------------------------------------------------------------

    @Test
    public void insuranceDecisionIsExplicit() {
        GameController c = betAndDeal(100, TEN, ACE, NINE, KING);
        assertEquals(GameController.Status.INSURANCE, c.getStatus());
        assertTrue(c.isInsuranceDecisionPending());
        assertEquals(50, c.getInsuranceAmount());
        assertFalse(c.canHit());
        assertTrue(c.takeInsurance());
        // Crupier con blackjack: pierde 100, el seguro devuelve 150.
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
        assertEquals(1000, c.getHuman().getBalance());
        assertFalse(c.takeInsurance());
    }

    @Test
    public void insuranceDeclinedContinuesToPlayerTurn() {
        GameController c = betAndDeal(100, TEN, ACE, NINE, SEVEN);
        assertTrue(c.declineInsurance());
        assertEquals(GamePhase.PLAYER_TURN, c.getPhase());
        assertFalse(c.declineInsurance());
        assertEquals(900, c.getHuman().getBalance());
    }

    // 11. Turnos automáticos ---------------------------------------------------------------

    @Test
    public void botsPlayAutomaticallyAroundHuman() {
        for (int seed = 0; seed < 30; seed++) {
            GameController c = GameController.createDefault("Tú", "CRUPIER", seed);
            addBet(c, 25);
            assertTrue(c.deal());
            GamePhase phase = c.getPhase();
            assertTrue(phase == GamePhase.PLAYER_TURN || phase == GamePhase.INSURANCE_DECISION
                    || phase == GamePhase.ROUND_FINISHED);
            if (phase == GamePhase.PLAYER_TURN) {
                assertTrue(c.isHumanTurn());
                // Los bots sentados antes del humano ya han terminado.
                for (int i = 0; i < 2; i++) {
                    PlayerState bot = c.getEngine().getPlayer(c.getBotIds().get(i));
                    bot.getHands().forEach(h -> assertTrue(h.isFinished()));
                }
            }
        }
    }

    // 12-13. Resultado y saldo ----------------------------------------------------------

    @Test
    public void finalResultAndBalanceAreShown() {
        GameController c = betAndDeal(100, ACE, TEN, KING, SEVEN);
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
        SeatInfo p = player(c);
        assertEquals(HandView.Result.BLACKJACK, p.hands.get(0).result);
        assertEquals(Integer.valueOf(150), p.net);
        assertEquals(1150, p.balance);
        assertEquals(150, c.getHumanRoundNet());
        assertEquals(GameController.Status.ROUND_OVER, c.getStatus());
    }

    @Test
    public void losingRoundShowsNegativeNetAndBust() {
        GameController c = betAndDeal(50, TEN, TEN, SIX, SEVEN, KING);
        assertTrue(c.hit());
        SeatInfo p = player(c);
        assertEquals(HandView.Result.BUST, p.hands.get(0).result);
        assertEquals(Integer.valueOf(-50), p.net);
        assertEquals(950, c.getHuman().getBalance());
    }

    @Test
    public void nextRoundKeepsBalanceAndRepeatsBet() {
        GameController c = betAndDeal(100, TEN, TEN, NINE, SEVEN);
        assertTrue(c.stand());
        assertTrue(c.startNextRound());
        assertEquals(GamePhase.WAITING_FOR_BETS, c.getPhase());
        assertEquals(1100, c.getHuman().getBalance());
        assertEquals(100, c.getPendingBet());
        assertTrue(player(c).hands.isEmpty());
        assertNull(player(c).net);
    }

    // 14. Acciones ilegales --------------------------------------------------------------

    @Test
    public void illegalActionsAreIgnored() {
        GameController c = solo(TEN, TEN, EIGHT, NINE, SEVEN, TWO);
        assertFalse(c.hit());
        assertFalse(c.stand());
        assertFalse(c.split());
        assertFalse(c.takeInsurance());
        assertFalse(c.deal()); // sin apuesta
        assertFalse(c.startNextRound());

        addBet(c, 10);
        assertTrue(c.deal());
        assertFalse(c.addChip(5)); // apuesta fuera de fase
        assertFalse(c.clearBet());
        assertFalse(c.split());    // 10 y 8 no son par
        assertFalse(c.takeInsurance());
        assertTrue(c.hit());       // 10 + 8 + 7 = 25: se pasa
        assertFalse(c.doubleDown());
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
    }

    @Test
    public void doubleNotAllowedWithoutBalance() {
        GameController c = solo(150, FIVE, TEN, SIX, SEVEN);
        addBet(c, 100);
        assertTrue(c.deal());
        assertFalse(c.canDouble());
        assertFalse(c.doubleDown());
        assertEquals(50, c.getHuman().getBalance());
    }

    // 15. Pulsaciones repetidas -------------------------------------------------------

    @Test
    public void repeatedPressesHaveNoExtraEffect() {
        GameController c = solo(TEN, TEN, NINE, SEVEN);
        addBet(c, 100);
        assertTrue(c.deal());
        assertFalse(c.deal());
        assertEquals(900, c.getHuman().getBalance());

        assertTrue(c.stand());
        assertFalse(c.stand());
        int balance = c.getHuman().getBalance();
        assertEquals(1100, balance);

        long payouts = c.getEngine().getBetManager().getLedger().stream()
                .filter(e -> e.type == BetManager.EntryType.PAYOUT).count();
        assertEquals(1, payouts);

        assertTrue(c.startNextRound());
        assertFalse(c.startNextRound());
        assertEquals(balance, c.getHuman().getBalance());
    }

    @Test
    public void listenerIsNotifiedOnChanges() {
        GameController c = solo(TEN, TEN, NINE, SEVEN);
        int[] calls = {0};
        c.setListener(() -> calls[0]++);
        c.addChip(25);
        assertEquals(1, calls[0]);
        assertFalse(c.hit());
        assertEquals(1, calls[0]); // acción ilegal: sin notificación
    }

    // Integración --------------------------------------------------------------------

    @Test
    public void manyRoundsWithBotsKeepConsistentBalances() {
        GameController c = GameController.createDefault("Tú", "CRUPIER", 99);
        Random r = new Random(99);
        for (int round = 0; round < 250; round++) {
            c.clearBet(); // se propone la apuesta anterior; la sustituimos
            int balance = c.getHuman().getBalance();
            if (balance >= 10) {
                int bet = Math.min(10 + 5 * r.nextInt(5), balance / 5 * 5);
                addBet(c, bet);
            }
            if (!c.canDeal()) {
                break;
            }
            assertTrue(c.deal());
            int guard = 0;
            while (c.getPhase() != GamePhase.ROUND_FINISHED && guard++ < 50) {
                if (c.isInsuranceDecisionPending()) {
                    assertTrue(r.nextBoolean() && c.canTakeInsurance()
                            ? c.takeInsurance() : c.declineInsurance());
                } else if (c.canSplit() && r.nextInt(3) == 0) {
                    assertTrue(c.split());
                } else if (c.canDouble() && r.nextInt(4) == 0) {
                    assertTrue(c.doubleDown());
                } else if (c.canHit() && c.getEngine().getCurrentHand().getTotal() < 16) {
                    assertTrue(c.hit());
                } else {
                    assertTrue(c.stand());
                }
            }
            assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
            TableSnapshot s = c.snapshot();
            assertNotNull(s.dealer);
            assertFalse(s.dealer.hands.get(0).cards.stream().anyMatch(t -> t.faceDown));
            assertTrue(c.startNextRound());
        }

        BetManager bets = c.getEngine().getBetManager();
        for (PlayerState p : c.getEngine().getState().getPlayers()) {
            int sum = 1000;
            for (BetManager.Entry e : bets.getLedger()) {
                if (e.playerId.equals(p.getId())) {
                    sum += e.signedAmount();
                }
            }
            assertEquals(sum, p.getBalance());
            assertTrue(p.getBalance() >= 0);
        }
    }
}
