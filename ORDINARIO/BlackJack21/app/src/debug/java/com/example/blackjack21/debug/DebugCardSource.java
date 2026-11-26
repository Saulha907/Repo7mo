package com.example.blackjack21.debug;

import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.CardSource;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Suit;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * SOLO DEPURACIÓN. Entrega primero un prefijo fijo de cartas y después
 * continúa con un zapato normal. No existe en las compilaciones de producción.
 */
final class DebugCardSource implements CardSource {

    private final Deque<Card> prefix = new ArrayDeque<>();
    private final CardSource fallback;

    DebugCardSource(CardSource fallback, Rank... ranks) {
        this.fallback = fallback;
        Suit[] suits = Suit.values();
        for (int i = 0; i < ranks.length; i++) {
            prefix.add(Card.of(ranks[i], suits[i % suits.length]));
        }
    }

    @Override
    public void beginRound() {
        fallback.beginRound();
    }

    @Override
    public Card draw() {
        return prefix.isEmpty() ? fallback.draw() : prefix.poll();
    }

    @Override
    public void endRound() {
        fallback.endRound();
    }
}
