package com.example.blackjack21.engine;

import com.example.blackjack21.engine.BetManager.EntryType;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.CardSource;
import com.example.blackjack21.model.Hand;
import com.example.blackjack21.model.Shoe;

import java.util.Collections;
import java.util.List;

/**
 * Motor de partida: apuestas, reparto, seguro, turnos, crupier y pagos.
 *
 * <p>Máquina de estados: WAITING_FOR_BETS → DEALING → [INSURANCE_DECISION] →
 * PLAYER_TURN → DEALER_TURN → SETTLEMENT → ROUND_FINISHED. DEALING, DEALER_TURN y
 * SETTLEMENT son transitorias: el motor las recorre de forma síncrona.
 * Cualquier acción fuera de su fase o de su turno lanza {@link IllegalStateException};
 * los argumentos inválidos lanzan {@link IllegalArgumentException}.</p>
 *
 * <p>La carta oculta del crupier solo es visible a través de
 * {@link #getDealerVisibleCards()} una vez revelada.</p>
 */
public final class GameEngine {

    public static final int DEFAULT_BALANCE = 1000;

    private final CardSource source;
    private final BetManager bets = new BetManager();
    private final GameState state = new GameState();
    private GameEventListener eventListener;

    public GameEngine() {
        this(new Shoe());
    }

    public GameEngine(CardSource source) {
        if (source == null) {
            throw new IllegalArgumentException("Origen de cartas obligatorio");
        }
        this.source = source;
    }

    /** Observador opcional para presentar los eventos (no altera las reglas). */
    public void setEventListener(GameEventListener listener) {
        this.eventListener = listener;
    }

    private void emit(GameEvent event) {
        if (eventListener != null) {
            eventListener.onGameEvent(event);
        }
    }

    // --- Jugadores ----------------------------------------------------------

    public PlayerState addPlayer(String id, String name) {
        return addPlayer(id, name, DEFAULT_BALANCE);
    }

    /** Añade un jugador (en orden de asiento). Solo antes de que haya apuestas. */
    public PlayerState addPlayer(String id, String name, int initialBalance) {
        requirePhase(GamePhase.WAITING_FOR_BETS);
        if (!state.getActivePlayers().isEmpty()) {
            throw new IllegalStateException("No se puede sentar a nadie con apuestas en curso");
        }
        if (id == null || findPlayer(id) != null) {
            throw new IllegalArgumentException("Identificador inválido o repetido: " + id);
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Saldo inicial negativo");
        }
        PlayerState player = new PlayerState(id, name, initialBalance);
        state.addPlayer(player);
        return player;
    }

    // --- Consulta -----------------------------------------------------------

    public GameState getState() {
        return state;
    }

    public GamePhase getPhase() {
        return state.getPhase();
    }

    public int getRoundNumber() {
        return state.getRound().getNumber();
    }

    public BetManager getBetManager() {
        return bets;
    }

    public PlayerState getPlayer(String id) {
        PlayerState p = findPlayer(id);
        if (p == null) {
            throw new IllegalArgumentException("Jugador desconocido: " + id);
        }
        return p;
    }

    /** Jugador con el turno, o null fuera de PLAYER_TURN. */
    public PlayerState getCurrentPlayer() {
        if (getPhase() != GamePhase.PLAYER_TURN) {
            return null;
        }
        return state.getActivePlayers().get(state.getRound().playerIndex());
    }

    /** Mano activa, o null fuera de PLAYER_TURN. */
    public HandState getCurrentHand() {
        PlayerState p = getCurrentPlayer();
        return p == null ? null : p.getHands().get(state.getRound().handIndex());
    }

    /** Cartas del crupier visibles para los jugadores (la oculta no se incluye). */
    public List<Card> getDealerVisibleCards() {
        List<Card> cards = state.getRound().dealer().getCards();
        if (state.getRound().isHoleRevealed() || cards.size() < 2) {
            return cards;
        }
        return Collections.singletonList(cards.get(0));
    }

    /** Total visible del crupier. */
    public int getDealerVisibleTotal() {
        Hand visible = new Hand();
        for (Card c : getDealerVisibleCards()) {
            visible.add(c);
        }
        return visible.getTotal();
    }

    public boolean isDealerHoleRevealed() {
        return state.getRound().isHoleRevealed();
    }

    /** Mano completa del crupier; solo para pruebas del paquete. */
    Hand dealerHand() {
        return state.getRound().dealer();
    }

    // --- Apuestas -----------------------------------------------------------

