package com.example.blackjack21.engine;

/**
 * Notificación de algo que acaba de ocurrir en la mesa, en el orden real.
 * Solo sirve para presentar la partida: no transporta el valor de ninguna
 * carta (la carta oculta del crupier nunca se filtra por aquí).
 */
public final class GameEvent {

    public enum Type {
        BET_PLACED,
        CARD_DEALT,
        DEALER_CARD,
        HOLE_REVEALED,
        SPLIT,
        DOUBLED,
        INSURANCE_TAKEN,
        ROUND_SETTLED
    }

    public final Type type;
    /** Jugador afectado; null para el crupier o eventos de mesa. */
    public final String playerId;
    /** Posición de la mano en la lista del jugador (0 para el crupier). */
    public final int handIndex;
    /** Posición de la carta dentro de la mano; -1 si no aplica. */
    public final int cardIndex;
    /** La carta se repartió boca abajo (carta oculta del crupier). */
    public final boolean faceDown;

    private GameEvent(Type type, String playerId, int handIndex, int cardIndex, boolean faceDown) {
        this.type = type;
        this.playerId = playerId;
        this.handIndex = handIndex;
        this.cardIndex = cardIndex;
        this.faceDown = faceDown;
    }

    static GameEvent player(Type type, String playerId, int handIndex, int cardIndex) {
        return new GameEvent(type, playerId, handIndex, cardIndex, false);
    }

    static GameEvent dealerCard(int cardIndex, boolean faceDown) {
        return new GameEvent(Type.DEALER_CARD, null, 0, cardIndex, faceDown);
    }

    static GameEvent table(Type type) {
        return new GameEvent(type, null, 0, -1, false);
    }

    @Override
    public String toString() {
        return type + (playerId != null ? "(" + playerId + "," + handIndex + "," + cardIndex + ")"
                : "(dealer," + cardIndex + (faceDown ? ",oculta" : "") + ")");
    }
}
