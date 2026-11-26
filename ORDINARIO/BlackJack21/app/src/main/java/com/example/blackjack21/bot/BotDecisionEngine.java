package com.example.blackjack21.bot;

import com.example.blackjack21.engine.BlackjackRules;
import com.example.blackjack21.model.Rank;

import java.util.Random;

/**
 * Estrategia básica simplificada (6 barajas, crupier se planta en 17 suave,
 * doblar tras dividir permitido) ajustada por personalidad:
 *
 * <ul>
 *   <li>CONSERVADOR: solo divide ases y ochos, solo dobla con 11 duro, se planta
 *       antes para no pasarse y asegura manos fuertes.</li>
 *   <li>EQUILIBRADO: estrategia básica.</li>
 *   <li>ARRIESGADO: estrategia básica con más dobles y divisiones.</li>
 * </ul>
 *
 * Las decisiones son deterministas para un mismo contexto; el azar solo
 * interviene en el importe de la apuesta.
 */
public final class BotDecisionEngine implements BotStrategy {

    private static final int CHIP_STEP = 5;

    private final BotPersonality personality;

    public BotDecisionEngine(BotPersonality personality) {
        this.personality = personality;
    }

    public BotPersonality getPersonality() {
        return personality;
    }

    // --- Apuestas -------------------------------------------------------------

    @Override
    public int chooseBet(int balance, Random random) {
        if (balance < BlackjackRules.MIN_BET) {
            return 0;
        }
        double min = personality.getMinBetFraction();
        double max = personality.getMaxBetFraction();
        double fraction = min + (max - min) * random.nextDouble();
        int bet = (int) (balance * fraction);
        bet = bet / CHIP_STEP * CHIP_STEP;
        bet = Math.max(BlackjackRules.MIN_BET, Math.min(bet, personality.getMaxBet()));
        return Math.min(bet, balance);
    }

    // --- Seguro ---------------------------------------------------------------

    @Override
    public boolean wantsInsurance(int handTotal, boolean natural) {
        // Solo el conservador protege manos fuertes; los demás siguen la
        // estrategia básica (el seguro tiene valor esperado negativo).
        return personality == BotPersonality.CONSERVATIVE && (natural || handTotal >= 20);
    }

    // --- Turno ------------------------------------------------------------------

    @Override
    public BotAction decide(DecisionContext c) {
        if (!c.canHit) {
            return BotAction.STAND;
        }
        if (c.pair && c.canSplit && shouldSplit(c.pairRank, c.dealerUpValue)) {
            return BotAction.SPLIT;
        }
        BotAction action = c.soft ? softDecision(c.total, c.dealerUpValue)
                : hardDecision(c.total, c.dealerUpValue);
        if (action == BotAction.DOUBLE && !c.canDouble) {
            // Sin poder doblar: con 18+ suave conviene plantarse; si no, pedir.
            return c.soft && c.total >= 18 ? BotAction.STAND : BotAction.HIT;
        }
        return action;
    }

    private boolean shouldSplit(Rank rank, int up) {
        switch (personality) {
            case CONSERVATIVE:
                return rank == Rank.ACE || rank == Rank.EIGHT;
            case AGGRESSIVE:
                if (rank.getValue() == 10) {
                    return up == 5 || up == 6;
                }
                return rank != Rank.FIVE;
            default:
                return basicSplit(rank, up);
        }
    }

    private static boolean basicSplit(Rank rank, int up) {
        switch (rank) {
            case ACE:
            case EIGHT:
                return true;
            case NINE:
                return up <= 9 && up != 7;
            case SEVEN:
            case TWO:
            case THREE:
                return up <= 7;
            case SIX:
                return up <= 6;
            case FOUR:
                return up == 5 || up == 6;
            default:
                return false; // 5 y cartas de valor 10
        }
    }

    private BotAction softDecision(int total, int up) {
        if (personality == BotPersonality.CONSERVATIVE) {
            return total >= 18 ? BotAction.STAND : BotAction.HIT;
        }
        boolean aggressive = personality == BotPersonality.AGGRESSIVE;
        if (total >= 20) {
            return BotAction.STAND;
        }
        if (total == 19) {
            return up == 6 && aggressive ? BotAction.DOUBLE : BotAction.STAND;
        }
        if (total == 18) {
            if (up <= 6) {
                return up >= 3 || aggressive ? BotAction.DOUBLE : BotAction.STAND;
            }
            return up <= 8 ? BotAction.STAND : BotAction.HIT;
        }
        if (total == 17) {
            return up >= 3 && up <= 6 ? BotAction.DOUBLE : BotAction.HIT;
        }
        if (total >= 15) {
            return up >= 4 && up <= 6 ? BotAction.DOUBLE : BotAction.HIT;
        }
        if (total >= 13) {
            int from = aggressive ? 4 : 5;
            return up >= from && up <= 6 ? BotAction.DOUBLE : BotAction.HIT;
        }
        return BotAction.HIT;
    }

    private BotAction hardDecision(int total, int up) {
        switch (personality) {
            case CONSERVATIVE:
                return conservativeHard(total, up);
            case AGGRESSIVE:
                return aggressiveHard(total, up);
            default:
                return basicHard(total, up);
        }
    }

    private static BotAction basicHard(int total, int up) {
        if (total >= 17) {
            return BotAction.STAND;
        }
        if (total >= 13) {
            return up <= 6 ? BotAction.STAND : BotAction.HIT;
        }
        if (total == 12) {
            return up >= 4 && up <= 6 ? BotAction.STAND : BotAction.HIT;
        }
        if (total == 11) {
            return up <= 10 ? BotAction.DOUBLE : BotAction.HIT;
        }
        if (total == 10) {
            return up <= 9 ? BotAction.DOUBLE : BotAction.HIT;
        }
        if (total == 9) {
            return up >= 3 && up <= 6 ? BotAction.DOUBLE : BotAction.HIT;
        }
        return BotAction.HIT;
    }

    private static BotAction conservativeHard(int total, int up) {
        if (total >= 15) {
            return BotAction.STAND;
        }
        if (total >= 12) {
            return up <= 6 ? BotAction.STAND : BotAction.HIT;
        }
        if (total == 11) {
            return up <= 10 ? BotAction.DOUBLE : BotAction.HIT;
        }
        return BotAction.HIT;
    }

    private static BotAction aggressiveHard(int total, int up) {
        if (total >= 17) {
            return BotAction.STAND;
        }
        if (total >= 13) {
            return up <= 6 ? BotAction.STAND : BotAction.HIT;
        }
        if (total == 12) {
            return up >= 4 && up <= 6 ? BotAction.STAND : BotAction.HIT;
        }
        if (total == 11 || total == 10) {
            return BotAction.DOUBLE;
        }
        if (total == 9) {
            return up <= 10 ? BotAction.DOUBLE : BotAction.HIT;
        }
        return BotAction.HIT;
    }
}
