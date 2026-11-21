package com.example.blackjack21.view.table.model;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Datos visibles de un participante. El controlador de partida produce estas
 * instancias a partir del motor; la vista solo las dibuja.
 */
public final class SeatInfo {

    public final String name;
    public final int balance;
    /** Apuesta total en la mesa (o apuesta pendiente antes de repartir). */
    public final int bet;
    public final List<HandView> hands;
    public final boolean human;
    /** Tiene el turno o una decisión pendiente. */
    public final boolean active;
    /** No participa en la ronda en curso. */
    public final boolean sittingOut;
    /** No tiene fichas para la apuesta mínima: asiento inactivo. */
    public final boolean outOfChips;
    /** Resultado neto de la ronda ya liquidada; null si no aplica. */
    @Nullable
    public final Integer net;
    /** Apuesta de seguro en la mesa (0 si no hay). */
    public final int insuranceBet;

    public SeatInfo(String name, int balance, int bet, List<HandView> hands, boolean human,
                    boolean active, boolean sittingOut, boolean outOfChips, @Nullable Integer net) {
        this(name, balance, bet, hands, human, active, sittingOut, outOfChips, net, 0);
    }

    public SeatInfo(String name, int balance, int bet, List<HandView> hands, boolean human,
                    boolean active, boolean sittingOut, boolean outOfChips, @Nullable Integer net,
                    int insuranceBet) {
        this.name = name;
        this.balance = balance;
        this.bet = bet;
        this.hands = Collections.unmodifiableList(hands);
        this.human = human;
        this.active = active;
        this.sittingOut = sittingOut;
        this.outOfChips = outOfChips;
        this.net = net;
        this.insuranceBet = insuranceBet;
    }

    /** Asiento con una sola mano (usado por los datos de demostración). */
    public SeatInfo(String name, int balance, int bet, List<TableCard> cards,
                    @Nullable String score, boolean human, boolean active) {
        this(name, balance, bet,
                Collections.singletonList(new HandView(cards, score, bet, active, HandView.Result.NONE)),
                human, active, false, false, null);
    }
}
