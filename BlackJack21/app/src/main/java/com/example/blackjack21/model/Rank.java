package com.example.blackjack21.model;

/**
 * Rangos de la baraja. El As vale 1 aquí; {@link Hand} decide si cuenta como 11.
 */
public enum Rank {
    ACE("A", 1),
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 10),
    QUEEN("Q", 10),
    KING("K", 10);

    private final String symbol;
    private final int value;

    Rank(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Valor base: As = 1, figuras = 10. */
    public int getValue() {
        return value;
    }

    public boolean isAce() {
        return this == ACE;
    }

    public boolean isFaceCard() {
        return this == JACK || this == QUEEN || this == KING;
    }
}
