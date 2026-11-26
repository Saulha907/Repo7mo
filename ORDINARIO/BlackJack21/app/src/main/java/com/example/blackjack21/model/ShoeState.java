package com.example.blackjack21.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado guardable de un {@link Shoe} entre rondas. {@code cards} está en orden
 * de extracción (el índice 0 es la próxima carta) y {@code random} es el
 * estado serializado del generador que barajará el siguiente zapato.
 */
public final class ShoeState {

    public final int decks;
    public final int cutCardRemaining;
    public final List<Card> cards;
    public final byte[] random;

    public ShoeState(int decks, int cutCardRemaining, List<Card> cards, byte[] random) {
        if (cards == null || random == null) {
            throw new IllegalArgumentException("Estado de zapato incompleto");
        }
        this.decks = decks;
        this.cutCardRemaining = cutCardRemaining;
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
        this.random = random.clone();
    }
}
