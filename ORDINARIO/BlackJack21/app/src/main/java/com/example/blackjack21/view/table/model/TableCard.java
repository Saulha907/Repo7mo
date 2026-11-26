package com.example.blackjack21.view.table.model;

/**
 * Carta tal como se muestra en la mesa (modelo de presentación, sin reglas).
 * El palo usa las constantes de {@link com.example.blackjack21.view.paint.SuitPaths}.
 */
public final class TableCard {

    public final String rank;
    public final int suit;
    public final boolean faceDown;

    private TableCard(String rank, int suit, boolean faceDown) {
        this.rank = rank;
        this.suit = suit;
        this.faceDown = faceDown;
    }

    public static TableCard up(String rank, int suit) {
        return new TableCard(rank, suit, false);
    }

    public static TableCard down() {
        return new TableCard("", 0, true);
    }

    public boolean isCourt() {
        return "J".equals(rank) || "Q".equals(rank) || "K".equals(rank);
    }
}
