package com.example.blackjack21.bot;

import static com.example.blackjack21.bot.BotPersonality.AGGRESSIVE;
import static com.example.blackjack21.bot.BotPersonality.BALANCED;
import static com.example.blackjack21.bot.BotPersonality.CONSERVATIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.model.Rank;

import org.junit.Test;

import java.util.Random;

public class BotDecisionEngineTest {

    private static BotDecisionEngine bot(BotPersonality p) {
        return new BotDecisionEngine(p);
    }

    /** Mano normal con todas las acciones legales. */
    private static DecisionContext ctx(int total, boolean soft, int up) {
        return new DecisionContext(total, soft, null, 2, up, 1000, 50, true, true, false);
    }

    private static DecisionContext pair(Rank rank, int up, boolean canSplit) {
        int total = rank == Rank.ACE ? 12 : rank.getValue() * 2;
        return new DecisionContext(total, rank == Rank.ACE, rank, 2, up, 1000, 50, true, true, canSplit);
    }

    // 4-7. Apuestas ---------------------------------------------------------

    @Test
    public void conservativeBetsAreSmall() {
        Random r = new Random(1);
        for (int i = 0; i < 200; i++) {
            int bet = bot(CONSERVATIVE).chooseBet(1000, r);
            assertTrue(bet >= 20 && bet <= 30);
            assertEquals(0, bet % 5);
        }
    }

    @Test
    public void balancedBetsScaleWithBalance() {
        Random r = new Random(2);
        for (int i = 0; i < 200; i++) {
            int bet = bot(BALANCED).chooseBet(1000, r);
            assertTrue(bet >= 40 && bet <= 60);
        }
        int richBet = bot(BALANCED).chooseBet(10000, new Random(2));
        assertTrue(richBet > 60 && richBet <= 200);
    }

    @Test
    public void aggressiveBetsAreLarger() {
        Random r = new Random(3);
        for (int i = 0; i < 200; i++) {
            int bet = bot(AGGRESSIVE).chooseBet(1000, r);
            assertTrue(bet >= 100 && bet <= 150);
        }
        assertTrue(bot(AGGRESSIVE).chooseBet(100000, new Random(3)) <= 500);
    }

    @Test
    public void betsNeverExceedBalanceOrGoBelowMinimum() {
        Random r = new Random(4);
        for (BotPersonality p : BotPersonality.values()) {
            for (int balance = 0; balance <= 300; balance++) {
                int bet = bot(p).chooseBet(balance, r);
                if (balance < 10) {
                    assertEquals(0, bet);
                } else {
                    assertTrue(bet >= 10 && bet <= balance);
                }
            }
        }
    }

    // 8-10. Decisiones ------------------------------------------------------

    @Test
    public void softHands() {
        assertEquals(BotAction.HIT, bot(BALANCED).decide(ctx(17, true, 9)));
        assertEquals(BotAction.DOUBLE, bot(BALANCED).decide(ctx(17, true, 5)));
        assertEquals(BotAction.STAND, bot(BALANCED).decide(ctx(18, true, 7)));
        assertEquals(BotAction.HIT, bot(BALANCED).decide(ctx(18, true, 10)));
        assertEquals(BotAction.STAND, bot(BALANCED).decide(ctx(19, true, 6)));
        assertEquals(BotAction.DOUBLE, bot(AGGRESSIVE).decide(ctx(19, true, 6)));
        assertEquals(BotAction.STAND, bot(CONSERVATIVE).decide(ctx(18, true, 10)));
        assertEquals(BotAction.HIT, bot(CONSERVATIVE).decide(ctx(17, true, 5)));
    }

    @Test
    public void hardHands() {
        assertEquals(BotAction.STAND, bot(BALANCED).decide(ctx(17, false, 10)));
        assertEquals(BotAction.HIT, bot(BALANCED).decide(ctx(16, false, 10)));
        assertEquals(BotAction.HIT, bot(BALANCED).decide(ctx(8, false, 6)));
        assertEquals(BotAction.STAND, bot(CONSERVATIVE).decide(ctx(15, false, 10)));
        assertEquals(BotAction.HIT, bot(AGGRESSIVE).decide(ctx(15, false, 10)));
    }

