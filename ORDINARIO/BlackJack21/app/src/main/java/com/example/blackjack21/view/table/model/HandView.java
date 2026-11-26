package com.example.blackjack21.view.table.model;

import java.util.Collections;
import java.util.List;

/**
 * Una mano tal como se dibuja: cartas, puntuación ya formateada, apuesta,
 * si es la mano activa y su resultado (si ya se liquidó).
 */
public final class HandView {

    public enum Result {
        NONE,
        BLACKJACK,
        WIN,
        PUSH,
        LOSE,
        BUST
    }

    public final List<TableCard> cards;
    /** Puntuación visible, o null si no debe mostrarse. */
    public final String score;
    public final int bet;
    public final boolean active;
    public final Result result;
    /** Ganancia neta de esta mano tras liquidar; null antes. */
    public final Integer net;

    public HandView(List<TableCard> cards, String score, int bet, boolean active, Result result) {
        this(cards, score, bet, active, result, null);
    }

    public HandView(List<TableCard> cards, String score, int bet, boolean active, Result result,
                    Integer net) {
        this.cards = Collections.unmodifiableList(cards);
        this.score = score;
        this.bet = bet;
        this.active = active;
        this.result = result == null ? Result.NONE : result;
        this.net = net;
    }
}
