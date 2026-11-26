package com.example.blackjack21.bot;

/**
 * Perfiles de juego. Los porcentajes definen la apuesta respecto al saldo
 * y el tope absoluto por ronda.
 */
public enum BotPersonality {
    CONSERVATIVE("Conservador", 0.02, 0.03, 50),
    BALANCED("Equilibrado", 0.04, 0.06, 200),
    AGGRESSIVE("Arriesgado", 0.10, 0.15, 500);

    private final String displayName;
    private final double minBetFraction;
    private final double maxBetFraction;
    private final int maxBet;

    BotPersonality(String displayName, double minBetFraction, double maxBetFraction, int maxBet) {
        this.displayName = displayName;
        this.minBetFraction = minBetFraction;
        this.maxBetFraction = maxBetFraction;
        this.maxBet = maxBet;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinBetFraction() {
        return minBetFraction;
    }

    public double getMaxBetFraction() {
        return maxBetFraction;
    }

    public int getMaxBet() {
        return maxBet;
    }
}
