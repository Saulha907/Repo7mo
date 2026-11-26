package com.example.blackjack21.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HandTest {

    private static Card c(Rank rank) {
        return Card.of(rank, Suit.SPADES);
    }

    private static Hand hand(Rank... ranks) {
        Hand h = new Hand();
        for (Rank r : ranks) {
            h.add(c(r));
        }
        return h;
    }

    @Test
    public void aceKingIsNaturalBlackjack() {
        Hand h = hand(Rank.ACE, Rank.KING);
        assertEquals(21, h.getTotal());
        assertTrue(h.isNaturalBlackjack());
        assertTrue(h.isTwentyOne());
    }

    @Test
    public void aceNineAceIs21() {
        Hand h = hand(Rank.ACE, Rank.NINE, Rank.ACE);
        assertEquals(21, h.getTotal());
        assertTrue(h.isTwentyOne());
        assertTrue(h.isSoft());
    }

    @Test
    public void aceSixIsSoft17() {
        Hand h = hand(Rank.ACE, Rank.SIX);
        assertEquals(17, h.getTotal());
        assertTrue(h.isSoft());
    }

    @Test
    public void aceSixTenIsHard17() {
        Hand h = hand(Rank.ACE, Rank.SIX, Rank.TEN);
        assertEquals(17, h.getTotal());
        assertFalse(h.isSoft());
    }

    @Test
    public void overTwentyOneIsBust() {
        Hand h = hand(Rank.KING, Rank.QUEEN, Rank.TWO);
        assertEquals(22, h.getTotal());
        assertTrue(h.isBust());
    }

    @Test
    public void threeCard21IsNotNatural() {
        Hand h = hand(Rank.SEVEN, Rank.SEVEN, Rank.SEVEN);
        assertTrue(h.isTwentyOne());
        assertFalse(h.isNaturalBlackjack());
    }

    @Test
    public void twentyOneAfterSplitIsNotNatural() {
        Hand h = Hand.fromSplit(c(Rank.ACE));
        h.add(c(Rank.KING));
        assertEquals(2, h.size());
        assertTrue(h.isTwentyOne());
        assertTrue(h.isFromSplit());
        assertFalse(h.isNaturalBlackjack());
    }

    @Test
    public void multipleAcesCountOnlyOneAsEleven() {
        Hand h = hand(Rank.ACE, Rank.ACE, Rank.ACE, Rank.ACE);
        assertEquals(14, h.getTotal());
        assertTrue(h.isSoft());
        h.add(c(Rank.KING));
        assertEquals(14, h.getTotal());
        assertFalse(h.isSoft());
    }

    @Test
    public void pairDetection() {
        assertTrue(hand(Rank.EIGHT, Rank.EIGHT).isPair());
        assertTrue(hand(Rank.ACE, Rank.ACE).isPair());
        assertFalse(hand(Rank.KING, Rank.QUEEN).isPair());
        assertFalse(hand(Rank.EIGHT, Rank.EIGHT, Rank.TWO).isPair());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cardsListIsReadOnly() {
        hand(Rank.TWO).getCards().clear();
    }

    @Test
    public void cardsAreSharedImmutableValues() {
        assertSame(Card.of(Rank.ACE, Suit.HEARTS), Card.of(Rank.ACE, Suit.HEARTS));
        assertNotSame(Card.of(Rank.ACE, Suit.HEARTS), Card.of(Rank.ACE, Suit.CLUBS));
        assertEquals(10, Card.of(Rank.QUEEN, Suit.DIAMONDS).getValue());
        assertEquals(1, Card.of(Rank.ACE, Suit.DIAMONDS).getValue());
    }
}
