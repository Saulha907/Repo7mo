package com.example.blackjack21.bot;

import com.example.blackjack21.engine.PlayerState;

/**
 * Jugador automático. Su saldo y manos viven en el {@link PlayerState} del
 * motor (solo lectura aquí); el bot únicamente decide.
 */
public final class BotPlayer {

    /** Estado del bot en la ronda actual. */
    public enum Status {
        WAITING,
        BETTING,
        SITTING_OUT,
        PLAYING,
        DONE
    }

    private final PlayerState player;
    private BotPersonality personality;
    private BotStrategy strategy;
    private Status status = Status.WAITING;

    BotPlayer(PlayerState player, BotPersonality personality) {
        this.player = player;
        setPersonality(personality);
    }

    public String getId() {
        return player.getId();
    }

    public String getName() {
        return player.getName();
    }

    public int getBalance() {
        return player.getBalance();
    }

    /** Apuesta inicial de la ronda actual (0 si no participa). */
    public int getCurrentBet() {
        return player.getInitialBet();
    }

    public BotPersonality getPersonality() {
        return personality;
    }

    public BotStrategy getStrategy() {
        return strategy;
    }

    public Status getStatus() {
        return status;
    }

    public PlayerState getPlayerState() {
        return player;
    }

    void setPersonality(BotPersonality personality) {
        this.personality = personality;
        this.strategy = new BotDecisionEngine(personality);
    }

    void setStatus(Status status) {
        this.status = status;
    }
}
