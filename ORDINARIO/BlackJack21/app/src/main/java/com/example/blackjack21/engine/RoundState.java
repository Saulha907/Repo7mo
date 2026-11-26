package com.example.blackjack21.engine;

import com.example.blackjack21.model.Hand;

/**
 * Estado interno de la ronda en curso: mano del crupier y posición del turno.
 */
public final class RoundState {

    private final int number;
    private final Hand dealer = new Hand();
    private boolean holeRevealed;
    private boolean settled;
    private int playerIndex;
    private int handIndex;

    RoundState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public boolean isHoleRevealed() {
        return holeRevealed;
    }

    public boolean isSettled() {
        return settled;
    }

    Hand dealer() {
        return dealer;
    }

    void revealHole() {
        holeRevealed = true;
    }

    void markSettled() {
        settled = true;
    }

    int playerIndex() {
        return playerIndex;
    }

    int handIndex() {
        return handIndex;
    }

    void setTurn(int player, int hand) {
        playerIndex = player;
        handIndex = hand;
    }
}
