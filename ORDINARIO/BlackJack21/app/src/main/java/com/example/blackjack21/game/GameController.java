package com.example.blackjack21.game;

import com.example.blackjack21.bot.BotAction;
import com.example.blackjack21.bot.BotManager;
import com.example.blackjack21.bot.BotPlayer;
import com.example.blackjack21.data.GameSaveData;
import com.example.blackjack21.engine.BlackjackRules;
import com.example.blackjack21.engine.GameEvent;
import com.example.blackjack21.engine.GameEngine;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.model.CardSource;
import com.example.blackjack21.model.RandomCodec;
import com.example.blackjack21.model.Shoe;
import com.example.blackjack21.view.table.model.TableSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Coordina una partida entre la interfaz, el {@link GameEngine} y los bots.
 * Es Java puro: la Activity solo traduce pulsaciones a llamadas de este objeto
 * y dibuja {@link #snapshot()}.
 *
 * <p>Cada acción del usuario comprueba primero su legalidad y devuelve
 * {@code false} si no procede, de modo que las pulsaciones repetidas o fuera
 * de turno no tienen efecto. Los turnos de los bots se resuelven en el acto.</p>
 */
public final class GameController {

    public static final String HUMAN_ID = "human";
    public static final int[] CHIP_VALUES = {5, 25, 100, 500};

    /** Mensaje de estado para el usuario. */
    public enum Status {
        PLACE_BET,
        BET_READY,
        GAME_OVER,
        INSURANCE,
        YOUR_TURN,
        BOT_TURN,
        ROUND_OVER
    }

    public interface Listener {
        void onGameChanged();
    }

    private final CardSource source;
    private final Random random;
    private final String humanName;
    private final List<String> botNames;
    private final int humanSeat;
    private final String dealerName;
    private final List<String> botIds = new ArrayList<>();

    private GameEngine engine;
    private BotManager bots;
    private int gameNumber;

    /** true: los bots juegan en el acto; false: avanzan con {@link #stepAutomatic()}. */
    private boolean autoAdvance = true;
    private boolean recordFrames;
    private final List<Frame> frames = new ArrayList<>();

    private int pendingBet;
    private int lastBet;
    private boolean busy;
    private Listener listener;
    private Runnable changeObserver;

    /** Punto de control al inicio de la ronda (null si el origen de cartas no es guardable). */
    private GameSaveData checkpoint;
    /** Acciones desde el punto de control, en orden. */
    private final List<String> journal = new ArrayList<>();

    /** Partida estándar: Alex, Max, humano, Sam con 1,000 fichas cada uno. */
    public static GameController createDefault(String humanName, String dealerName, long seed) {
        Random random = new Random(seed);
        return new GameController(new Shoe(Shoe.DEFAULT_DECKS, new Random(random.nextLong())),
                new Random(random.nextLong()), humanName, dealerName,
                BotManager.DEFAULT_NAMES, 2, GameEngine.DEFAULT_BALANCE);
    }

    /**
     * @param humanSeat posición del humano entre los bots (orden de turnos).
     */
    public GameController(CardSource source, Random random, String humanName, String dealerName,
                          List<String> botNames, int humanSeat, int initialBalance) {
        if (botNames.size() > TableSnapshot.MAX_BOTS) {
            throw new IllegalArgumentException("Máximo " + TableSnapshot.MAX_BOTS + " bots");
        }
        this.source = source;
        this.random = random;
        this.humanName = humanName;
        this.botNames = new ArrayList<>(botNames);
        this.humanSeat = Math.max(0, Math.min(humanSeat, botNames.size()));
        this.dealerName = dealerName;
        newGame(initialBalance);
    }

    /** Sienta a todos con el saldo indicado y reparte personalidades nuevas. */
    private void newGame(int balance) {
        engine = new GameEngine(source);
        engine.setEventListener(this::onEngineEvent);
        bots = new BotManager(engine, random);
        botIds.clear();
        for (int i = 0; i <= botNames.size(); i++) {
            if (i == humanSeat) {
                engine.addPlayer(HUMAN_ID, humanName, balance);
            }
            if (i < botNames.size()) {
                BotPlayer bot = bots.seatBot(botNames.get(i), balance);
                botIds.add(bot.getId());
            }
        }
        bots.assignPersonalities();
        pendingBet = 0;
        lastBet = 0;
        gameNumber++;
        captureCheckpoint();
    }

    // --- Persistencia ----------------------------------------------------------------

    /**
     * Recrea una partida guardada: punto de control + repetición del diario.
     *
     * @throws IllegalArgumentException si los datos no son coherentes o el diario
     *                                  no puede reproducirse.
     */
    public static GameController restore(GameSaveData data, String humanName, String dealerName) {
        data.validate();
        GameController c;
        try {
            c = new GameController(data, humanName, dealerName);
            c.replay(data.actions);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Partida guardada no reproducible", e);
        }
        int pending = data.pendingBet;
        c.pendingBet = c.canChangeBet() && pending <= c.getHuman().getBalance() ? pending : 0;
        return c;
    }

    private GameController(GameSaveData data, String humanName, String dealerName) {
        this.source = Shoe.restore(data.shoe);
        this.random = RandomCodec.decode(data.random);
        this.humanName = humanName;
        this.dealerName = dealerName;
        List<String> names = new ArrayList<>();
        int seat = 0;
        for (GameSaveData.Seat s : data.seats) {
            if (s.human) {
                seat = names.size();
            } else {
                names.add(s.name);
            }
        }
        if (names.size() > TableSnapshot.MAX_BOTS) {
            throw new IllegalArgumentException("Demasiados bots guardados");
        }
        this.botNames = names;
        this.humanSeat = seat;

        engine = new GameEngine(source);
        engine.setEventListener(this::onEngineEvent);
        bots = new BotManager(engine, random);
        for (GameSaveData.Seat s : data.seats) {
            if (s.human) {
                if (!HUMAN_ID.equals(s.id)) {
                    throw new IllegalArgumentException("Identificador humano inválido");
                }
                engine.addPlayer(HUMAN_ID, humanName, s.balance);
            } else {
                BotPlayer bot = bots.seatBot(s.name, s.balance);
                if (!bot.getId().equals(s.id)) {
                    throw new IllegalArgumentException("Bot guardado inválido: " + s.id);
                }
                bots.restorePersonality(bot.getId(), s.personality);
                botIds.add(bot.getId());
            }
        }
        engine.resumeAtRound(data.roundNumber);
        gameNumber = data.gameNumber;
        lastBet = data.lastBet;
        captureCheckpoint();
    }

    /** Estado guardable actual, o null si la partida no admite guardado (mazos de prueba). */
    public GameSaveData toSaveData() {
        return checkpoint == null ? null : checkpoint.withProgress(journal, pendingBet);
    }

    /**
     * Partida nueva sin datos guardados: continúa la numeración histórica para
     * que cada ronda tenga una clave única en las estadísticas.
     */
    public boolean continueNumbering(int previousGames) {
        if (busy || previousGames < 0 || !journal.isEmpty() || gameNumber != 1
                || getPhase() != GamePhase.WAITING_FOR_BETS) {
            return false;
        }
        gameNumber = previousGames + 1;
        captureCheckpoint();
        return true;
    }

    /** Se avisa tras cada cambio de estado (para guardar). Independiente del Listener. */
    public void setChangeObserver(Runnable observer) {
        this.changeObserver = observer;
    }

    private void captureCheckpoint() {
        journal.clear();
        if (!(source instanceof Shoe)) {
            checkpoint = null;
            return;
        }
        List<GameSaveData.Seat> seats = new ArrayList<>();
        for (PlayerState p : engine.getState().getPlayers()) {
            BotPlayer bot = bots.find(p.getId());
            seats.add(new GameSaveData.Seat(p.getId(), p.getName(), bot == null, p.getBalance(),
                    bot == null ? null : bot.getPersonality()));
        }
        checkpoint = new GameSaveData(gameNumber, engine.getRoundNumber(), lastBet, 0, seats,
                ((Shoe) source).saveState(), RandomCodec.encode(random),
                Collections.<String>emptyList());
    }

    /** Repite el diario sin animaciones ni avances automáticos extra. */
    private void replay(List<String> actions) {
        boolean auto = autoAdvance;
        boolean record = recordFrames;
        autoAdvance = false;
        recordFrames = false;
        try {
            for (String action : actions) {
                if (!applyAction(action)) {
                    throw new IllegalArgumentException("Acción no reproducible: " + action);
                }
            }
        } finally {
            autoAdvance = auto;
            recordFrames = record;
            frames.clear();
        }
    }

    private boolean applyAction(String action) {
        if (action.startsWith(GameSaveData.DEAL + ":")) {
            if (getPhase() != GamePhase.WAITING_FOR_BETS) {
                return false;
            }
            pendingBet = Integer.parseInt(action.substring(GameSaveData.DEAL.length() + 1));
            return deal();
        }
        switch (action) {
            case GameSaveData.HIT:
                return hit();
            case GameSaveData.STAND:
                return stand();
            case GameSaveData.DOUBLE:
                return doubleDown();
            case GameSaveData.SPLIT:
                return split();
            case GameSaveData.INSURANCE:
                return takeInsurance();
            case GameSaveData.DECLINE:
                return declineInsurance();
            case GameSaveData.STEP:
                return stepAutomatic();
            default:
                return false;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    // --- Presentación paso a paso ------------------------------------------------

    /**
     * Con {@code false} los turnos automáticos (bots, seguro de los bots) no se
     * resuelven al instante, sino uno por uno mediante {@link #stepAutomatic()}.
     */
    public void setAutoAdvance(boolean autoAdvance) {
        this.autoAdvance = autoAdvance;
    }

    /** Activa el registro de fotogramas para la presentación animada. */
    public void setRecordFrames(boolean record) {
        this.recordFrames = record;
        if (!record) {
            frames.clear();
        }
    }

    /** Devuelve y vacía los fotogramas registrados desde la última llamada. */
    public List<Frame> drainFrames() {
        List<Frame> out = new ArrayList<>(frames);
        frames.clear();
        return out;
    }

    /** Queda alguna decisión automática (de un bot) antes de que actúe el humano. */
    public boolean hasAutomaticStep() {
        if (busy) {
            return false;
        }
        if (getPhase() == GamePhase.INSURANCE_DECISION) {
            return bots.hasPendingInsurance();
        }
        return bots.currentBot() != null;
    }

    /** Ejecuta una única decisión automática: el seguro de los bots o una acción de un bot. */
    public boolean stepAutomatic() {
        return hasAutomaticStep() && run(this::doAutomaticStep);
    }

    private void doAutomaticStep() {
        journal.add(GameSaveData.STEP);
        if (getPhase() == GamePhase.INSURANCE_DECISION) {
            bots.decideInsurance();
            return;
        }
        BotPlayer bot = bots.currentBot();
        BotAction action = bots.nextAction();
        if (bot == null || action == null) {
            return;
        }
        record(Frame.botAction(bot.getId(), currentHandIndex(), action, null));
        bots.perform(action);
    }

    private int currentHandIndex() {
        PlayerState p = engine.getCurrentPlayer();
        HandState h = engine.getCurrentHand();
        return p == null || h == null ? 0 : p.getHands().indexOf(h);
    }

    private void onEngineEvent(GameEvent event) {
        if (recordFrames) {
            frames.add(Frame.fromEvent(event, snapshot()));
        }
    }

    /** Registra un fotograma propio del controlador (con la mesa actual). */
    private void record(Frame frame) {
        if (recordFrames) {
            frames.add(new Frame(frame.kind, frame.playerId, frame.handIndex, frame.cardIndex,
                    frame.faceDown, frame.action, snapshot()));
        }
    }

    // --- Consulta ------------------------------------------------------------------

    public GameEngine getEngine() {
        return engine;
    }

    public BotManager getBotManager() {
        return bots;
    }

    public GamePhase getPhase() {
        return engine.getPhase();
    }

    public PlayerState getHuman() {
        return engine.getPlayer(HUMAN_ID);
    }

    public int getPendingBet() {
        return pendingBet;
    }

    public List<String> getBotIds() {
        return Collections.unmodifiableList(botIds);
    }

    public TableSnapshot snapshot() {
        return TableSnapshotMapper.toSnapshot(engine, botIds, HUMAN_ID, dealerName,
                pendingBet, isInsuranceDecisionPending());
    }

    /** Ganancia o pérdida neta del humano en la ronda terminada (0 si no jugó). */
    public int getHumanRoundNet() {
        if (getPhase() != GamePhase.ROUND_FINISHED || !getHuman().isInRound()) {
            return 0;
        }
        return engine.getBetManager().netForRound(HUMAN_ID, engine.getRoundNumber());
    }

    public boolean humanPlayedRound() {
        return getPhase() == GamePhase.ROUND_FINISHED && getHuman().isInRound();
    }

    /** Nombre de quien tiene el turno, o null. */
    public String getCurrentTurnName() {
        PlayerState p = engine.getCurrentPlayer();
        return p == null ? null : p.getName();
    }

    public Status getStatus() {
        switch (getPhase()) {
            case WAITING_FOR_BETS:
                if (isGameOver()) {
                    return Status.GAME_OVER;
                }
                return pendingBet >= BlackjackRules.MIN_BET ? Status.BET_READY : Status.PLACE_BET;
            case INSURANCE_DECISION:
                return isInsuranceDecisionPending() ? Status.INSURANCE : Status.BOT_TURN;
            case PLAYER_TURN:
                return isHumanTurn() ? Status.YOUR_TURN : Status.BOT_TURN;
            case ROUND_FINISHED:
                return Status.ROUND_OVER;
            default:
                return Status.BOT_TURN;
        }
    }

    // --- Disponibilidad de acciones ----------------------------------------------

    public boolean isBusy() {
        return busy;
    }

    public boolean canChangeBet() {
        return !busy && getPhase() == GamePhase.WAITING_FOR_BETS
                && getHuman().getBalance() >= BlackjackRules.MIN_BET;
    }

    public boolean canAddChip(int value) {
        return canChangeBet() && value > 0 && pendingBet + value <= getHuman().getBalance();
    }

    public boolean canClearBet() {
        return canChangeBet() && pendingBet > 0;
    }

    /** REPARTIR: con apuesta válida, o solo bots si el humano no tiene fichas. */
    public boolean canDeal() {
        if (busy || getPhase() != GamePhase.WAITING_FOR_BETS) {
            return false;
        }
        return pendingBet >= BlackjackRules.MIN_BET && pendingBet <= getHuman().getBalance();
    }

    /**
     * Fin de partida: el humano no cubre la apuesta mínima y no hay ninguna
     * mano en juego (fase de apuestas o ronda ya liquidada).
     */
    public boolean isGameOver() {
        GamePhase phase = getPhase();
        boolean idle = phase == GamePhase.WAITING_FOR_BETS || phase == GamePhase.ROUND_FINISHED;
        return idle && getHuman().getBalance() < BlackjackRules.MIN_BET;
    }

    /** Número de partida (aumenta con cada reinicio). */
    public int getGameNumber() {
        return gameNumber;
    }

    /**
     * Comienza una partida nueva con 1,000 fichas para todos. Solo disponible
     * tras el fin de partida, para no descartar una ronda en curso.
     */
    public boolean restartGame() {
        if (busy || !isGameOver()) {
            return false;
        }
        return run(() -> newGame(GameEngine.DEFAULT_BALANCE));
    }

    public boolean canStartNextRound() {
        return !busy && getPhase() == GamePhase.ROUND_FINISHED;
    }

    public boolean isHumanTurn() {
        PlayerState current = engine.getCurrentPlayer();
        return current != null && HUMAN_ID.equals(current.getId());
    }

    public boolean canHit() {
        return !busy && isHumanTurn() && engine.canHit(HUMAN_ID);
    }

    public boolean canStand() {
        return !busy && isHumanTurn();
    }

    public boolean canDouble() {
        return !busy && isHumanTurn() && engine.canDouble(HUMAN_ID);
    }

    public boolean canSplit() {
        return !busy && isHumanTurn() && engine.canSplit(HUMAN_ID);
    }

    public boolean isInsuranceDecisionPending() {
        PlayerState h = getHuman();
        return getPhase() == GamePhase.INSURANCE_DECISION && h.isInRound()
                && !h.hasDecidedInsurance();
    }

    public boolean canTakeInsurance() {
        return !busy && isInsuranceDecisionPending() && engine.canTakeInsurance(HUMAN_ID);
    }

    /** Importe del seguro que se contrataría (la mitad de la apuesta, limitado por el saldo). */
    public int getInsuranceAmount() {
        return engine.maxInsuranceFor(getHuman());
    }

    // --- Apuesta --------------------------------------------------------------------

    public boolean addChip(int value) {
        if (!canAddChip(value)) {
            return false;
        }
        pendingBet += value;
        notifyChanged();
        return true;
    }

    public boolean clearBet() {
        if (!canClearBet()) {
            return false;
        }
        pendingBet = 0;
        notifyChanged();
        return true;
    }

    /** Confirma la apuesta, hace apostar a los bots y reparte. */
    public boolean deal() {
        if (!canDeal()) {
            return false;
        }
        return run(() -> {
            journal.add(GameSaveData.DEAL + ":" + pendingBet);
            engine.placeBet(HUMAN_ID, pendingBet);
            lastBet = pendingBet;
            pendingBet = 0;
            bots.placeBets();
            engine.deal();
            advance();
        });
    }

    // --- Acciones del turno -------------------------------------------------------

    public boolean hit() {
        return canHit() && run(() -> {
            journal.add(GameSaveData.HIT);
            engine.hit(HUMAN_ID);
            advance();
        });
    }

    public boolean stand() {
        return canStand() && run(() -> {
            journal.add(GameSaveData.STAND);
            record(Frame.stand(HUMAN_ID, currentHandIndex(), null));
            engine.stand(HUMAN_ID);
            advance();
        });
    }

    public boolean doubleDown() {
        return canDouble() && run(() -> {
            journal.add(GameSaveData.DOUBLE);
            engine.doubleDown(HUMAN_ID);
            advance();
        });
    }

    public boolean split() {
        return canSplit() && run(() -> {
            journal.add(GameSaveData.SPLIT);
            engine.split(HUMAN_ID);
            advance();
        });
    }

    public boolean takeInsurance() {
        return canTakeInsurance() && run(() -> {
            journal.add(GameSaveData.INSURANCE);
            engine.takeInsurance(HUMAN_ID, getInsuranceAmount());
            advance();
        });
    }

    public boolean declineInsurance() {
        return !busy && isInsuranceDecisionPending() && run(() -> {
            journal.add(GameSaveData.DECLINE);
            engine.declineInsurance(HUMAN_ID);
            advance();
        });
    }

    /** Limpia la mesa y propone repetir la última apuesta si el saldo lo permite. */
    public boolean startNextRound() {
        return canStartNextRound() && run(() -> {
            engine.startNextRound();
            bots.onNewRound();
            int balance = getHuman().getBalance();
            pendingBet = lastBet >= BlackjackRules.MIN_BET && lastBet <= balance ? lastBet : 0;
            captureCheckpoint();
        });
    }

    // --- Flujo automático ---------------------------------------------------------

    /**
     * Hace actuar a los bots hasta que le toque al humano o termine la ronda
     * (solo en modo automático; en modo paso a paso lo dirige la presentación).
     */
    private void advance() {
        if (!autoAdvance) {
            return;
        }
        for (int guard = 0; guard < 1000; guard++) {
            boolean pending = getPhase() == GamePhase.INSURANCE_DECISION
                    ? bots.hasPendingInsurance() : bots.currentBot() != null;
            if (!pending) {
                return;
            }
            doAutomaticStep();
        }
        throw new IllegalStateException("El flujo automático no progresa");
    }

    private boolean run(Runnable action) {
        if (busy) {
            return false;
        }
        busy = true;
        try {
            action.run();
        } finally {
            busy = false;
        }
        notifyChanged();
        return true;
    }


    private void notifyChanged() {
        if (listener != null) {
            listener.onGameChanged();
        }
        if (changeObserver != null) {
            changeObserver.run();
        }
    }

    /** Manos del humano en la ronda (para la interfaz y las pruebas). */
    public List<HandState> getHumanHands() {
        return getHuman().getHands();
    }
}
