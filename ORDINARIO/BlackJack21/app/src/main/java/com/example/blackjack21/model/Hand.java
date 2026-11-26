package com.example.blackjack21.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mano de Blackjack. Calcula totales considerando el As como 1 u 11.
 *
 * <p>Una mano creada a partir de una división ({@link #fromSplit(Card)}) nunca
 * cuenta como blackjack natural, aunque sume 21 con dos cartas.</p>
 */
public final class Hand {

    private static final int LIMIT = 21;
    private static final int ACE_BONUS = 10;

    private final List<Card> cards = new ArrayList<>();
    private final boolean fromSplit;

    public Hand() {
        this(false);
    }

    private Hand(boolean fromSplit) {
        this.fromSplit = fromSplit;
    }

    /** Mano nueva tras dividir, iniciada con la carta separada. */
    public static Hand fromSplit(Card first) {
        Hand hand = new Hand(true);
        hand.add(first);
        return hand;
    }

    public void add(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Carta nula");
        }
        cards.add(card);
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public boolean isFromSplit() {
        return fromSplit;
    }

    /** Total contando todos los ases como 1. */
    public int getHardTotal() {
        int total = 0;
        for (Card card : cards) {
            total += card.getValue();
        }
        return total;
    }

    private boolean hasAce() {
        for (Card card : cards) {
            if (card.isAce()) {
                return true;
            }
        }
        return false;
    }

    /** Total óptimo: un As vale 11 si no se pasa de 21. */
    public int getTotal() {
        int hard = getHardTotal();
        return isSoftFor(hard) ? hard + ACE_BONUS : hard;
    }

    /** Mano suave: un As está contando como 11. */
    public boolean isSoft() {
        return isSoftFor(getHardTotal());
    }

    private boolean isSoftFor(int hard) {
        return hard + ACE_BONUS <= LIMIT && hasAce();
    }

    public boolean isBust() {
        return getHardTotal() > LIMIT;
    }

    public boolean isTwentyOne() {
        return getTotal() == LIMIT;
    }

    /** As + carta de valor 10 en las dos cartas iniciales, sin dividir. */
    public boolean isNaturalBlackjack() {
        return !fromSplit && cards.size() == 2 && getTotal() == LIMIT;
    }

    /** Dos cartas del mismo rango (p. ej. 8-8, K-K; no K-Q). */
    public boolean isPair() {
        return cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank();
    }

    @Override
    public String toString() {
        return cards + " = " + getTotal() + (isSoft() ? " (suave)" : "");
    }
}
