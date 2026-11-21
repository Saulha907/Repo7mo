package com.example.blackjack21.engine;

/** Resultado de una mano del jugador frente al crupier. */
public enum Outcome {
    /** Blackjack natural del jugador (paga 3:2). */
    BLACKJACK,
    /** Victoria normal (paga 1:1). */
    WIN,
    /** Empate: se devuelve la apuesta. */
    PUSH,
    LOSE
}