    /** Confirma la apuesta de un jugador: se descuenta del saldo al instante. */
    public void placeBet(String playerId, int amount) {
        requirePhase(GamePhase.WAITING_FOR_BETS);
        PlayerState p = getPlayer(playerId);
        if (p.isInRound()) {
            throw new IllegalStateException(playerId + " ya apostó en esta ronda");
        }
        if (!BlackjackRules.isValidBet(amount)) {
            throw new IllegalArgumentException("La apuesta mínima es " + BlackjackRules.MIN_BET);
        }
        if (!bets.canAfford(p, amount)) {
            throw new IllegalArgumentException("Saldo insuficiente para apostar " + amount);
        }
        bets.debit(p, getRoundNumber(), EntryType.BET, p.peekNextHandId(), amount);
        p.openHand(new Hand(), amount);
        emit(GameEvent.player(GameEvent.Type.BET_PLACED, playerId, 0, -1));
    }

    // --- Reparto ------------------------------------------------------------

    /** Reparte dos cartas a cada jugador con apuesta y al crupier. */
    public void deal() {
        requirePhase(GamePhase.WAITING_FOR_BETS);
        List<PlayerState> active = state.getActivePlayers();
        if (active.isEmpty()) {
            throw new IllegalStateException("Nadie ha apostado");
        }
        state.setPhase(GamePhase.DEALING);
        source.beginRound();
        Hand dealer = state.getRound().dealer();

        for (PlayerState p : active) {
            dealTo(p, 0);
        }
        dealer.add(source.draw());
        emit(GameEvent.dealerCard(0, false));
        for (PlayerState p : active) {
            dealTo(p, 0);
        }
        dealer.add(source.draw());
        emit(GameEvent.dealerCard(1, true));

        for (PlayerState p : active) {
            HandState h = p.getHands().get(0);
            if (h.isNaturalBlackjack()) {
                h.finish();
            }
        }

        Card up = dealer.getCards().get(0);
        if (up.isAce()) {
            state.setPhase(GamePhase.INSURANCE_DECISION);
        } else if (up.getValue() == 10 && dealer.isNaturalBlackjack()) {
            settle();
        } else {
            beginPlayerTurns();
        }
    }

    // --- Seguro -------------------------------------------------------------

    public boolean canTakeInsurance(String playerId) {
        if (getPhase() != GamePhase.INSURANCE_DECISION) {
            return false;
        }
        PlayerState p = getPlayer(playerId);
        return p.isInRound() && !p.hasDecidedInsurance()
                && maxInsuranceFor(p) >= 1;
    }

    /** Seguro máximo permitido: mitad de la apuesta original, limitado por el saldo. */
    public int maxInsuranceFor(PlayerState p) {
        return Math.min(PayoutCalculator.maxInsurance(p.getInitialBet()), p.getBalance());
    }

    public void takeInsurance(String playerId, int amount) {
        PlayerState p = requireInsuranceDecision(playerId);
        if (amount < 1 || amount > PayoutCalculator.maxInsurance(p.getInitialBet())) {
            throw new IllegalArgumentException("Seguro fuera de rango: " + amount);
        }
        if (!bets.canAfford(p, amount)) {
            throw new IllegalArgumentException("Saldo insuficiente para el seguro");
        }
        bets.debit(p, getRoundNumber(), EntryType.INSURANCE, 0, amount);
        p.setInsurance(amount);
        emit(GameEvent.player(GameEvent.Type.INSURANCE_TAKEN, playerId, 0, -1));
        afterInsuranceDecision();
    }

    public void declineInsurance(String playerId) {
        PlayerState p = requireInsuranceDecision(playerId);
        p.setInsurance(0);
        afterInsuranceDecision();
    }

    private PlayerState requireInsuranceDecision(String playerId) {
        requirePhase(GamePhase.INSURANCE_DECISION);
        PlayerState p = getPlayer(playerId);
        if (!p.isInRound()) {
            throw new IllegalStateException(playerId + " no participa en la ronda");
        }
        if (p.hasDecidedInsurance()) {
            throw new IllegalStateException(playerId + " ya decidió el seguro");
        }
        return p;
    }

    private void afterInsuranceDecision() {
        for (PlayerState p : state.getActivePlayers()) {
            if (!p.hasDecidedInsurance()) {
                return;
            }
        }
        boolean dealerNatural = state.getRound().dealer().isNaturalBlackjack();
        for (PlayerState p : state.getActivePlayers()) {
            if (p.getInsuranceBet() > 0) {
                int returned = PayoutCalculator.insuranceReturn(dealerNatural, p.getInsuranceBet());
                bets.credit(p, getRoundNumber(), EntryType.INSURANCE_PAYOUT, 0, returned);
                p.resolveInsurance(dealerNatural);
            }
        }
        if (dealerNatural) {
            settle();
        } else {
            beginPlayerTurns();
        }
    }

