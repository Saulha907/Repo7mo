package com.example.blackjack21.ui.anim;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.example.blackjack21.R;
import com.example.blackjack21.bot.BotAction;
import com.example.blackjack21.game.Frame;
import com.example.blackjack21.game.GameController;
import com.example.blackjack21.game.GameDirector;
import com.example.blackjack21.view.table.TableView;
import com.example.blackjack21.view.table.model.TableSnapshot;

import java.util.List;

/**
 * Traduce los {@link Frame} del director en animaciones de {@link TableView}
 * y programa las pausas en el hilo principal. Todas las tareas pendientes se
 * pueden cancelar de golpe (pausa de la Activity o destrucción).
 */
public final class TableAnimationPlayer implements GameDirector.FramePlayer, GameDirector.Scheduler {

    /** Avisa cuando un fotograma empieza a verse (p. ej. para sincronizar sonidos). */
    public interface FrameStartListener {
        void onFrameStarted(Frame frame);
    }

    // Duraciones (ms).
    static final long CARD_MS = 300L;
    static final long DEALER_THINK_MS = 260L;
    static final long FLIP_MS = 420L;
    static final long BET_MS = 200L;
    static final long CHIP_MS = 320L;
    static final long SPLIT_MS = 380L;
    static final long STAND_MS = 300L;
    static final long ANNOUNCE_MS = 420L;
    static final long CALLOUT_MS = 950L;
    static final long RESULTS_MS = 850L;

    private static final Object FRAME_TOKEN = new Object();
    private static final Object STEP_TOKEN = new Object();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TableView table;
    private final List<String> botIds;
    private final String hitLabel;
    private final String standLabel;
    private final String doubleLabel;
    private final String splitLabel;
    private FrameStartListener startListener;

    public TableAnimationPlayer(Context context, TableView table, List<String> botIds) {
        this.table = table;
        this.botIds = botIds;
        hitLabel = context.getString(R.string.callout_hit);
        standLabel = context.getString(R.string.callout_stand);
        doubleLabel = context.getString(R.string.callout_double);
        splitLabel = context.getString(R.string.callout_split);
    }

    public void setFrameStartListener(FrameStartListener listener) {
        startListener = listener;
    }

    // --- FramePlayer -------------------------------------------------------------

    @Override
    public void play(Frame frame, Runnable onDone) {
        int slot = slotOf(frame.playerId);
        if (frame.kind == Frame.Kind.SETTLED) {
            later(() -> {
                notifyStarted(frame);
                table.animateResults(frame.snapshot, RESULTS_MS, onDone);
            }, 150L);
            return;
        }
        if (frame.kind == Frame.Kind.DEALER_CARD && frame.cardIndex >= 2) {
            // El crupier "piensa" un instante antes de cada carta adicional.
            later(() -> {
                table.setSnapshot(frame.snapshot);
                notifyStarted(frame);
                table.animateCardDeal(slot, 0, frame.cardIndex, CARD_MS, onDone);
            }, DEALER_THINK_MS);
            return;
        }
        table.setSnapshot(frame.snapshot);
        notifyStarted(frame);
        switch (frame.kind) {
            case BET:
                table.animateChip(slot, TableView.CHIP_BET, BET_MS, onDone);
                break;
            case CARD:
                table.animateCardDeal(slot, frame.handIndex, frame.cardIndex, CARD_MS, onDone);
                break;
            case DEALER_CARD:
                table.animateCardDeal(slot, 0, frame.cardIndex, CARD_MS, onDone);
                break;
            case HOLE_REVEAL:
                table.animateHoleFlip(FLIP_MS, onDone);
                break;
            case SPLIT:
                table.animateHandFlash(slot, frame.handIndex, SPLIT_MS, onDone);
                break;
            case DOUBLE:
                table.animateChip(slot, TableView.CHIP_DOUBLE, CHIP_MS, onDone);
                break;
            case INSURANCE:
                table.animateChip(slot, TableView.CHIP_INSURANCE, CHIP_MS, onDone);
                break;
            case BOT_ACTION:
                table.showCallout(slot, labelOf(frame.action), CALLOUT_MS);
                later(onDone, ANNOUNCE_MS);
                break;
            case STAND:
                table.animateHandFlash(slot, frame.handIndex, STAND_MS, onDone);
                break;
            default:
                onDone.run();
                break;
        }
    }

    @Override
    public void cancel() {
        handler.removeCallbacksAndMessages(FRAME_TOKEN);
        table.cancelAllAnimations();
    }

    @Override
    public void show(TableSnapshot snapshot) {
        table.cancelMotion();
        table.setSnapshot(snapshot);
    }

    // --- Scheduler ------------------------------------------------------------------

    @Override
    public void post(Runnable task, long delayMs) {
        handler.postAtTime(task, STEP_TOKEN, SystemClock.uptimeMillis() + delayMs);
    }

    @Override
    public void cancelAll() {
        handler.removeCallbacksAndMessages(STEP_TOKEN);
    }

    /** Libera todas las tareas pendientes (destrucción de la Activity). */
    public void release() {
        startListener = null;
        handler.removeCallbacksAndMessages(null);
        table.cancelAllAnimations();
    }

    // --- Utilidades -----------------------------------------------------------------

    private void notifyStarted(Frame frame) {
        if (startListener != null) {
            startListener.onFrameStarted(frame);
        }
    }

    private void later(Runnable task, long delayMs) {
        handler.postAtTime(task, FRAME_TOKEN, SystemClock.uptimeMillis() + delayMs);
    }

    private int slotOf(String playerId) {
        if (playerId == null) {
            return TableView.SLOT_DEALER;
        }
        if (GameController.HUMAN_ID.equals(playerId)) {
            return TableView.SLOT_HUMAN;
        }
        int index = botIds.indexOf(playerId);
        return index >= 0 ? TableView.slotForBot(index) : TableView.SLOT_HUMAN;
    }

    private String labelOf(BotAction action) {
        if (action == null) {
            return standLabel;
        }
        switch (action) {
            case HIT:
                return hitLabel;
            case DOUBLE:
                return doubleLabel;
            case SPLIT:
                return splitLabel;
            default:
                return standLabel;
        }
    }
}
