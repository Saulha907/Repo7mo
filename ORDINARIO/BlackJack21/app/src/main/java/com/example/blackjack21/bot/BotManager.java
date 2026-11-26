package com.example.blackjack21.bot;

import com.example.blackjack21.engine.GameEngine;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.PlayerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Sienta a los bots en la mesa y los hace actuar a través de la API pública
 * de {@link GameEngine}. No toca saldos ni cartas directamente.
 */
public final class BotManager {

    public static final List<String> DEFAULT_NAMES = Collections.unmodifiableList(
            Arrays.asList("Alex", "Max", "Sam"));
    private static final int MAX_ACTIONS_PER_CALL = 200;

    private final GameEngine engine;
    private final Random random;
    private final List<BotPlayer> bots = new ArrayList<>();

    public BotManager(GameEngine engine, Random random) {
        this.engine = engine;
        this.random = random;
    }

    // --- Configuración --------------------------------------------------------

    /** Sienta un bot en el siguiente asiento libre (orden de turnos = orden de asiento). */
    public BotPlayer seatBot(String name, int balance) {
        PlayerState state = engine.addPlayer(botId(name), name, balance);
        BotPlayer bot = new BotPlayer(state, BotPersonality.BALANCED);
        bots.add(bot);
        return bot;
    }

    /** Sienta a Alex, Max y Sam con el saldo indicado y les asigna personalidad. */
    public List<BotPlayer> seatDefaultBots(int balance) {
        for (String name : DEFAULT_NAMES) {
            seatBot(name, balance);
        }
        assignPersonalities();
        return getBots();
    }

    /**
     * Reparte personalidades al azar procurando variedad: se baraja la lista de
     * perfiles para que, con tres bots, cada uno tenga uno distinto.
     */
    public void assignPersonalities() {
        List<BotPersonality> pool = new ArrayList<>();
        while (pool.size() < bots.size()) {
            List<BotPersonality> block = new ArrayList<>(Arrays.asList(BotPersonality.values()));
            Collections.shuffle(block, random);
            pool.addAll(block);
        }
        for (int i = 0; i < bots.size(); i++) {
            bots.get(i).setPersonality(pool.get(i));
        }
    }

    /** Restauración de una partida guardada: recupera la personalidad asignada. */
    public void restorePersonality(String botId, BotPersonality personality) {
        BotPlayer bot = find(botId);
        if (bot == null || personality == null) {
            throw new IllegalArgumentException("Bot o personalidad desconocidos: " + botId);
        }
        bot.setPersonality(personality);
    }

    public List<BotPlayer> getBots() {
        return Collections.unmodifiableList(bots);
    }

    public boolean isBot(String playerId) {
        return find(playerId) != null;
    }

    public BotPlayer find(String playerId) {
        for (BotPlayer bot : bots) {
            if (bot.getId().equals(playerId)) {
                return bot;
            }
        }
        return null;
    }

    // --- Fases ------------------------------------------------------------------

    /** Cada bot apuesta según su personalidad; los que no cubren el mínimo se quedan fuera. */
    public void placeBets() {
        if (engine.getPhase() != GamePhase.WAITING_FOR_BETS) {
            throw new IllegalStateException("No es momento de apostar");
        }
        for (BotPlayer bot : bots) {
            bot.setStatus(BotPlayer.Status.BETTING);
            int bet = bot.getStrategy().chooseBet(bot.getBalance(), random);
            if (bet <= 0) {
                bot.setStatus(BotPlayer.Status.SITTING_OUT);
                continue;
            }
            engine.placeBet(bot.getId(), bet);
            bot.setStatus(BotPlayer.Status.PLAYING);
        }
    }

    /** Decide el seguro de los bots pendientes. */
    public void decideInsurance() {
        for (BotPlayer bot : bots) {
            if (engine.getPhase() != GamePhase.INSURANCE_DECISION) {
                return;
            }
            PlayerState p = bot.getPlayerState();
            if (!p.isInRound() || p.hasDecidedInsurance()) {
                continue;
            }
            HandState hand = p.getHands().get(0);
            int amount = engine.maxInsuranceFor(p);
            boolean wants = bot.getStrategy().wantsInsurance(hand.getTotal(), hand.isNaturalBlackjack());
            if (wants && engine.canTakeInsurance(bot.getId()) && amount >= 1) {
                engine.takeInsurance(bot.getId(), amount);
            } else {
                engine.declineInsurance(bot.getId());
            }
        }
    }

    /**
     * Ejecuta acciones mientras el turno pertenezca a un bot.
     *
     * @return número de acciones realizadas.
     */
    public int playPendingTurns() {
        int actions = 0;
        while (actions < MAX_ACTIONS_PER_CALL) {
            BotAction action = nextAction();
            if (action == null) {
                break;
            }
            perform(action);
            actions++;
        }
        if (engine.getPhase() == GamePhase.ROUND_FINISHED) {
            for (BotPlayer bot : bots) {
                if (bot.getStatus() == BotPlayer.Status.PLAYING) {
                    bot.setStatus(BotPlayer.Status.DONE);
                }
            }
        }
        return actions;
    }

    /** Bot que tiene el turno, o null si le toca a un humano o no hay turno. */
    public BotPlayer currentBot() {
        if (engine.getPhase() != GamePhase.PLAYER_TURN) {
            return null;
        }
        PlayerState current = engine.getCurrentPlayer();
        return current == null ? null : find(current.getId());
    }

    /** Decide (sin ejecutar) la acción del bot con el turno; null si no es turno de un bot. */
    public BotAction nextAction() {
        BotPlayer bot = currentBot();
        if (bot == null) {
            return null;
        }
        return bot.getStrategy().decide(DecisionContext.forCurrentTurn(engine, bot.getId()));
    }

    /** Ejecuta en el motor la acción del bot con el turno. */
    public void perform(BotAction action) {
        BotPlayer bot = currentBot();
        if (bot == null) {
            throw new IllegalStateException("No es el turno de un bot");
        }
        act(bot, action);
    }

    /** Hay bots en la ronda pendientes de decidir el seguro. */
    public boolean hasPendingInsurance() {
        if (engine.getPhase() != GamePhase.INSURANCE_DECISION) {
            return false;
        }
        for (BotPlayer bot : bots) {
            PlayerState p = bot.getPlayerState();
            if (p.isInRound() && !p.hasDecidedInsurance()) {
                return true;
            }
        }
        return false;
    }

    private void act(BotPlayer bot, BotAction action) {
        String id = bot.getId();
        switch (action) {
            case SPLIT:
                engine.split(id);
                break;
            case DOUBLE:
                engine.doubleDown(id);
                break;
            case HIT:
                engine.hit(id);
                break;
            default:
                engine.stand(id);
                break;
        }
    }

    /** Restablece el estado de los bots para una ronda nueva. */
    public void onNewRound() {
        for (BotPlayer bot : bots) {
            bot.setStatus(BotPlayer.Status.WAITING);
        }
    }

    private static String botId(String name) {
        return "bot:" + name.toLowerCase(java.util.Locale.ROOT);
    }
}
