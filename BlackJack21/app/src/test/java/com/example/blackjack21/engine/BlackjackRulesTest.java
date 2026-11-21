package com.example.blackjack21.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.Hand;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Suit;

import org.junit.Test;

public class BlackjackRulesTest {

    private static Hand hand(Rank... ranks) {
        Hand h = new Hand();
        for (Rank r : ranks) {
            h.add(Card.of(r, Suit.HEARTS));
        }
        return h;
    }

    @Test
    public void dealerStandsOnSoft17() {
        assertFalse(BlackjackRules.dealerMustHit(hand(Rank.ACE, Rank.SIX)));
    }

    @Test
    public void dealerStandsOnHard17AndHitsOn16() {
        assertFalse(BlackjackRules.dealerMustHit(hand(Rank.TEN, Rank.SEVEN)));
        assertTrue(BlackjackRules.dealerMustHit(hand(Rank.TEN, Rank.SIX)));
    }

    @Test
    public void minimumBetIsTen() {
        assertFalse(BlackjackRules.isValidBet(5));
        assertTrue(BlackjackRules.isValidBet(10));
    }

    @Test
    public void naturalPaysThreeToTwo() {
        assertEquals(15, BlackjackRules.profit(Outcome.BLACKJACK, 10));
        assertEquals(150, BlackjackRules.profit(Outcome.BLACKJACK, 100));
        assertEquals(250, BlackjackRules.totalReturn(Outcome.BLACKJACK, 100));
    }

    @Test
    public void winPaysEvenMoneyAndPushReturnsBet() {
        assertEquals(50, BlackjackRules.profit(Outcome.WIN, 50));
        assertEquals(50, BlackjackRules.totalReturn(Outcome.PUSH, 50));
        assertEquals(0, BlackjackRules.totalReturn(Outcome.LOSE, 50));
    }

    @Test
    public void resolveOutcomes() {
        Hand dealer20 = hand(Rank.KING, Rank.QUEEN);
        assertEquals(Outcome.BLACKJACK, BlackjackRules.resolve(hand(Rank.ACE, Rank.KING), dealer20));
        assertEquals(Outcome.WIN, BlackjackRules.resolve(hand(Rank.TEN, Rank.FIVE, Rank.SIX), dealer20));
        assertEquals(Outcome.PUSH, BlackjackRules.resolve(hand(Rank.TEN, Rank.JACK), dealer20));
        assertEquals(Outcome.LOSE, BlackjackRules.resolve(hand(Rank.TEN, Rank.NINE), dealer20));
        assertEquals(Outcome.LOSE, BlackjackRules.resolve(
                hand(Rank.TEN, Rank.NINE, Rank.FIVE), hand(Rank.TEN, Rank.SIX, Rank.NINE)));
        assertEquals(Outcome.WIN, BlackjackRules.resolve(
                hand(Rank.TEN, Rank.TWO), hand(Rank.TEN, Rank.SIX, Rank.NINE)));
    }

    @Test
    public void naturalBeatsThreeCard21AndNaturalsPush() {
        Hand natural = hand(Rank.ACE, Rank.QUEEN);
        Hand threeCard21 = hand(Rank.SEVEN, Rank.SEVEN, Rank.SEVEN);
        assertEquals(Outcome.LOSE, BlackjackRules.resolve(threeCard21, natural));
        assertEquals(Outcome.BLACKJACK, BlackjackRules.resolve(natural, threeCard21));
        assertEquals(Outcome.PUSH, BlackjackRules.resolve(hand(Rank.ACE, Rank.KING), natural));
    }

    @Test
    public void splitTwentyOneIsNormalWin() {
        Hand split = Hand.fromSplit(Card.of(Rank.ACE, Suit.CLUBS));
        split.add(Card.of(Rank.KING, Suit.CLUBS));
        assertEquals(Outcome.WIN, BlackjackRules.resolve(split, hand(Rank.TEN, Rank.NINE)));
    }

    @Test
    public void splitLimitIsFourHands() {
        Hand pair = hand(Rank.EIGHT, Rank.EIGHT);
        assertTrue(BlackjackRules.canSplit(pair, 3));
        assertFalse(BlackjackRules.canSplit(pair, 4));
        assertFalse(BlackjackRules.canSplit(hand(Rank.EIGHT, Rank.NINE), 1));
    }

    @Test
    public void splitAcesCanKeepHitting() {
        Hand splitAce = Hand.fromSplit(Card.of(Rank.ACE, Suit.SPADES));
        splitAce.add(Card.of(Rank.FIVE, Suit.SPADES));
        assertTrue(BlackjackRules.canHit(splitAce));
        assertFalse(BlackjackRules.canHit(hand(Rank.ACE, Rank.KING)));
        assertFalse(BlackjackRules.canHit(hand(Rank.KING, Rank.QUEEN, Rank.FIVE)));
    }
}
