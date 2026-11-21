package com.example.blackjack21.engine;

import static com.example.blackjack21.model.Rank.ACE;
import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FIVE;
import static com.example.blackjack21.model.Rank.FOUR;
import static com.example.blackjack21.model.Rank.JACK;
import static com.example.blackjack21.model.Rank.KING;
import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.QUEEN;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static com.example.blackjack21.model.Rank.THREE;
import static com.example.blackjack21.model.Rank.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.blackjack21.engine.BetManager.EntryType;
import com.example.blackjack21.model.Rank;

import org.junit.Test;

/**
 * Orden de reparto con un jugador: J1, crupier (visible), J2, crupier (oculta),
 * y después las cartas pedidas en orden.
 */
public class GameEngineTest {

    private static final String P = "p";

    private StackedSource source;

    private GameEngine engine(Rank... cards) {
        return engine(GameEngine.DEFAULT_BALANCE, cards);
    }

    private GameEngine engine(int balance, Rank... cards) {
        source = new StackedSource(cards);
        GameEngine e = new GameEngine(source);
        e.addPlayer(P, "Jugador", balance);
        return e;
    }

    private static HandState hand(GameEngine e, int index) {
        return e.getPlayer(P).getHands().get(index);
    }

    private static int balance(GameEngine e) {
        return e.getPlayer(P).getBalance();
    }

    private static void expectIllegalState(Runnable action) {
        try {
            action.run();
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException expected) {
            // correcto
        }
    }

    // 1-3. Apuestas ----------------------------------------------------------

    @Test
    public void validBetIsDeducted() {
        GameEngine e = engine();
        e.placeBet(P, 100);
        assertEquals(900, balance(e));
        assertEquals(1, e.getBetManager().getLedger().size());
        assertEquals(EntryType.BET, e.getBetManager().getLedger().get(0).type);
    }

    @Test
    public void betAboveBalanceIsRejected() {
        GameEngine e = engine(50);
        try {
            e.placeBet(P, 60);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(50, balance(e));
            assertFalse(e.getPlayer(P).isInRound());
        }
    }

    @Test
    public void betBelowMinimumIsRejected() {
        GameEngine e = engine();
        try {
            e.placeBet(P, 5);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(1000, balance(e));
        }
    }

    // 4-5. Pedir y plantarse -------------------------------------------------

    @Test
    public void hitAddsCard() {
        GameEngine e = engine(FIVE, TEN, SIX, SEVEN, THREE);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(GamePhase.PLAYER_TURN, e.getPhase());
        e.hit(P);
        assertEquals(3, hand(e, 0).getCards().size());
        assertEquals(14, hand(e, 0).getTotal());
        assertEquals(GamePhase.PLAYER_TURN, e.getPhase());
    }

