package com.example.blackjack21.engine;

import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.Hand;

import java.util.List;

/**
 * Una mano de un jugador con su apuesta y resultado. Las mutaciones solo
 * son posibles desde el motor (mismo paquete); fuera es de solo lectura.
 */
public final class HandState {

    private final int id;
    private Hand hand;
    private int bet;
    private boolean doubled;
    private boolean finished;
    private boolean settled;
    private Outcome outcome;
    private int payout;

    HandState(int id, Hand hand, int bet) {
        this.id = id;
        this.hand = hand;
        this.bet = bet;
    }

    // --- Lectura pública ---------------------------------------------------

    /** Identificador estable dentro de la ronda (no cambia al dividir). */
    public int getId() {
        return id;
    }

    public List<Card> getCards() {
        return hand.getCards();
    }

    public int getTotal() {
        return hand.getTotal();
    }

    public boolean isSoft() {
        return hand.isSoft();
    }

    public boolean isBust() {
        return hand.isBust();
    }

    public boolean isNaturalBlackjack() {
        return hand.isNaturalBlackjack();
    }

    public boolean isFromSplit() {
        return hand.isFromSplit();
    }

    public int getBet() {
        return bet;
    }

    public boolean isDoubled() {
        return doubled;
    }

    /** La mano ya no admite acciones. */
    public boolean isFinished() {
        return finished;
    }

    public boolean isSettled() {
        return settled;
    }

    /** Resultado tras la liquidación; null antes. */
    public Outcome getOutcome() {
        return outcome;
    }

    /** Fichas devueltas al jugador por esta mano (apuesta + ganancia). */
    public int getPayout() {
        return payout;
    }

    /** Ganancia neta de la mano tras liquidar (negativa si pierde). */
    public int getNet() {
        return settled ? payout - bet : 0;
    }

    // --- Mutaciones del motor ---------------------------------------------

    Hand hand() {
        return hand;
    }

    void replaceHand(Hand newHand) {
        this.hand = newHand;
    }

    void doubleBet() {
        bet *= 2;
        doubled = true;
    }

    void finish() {
        finished = true;
    }

    void settle(Outcome result, int returned) {
        outcome = result;
        payout = returned;
        settled = true;
        finished = true;
    }
}
