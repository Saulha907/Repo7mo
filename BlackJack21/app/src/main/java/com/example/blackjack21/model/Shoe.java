package com.example.blackjack21.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Zapato de varias barajas con carta de corte.
 *
 * <p>Ciclo de uso: {@link #beginRound()} → {@link #draw()}… → {@link #endRound()}.
 * Si se alcanzó la carta de corte, el zapato se rehace al iniciar la siguiente
 * ronda, nunca a mitad de una. Las cartas restantes no se exponen: solo su número.</p>
 */
public final class Shoe implements CardSource {

    public static final int DEFAULT_DECKS = 6;
    public static final int CARDS_PER_DECK = 52;
    /** Fracción del zapato que se reparte antes de la carta de corte. */
    public static final double DEFAULT_PENETRATION = 0.75;

    private final int decks;
    private final Random random;
    private final int cutCardRemaining;
    private final List<Card> cards;
    private boolean roundInProgress;

    public Shoe() {
        this(DEFAULT_DECKS, new Random());
    }

    public Shoe(int decks, Random random) {
        this(decks, random, DEFAULT_PENETRATION);
    }

    public Shoe(int decks, Random random, double penetration) {
        if (decks < 1) {
            throw new IllegalArgumentException("Se necesita al menos una baraja");
        }
        if (penetration <= 0 || penetration > 1) {
            throw new IllegalArgumentException("Penetración fuera de rango: " + penetration);
        }
        if (random == null) {
            throw new IllegalArgumentException("Random es obligatorio");
        }
        this.decks = decks;
        this.random = random;
        int capacity = decks * CARDS_PER_DECK;
        this.cutCardRemaining = (int) Math.round(capacity * (1 - penetration));
        this.cards = new ArrayList<>(capacity);
        rebuild();
    }

    /** Zapato restaurado: mismas cartas pendientes, mismo orden y mismo generador. */
    private Shoe(ShoeState state) {
        if (state.decks < 1 || state.cutCardRemaining < 0
                || state.cutCardRemaining > state.decks * CARDS_PER_DECK
                || state.cards.size() > state.decks * CARDS_PER_DECK) {
            throw new IllegalArgumentException("Estado de zapato inválido");
        }
        this.decks = state.decks;
        this.random = RandomCodec.decode(state.random);
        this.cutCardRemaining = state.cutCardRemaining;
        this.cards = new ArrayList<>(decks * CARDS_PER_DECK);
        // Internamente se extrae desde el final de la lista.
        for (int i = state.cards.size() - 1; i >= 0; i--) {
            cards.add(state.cards.get(i));
        }
    }

    public static Shoe restore(ShoeState state) {
        return new Shoe(state);
    }

    /**
     * Copia del estado entre rondas (cartas pendientes en orden de extracción y
     * generador). Restaurarla reproduce exactamente las mismas cartas futuras.
     */
    public ShoeState saveState() {
        if (roundInProgress) {
            throw new IllegalStateException("No se guarda el zapato a mitad de ronda");
        }
        List<Card> order = new ArrayList<>(cards.size());
        for (int i = cards.size() - 1; i >= 0; i--) {
            order.add(cards.get(i));
        }
        return new ShoeState(decks, cutCardRemaining, order, RandomCodec.encode(random));
    }

    /** Inicia una ronda; rehace el zapato si se pasó la carta de corte. */
    @Override
    public void beginRound() {
        if (roundInProgress) {
            throw new IllegalStateException("Ya hay una ronda en curso");
        }
        if (needsShuffle()) {
            rebuild();
        }
        roundInProgress = true;
    }

    @Override
    public void endRound() {
        roundInProgress = false;
    }

    public boolean isRoundInProgress() {
        return roundInProgress;
    }

    /**
     * Extrae una carta sin reemplazo.
     *
     * @throws IllegalStateException si el zapato está vacío (no se rehace en mitad de ronda).
     */
    @Override
    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("El zapato está vacío");
        }
        return cards.remove(cards.size() - 1);
    }

    /** Rehace y baraja el zapato manualmente. Prohibido durante una ronda. */
    public void reshuffle() {
        if (roundInProgress) {
            throw new IllegalStateException("No se puede barajar durante una ronda");
        }
        rebuild();
    }

    /** Se alcanzó la carta de corte: habrá que barajar al empezar la próxima ronda. */
    public boolean needsShuffle() {
        return cards.size() <= cutCardRemaining;
    }

    public int remaining() {
        return cards.size();
    }

    public int capacity() {
        return decks * CARDS_PER_DECK;
    }

    public int getDecks() {
        return decks;
    }

    private void rebuild() {
        cards.clear();
        for (int d = 0; d < decks; d++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(Card.of(rank, suit));
                }
            }
        }
        Collections.shuffle(cards, random);
    }
}
