package com.example.blackjack21.engine;

/**
 * Cálculo de pagos en fichas enteras (las fracciones se redondean hacia abajo).
 */
public final class PayoutCalculator {

    public static final int INSURANCE_PAYOUT_RATIO = 2;

    private PayoutCalculator() {
    }

    /** Fichas devueltas por la apuesta principal (apuesta + ganancia). */
    public static int mainReturn(Outcome outcome, int bet) {
        return BlackjackRules.totalReturn(outcome, bet);
    }

    /** Fichas devueltas por el seguro: 2:1 si el crupier tiene blackjack, 0 si no. */
    public static int insuranceReturn(boolean dealerHasBlackjack, int insuranceBet) {
        return dealerHasBlackjack ? insuranceBet * (INSURANCE_PAYOUT_RATIO + 1) : 0;
    }

    /** Seguro máximo: la mitad de la apuesta original, redondeada hacia abajo. */
    public static int maxInsurance(int originalBet) {
        return originalBet / 2;
    }
}
