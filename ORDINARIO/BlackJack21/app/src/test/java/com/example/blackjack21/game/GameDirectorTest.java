package com.example.blackjack21.game;

import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FIVE;
import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static com.example.blackjack21.model.Rank.THREE;
import static com.example.blackjack21.model.Rank.TWO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.bot.BotAction;
import com.example.blackjack21.engine.BetManager;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.view.table.model.HandView;
import com.example.blackjack21.view.table.model.SeatInfo;
import com.example.blackjack21.view.table.model.TableCard;
import com.example.blackjack21.view.table.model.TableSnapshot;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** Secuencia de presentación: orden, bloqueo, cancelación y sincronía con el motor. */
public class GameDirectorTest {

    private static final String H = GameController.HUMAN_ID;

    /** Reproductor de prueba: completa al instante o retiene el fotograma. */
    static final class FakePlayer implements GameDirector.FramePlayer {
        final List<Frame> played = new ArrayList<>();
        boolean autoComplete = true;
        Runnable pending;
        TableSnapshot shown;
        int cancels;

        @Override
        public void play(Frame frame, Runnable onDone) {
            played.add(frame);
            if (autoComplete) {
                onDone.run();
            } else {
                pending = onDone;
            }
        }

        @Override
        public void cancel() {
            cancels++;
        }

        @Override
        public void show(TableSnapshot snapshot) {
            shown = snapshot;
        }

        /** Completa fotogramas retenidos hasta que no quede ninguno. */
        void finishAll() {
            autoComplete = true;
            while (pending != null) {
                Runnable r = pending;
                pending = null;
                r.run();
            }
        }
    }

    static final class FakeScheduler implements GameDirector.Scheduler {
        final Deque<Runnable> tasks = new ArrayDeque<>();

        @Override
        public void post(Runnable task, long delayMs) {
            tasks.add(task);
        }

        @Override
        public void cancelAll() {
            tasks.clear();
        }

        void runAll() {
            int guard = 0;
            while (!tasks.isEmpty() && guard++ < 500) {
                tasks.poll().run();
            }
        }
    }

    private final FakePlayer player = new FakePlayer();
    private final FakeScheduler scheduler = new FakeScheduler();

    private GameDirector director(GameController c) {
        GameDirector d = new GameDirector(c);
        d.attach(player, scheduler, () -> { });
        return d;
    }

    private static GameController solo(Rank... cards) {
        return new GameController(new GameTestSource(cards), new Random(1), "Tú", "CRUPIER",
                Collections.<String>emptyList(), 0, 1000);
    }

    /** Alex y Max (se plantan con 20), humano en el tercer asiento. Crupier 7 + 10. */
    private static GameController withBots(Rank... humanAndRest) {
        List<Rank> deck = new ArrayList<>(Arrays.asList(
                TEN, TEN, humanAndRest[0], SEVEN,      // primera ronda + carta visible
                TEN, TEN, humanAndRest[1], TEN));      // segunda ronda + oculta
        deck.addAll(Arrays.asList(humanAndRest).subList(2, humanAndRest.length));
        return new GameController(new GameTestSource(deck.toArray(new Rank[0])), new Random(5),
                "Tú", "CRUPIER", Arrays.asList("Alex", "Max"), 2, 1000);
    }

    private static String describe(TableSnapshot s) {
        StringBuilder b = new StringBuilder();
        List<SeatInfo> seats = new ArrayList<>();
        seats.add(s.dealer);
        seats.addAll(s.bots);
        seats.add(s.player);
        for (SeatInfo seat : seats) {
            b.append(seat.name).append(':').append(seat.balance).append('/').append(seat.bet);
            for (HandView h : seat.hands) {
                b.append('[');
                for (TableCard c : h.cards) {
                    b.append(c.faceDown ? "##" : c.rank + c.suit).append(' ');
                }
                b.append(h.score).append(' ').append(h.result).append(']');
            }
            b.append(seat.net).append(';');
        }
        return b.toString();
    }

    private static void bet(GameDirector d, int amount) {
        int remaining = amount;
        int[] chips = GameController.CHIP_VALUES;
        for (int i = chips.length - 1; i >= 0; i--) {
            while (remaining >= chips[i]) {
                assertTrue(d.addChip(chips[i]));
                remaining -= chips[i];
            }
        }
    }

    // 1. Orden del reparto ---------------------------------------------------------------

