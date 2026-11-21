package com.example.blackjack21.model;

/**
 * Carta inmutable. Las 52 combinaciones se comparten, por lo que
 * {@link #of(Rank, Suit)} siempre devuelve la misma instancia.
 */
public final class Card {

    private static final Card[][] CACHE = new Card[Rank.values().length][Suit.values().length];

    static {
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                CACHE[rank.ordinal()][suit.ordinal()] = new Card(rank, suit);
            }
        }
    }

    private final Rank rank;
    private final Suit suit;

    private Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public static Card of(Rank rank, Suit suit) {
        if (rank == null || suit == null) {
            throw new IllegalArgumentException("Rango y palo son obligatorios");
        }
        return CACHE[rank.ordinal()][suit.ordinal()];
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    /** Valor base (As = 1). */
    public int getValue() {
        return rank.getValue();
    }

    public boolean isAce() {
        return rank.isAce();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }
        Card other = (Card) o;
        return rank == other.rank && suit == other.suit;
    }

    @Override
    public int hashCode() {
        return rank.ordinal() * 31 + suit.ordinal();
    }

    @Override
    public String toString() {
        return rank.getSymbol() + " de " + suit.getDisplayName();
    }
}
