package com.example.blackjack21.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado global de la partida: fase, jugadores (en orden de asiento) y ronda.
 */
public final class GameState {

    private GamePhase phase = GamePhase.WAITING_FOR_BETS;
    private final List<PlayerState> players = new ArrayList<>();
    private RoundState round = new RoundState(1);

    public GamePhase getPhase() {
        return phase;
    }

    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public RoundState getRound() {
        return round;
    }

    /** Jugadores con apuesta en la ronda, en orden de asiento. */
    public List<PlayerState> getActivePlayers() {
        List<PlayerState> active = new ArrayList<>();
        for (PlayerState p : players) {
            if (p.isInRound()) {
                active.add(p);
            }
        }
        return active;
    }

    void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    void addPlayer(PlayerState player) {
        players.add(player);
    }

    void setRoundNumber(int number) {
        round = new RoundState(number);
    }

    void nextRound() {
        round = new RoundState(round.getNumber() + 1);
    }
}
