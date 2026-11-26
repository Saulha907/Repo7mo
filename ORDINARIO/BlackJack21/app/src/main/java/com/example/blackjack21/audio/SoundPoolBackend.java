package com.example.blackjack21.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.blackjack21.R;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reproductor basado en {@link SoundPool}. Los efectos se decodifican en
 * segundo plano (SoundPool.load es asíncrono); hasta que un efecto está
 * cargado simplemente no suena, sin errores ni reproducciones diferidas.
 */
final class SoundPoolBackend implements SoundManager.Backend {

    private static final int MAX_STREAMS = 6;
    private static final int TRACKED_STREAMS = 12;

    private final SoundPool pool;
    private final int[] sampleIds = new int[SoundEffect.values().length];
    private final boolean[] loaded = new boolean[SoundEffect.values().length];
    private final Deque<Integer> streams = new ArrayDeque<>();
    private boolean released;

    SoundPoolBackend(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attributes)
                .build();
        pool.setOnLoadCompleteListener((p, sampleId, status) -> onLoaded(sampleId, status));
        Context app = context.getApplicationContext();
        for (SoundEffect effect : SoundEffect.values()) {
            try {
                sampleIds[effect.ordinal()] = pool.load(app, rawResource(effect), 1);
            } catch (RuntimeException e) {
                sampleIds[effect.ordinal()] = 0; // recurso ausente: el efecto queda mudo
            }
        }
    }

    private synchronized void onLoaded(int sampleId, int status) {
        if (status != 0) {
            return;
        }
        for (int i = 0; i < sampleIds.length; i++) {
            if (sampleIds[i] == sampleId) {
                loaded[i] = true;
            }
        }
    }

    @Override
    public synchronized boolean play(SoundEffect effect, float volume) {
        int i = effect.ordinal();
        if (released || !loaded[i] || sampleIds[i] == 0 || volume <= 0f) {
            return false;
        }
        int stream = pool.play(sampleIds[i], volume, volume, 1, 0, 1f);
        if (stream == 0) {
            return false;
        }
        streams.addLast(stream);
        while (streams.size() > TRACKED_STREAMS) {
            streams.removeFirst();
        }
        return true;
    }

    @Override
    public synchronized void stopAll() {
        if (released) {
            return;
        }
        for (Integer stream : streams) {
            pool.stop(stream);
        }
        streams.clear();
    }

    @Override
    public synchronized void release() {
        if (!released) {
            released = true;
            streams.clear();
            pool.setOnLoadCompleteListener(null);
            pool.release();
        }
    }

    static int rawResource(SoundEffect effect) {
        switch (effect) {
            case CHIP_SELECT:
                return R.raw.sfx_chip_select;
            case CHIP_BET:
                return R.raw.sfx_chip_bet;
            case CARD_DEAL:
                return R.raw.sfx_card_deal;
            case CARD_FLIP:
                return R.raw.sfx_card_flip;
            case HIT:
                return R.raw.sfx_hit;
            case STAND:
                return R.raw.sfx_stand;
            case DOUBLE:
                return R.raw.sfx_double;
            case SPLIT:
                return R.raw.sfx_split;
            case INSURANCE:
                return R.raw.sfx_insurance;
            case WIN:
                return R.raw.sfx_win;
            case LOSE:
                return R.raw.sfx_lose;
            case PUSH:
                return R.raw.sfx_push;
            case BLACKJACK:
                return R.raw.sfx_blackjack;
            default:
                return R.raw.sfx_button;
        }
    }
}
