package com.example.blackjack21.data;

import com.example.blackjack21.bot.BotPersonality;
import com.example.blackjack21.model.ShoeState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Partida guardada (Java puro). Consta de un punto de control tomado al
 * empezar la ronda (saldos, personalidades, zapato y generadores) y del
 * diario de acciones realizadas desde entonces. Restaurar = recrear el punto
 * de control y repetir el diario: el motor vuelve exactamente al mismo estado,
 * con las mismas cartas futuras y sin cobrar ni pagar nada dos veces.
 */
public final class GameSaveData {

    public static final int VERSION = 1;

    /** Acciones del diario. El reparto lleva la apuesta: "D:25". */
    public static final String DEAL = "D";
    public static final String HIT = "H";
    public static final String STAND = "T";
    public static final String DOUBLE = "X";
    public static final String SPLIT = "P";
    public static final String INSURANCE = "I";
    public static final String DECLINE = "N";
    public static final String STEP = "S";

    /** Asiento en orden de turno. {@code personality} es null para el humano. */
    public static final class Seat {
        public final String id;
        public final String name;
        public final boolean human;
        public final int balance;
        public final BotPersonality personality;

        public Seat(String id, String name, boolean human, int balance, BotPersonality personality) {
            this.id = id;
            this.name = name;
            this.human = human;
            this.balance = balance;
            this.personality = personality;
        }
    }

    public final int gameNumber;
    public final int roundNumber;
    public final int lastBet;
    public final int pendingBet;
    public final List<Seat> seats;
    public final ShoeState shoe;
    /** Estado del generador de la partida (apuestas de los bots). */
    public final byte[] random;
    public final List<String> actions;

    public GameSaveData(int gameNumber, int roundNumber, int lastBet, int pendingBet,
                        List<Seat> seats, ShoeState shoe, byte[] random, List<String> actions) {
        this.gameNumber = gameNumber;
        this.roundNumber = roundNumber;
        this.lastBet = lastBet;
        this.pendingBet = pendingBet;
        this.seats = Collections.unmodifiableList(new ArrayList<>(seats));
        this.shoe = shoe;
        this.random = random.clone();
        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
    }

    /** Misma partida con otro diario y otra apuesta pendiente. */
    public GameSaveData withProgress(List<String> newActions, int newPendingBet) {
        return new GameSaveData(gameNumber, roundNumber, lastBet, newPendingBet, seats, shoe,
                random, newActions);
    }

    /** Comprobaciones de coherencia; lanza IllegalArgumentException si algo no cuadra. */
    public void validate() {
        require(gameNumber >= 1 && roundNumber >= 1, "numeración");
        require(lastBet >= 0 && pendingBet >= 0, "apuestas");
        require(shoe != null && random.length > 0, "zapato");
        require(!seats.isEmpty(), "asientos");
        int humans = 0;
        List<String> ids = new ArrayList<>();
        for (Seat seat : seats) {
            require(seat.id != null && seat.name != null && !ids.contains(seat.id), "asiento");
            require(seat.balance >= 0, "saldo");
            require(seat.human == (seat.personality == null), "personalidad");
            ids.add(seat.id);
            if (seat.human) {
                humans++;
            }
        }
        require(humans == 1, "jugador humano");
        for (String a : actions) {
            require(a != null && !a.isEmpty(), "acción");
        }
    }

    public Seat humanSeat() {
        for (Seat seat : seats) {
            if (seat.human) {
                return seat;
            }
        }
        return null;
    }

    private static void require(boolean ok, String what) {
        if (!ok) {
            throw new IllegalArgumentException("Partida guardada inválida: " + what);
        }
    }
}