    @Test
    public void dealFramesFollowRealDealingOrder() {
        GameController c = withBots(EIGHT, NINE);
        GameDirector d = director(c);
        bet(d, 25);
        assertTrue(d.deal());

        List<String> order = new ArrayList<>();
        for (Frame f : player.played) {
            if (f.kind == Frame.Kind.CARD || f.kind == Frame.Kind.DEALER_CARD) {
                order.add((f.playerId == null ? "D" : f.playerId) + f.cardIndex);
            }
        }
        assertEquals(Arrays.asList("bot:alex0", "bot:max0", H + "0", "D0",
                "bot:alex1", "bot:max1", H + "1", "D1"), order);
        assertEquals(Frame.Kind.BET, player.played.get(0).kind);

        // En cada fotograma de apuesta, la apuesta ya cobrada se ve en la mesa.
        for (Frame f : player.played) {
            if (f.kind == Frame.Kind.BET) {
                SeatInfo seat = H.equals(f.playerId) ? f.snapshot.player
                        : f.snapshot.bots.get(c.getBotIds().indexOf(f.playerId));
                assertTrue(f.playerId, seat.bet > 0);
            }
        }
    }

    // 2. Sincronía motor-interfaz ------------------------------------------------------

    @Test
    public void lastShownStateMatchesEngine() {
        GameController c = withBots(EIGHT, NINE);
        GameDirector d = director(c);
        bet(d, 25);
        d.deal();
        scheduler.runAll();
        assertTrue(c.isHumanTurn());
        assertFalse(d.isBusy());
        assertEquals(describe(c.snapshot()), describe(player.shown));
    }

    // 3. Bloqueo durante la animación ----------------------------------------------------

    @Test
    public void actionsAreBlockedWhileAnimating() {
        GameController c = solo(TEN, TEN, SIX, SEVEN, FIVE);
        GameDirector d = director(c);
        bet(d, 50);
        player.autoComplete = false;
        assertTrue(d.deal());
        assertTrue(d.isBusy());
        assertFalse(d.canAct());
        assertFalse(d.hit());
        assertFalse(d.stand());
        assertFalse(d.deal());
        assertFalse(d.addChip(25));
        assertEquals(2, c.getHumanHands().get(0).getCards().size());
        assertEquals(950, c.getHuman().getBalance());

        player.finishAll();
        assertFalse(d.isBusy());
        assertTrue(d.hit());
    }

    // 4 y 10. Cancelación y recuperación ------------------------------------------------

    @Test
    public void pauseCancelsAndShowsRealState() {
        GameController c = solo(TEN, TEN, SIX, SEVEN, FIVE);
        GameDirector d = director(c);
        bet(d, 50);
        player.autoComplete = false;
        d.deal();
        Runnable stale = player.pending;
        int playedBefore = player.played.size();

        d.pause();
        assertTrue(player.cancels > 0);
        assertFalse(d.isBusy());
        assertEquals(describe(c.snapshot()), describe(player.shown));

        stale.run(); // callback tardío de la animación cancelada: se ignora
        assertEquals(playedBefore, player.played.size());

        d.resume();
        player.autoComplete = true;
        assertTrue(d.canAct());
        assertTrue(d.stand());
        assertEquals(GamePhase.ROUND_FINISHED, c.getPhase());
    }

    @Test
    public void interruptedBotTurnsResumeWithoutDuplicates() {
        GameController c = withBots(EIGHT, NINE);
        GameDirector d = director(c);
        bet(d, 25);
        d.deal();
        assertFalse(scheduler.tasks.isEmpty()); // turno de Alex programado

        d.detach();
        assertTrue(scheduler.tasks.isEmpty());
        assertEquals("bot:alex", c.getEngine().getCurrentPlayer().getId());

        d.attach(player, scheduler, () -> { });
        scheduler.runAll();
        assertTrue(c.isHumanTurn());
        long bets = c.getEngine().getBetManager().getLedger().stream()
                .filter(e -> e.type == BetManager.EntryType.BET).count();
        assertEquals(3, bets);
        assertEquals(describe(c.snapshot()), describe(player.shown));
    }

    // 5. Carta oculta -----------------------------------------------------------------

    @Test
    public void holeCardIsDealtFaceDownAndHidden() {
        GameController c = solo(TEN, TEN, EIGHT, SEVEN);
        GameDirector d = director(c);
        bet(d, 10);
        d.deal();
        Frame hole = null;
        for (Frame f : player.played) {
            if (f.kind == Frame.Kind.DEALER_CARD && f.cardIndex == 1) {
                hole = f;
            }
        }
        assertNotNull(hole);
        assertTrue(hole.faceDown);
        HandView dealer = hole.snapshot.dealer.hands.get(0);
        assertTrue(dealer.cards.get(1).faceDown);
        assertEquals("", dealer.cards.get(1).rank);
        assertEquals("10", dealer.score);
    }