    // --- Acciones del jugador -----------------------------------------------

    public boolean canHit(String playerId) {
        HandState h = currentHandOf(playerId);
        return h != null && BlackjackRules.canHit(h.hand());
    }

    public boolean canDouble(String playerId) {
        HandState h = currentHandOf(playerId);
        return h != null && h.getCards().size() == 2
                && bets.canAfford(getPlayer(playerId), h.getBet());
    }

    public boolean canSplit(String playerId) {
        HandState h = currentHandOf(playerId);
        if (h == null) {
            return false;
        }
        PlayerState p = getPlayer(playerId);
        return BlackjackRules.canSplit(h.hand(), p.getHands().size())
                && bets.canAfford(p, h.getBet());
    }

    public void hit(String playerId) {
        HandState h = requireTurn(playerId);
        if (!BlackjackRules.canHit(h.hand())) {
            throw new IllegalStateException("La mano no admite más cartas");
        }
        dealTo(getPlayer(playerId), state.getRound().handIndex());
        if (h.getTotal() >= BlackjackRules.BLACKJACK) {
            h.finish();
        }
        advanceTurn();
    }

    public void stand(String playerId) {
        HandState h = requireTurn(playerId);
        h.finish();
        advanceTurn();
    }

    /** Dobla la apuesta, recibe exactamente una carta y termina la mano. */
    public void doubleDown(String playerId) {
        HandState h = requireTurn(playerId);
        PlayerState p = getPlayer(playerId);
        if (h.getCards().size() != 2) {
            throw new IllegalStateException("Solo se puede doblar con dos cartas");
        }
        if (!bets.canAfford(p, h.getBet())) {
            throw new IllegalArgumentException("Saldo insuficiente para doblar");
        }
        bets.debit(p, getRoundNumber(), EntryType.DOUBLE, h.getId(), h.getBet());
        h.doubleBet();
        int handIndex = state.getRound().handIndex();
        emit(GameEvent.player(GameEvent.Type.DOUBLED, playerId, handIndex, -1));
        dealTo(p, handIndex);
        h.finish();
        advanceTurn();
    }

    /** Divide un par en dos manos con apuestas independientes. */
    public void split(String playerId) {
        HandState h = requireTurn(playerId);
        PlayerState p = getPlayer(playerId);
        if (!h.hand().isPair()) {
            throw new IllegalStateException("Solo se dividen dos cartas del mismo rango");
        }
        if (p.getHands().size() >= BlackjackRules.MAX_HANDS) {
            throw new IllegalStateException("Máximo " + BlackjackRules.MAX_HANDS + " manos");
        }
        if (!bets.canAfford(p, h.getBet())) {
            throw new IllegalArgumentException("Saldo insuficiente para dividir");
        }
        int handIndex = state.getRound().handIndex();
        int newId = p.peekNextHandId();
        bets.debit(p, getRoundNumber(), EntryType.SPLIT, newId, h.getBet());

        List<Card> cards = h.getCards();
        Card first = cards.get(0);
        Card second = cards.get(1);
        h.replaceHand(Hand.fromSplit(first));
        p.insertHandAfter(handIndex, Hand.fromSplit(second), h.getBet());
        emit(GameEvent.player(GameEvent.Type.SPLIT, playerId, handIndex, -1));
        // Cada mano dividida recibe su segunda carta al empezar a jugarse.
        advanceTurn();
    }

    // --- Turnos -------------------------------------------------------------

    private void beginPlayerTurns() {
        state.setPhase(GamePhase.PLAYER_TURN);
        state.getRound().setTurn(0, 0);
        advanceTurn();
    }

    /**
     * Avanza hasta la siguiente mano jugable. Reparte la segunda carta a las
     * manos divididas y cierra automáticamente las que llegan a 21 o se pasan.
     */
    private void advanceTurn() {
        RoundState round = state.getRound();
        List<PlayerState> active = state.getActivePlayers();
        int pi = round.playerIndex();
        int hi = round.handIndex();
        while (pi < active.size()) {
            List<HandState> hands = active.get(pi).getHands();
            while (hi < hands.size()) {
                HandState h = hands.get(hi);
                if (!h.isFinished()) {
                    if (h.getCards().size() < 2) {
                        dealTo(active.get(pi), hi);
                    }
                    if (h.getTotal() >= BlackjackRules.BLACKJACK) {
                        h.finish();
                    } else {
                        round.setTurn(pi, hi);
                        return;
                    }
                }
                hi++;
            }
            pi++;
            hi = 0;
        }
        playDealer();
    }

