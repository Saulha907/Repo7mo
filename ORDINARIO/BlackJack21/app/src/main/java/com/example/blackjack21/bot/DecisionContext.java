package com.example.blackjack21.bot;

import com.example.blackjack21.engine.GameEngine;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.Rank;

import java.util.List;

/**
 * Información que un bot puede conocer al decidir: su propia mano, la carta
 * visible del crupier, su saldo y las acciones legales. No incluye la carta
 * oculta ni nada del zapato.
 */
public final class DecisionContext {

    public final int total;
    public final boolean soft;
    public final boolean pair;
    /** Rango del par, o null si no es par. */
    public final Rank pairRank;
    public final int cardCount;
    /** Valor de la carta visible del crupier: 2..10, As = 11. */
    public final int dealerUpValue;
    public final int balance;
    public final int bet;
    public final boolean canHit;
    public final boolean canDouble;
    public final boolean canSplit;

    public DecisionContext(int total, boolean soft, Rank pairRank, int cardCount, int dealerUpValue,
                           int balance, int bet, boolean canHit, boolean canDouble, boolean canSplit) {
        this.total = total;
        this.soft = soft;
        this.pairRank = pairRank;
        this.pair = pairRank != null;
        this.cardCount = cardCount;
        this.dealerUpValue = dealerUpValue;
        this.balance = balance;
        this.bet = bet;
        this.canHit = canHit;
        this.canDouble = canDouble;
        this.canSplit = canSplit;
    }

    /** Construye el contexto del jugador con el turno usando solo la API pública del motor. */
    public static DecisionContext forCurrentTurn(GameEngine engine, String playerId) {
        PlayerState player = engine.getPlayer(playerId);
        HandState hand = engine.getCurrentHand();
        if (hand == null || engine.getCurrentPlayer() != player) {
            throw new IllegalStateException("No es el turno de " + playerId);
        }
        List<Card> cards = hand.getCards();
        Rank pairRank = cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank()
                ? cards.get(0).getRank() : null;
        return new DecisionContext(hand.getTotal(), hand.isSoft(), pairRank, cards.size(),
                dealerUpValue(engine), player.getBalance(), hand.getBet(),
                engine.canHit(playerId), engine.canDouble(playerId), engine.canSplit(playerId));
    }

    /** Valor de la carta visible del crupier (As = 11). */
    public static int dealerUpValue(GameEngine engine) {
        List<Card> visible = engine.getDealerVisibleCards();
        if (visible.isEmpty()) {
            throw new IllegalStateException("El crupier no tiene cartas visibles");
        }
        Card up = visible.get(0);
        return up.isAce() ? 11 : up.getValue();
    }
}
