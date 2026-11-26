package com.example.blackjack21.audio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Preferencias de audio: valores por defecto, límites y persistencia. */
public class AudioSettingsTest {

    private final AudioTestDoubles.MemoryStore store = new AudioTestDoubles.MemoryStore();

    @Test
    public void defaultsWhenNothingSaved() {
        AudioSettings s = new AudioSettings(store);
        assertTrue(s.isEnabled());
        assertEquals(AudioSettings.DEFAULT_VOLUME, s.getVolume());
        assertTrue(s.isAudible());
    }

    @Test
    public void enableAndDisable() {
        AudioSettings s = new AudioSettings(store);
        s.setEnabled(false);
        assertFalse(s.isEnabled());
        assertFalse(s.isAudible());
        s.setEnabled(true);
        assertTrue(s.isEnabled());
    }

    @Test
    public void volumeIsClampedToPercentRange() {
        AudioSettings s = new AudioSettings(store);
        s.setVolume(150);
        assertEquals(100, s.getVolume());
        s.setVolume(-20);
        assertEquals(0, s.getVolume());
        assertFalse("Volumen 0 equivale a silencio", s.isAudible());
        s.setVolume(42);
        assertEquals(42, s.getVolume());
    }

    @Test
    public void preferencesSurviveRestart() {
        AudioSettings first = new AudioSettings(store);
        first.setEnabled(false);
        first.setVolume(35);

        AudioSettings reopened = new AudioSettings(store);
        assertFalse(reopened.isEnabled());
        assertEquals(35, reopened.getVolume());
    }

    @Test
    public void corruptSavedVolumeIsClamped() {
        store.putInt(AudioSettings.KEY_VOLUME, 999);
        assertEquals(100, new AudioSettings(store).getVolume());
    }

    @Test
    public void unchangedValuesAreNotRewritten() {
        AudioSettings s = new AudioSettings(store);
        s.setVolume(AudioSettings.DEFAULT_VOLUME);
        s.setEnabled(true);
        assertEquals(0, store.writes);
    }
}