    // --- Crupier y liquidación ----------------------------------------------

    private void playDealer() {
        state.setPhase(GamePhase.DEALER_TURN);
        RoundState round = state.getRound();
        round.revealHole();
        emit(GameEvent.table(GameEvent.Type.HOLE_REVEALED));
        if (dealerNeedsToPlay()) {
            Hand dealer = round.dealer();
            while (BlackjackRules.dealerMustHit(dealer)) {
                dealer.add(source.draw());
                emit(GameEvent.dealerCard(dealer.size() - 1, false));
            }
        }
        settle();
    }

    /** El crupier solo pide si queda alguna mano viva que no sea blackjack natural. */
    private boolean dealerNeedsToPlay() {
        for (PlayerState p : state.getActivePlayers()) {
            for (HandState h : p.getHands()) {
                if (!h.isBust() && !h.isNaturalBlackjack()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Liquida todas las manos una única vez y cierra la ronda. */
    private void settle() {
        RoundState round = state.getRound();
        if (round.isSettled()) {
            return;
        }
        state.setPhase(GamePhase.SETTLEMENT);
        if (!round.isHoleRevealed()) {
            round.revealHole();
            emit(GameEvent.table(GameEvent.Type.HOLE_REVEALED));
        }
        Hand dealer = round.dealer();
        for (PlayerState p : state.getActivePlayers()) {
            for (HandState h : p.getHands()) {
                if (h.isSettled()) {
                    continue;
                }
                Outcome outcome = BlackjackRules.resolve(h.hand(), dealer);
                int returned = PayoutCalculator.mainReturn(outcome, h.getBet());
                if (bets.credit(p, getRoundNumber(), EntryType.PAYOUT, h.getId(), returned)) {
                    h.settle(outcome, returned);
                }
            }
        }
        round.markSettled();
        source.endRound();
        state.setPhase(GamePhase.ROUND_FINISHED);
        emit(GameEvent.table(GameEvent.Type.ROUND_SETTLED));
    }

    /** Reparte una carta a la mano indicada y lo notifica. */
    private void dealTo(PlayerState player, int handIndex) {
        Hand hand = player.getHands().get(handIndex).hand();
        hand.add(source.draw());
        emit(GameEvent.player(GameEvent.Type.CARD_DEALT, player.getId(), handIndex, hand.size() - 1));
    }

    /**
     * Restauración de una partida guardada: continúa la numeración de rondas.
     * Solo antes de cualquier apuesta; no altera ninguna regla.
     */
    public void resumeAtRound(int roundNumber) {
        requirePhase(GamePhase.WAITING_FOR_BETS);
        if (!state.getActivePlayers().isEmpty()) {
            throw new IllegalStateException("Hay apuestas en curso");
        }
        if (roundNumber < 1) {
            throw new IllegalArgumentException("Número de ronda inválido: " + roundNumber);
        }
        state.setRoundNumber(roundNumber);
    }

    /** Limpia la mesa y abre la siguiente ronda de apuestas. */
    public void startNextRound() {
        requirePhase(GamePhase.ROUND_FINISHED);
        for (PlayerState p : state.getPlayers()) {
            p.resetRound();
        }
        state.nextRound();
        state.setPhase(GamePhase.WAITING_FOR_BETS);
    }

    // --- Utilidades ---------------------------------------------------------

    private void requirePhase(GamePhase expected) {
        if (state.getPhase() != expected) {
            throw new IllegalStateException("Acción no válida en " + state.getPhase()
                    + " (se esperaba " + expected + ")");
        }
    }

    private HandState requireTurn(String playerId) {
        requirePhase(GamePhase.PLAYER_TURN);
        PlayerState current = getCurrentPlayer();
        if (current == null || !current.getId().equals(playerId)) {
            throw new IllegalStateException("No es el turno de " + playerId);
        }
        return getCurrentHand();
    }

    private HandState currentHandOf(String playerId) {
        PlayerState current = getCurrentPlayer();
        if (current == null || !current.getId().equals(playerId)) {
            return null;
        }
        return getCurrentHand();
    }

    private PlayerState findPlayer(String id) {
        for (PlayerState p : state.getPlayers()) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }
}
