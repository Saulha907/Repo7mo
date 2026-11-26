package com.example.blackjack21.game;

import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.CardSource;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Suit;

import java.util.ArrayDeque;
import java.util.Deque;

/** Mazo de prueba con orden fijo. */
final class GameTestSource implements CardSource {

    private final Deque<Card> cards = new ArrayDeque<>();

    GameTestSource(Rank... ranks) {
        Suit[] suits = Suit.values();
        for (int i = 0; i < ranks.length; i++) {
            cards.add(Card.of(ranks[i], suits[i % suits.length]));
        }
    }

    @Override
    public void beginRound() {
    }

    @Override
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Mazo de prueba agotado");
        }
        return cards.poll();
    }

    @Override
    public void endRound() {
    }
}
