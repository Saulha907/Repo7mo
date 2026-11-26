package com.example.blackjack21.audio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Pausa en segundo plano y recuperación al volver. */
public class AudioLifecycleTest {

    private final AudioTestDoubles.Factory factory = new AudioTestDoubles.Factory();
    private final SoundManager manager = new SoundManager(
            new AudioSettings(new AudioTestDoubles.MemoryStore()), factory,
            new AudioTestDoubles.ManualClock());
    private final AudioLifecycle lifecycle = new AudioLifecycle(manager);

    @Test
    public void firstScreenPreloadsEffects() {
        lifecycle.onScreenStarted();
        assertTrue(manager.hasBackend());
        assertFalse(manager.isPaused());
    }

    @Test
    public void navigatingBetweenScreensDoesNotPause() {
        lifecycle.onScreenStarted();          // menú
        lifecycle.onScreenStarted();          // mesa se abre antes de que el menú se detenga
        lifecycle.onScreenStopped(false);     // menú detenido
        assertFalse(manager.isPaused());
        assertEquals(1, factory.created.size());
    }

    @Test
    public void backgroundPausesAndForegroundRecovers() {
        lifecycle.onScreenStarted();
        lifecycle.onScreenStopped(false);
        assertTrue(manager.isPaused());
        assertFalse(manager.play(SoundEffect.WIN));
        assertTrue(factory.last().released);

        lifecycle.onScreenStarted();
        assertFalse(manager.isPaused());
        assertTrue(manager.play(SoundEffect.WIN));
        assertEquals(2, factory.created.size());
        assertTrue(factory.created.get(0).played.isEmpty());
    }

    @Test
    public void configurationChangeKeepsAudio() {
        lifecycle.onScreenStarted();
        lifecycle.onScreenStopped(true);
        assertFalse(manager.isPaused());
        lifecycle.onScreenStarted();
        assertEquals(1, factory.created.size());
    }

    @Test
    public void extraStopNeverGoesNegative() {
        lifecycle.onScreenStopped(false);
        assertEquals(0, lifecycle.getStartedScreens());
        lifecycle.onScreenStarted();
        assertEquals(1, lifecycle.getStartedScreens());
        assertFalse(manager.isPaused());
    }
}