    @Test
    public void standEndsTurnAndDealerPlays() {
        GameEngine e = engine(TEN, TEN, EIGHT, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertTrue(e.isDealerHoleRevealed());
        assertEquals(Outcome.WIN, hand(e, 0).getOutcome());
    }

    // 6-7. Doblar -------------------------------------------------------------

    @Test
    public void doubleDownTakesExactlyOneCard() {
        GameEngine e = engine(FIVE, TEN, SIX, SEVEN, TEN);
        e.placeBet(P, 100);
        e.deal();
        e.doubleDown(P);
        HandState h = hand(e, 0);
        assertEquals(3, h.getCards().size());
        assertEquals(200, h.getBet());
        assertTrue(h.isDoubled());
        assertEquals(Outcome.WIN, h.getOutcome());
        assertEquals(1000 - 200 + 400, balance(e));
    }

    @Test
    public void doubleDownWithoutBalanceIsRejected() {
        GameEngine e = engine(150, FIVE, TEN, SIX, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        assertFalse(e.canDouble(P));
        try {
            e.doubleDown(P);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(50, balance(e));
            assertEquals(100, hand(e, 0).getBet());
            assertEquals(GamePhase.PLAYER_TURN, e.getPhase());
        }
    }

    // 8-11. Dividir -------------------------------------------------------------

    @Test
    public void splitValidPair() {
        GameEngine e = engine(EIGHT, TEN, EIGHT, SEVEN, THREE, TWO);
        e.placeBet(P, 100);
        e.deal();
        assertTrue(e.canSplit(P));
        e.split(P);
        assertEquals(2, e.getPlayer(P).getHands().size());
        assertEquals(800, balance(e));
        assertEquals(100, hand(e, 0).getBet());
        assertEquals(100, hand(e, 1).getBet());
        assertEquals(11, hand(e, 0).getTotal());      // 8 + 3
        assertEquals(1, hand(e, 1).getCards().size()); // recibe su carta al jugarse
        e.stand(P);
        assertEquals(10, hand(e, 1).getTotal());      // 8 + 2
        assertEquals(1, e.getCurrentHand().getId());
    }

    @Test
    public void splitInvalidPairIsRejected() {
        GameEngine e = engine(EIGHT, TEN, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        assertFalse(e.canSplit(P));
        expectIllegalState(() -> e.split(P));
        // K y Q valen 10 pero no son del mismo rango.
        GameEngine e2 = engine(KING, TEN, QUEEN, SEVEN);
        e2.placeBet(P, 100);
        e2.deal();
        expectIllegalState(() -> e2.split(P));
        assertEquals(900, balance(e2));
    }

    @Test
    public void splitLimitIsFourHands() {
        GameEngine e = engine(EIGHT, TEN, EIGHT, SEVEN, EIGHT, EIGHT, EIGHT, EIGHT);
        e.placeBet(P, 100);
        e.deal();
        e.split(P); // 2 manos, la primera recibe 8 → par
        e.split(P); // 3 manos
        e.split(P); // 4 manos
        assertEquals(4, e.getPlayer(P).getHands().size());
        assertTrue(hand(e, 0).getCards().size() == 2);
        assertFalse(e.canSplit(P));
        expectIllegalState(() -> e.split(P));
        assertEquals(1000 - 400, balance(e));
    }

    @Test
    public void splitAcesCanHitAndTwentyOneIsNotNatural() {
        GameEngine e = engine(ACE, TEN, ACE, SEVEN, FIVE, TWO, KING);
        e.placeBet(P, 100);
        e.deal();
        e.split(P);
        HandState first = hand(e, 0);
        assertEquals(16, first.getTotal()); // A + 5 suave
        assertTrue(e.canHit(P));
        e.hit(P);                           // + 2 → 18
        assertEquals(18, first.getTotal());
        e.stand(P);
        HandState second = hand(e, 1);      // A + K = 21 tras dividir
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertFalse(second.isNaturalBlackjack());
        assertEquals(Outcome.WIN, second.getOutcome());
        assertEquals(200, second.getPayout()); // 1:1, no 3:2
        assertEquals(Outcome.WIN, first.getOutcome());
        assertEquals(1000 - 200 + 400, balance(e));
    }

    // 12-15. Resultados ---------------------------------------------------------

    @Test
    public void naturalBlackjackPaysThreeToTwo() {
        GameEngine e = engine(ACE, TEN, KING, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertEquals(Outcome.BLACKJACK, hand(e, 0).getOutcome());
        assertEquals(1150, balance(e));
        assertEquals(2, e.dealerHand().size()); // el crupier no pide
    }

    @Test
    public void naturalPayoutRoundsDown() {
        GameEngine e = engine(ACE, TEN, KING, SEVEN);
        e.placeBet(P, 15);
        e.deal();
        assertEquals(15 + 22, hand(e, 0).getPayout());
        assertEquals(1000 - 15 + 37, balance(e));
    }

    @Test
    public void normalWinPaysEvenMoney() {
        GameEngine e = engine(TEN, TEN, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(1100, balance(e));
        assertEquals(100, e.getBetManager().netForRound(P, 1));
    }

    @Test
    public void pushReturnsBet() {
        GameEngine e = engine(TEN, TEN, SEVEN, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(Outcome.PUSH, hand(e, 0).getOutcome());
        assertEquals(1000, balance(e));
    }

    @Test
    public void lossKeepsBet() {
        GameEngine e = engine(TEN, TEN, SIX, EIGHT);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(Outcome.LOSE, hand(e, 0).getOutcome());
        assertEquals(900, balance(e));
    }

    @Test
    public void bustLosesAndDealerDoesNotDraw() {
        GameEngine e = engine(TEN, TEN, SIX, SIX, TEN, FIVE);
        e.placeBet(P, 100);
        e.deal();
        e.hit(P);
        assertTrue(hand(e, 0).isBust());
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertEquals(Outcome.LOSE, hand(e, 0).getOutcome());
        assertEquals(2, e.dealerHand().size());
        assertEquals(1, source.remaining());
        assertEquals(900, balance(e));
    }

    // 16-17. Seguro --------------------------------------------------------------

    @Test
    public void insuranceWinsWhenDealerHasBlackjack() {
        GameEngine e = engine(TEN, ACE, NINE, KING);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(GamePhase.INSURANCE_DECISION, e.getPhase());
        assertTrue(e.canTakeInsurance(P));
        e.takeInsurance(P, 50);
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertEquals(Boolean.TRUE, e.getPlayer(P).getInsuranceWon());
        assertEquals(Outcome.LOSE, hand(e, 0).getOutcome());
        // -100 apuesta, -50 seguro, +150 seguro (2:1 + devolución) = 1000
        assertEquals(1000, balance(e));
    }

    @Test
    public void insuranceLosesWhenDealerHasNoBlackjack() {
        GameEngine e = engine(TEN, ACE, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        e.takeInsurance(P, 50);
        assertEquals(GamePhase.PLAYER_TURN, e.getPhase());
        assertEquals(Boolean.FALSE, e.getPlayer(P).getInsuranceWon());
        assertEquals(850, balance(e));
        e.stand(P); // 19 contra 18 suave
        assertEquals(Outcome.WIN, hand(e, 0).getOutcome());
        assertEquals(1050, balance(e));
    }

    @Test
    public void insuranceLimitIsHalfTheBet() {
        GameEngine e = engine(TEN, ACE, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        try {
            e.takeInsurance(P, 51);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals(900, balance(e));
        }
    }

    @Test
    public void insuranceNotOfferedWithoutAce() {
        GameEngine e = engine(TEN, TEN, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        assertFalse(e.canTakeInsurance(P));
        expectIllegalState(() -> e.takeInsurance(P, 10));
    }

    // 18. Crupier -------------------------------------------------------------

    @Test
    public void dealerStandsOnSoft17() {
        GameEngine e = engine(TEN, SIX, NINE, ACE, TEN);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(2, e.dealerHand().size());
        assertEquals(17, e.dealerHand().getTotal());
        assertTrue(e.dealerHand().isSoft());
        assertEquals(Outcome.WIN, hand(e, 0).getOutcome());
    }

    @Test
    public void dealerHitsBelow17() {
        GameEngine e = engine(TEN, TEN, NINE, SIX, FOUR, TWO);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(3, e.dealerHand().size()); // 16 + 4 = 20
        assertEquals(20, e.dealerHand().getTotal());
        assertEquals(Outcome.LOSE, hand(e, 0).getOutcome());
    }

    // 19. Fuera de turno ----------------------------------------------------------

    @Test
    public void actionsOutOfTurnAreRejected() {
        GameEngine e = engine(TEN, TEN, NINE, SEVEN);
        expectIllegalState(() -> e.hit(P));
        expectIllegalState(e::deal); // nadie apostó
        e.placeBet(P, 100);
        expectIllegalState(() -> e.placeBet(P, 100)); // apuesta repetida
        e.deal();
        expectIllegalState(() -> e.placeBet(P, 50));
        expectIllegalState(e::deal);
        expectIllegalState(e::startNextRound);
        e.stand(P);
        expectIllegalState(() -> e.stand(P));
        expectIllegalState(() -> e.hit(P));
    }

    @Test
    public void otherPlayerCannotActOnSomeoneElsesTurn() {
        source = new StackedSource(TEN, NINE, TEN, TEN, EIGHT, SEVEN);
        GameEngine e = new GameEngine(source);
        e.addPlayer("a", "A");
        e.addPlayer("b", "B");
        e.placeBet("a", 10);
        e.placeBet("b", 10);
        e.deal(); // a: 10,10  b: 9,8  crupier: 10,7
        assertEquals("a", e.getCurrentPlayer().getId());
        expectIllegalState(() -> e.hit("b"));
        e.stand("a");
        assertEquals("b", e.getCurrentPlayer().getId());
        e.stand("b");
        assertEquals(Outcome.WIN, e.getPlayer("a").getHands().get(0).getOutcome());
        assertEquals(Outcome.PUSH, e.getPlayer("b").getHands().get(0).getOutcome());
    }

    // 20. Pagos duplicados --------------------------------------------------------

    @Test
    public void payoutsAreNotDuplicated() {
        GameEngine e = engine(TEN, TEN, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        int after = balance(e);
        long payouts = e.getBetManager().getLedger().stream()
                .filter(x -> x.type == EntryType.PAYOUT).count();
        assertEquals(1, payouts);

        PlayerState p = e.getPlayer(P);
        assertFalse(e.getBetManager().credit(p, 1, EntryType.PAYOUT, 0, 200));
        expectIllegalState(() -> e.stand(P));
        assertEquals(after, balance(e));
        assertEquals(1, source.roundsEnded);
    }

    @Test
    public void nextRoundResetsHandsAndKeepsBalance() {
        GameEngine e = engine(TEN, TEN, NINE, SEVEN, TEN, TEN, EIGHT, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        e.startNextRound();
        assertEquals(GamePhase.WAITING_FOR_BETS, e.getPhase());
        assertEquals(2, e.getRoundNumber());
        assertFalse(e.getPlayer(P).isInRound());
        assertEquals(1100, balance(e));
        e.placeBet(P, 100);
        e.deal();
        e.stand(P);
        assertEquals(1200, balance(e));
    }

    // Blackjack simultáneo y del crupier ----------------------------------------

    @Test
    public void simultaneousBlackjackIsPush() {
        GameEngine e = engine(ACE, KING, KING, ACE);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertEquals(Outcome.PUSH, hand(e, 0).getOutcome());
        assertEquals(1000, balance(e));
    }

    @Test
    public void simultaneousBlackjackWithAceUpAfterInsuranceDecision() {
        GameEngine e = engine(ACE, ACE, KING, QUEEN);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(GamePhase.INSURANCE_DECISION, e.getPhase());
        e.declineInsurance(P);
        assertEquals(Outcome.PUSH, hand(e, 0).getOutcome());
        assertNull(e.getPlayer(P).getInsuranceWon());
        assertEquals(1000, balance(e));
    }

    @Test
    public void dealerBlackjackEndsRoundBeforePlayerTurn() {
        GameEngine e = engine(TEN, JACK, NINE, ACE, FIVE);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(GamePhase.ROUND_FINISHED, e.getPhase());
        assertTrue(e.isDealerHoleRevealed());
        assertEquals(Outcome.LOSE, hand(e, 0).getOutcome());
        assertEquals(900, balance(e));
        expectIllegalState(() -> e.hit(P));
        assertEquals(1, source.remaining());
    }

    @Test
    public void holeCardIsHiddenUntilRevealed() {
        GameEngine e = engine(TEN, TEN, NINE, SEVEN);
        e.placeBet(P, 100);
        e.deal();
        assertEquals(1, e.getDealerVisibleCards().size());
        assertEquals(10, e.getDealerVisibleTotal());
        e.stand(P);
        assertEquals(2, e.getDealerVisibleCards().size());
        assertEquals(17, e.getDealerVisibleTotal());
    }

    @Test
    public void balanceNeverGoesNegative() {
        GameEngine e = engine(10, TEN, TEN, SIX, SEVEN);
        e.placeBet(P, 10);
        assertEquals(0, balance(e));
        e.deal();
        assertFalse(e.canDouble(P));
        assertFalse(e.canSplit(P));
        e.stand(P);
        assertTrue(balance(e) >= 0);
    }
}
