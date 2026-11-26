package com.example.blackjack21.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShoeTest {

    private static Shoe newShoe() {
        return new Shoe(Shoe.DEFAULT_DECKS, new Random(42));
    }

    @Test
    public void shoeHas312Cards() {
        Shoe shoe = newShoe();
        assertEquals(312, shoe.remaining());
        assertEquals(312, shoe.capacity());
    }

    @Test
    public void shoeHasSixCopiesOfEachCard() {
        Shoe shoe = newShoe();
        Map<Card, Integer> counts = new HashMap<>();
        while (shoe.remaining() > 0) {
            Card c = shoe.draw();
            Integer n = counts.get(c);
            counts.put(c, n == null ? 1 : n + 1);
        }
        assertEquals(52, counts.size());
        for (Integer n : counts.values()) {
            assertEquals(6, n.intValue());
        }
    }

    @Test
    public void drawIsWithoutReplacement() {
        Shoe shoe = newShoe();
        Card first = shoe.draw();
        assertEquals(311, shoe.remaining());
        int sameAsFirst = 0;
        while (shoe.remaining() > 0) {
            if (shoe.draw().equals(first)) {
                sameAsFirst++;
            }
        }
        assertEquals(5, sameAsFirst);
    }

    @Test(expected = IllegalStateException.class)
    public void drawingFromEmptyShoeFails() {
        Shoe shoe = new Shoe(1, new Random(1));
        for (int i = 0; i < 52; i++) {
            shoe.draw();
        }
        shoe.draw();
    }

    @Test
    public void sameSeedGivesSameOrder() {
        Shoe a = new Shoe(6, new Random(7));
        Shoe b = new Shoe(6, new Random(7));
        for (int i = 0; i < 312; i++) {
            assertEquals(a.draw(), b.draw());
        }
    }

    @Test
    public void shoeIsShuffled() {
        Shoe shoe = newShoe();
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            drawn.add(shoe.draw());
        }
        List<Card> ordered = new ArrayList<>();
        for (Suit s : Suit.values()) {
            for (Rank r : Rank.values()) {
                ordered.add(Card.of(r, s));
            }
        }
        assertFalse(drawn.equals(ordered));
    }

    @Test
    public void shoeIsNotRebuiltDuringRound() {
        Shoe shoe = newShoe();
        shoe.beginRound();
        while (!shoe.needsShuffle()) {
            shoe.draw();
        }
        int remaining = shoe.remaining();

        try {
            shoe.reshuffle();
            fail("No debe barajar durante una ronda");
        } catch (IllegalStateException expected) {
            // correcto
        }
        shoe.draw();
        assertEquals(remaining - 1, shoe.remaining());
        assertTrue(shoe.isRoundInProgress());

        shoe.endRound();
        shoe.beginRound();
        assertEquals(312, shoe.remaining());
    }

    @Test
    public void shoeKeepsCardsBetweenRoundsBeforeCutCard() {
        Shoe shoe = newShoe();
        shoe.beginRound();
        for (int i = 0; i < 10; i++) {
            shoe.draw();
        }
        shoe.endRound();
        shoe.beginRound();
        assertEquals(302, shoe.remaining());
    }

    @Test(expected = IllegalStateException.class)
    public void cannotBeginTwoRounds() {
        Shoe shoe = newShoe();
        shoe.beginRound();
        shoe.beginRound();
    }
}
