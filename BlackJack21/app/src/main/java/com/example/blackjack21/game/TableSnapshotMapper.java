package com.example.blackjack21.game;

import com.example.blackjack21.engine.BlackjackRules;
import com.example.blackjack21.engine.GameEngine;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.Outcome;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.Hand;
import com.example.blackjack21.view.paint.SuitPaths;
import com.example.blackjack21.view.table.model.HandView;
import com.example.blackjack21.view.table.model.SeatInfo;
import com.example.blackjack21.view.table.model.TableCard;
import com.example.blackjack21.view.table.model.TableSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convierte el estado del motor en el modelo de presentación de la mesa.
 * Solo usa información pública: la carta oculta del crupier se representa
 * con su reverso y su puntuación nunca se incluye antes de revelarse.
 */
public final class TableSnapshotMapper {

    private TableSnapshotMapper() {
    }

    // --- Cartas y manos ---------------------------------------------------------

    public static TableCard toTableCard(Card card) {
        return TableCard.up(card.getRank().getSymbol(), toSuit(card));
    }

    static int toSuit(Card card) {
        switch (card.getSuit()) {
            case HEARTS:
                return SuitPaths.HEART;
            case DIAMONDS:
                return SuitPaths.DIAMOND;
            case CLUBS:
                return SuitPaths.CLUB;
            default:
                return SuitPaths.SPADE;
        }
    }

    public static List<TableCard> toTableCards(List<Card> cards) {
        List<TableCard> out = new ArrayList<>(cards.size());
        for (Card c : cards) {
            out.add(toTableCard(c));
        }
        return out;
    }

    /** "17", o "7/17" para manos suaves por debajo de 21. */
    public static String formatScore(int total, boolean soft) {
        if (soft && total < 21) {
            return (total - 10) + "/" + total;
        }
        return String.valueOf(total);
    }

    public static HandView toHandView(HandState hand, boolean active) {
        List<Card> cards = hand.getCards();
        // Manos terminadas: solo el total definitivo.
        String score = cards.isEmpty() ? null
                : formatScore(hand.getTotal(), hand.isSoft() && !hand.isFinished());
        Integer net = hand.isSettled() ? hand.getNet() : null;
        return new HandView(toTableCards(cards), score, hand.getBet(), active, toResult(hand), net);
    }

    static HandView.Result toResult(HandState hand) {
        Outcome outcome = hand.getOutcome();
        if (!hand.isSettled() || outcome == null) {
            return HandView.Result.NONE;
        }
        switch (outcome) {
            case BLACKJACK:
                return HandView.Result.BLACKJACK;
            case WIN:
                return HandView.Result.WIN;
            case PUSH:
                return HandView.Result.PUSH;
            default:
                return hand.isBust() ? HandView.Result.BUST : HandView.Result.LOSE;
        }
    }

    // --- Participantes ------------------------------------------------------------

    /** Mano del crupier: cartas visibles más el reverso de la oculta. */
    public static SeatInfo toDealer(GameEngine engine, String dealerName) {
        List<Card> visible = engine.getDealerVisibleCards();
        List<TableCard> cards = toTableCards(visible);
        boolean hidden = !engine.isDealerHoleRevealed() && visible.size() == 1;
        if (hidden) {
            cards.add(TableCard.down());
        }
        String score = null;
        if (!visible.isEmpty()) {
            Hand shown = new Hand();
            for (Card c : visible) {
                shown.add(c);
            }
            score = String.valueOf(shown.getTotal());
        }
        HandView hand = new HandView(cards, score, 0, false, HandView.Result.NONE);
        boolean dealerTurn = engine.getPhase() == GamePhase.DEALER_TURN;
        return new SeatInfo(dealerName, 0, 0, Collections.singletonList(hand),
                false, dealerTurn, false, false, null);
    }

    /**
     * @param pendingBet apuesta aún no confirmada (solo para el humano en fase de apuestas).
     */
    public static SeatInfo toSeat(GameEngine engine, PlayerState player, boolean human,
                                  int pendingBet, boolean awaitingDecision) {
        GamePhase phase = engine.getPhase();
        HandState current = engine.getCurrentHand();
        boolean isCurrent = engine.getCurrentPlayer() == player;

        List<HandView> hands = new ArrayList<>();
        int bet = 0;
        for (HandState h : player.getHands()) {
            hands.add(toHandView(h, isCurrent && h == current));
            bet += h.getBet();
        }
        if (phase == GamePhase.WAITING_FOR_BETS && !player.isInRound()) {
            bet = pendingBet; // apuesta aún sin confirmar (solo el humano)
        }

        boolean inRound = player.isInRound();
        boolean outOfChips = !inRound && player.getBalance() < BlackjackRules.MIN_BET;
        boolean sittingOut = !inRound && (phase != GamePhase.WAITING_FOR_BETS || outOfChips);
        Integer net = null;
        if (phase == GamePhase.ROUND_FINISHED && inRound) {
            net = engine.getBetManager().netForRound(player.getId(), engine.getRoundNumber());
        }
        boolean active = isCurrent || awaitingDecision;
        int insurance = phase == GamePhase.WAITING_FOR_BETS ? 0 : player.getInsuranceBet();
        return new SeatInfo(player.getName(), player.getBalance(), bet, hands, human,
                active, sittingOut, outOfChips, net, insurance);
    }

    /**
     * @param botIds ids de los bots en orden de asiento (máximo 3).
     */
    public static TableSnapshot toSnapshot(GameEngine engine, List<String> botIds, String humanId,
                                           String dealerName, int pendingBet,
                                           boolean humanAwaitingInsurance) {
        List<SeatInfo> bots = new ArrayList<>();
        for (String id : botIds) {
            bots.add(toSeat(engine, engine.getPlayer(id), false, 0, false));
        }
        SeatInfo human = toSeat(engine, engine.getPlayer(humanId), true, pendingBet,
                humanAwaitingInsurance);
        return new TableSnapshot(toDealer(engine, dealerName), bots, human);
    }
}
