package com.example.blackjack21.engine;

import com.example.blackjack21.model.Hand;

/**
 * Reglas de la mesa: constantes y comprobaciones puras sobre manos.
 * No mantiene estado ni gestiona turnos.
 */
public final class BlackjackRules {

    public static final int BLACKJACK = 21;
    public static final int DEALER_STAND_TOTAL = 17;
    /** El crupier se planta en cualquier 17, incluido el 17 suave. */
    public static final boolean DEALER_HITS_SOFT_17 = false;
    public static final int MIN_BET = 10;
    public static final int DECKS = 6;
    public static final int MAX_HANDS = 4;
    public static final boolean SPLIT_ACES_CAN_HIT = true;
    public static final int BLACKJACK_PAYOUT_NUMERATOR = 3;
    public static final int BLACKJACK_PAYOUT_DENOMINATOR = 2;

    private BlackjackRules() {
    }

    public static boolean isValidBet(int bet) {
        return bet >= MIN_BET;
    }

    /** El crupier pide con menos de 17 y se planta en cualquier 17 (S17). */
    public static boolean dealerMustHit(Hand dealer) {
        int total = dealer.getTotal();
        if (total < DEALER_STAND_TOTAL) {
            return true;
        }
        return DEALER_HITS_SOFT_17 && total == DEALER_STAND_TOTAL && dealer.isSoft();
    }

    /**
     * Si la mano puede recibir otra carta. Las manos de ases divididos también
     * pueden pedir ({@link #SPLIT_ACES_CAN_HIT}).
     */
    public static boolean canHit(Hand hand) {
        if (hand.isBust() || hand.getTotal() >= BLACKJACK) {
            return false;
        }
        return SPLIT_ACES_CAN_HIT || !(hand.isFromSplit() && hand.getCards().get(0).isAce());
    }

    /** Se puede dividir un par si aún no se alcanzó el máximo de manos. */
    public static boolean canSplit(Hand hand, int currentHandCount) {
        return hand.isPair() && currentHandCount < MAX_HANDS;
    }

    /** Compara una mano del jugador con la del crupier. */
    public static Outcome resolve(Hand player, Hand dealer) {
        if (player.isBust()) {
            return Outcome.LOSE;
        }
        boolean playerNatural = player.isNaturalBlackjack();
        boolean dealerNatural = dealer.isNaturalBlackjack();
        if (playerNatural && dealerNatural) {
            return Outcome.PUSH;
        }
        if (playerNatural) {
            return Outcome.BLACKJACK;
        }
        if (dealerNatural) {
            return Outcome.LOSE;
        }
        if (dealer.isBust()) {
            return Outcome.WIN;
        }
        int diff = player.getTotal() - dealer.getTotal();
        if (diff > 0) {
            return Outcome.WIN;
        }
        return diff < 0 ? Outcome.LOSE : Outcome.PUSH;
    }

    /**
     * Ganancia neta (sin incluir la apuesta). El 3:2 se redondea hacia abajo
     * a fichas enteras, como en los casinos (p. ej. 25 → 37).
     */
    public static int profit(Outcome outcome, int bet) {
        switch (outcome) {
            case BLACKJACK:
                return bet * BLACKJACK_PAYOUT_NUMERATOR / BLACKJACK_PAYOUT_DENOMINATOR;
            case WIN:
                return bet;
            case PUSH:
                return 0;
            default:
                return -bet;
        }
    }

    /** Cantidad total devuelta al jugador (apuesta + ganancia). */
    public static int totalReturn(Outcome outcome, int bet) {
        return bet + profit(outcome, bet);
    }
}