    @Test
    public void dealerUpCardChangesDecision() {
        BotDecisionEngine b = bot(BALANCED);
        assertEquals(BotAction.STAND, b.decide(ctx(13, false, 2)));
        assertEquals(BotAction.STAND, b.decide(ctx(13, false, 6)));
        assertEquals(BotAction.HIT, b.decide(ctx(13, false, 7)));
        assertEquals(BotAction.HIT, b.decide(ctx(13, false, 11)));
        assertEquals(BotAction.HIT, b.decide(ctx(12, false, 3)));
        assertEquals(BotAction.STAND, b.decide(ctx(12, false, 4)));
    }

    // 11-12. Doblar ------------------------------------------------------------

    @Test
    public void doublesWhenLegal() {
        assertEquals(BotAction.DOUBLE, bot(BALANCED).decide(ctx(11, false, 6)));
        assertEquals(BotAction.DOUBLE, bot(CONSERVATIVE).decide(ctx(11, false, 6)));
        assertEquals(BotAction.DOUBLE, bot(AGGRESSIVE).decide(ctx(10, false, 11)));
        assertEquals(BotAction.HIT, bot(CONSERVATIVE).decide(ctx(10, false, 6)));
    }

    @Test
    public void doesNotDoubleWithoutBalance() {
        DecisionContext noMoney = new DecisionContext(11, false, null, 2, 6, 20, 50, true, false, false);
        for (BotPersonality p : BotPersonality.values()) {
            assertEquals(BotAction.HIT, bot(p).decide(noMoney));
        }
        DecisionContext soft18 = new DecisionContext(18, true, null, 2, 5, 20, 50, true, false, false);
        assertEquals(BotAction.STAND, bot(BALANCED).decide(soft18));
    }

    // 13-14. Dividir -----------------------------------------------------------

    @Test
    public void splitsPairs() {
        for (BotPersonality p : BotPersonality.values()) {
            assertEquals(BotAction.SPLIT, bot(p).decide(pair(Rank.EIGHT, 10, true)));
            assertEquals(BotAction.SPLIT, bot(p).decide(pair(Rank.ACE, 6, true)));
        }
        assertEquals(BotAction.SPLIT, bot(BALANCED).decide(pair(Rank.NINE, 6, true)));
        assertEquals(BotAction.STAND, bot(BALANCED).decide(pair(Rank.NINE, 7, true)));
        assertEquals(BotAction.STAND, bot(CONSERVATIVE).decide(pair(Rank.NINE, 6, true)));
        assertEquals(BotAction.SPLIT, bot(AGGRESSIVE).decide(pair(Rank.KING, 6, true)));
    }

    @Test
    public void neverSplitsWhenIllegalOrUnwise() {
        for (BotPersonality p : BotPersonality.values()) {
            BotAction a = bot(p).decide(pair(Rank.EIGHT, 10, false));
            assertFalse(a == BotAction.SPLIT);
        }
        assertEquals(BotAction.STAND, bot(BALANCED).decide(pair(Rank.KING, 6, true)));
        assertEquals(BotAction.DOUBLE, bot(BALANCED).decide(pair(Rank.FIVE, 6, true)));
    }

    @Test
    public void standsWhenHittingIsNotAllowed() {
        DecisionContext c = new DecisionContext(21, false, null, 3, 10, 1000, 50, false, false, false);
        for (BotPersonality p : BotPersonality.values()) {
            assertEquals(BotAction.STAND, bot(p).decide(c));
        }
    }

    // 15. Seguro -----------------------------------------------------------------

    @Test
    public void insuranceDecisionByPersonality() {
        assertTrue(bot(CONSERVATIVE).wantsInsurance(20, false));
        assertTrue(bot(CONSERVATIVE).wantsInsurance(21, true));
        assertFalse(bot(CONSERVATIVE).wantsInsurance(15, false));
        assertFalse(bot(BALANCED).wantsInsurance(20, false));
        assertFalse(bot(AGGRESSIVE).wantsInsurance(21, true));
    }

    @Test
    public void decisionsAreDeterministic() {
        BotDecisionEngine b = bot(AGGRESSIVE);
        DecisionContext c = ctx(12, false, 3);
        BotAction first = b.decide(c);
        for (int i = 0; i < 50; i++) {
            assertEquals(first, b.decide(c));
        }
    }
}
