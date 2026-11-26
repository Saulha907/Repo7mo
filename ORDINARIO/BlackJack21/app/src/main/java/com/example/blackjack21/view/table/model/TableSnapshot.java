package com.example.blackjack21.view.table.model;

import java.util.Collections;
import java.util.List;

/**
 * Estado completo de la mesa en un instante, listo para dibujar.
 */
public final class TableSnapshot {

    public static final int MAX_BOTS = 3;

    public final SeatInfo dealer;
    public final List<SeatInfo> bots;
    public final SeatInfo player;

    public TableSnapshot(SeatInfo dealer, List<SeatInfo> bots, SeatInfo player) {
        if (bots.size() > MAX_BOTS) {
            throw new IllegalArgumentException("Máximo " + MAX_BOTS + " bots");
        }
        this.dealer = dealer;
        this.bots = Collections.unmodifiableList(bots);
        this.player = player;
    }
}
