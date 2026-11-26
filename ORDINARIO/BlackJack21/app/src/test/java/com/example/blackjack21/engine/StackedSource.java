package com.example.blackjack21.engine;

import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.CardSource;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Suit;

import java.util.ArrayDeque;
import java.util.Deque;

/** Mazo preparado: entrega las cartas en el orden indicado. */
final class StackedSource implements CardSource {

    private final Deque<Card> cards = new ArrayDeque<>();
    int drawn;
    int roundsBegun;
    int roundsEnded;

    StackedSource(Rank... ranks) {
        Suit[] suits = Suit.values();
        for (int i = 0; i < ranks.length; i++) {
            cards.add(Card.of(ranks[i], suits[i % suits.length]));
        }
    }

    @Override
    public void beginRound() {
        roundsBegun++;
    }

    @Override
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Mazo de prueba agotado");
        }
        drawn++;
        return cards.poll();
    }

    @Override
    public void endRound() {
        roundsEnded++;
    }

    int remaining() {
        return cards.size();
    }
}
