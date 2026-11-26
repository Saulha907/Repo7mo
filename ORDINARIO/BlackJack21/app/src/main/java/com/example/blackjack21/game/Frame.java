package com.example.blackjack21.game;

import com.example.blackjack21.bot.BotAction;
import com.example.blackjack21.engine.GameEvent;
import com.example.blackjack21.view.table.model.TableSnapshot;

/**
 * Fotograma de presentación: un suceso de la partida junto con el estado de la
 * mesa justo después de que ocurriera. Reproducirlos en orden recrea la ronda
 * tal como la jugó el motor; el último coincide con el estado real.
 */
public final class Frame {

    public enum Kind {
        BET,
        CARD,
        DEALER_CARD,
        HOLE_REVEAL,
        SPLIT,
        DOUBLE,
        INSURANCE,
        BOT_ACTION,
        STAND,
        SETTLED
    }

    public final Kind kind;
    /** Jugador afectado; null para el crupier. */
    public final String playerId;
    public final int handIndex;
    public final int cardIndex;
    public final boolean faceDown;
    /** Acción anunciada (solo BOT_ACTION). */
    public final BotAction action;
    public final TableSnapshot snapshot;

    Frame(Kind kind, String playerId, int handIndex, int cardIndex, boolean faceDown,
          BotAction action, TableSnapshot snapshot) {
        this.kind = kind;
        this.playerId = playerId;
        this.handIndex = handIndex;
        this.cardIndex = cardIndex;
        this.faceDown = faceDown;
        this.action = action;
        this.snapshot = snapshot;
    }

    static Frame fromEvent(GameEvent e, TableSnapshot snapshot) {
        return new Frame(kindOf(e.type), e.playerId, e.handIndex, e.cardIndex, e.faceDown, null, snapshot);
    }

    static Frame botAction(String playerId, int handIndex, BotAction action, TableSnapshot snapshot) {
        return new Frame(Kind.BOT_ACTION, playerId, handIndex, -1, false, action, snapshot);
    }

    static Frame stand(String playerId, int handIndex, TableSnapshot snapshot) {
        return new Frame(Kind.STAND, playerId, handIndex, -1, false, null, snapshot);
    }

    private static Kind kindOf(GameEvent.Type type) {
        switch (type) {
            case BET_PLACED:
                return Kind.BET;
            case CARD_DEALT:
                return Kind.CARD;
            case DEALER_CARD:
                return Kind.DEALER_CARD;
            case HOLE_REVEALED:
                return Kind.HOLE_REVEAL;
            case SPLIT:
                return Kind.SPLIT;
            case DOUBLED:
                return Kind.DOUBLE;
            case INSURANCE_TAKEN:
                return Kind.INSURANCE;
            default:
                return Kind.SETTLED;
        }
    }

    @Override
    public String toString() {
        return kind + "(" + (playerId == null ? "dealer" : playerId) + "," + handIndex + ","
                + cardIndex + (faceDown ? ",oculta" : "") + (action != null ? "," + action : "") + ")";
    }
}
