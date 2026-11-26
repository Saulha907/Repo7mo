package com.example.blackjack21.audio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** Gestor de efectos: configuración, volumen, duplicados, pausa y recursos. */
public class SoundManagerTest {

    private final AudioTestDoubles.MemoryStore store = new AudioTestDoubles.MemoryStore();
    private final AudioTestDoubles.Factory factory = new AudioTestDoubles.Factory();
    private final AudioTestDoubles.ManualClock clock = new AudioTestDoubles.ManualClock();
    private final SoundManager manager = new SoundManager(new AudioSettings(store), factory, clock);

    @Test
    public void playsWhenEnabled() {
        assertTrue(manager.play(SoundEffect.CARD_DEAL));
        assertEquals(Collections.singletonList(SoundEffect.CARD_DEAL), factory.last().played);
    }

    @Test
    public void disabledAudioIsSilentAndPersisted() {
        manager.setEnabled(false);
        assertFalse(manager.play(SoundEffect.WIN));
        assertTrue(factory.created.isEmpty() || factory.last().played.isEmpty());
        assertFalse(new AudioSettings(store).isEnabled());

        manager.setEnabled(true);
        assertTrue(manager.play(SoundEffect.WIN));
    }

    @Test
    public void disablingStopsSoundsInProgress() {
        manager.play(SoundEffect.BLACKJACK);
        manager.setEnabled(false);
        assertEquals(1, factory.last().stops);
    }

    @Test
    public void volumeScalesPlayback() {
        manager.setVolume(100);
        assertEquals(SoundEffect.CARD_DEAL.gain, manager.volumeFor(SoundEffect.CARD_DEAL), 1e-6);
        manager.setVolume(50);
        assertEquals(0.25f * SoundEffect.CARD_DEAL.gain, manager.volumeFor(SoundEffect.CARD_DEAL), 1e-6);

        manager.play(SoundEffect.CARD_DEAL);
        assertEquals(0.25f * SoundEffect.CARD_DEAL.gain, factory.last().volumes.get(0), 1e-6);
        assertEquals(50, new AudioSettings(store).getVolume());
    }

    @Test
    public void volumeIsMonotonicAndZeroIsSilent() {
        float previous = -1f;
        for (int v = 0; v <= 100; v += 10) {
            manager.setVolume(v);
            float now = manager.volumeFor(SoundEffect.CHIP_SELECT);
            assertTrue(now > previous || (v == 0 && now == 0f));
            previous = now;
        }
        manager.setVolume(0);
        assertFalse(manager.play(SoundEffect.CHIP_SELECT));
    }

    @Test
    public void immediateDuplicateIsIgnored() {
        assertTrue(manager.play(SoundEffect.CHIP_BET));
        assertFalse(manager.play(SoundEffect.CHIP_BET));
        // Un efecto distinto sí suena a la vez.
        assertTrue(manager.play(SoundEffect.CARD_DEAL));
        clock.now += SoundManager.MIN_REPEAT_MS;
        assertTrue(manager.play(SoundEffect.CHIP_BET));
        assertEquals(Arrays.asList(SoundEffect.CHIP_BET, SoundEffect.CARD_DEAL, SoundEffect.CHIP_BET),
                factory.last().played);
    }

    @Test
    public void missingOrBrokenSoundNeverThrows() {
        manager.preload();
        factory.last().available = false;
        assertFalse(manager.play(SoundEffect.SPLIT));
        factory.last().throwOnPlay = true;
        assertFalse(manager.play(SoundEffect.DOUBLE));

        SoundManager noBackend = new SoundManager(new AudioSettings(store), () -> {
            throw new IllegalStateException("sin audio");
        }, clock);
        assertFalse(noBackend.play(SoundEffect.WIN));
    }

    @Test
    public void backendIsCreatedOnceAndReused() {
        manager.play(SoundEffect.HIT);
        clock.now += 100;
        manager.play(SoundEffect.STAND);
        manager.preload();
        assertEquals(1, factory.created.size());
    }

    @Test
    public void pauseSilencesAndReleases() {
        manager.play(SoundEffect.WIN);
        AudioTestDoubles.FakeBackend first = factory.last();
        manager.pause();
        assertTrue(manager.isPaused());
        assertTrue(first.stops > 0);
        assertTrue(first.released);
        assertFalse(manager.hasBackend());
        assertFalse(manager.play(SoundEffect.LOSE));
        assertEquals(1, factory.created.size());
    }

    @Test
    public void resumeRecoversWithoutPendingSounds() {
        manager.pause();
        manager.play(SoundEffect.LOSE); // pedido en segundo plano: se descarta
        manager.resume();
        manager.preload();
        assertFalse(manager.isPaused());
        AudioTestDoubles.FakeBackend fresh = factory.last();
        assertTrue("Nada pendiente tras volver", fresh.played.isEmpty());
        assertTrue(manager.play(SoundEffect.CARD_DEAL));
        assertEquals(Collections.singletonList(SoundEffect.CARD_DEAL), fresh.played);
    }

    @Test
    public void releaseRecreatesOnNextUse() {
        manager.play(SoundEffect.BUTTON);
        manager.release();
        assertTrue(factory.created.get(0).released);
        clock.now += 100;
        assertTrue(manager.play(SoundEffect.BUTTON));
        assertEquals(2, factory.created.size());
    }
}
