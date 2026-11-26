package com.example.blackjack21.model;

/** Palos de la baraja francesa. */
public enum Suit {
    HEARTS("Corazones", true),
    DIAMONDS("Diamantes", true),
    CLUBS("Tréboles", false),
    SPADES("Picas", false);

    private final String displayName;
    private final boolean red;

    Suit(String displayName, boolean red) {
        this.displayName = displayName;
        this.red = red;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRed() {
        return red;
    }
}
