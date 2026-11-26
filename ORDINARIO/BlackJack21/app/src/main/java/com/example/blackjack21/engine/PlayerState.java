package com.example.blackjack21.engine;

import com.example.blackjack21.model.Hand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Jugador sentado a la mesa: saldo, manos de la ronda y seguro.
 * El saldo solo cambia a través de {@link BetManager}.
 */
public final class PlayerState {

    private final String id;
    private final String name;
    private int balance;
    private final List<HandState> hands = new ArrayList<>();
    private int nextHandId;
    private int initialBet;
    private int insuranceBet;
    private boolean insuranceDecided;
    private Boolean insuranceWon;

    PlayerState(String id, String name, int balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBalance() {
        return balance;
    }

    /** Participa en la ronda actual (ha apostado). */
    public boolean isInRound() {
        return !hands.isEmpty();
    }

    public List<HandState> getHands() {
        return Collections.unmodifiableList(hands);
    }

    /** Apuesta inicial de la ronda (antes de dividir o doblar). */
    public int getInitialBet() {
        return initialBet;
    }

    public int getInsuranceBet() {
        return insuranceBet;
    }

    public boolean hasDecidedInsurance() {
        return insuranceDecided;
    }

    /** null si no hubo seguro o aún no se resolvió. */
    public Boolean getInsuranceWon() {
        return insuranceWon;
    }

    // --- Mutaciones del motor ---------------------------------------------

    void addBalance(int amount) {
        balance += amount;
    }

    void removeBalance(int amount) {
        if (amount > balance) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        balance -= amount;
    }

    int peekNextHandId() {
        return nextHandId;
    }

    HandState openHand(Hand hand, int bet) {
        if (hands.isEmpty()) {
            initialBet = bet;
        }
        HandState state = new HandState(nextHandId++, hand, bet);
        hands.add(state);
        return state;
    }

    HandState insertHandAfter(int index, Hand hand, int bet) {
        HandState state = new HandState(nextHandId++, hand, bet);
        hands.add(index + 1, state);
        return state;
    }

    void setInsurance(int amount) {
        insuranceBet = amount;
        insuranceDecided = true;
    }

    void resolveInsurance(boolean won) {
        if (insuranceBet > 0) {
            insuranceWon = won;
        }
    }

    void resetRound() {
        hands.clear();
        nextHandId = 0;
        initialBet = 0;
        insuranceBet = 0;
        insuranceDecided = false;
        insuranceWon = null;
    }
}
