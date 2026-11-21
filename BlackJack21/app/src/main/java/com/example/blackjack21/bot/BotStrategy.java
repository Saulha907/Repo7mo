package com.example.blackjack21.bot;

import java.util.Random;

/** Contrato de decisión de un bot. Siempre devuelve acciones legales. */
public interface BotStrategy {

    /** Apuesta para la ronda; 0 si no puede cubrir el mínimo y debe quedarse fuera. */
    int chooseBet(int balance, Random random);

    BotAction decide(DecisionContext context);

    /**
     * Decide si contratar seguro.
     *
     * @param handTotal total de su mano inicial.
     * @param natural   si su mano es blackjack natural.
     */
    boolean wantsInsurance(int handTotal, boolean natural);
}
