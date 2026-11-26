package com.example.blackjack21.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Único punto que modifica saldos. Cada movimiento se identifica por
 * (ronda, jugador, tipo, mano); un movimiento repetido se rechaza, lo que
 * impide cobros o pagos duplicados.
 */
public final class BetManager {

    public enum EntryType {
        BET(true),
        DOUBLE(true),
        SPLIT(true),
        INSURANCE(true),
        PAYOUT(false),
        INSURANCE_PAYOUT(false);

        private final boolean debit;

        EntryType(boolean debit) {
            this.debit = debit;
        }

        public boolean isDebit() {
            return debit;
        }
    }

    /** Movimiento registrado (inmutable). {@code amount} siempre ≥ 0. */
    public static final class Entry {
        public final int round;
        public final String playerId;
        public final EntryType type;
        public final int handId;
        public final int amount;

        Entry(int round, String playerId, EntryType type, int handId, int amount) {
            this.round = round;
            this.playerId = playerId;
            this.type = type;
            this.handId = handId;
            this.amount = amount;
        }

        /** Efecto sobre el saldo: negativo para cargos. */
        public int signedAmount() {
            return type.isDebit() ? -amount : amount;
        }
    }

    private final List<Entry> ledger = new ArrayList<>();
    private final Set<String> keys = new HashSet<>();

    public boolean canAfford(PlayerState player, int amount) {
        return amount >= 0 && amount <= player.getBalance();
    }

    /** Cobra una apuesta. Falla si no hay saldo o si el movimiento ya existe. */
    void debit(PlayerState player, int round, EntryType type, int handId, int amount) {
        if (!type.isDebit()) {
            throw new IllegalArgumentException(type + " no es un cargo");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Importe inválido: " + amount);
        }
        if (!canAfford(player, amount)) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        String key = key(round, player.getId(), type, handId);
        if (keys.contains(key)) {
            throw new IllegalStateException("Movimiento duplicado: " + key);
        }
        player.removeBalance(amount);
        keys.add(key);
        ledger.add(new Entry(round, player.getId(), type, handId, amount));
    }

    /**
     * Abona un pago (puede ser 0 para dejar constancia de una derrota).
     *
     * @return false si ese pago ya se había registrado (no se abona de nuevo).
     */
    boolean credit(PlayerState player, int round, EntryType type, int handId, int amount) {
        if (type.isDebit()) {
            throw new IllegalArgumentException(type + " no es un abono");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Importe inválido: " + amount);
        }
        String key = key(round, player.getId(), type, handId);
        if (keys.contains(key)) {
            return false;
        }
        keys.add(key);
        player.addBalance(amount);
        ledger.add(new Entry(round, player.getId(), type, handId, amount));
        return true;
    }

    public List<Entry> getLedger() {
        return Collections.unmodifiableList(ledger);
    }

    /** Resultado neto de un jugador en una ronda (incluye seguro). */
    public int netForRound(String playerId, int round) {
        int net = 0;
        for (Entry e : ledger) {
            if (e.round == round && e.playerId.equals(playerId)) {
                net += e.signedAmount();
            }
        }
        return net;
    }

    private static String key(int round, String playerId, EntryType type, int handId) {
        return round + "|" + playerId + "|" + type + "|" + handId;
    }
}
