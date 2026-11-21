package com.example.blackjack21.game;

import com.example.blackjack21.view.table.model.TableSnapshot;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;

/**
 * Dirige la presentación de la partida (Java puro, sin Android).
 *
 * <p>Cada acción se ejecuta de inmediato en el {@link GameController}; el
 * director solo reproduce después, en orden, los {@link Frame} que produjo.
 * Mientras hay fotogramas pendientes o un turno de bot programado, todas las
 * acciones del usuario se rechazan. Si la presentación se interrumpe, se
 * muestra directamente el estado real del motor.</p>
 */
public final class GameDirector {

    /** Pausa antes de cada decisión de un bot (además de su anuncio). */
    public static final long BOT_THINK_MS = 250L;

    /** Reproduce un fotograma y avisa al terminar. Implementación Android en ui/anim. */
    public interface FramePlayer {
        void play(Frame frame, Runnable onDone);

        /** Detiene la animación en curso sin llamar a su {@code onDone}. */
        void cancel();

        /** Muestra un estado sin animar. */
        void show(TableSnapshot snapshot);
    }

    /** Planificador de tareas diferidas en el hilo de la interfaz. */
    public interface Scheduler {
        void post(Runnable task, long delayMs);

        void cancelAll();
    }

    public interface Listener {
        void onPresentationChanged();
    }

    private final GameController controller;
    private final Deque<Frame> queue = new ArrayDeque<>();
    private FramePlayer player;
    private Scheduler scheduler;
    private Listener listener;

    private Frame current;
    private boolean stepScheduled;
    private boolean acting;
    private boolean paused = true;
    /** Invalida los callbacks de animaciones canceladas. */
    private int generation;

    public GameDirector(GameController controller) {
        this.controller = controller;
        controller.setAutoAdvance(false);
        controller.setRecordFrames(true);
        controller.setListener(this::onControllerChanged);
    }

    /** Conecta la interfaz y empieza (o retoma) la presentación. */
    public void attach(FramePlayer player, Scheduler scheduler, Listener listener) {
        this.player = player;
        this.scheduler = scheduler;
        this.listener = listener;
        resume();
    }

    /** Desconecta la interfaz; el estado del motor se conserva intacto. */
    public void detach() {
        pause();
        player = null;
        scheduler = null;
        listener = null;
    }

    // --- Estado ----------------------------------------------------------------------

    public GameController getController() {
        return controller;
    }

    /** Hay presentación en curso: las acciones del usuario están bloqueadas. */
    public boolean isBusy() {
        return acting || current != null || !queue.isEmpty() || stepScheduled;
    }

    public boolean canAct() {
        return !paused && !isBusy();
    }

    /** Fotograma que se está mostrando, o null. */
    public Frame getCurrentFrame() {
        return current;
    }

    // --- Acciones del usuario ----------------------------------------------------------

    public boolean addChip(int value) {
        return canAct() && controller.addChip(value);
    }

    public boolean clearBet() {
        return canAct() && controller.clearBet();
    }

    public boolean deal() {
        return act(controller::deal);
    }

    public boolean hit() {
        return act(controller::hit);
    }

    public boolean stand() {
        return act(controller::stand);
    }

    public boolean doubleDown() {
        return act(controller::doubleDown);
    }

    public boolean split() {
        return act(controller::split);
    }

    public boolean takeInsurance() {
        return act(controller::takeInsurance);
    }

    public boolean declineInsurance() {
        return act(controller::declineInsurance);
    }

    public boolean startNextRound() {
        return act(controller::startNextRound);
    }

    public boolean restartGame() {
        return act(controller::restartGame);
    }

    private boolean act(BooleanSupplier action) {
        if (!canAct()) {
            return false;
        }
        acting = true;
        boolean done;
        try {
            done = action.getAsBoolean();
        } finally {
            acting = false;
        }
        if (done) {
            pump();
        }
        return done;
    }

    // --- Pausa y reanudación ---------------------------------------------------------

    /** Interrumpe la presentación y muestra el estado real del motor. */
    public void pause() {
        paused = true;
        generation++;
        stepScheduled = false;
        if (scheduler != null) {
            scheduler.cancelAll();
        }
        if (player != null) {
            player.cancel();
        }
        queue.clear();
        current = null;
        controller.drainFrames();
        if (player != null) {
            player.show(controller.snapshot());
        }
        notifyListener();
    }

    /** Muestra el estado real y continúa los turnos automáticos pendientes. */
    public void resume() {
        paused = false;
        if (player != null) {
            player.show(controller.snapshot());
        }
        scheduleAutomation();
        notifyListener();
    }

    // --- Reproducción -----------------------------------------------------------------

    private void pump() {
        queue.addAll(controller.drainFrames());
        if (current == null) {
            playNext();
        }
    }

    private void playNext() {
        if (paused || player == null) {
            return;
        }
        Frame next = queue.poll();
        if (next == null) {
            current = null;
            player.show(controller.snapshot());
            scheduleAutomation();
            notifyListener();
            return;
        }
        current = next;
        final int token = ++generation;
        notifyListener();
        player.play(next, () -> {
            if (token != generation) {
                return; // animación cancelada o sustituida
            }
            current = null;
            playNext();
        });
    }

    /** Programa la siguiente decisión de un bot tras una pausa. */
    private void scheduleAutomation() {
        if (paused || stepScheduled || scheduler == null || !controller.hasAutomaticStep()) {
            return;
        }
        stepScheduled = true;
        final int token = generation;
        scheduler.post(() -> {
            if (token != generation || paused) {
                return;
            }
            stepScheduled = false;
            acting = true;
            try {
                controller.stepAutomatic();
            } finally {
                acting = false;
            }
            pump();
            if (current == null && queue.isEmpty()) {
                scheduleAutomation();
                notifyListener();
            }
        }, BOT_THINK_MS);
        notifyListener();
    }

    private void onControllerChanged() {
        if (!acting) {
            notifyListener();
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onPresentationChanged();
        }
    }
}
