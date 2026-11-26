package com.example.blackjack21.engine;

/** Fases de una ronda. Cada acción del motor solo es válida en su fase. */
public enum GamePhase {
    WAITING_FOR_BETS,
    DEALING,
    INSURANCE_DECISION,
    PLAYER_TURN,
    DEALER_TURN,
    SETTLEMENT,
    ROUND_FINISHED
}
