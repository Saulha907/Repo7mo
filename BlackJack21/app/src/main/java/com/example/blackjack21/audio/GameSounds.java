package com.example.blackjack21.audio;

import com.example.blackjack21.game.Frame;
import com.example.blackjack21.view.table.model.HandView;
import com.example.blackjack21.view.table.model.SeatInfo;

/**
 * Asocia los sucesos de la mesa con efectos (Java puro). Cada fotograma suena
 * como máximo una vez y las acciones solo suenan si el motor las aceptó.
 */
public final class GameSounds {

    public interface Sink {
        boolean play(SoundEffect effect);
    }

    private final Sink sink;
    private Frame lastFrame;

    public GameSounds(Sink sink) {
        this.sink = sink;
    }

    /** El fotograma empieza a verse en pantalla. */
    public void onFrameStarted(Frame frame) {
        if (frame == null || frame == lastFrame) {
            return;
        }
        lastFrame = frame;
        SoundEffect effect = forFrame(frame);
        if (effect != null) {
            sink.play(effect);
        }
    }

    /** Acción del usuario: suena solo si fue aceptada. Devuelve {@code accepted}. */
    public boolean onAction(SoundEffect effect, boolean accepted) {
        if (accepted) {
            sink.play(effect);
        }
        return accepted;
    }

    public static SoundEffect forFrame(Frame frame) {
        switch (frame.kind) {
            case BET:
                return SoundEffect.CHIP_BET;
            case CARD:
            case DEALER_CARD:
                return SoundEffect.CARD_DEAL;
            case HOLE_REVEAL:
                return SoundEffect.CARD_FLIP;
            case SPLIT:
                return SoundEffect.SPLIT;
            case DOUBLE:
                return SoundEffect.DOUBLE;
            case INSURANCE:
                return SoundEffect.INSURANCE;
            case STAND:
                return SoundEffect.STAND;
            case BOT_ACTION:
                if (frame.action == null) {
                    return SoundEffect.STAND;
                }
                switch (frame.action) {
                    case HIT:
                        return SoundEffect.HIT;
                    case STAND:
                        return SoundEffect.STAND;
                    default:
                        return null; // DOUBLE/SPLIT suenan con su propio fotograma
                }
            case SETTLED:
                return frame.snapshot == null ? null : forResult(frame.snapshot.player);
            default:
                return null;
        }
    }

    /** Resultado de la ronda desde el punto de vista del jugador humano. */
    public static SoundEffect forResult(SeatInfo player) {
        if (player == null || player.sittingOut || player.net == null) {
            return null;
        }
        boolean played = false;
        for (HandView hand : player.hands) {
            if (hand.result == HandView.Result.BLACKJACK) {
                return SoundEffect.BLACKJACK;
            }
            played |= hand.result != HandView.Result.NONE;
        }
        if (!played) {
            return null;
        }
        int net = player.net;
        if (net > 0) {
            return SoundEffect.WIN;
        }
        return net < 0 ? SoundEffect.LOSE : SoundEffect.PUSH;
    }
}