    // 6. Turnos de los bots -------------------------------------------------------------

    @Test
    public void botsActOneStepAtATimeInSeatOrder() {
        GameController c = withBots(EIGHT, NINE);
        GameDirector d = director(c);
        bet(d, 25);
        d.deal();
        player.played.clear();

        assertEquals(1, scheduler.tasks.size());
        scheduler.tasks.poll().run();             // decide Alex
        assertEquals(Frame.Kind.BOT_ACTION, player.played.get(0).kind);
        assertEquals("bot:alex", player.played.get(0).playerId);
        assertEquals(BotAction.STAND, player.played.get(0).action);
        assertEquals("bot:max", c.getEngine().getCurrentPlayer().getId());
        assertFalse(d.canAct());                  // el humano aún no puede actuar

        scheduler.tasks.poll().run();             // decide Max
        assertEquals("bot:max", player.played.get(1).playerId);
        assertTrue(c.isHumanTurn());
        assertTrue(scheduler.tasks.isEmpty());
        assertTrue(d.canAct());
    }

    // 7. División ----------------------------------------------------------------------

    @Test
    public void splitIsBlockedDuringAnimationAndThenAnimated() {
        GameController c = solo(EIGHT, TEN, EIGHT, SEVEN, THREE, TWO);
        GameDirector d = director(c);
        bet(d, 100);
        player.autoComplete = false;
        d.deal();
        assertFalse(d.split());
        player.finishAll();
        player.played.clear();

        assertTrue(d.split());
        assertEquals(Frame.Kind.SPLIT, player.played.get(0).kind);
        assertEquals(2, player.played.get(0).snapshot.player.hands.size());
        assertEquals(1, player.played.get(0).snapshot.player.hands.get(0).cards.size());
        assertEquals(Frame.Kind.CARD, player.played.get(1).kind);
        assertEquals(0, player.played.get(1).handIndex);
        assertEquals(1, player.played.get(1).cardIndex);
        assertEquals(800, c.getHuman().getBalance());
    }

    // 8. Doblar -------------------------------------------------------------------------

    @Test
    public void doubleIsAnimatedOnceWithoutDuplicatingTheBet() {
        GameController c = solo(FIVE, TEN, SIX, SEVEN, TEN);
        GameDirector d = director(c);
        bet(d, 100);
        d.deal();
        player.played.clear();

        assertTrue(d.doubleDown());
        assertFalse(d.doubleDown());
        assertEquals(Frame.Kind.DOUBLE, player.played.get(0).kind);
        assertEquals(Frame.Kind.CARD, player.played.get(1).kind);
        assertEquals(2, player.played.get(1).cardIndex);
        long doubles = c.getEngine().getBetManager().getLedger().stream()
                .filter(e -> e.type == BetManager.EntryType.DOUBLE).count();
        assertEquals(1, doubles);
        assertEquals(1200, c.getHuman().getBalance());
    }

    // 9. Final de ronda -------------------------------------------------------------------

    @Test
    public void roundEndsWithRevealDealerCardsAndResults() {
        GameController c = solo(TEN, SIX, NINE, TEN, TWO);
        GameDirector d = director(c);
        bet(d, 10);
        d.deal();
        player.played.clear();

        assertTrue(d.stand());
        List<Frame.Kind> kinds = new ArrayList<>();
        for (Frame f : player.played) {
            kinds.add(f.kind);
        }
        // Plantarse, voltear la oculta, una carta más del crupier (16 → 18) y resultados.
        assertEquals(Arrays.asList(Frame.Kind.STAND, Frame.Kind.HOLE_REVEAL,
                Frame.Kind.DEALER_CARD, Frame.Kind.SETTLED), kinds);
        Frame last = player.played.get(player.played.size() - 1);
        assertEquals(Integer.valueOf(10), last.snapshot.player.net);
        assertFalse(d.isBusy());
        assertEquals(GameController.Status.ROUND_OVER, c.getStatus());
        assertTrue(d.startNextRound());
    }

    @Test
    public void dealerBlackjackRevealsBeforePlayerActs() {
        GameController c = solo(TEN, TEN, NINE, Rank.ACE);
        GameDirector d = director(c);
        bet(d, 10);
        d.deal();
        Frame last = player.played.get(player.played.size() - 1);
        Frame beforeLast = player.played.get(player.played.size() - 2);
        assertEquals(Frame.Kind.HOLE_REVEAL, beforeLast.kind);
        assertEquals(Frame.Kind.SETTLED, last.kind);
        assertFalse(d.hit());
    }
}
