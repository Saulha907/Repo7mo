package com.example.blackjack21.game;

import static com.example.blackjack21.model.Rank.ACE;
import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.FIVE;
import static com.example.blackjack21.model.Rank.NINE;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.blackjack21.audio.GameSounds;
import com.example.blackjack21.audio.SoundEffect;
import com.example.blackjack21.bot.BotAction;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.view.table.model.TableSnapshot;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Asociación de sucesos de la mesa con efectos y ausencia de duplicados. */
public class GameSoundsTest {

    private final List<SoundEffect> heard = new ArrayList<>();
    private final GameSounds sounds = new GameSounds(e -> heard.add(e));

    /** Reproductor que, como TableAnimationPlayer, avisa al empezar cada fotograma. */
    private final class SoundingPlayer implements GameDirector.FramePlayer {
        final List<Frame> frames = new ArrayList<>();
        boolean autoComplete = true;
        Runnable pending;

        @Override
        public void play(Frame frame, Runnable onDone) {
            frames.add(frame);
            sounds.onFrameStarted(frame);
            if (autoComplete) {
                onDone.run();
            } else {
                pending = onDone;
            }
        }

        @Override
        public void cancel() {
            pending = null;
        }

        @Override
        public void show(TableSnapshot snapshot) {
        }
    }

    private final SoundingPlayer player = new SoundingPlayer();
    private final GameDirectorTest.FakeScheduler scheduler = new GameDirectorTest.FakeScheduler();

    private GameDirector director(Rank... cards) {
        GameController c = new GameController(new GameTestSource(cards), new Random(1), "Tú",
                "CRUPIER", Collections.<String>emptyList(), 0, 1000);
        GameDirector d = new GameDirector(c);
        d.attach(player, scheduler, () -> { });
        return d;
    }

    private void bet(GameDirector d) {
        sounds.onAction(SoundEffect.CHIP_SELECT, d.addChip(25));
    }

    private int count(SoundEffect effect) {
        return Collections.frequency(heard, effect);
    }

    @Test
    public void fullRoundMapsEventsToSounds() {
        // Humano 10+9 = 19; crupier 10 visible + 7 oculta = 17 → victoria.
        GameDirector d = director(TEN, TEN, NINE, SEVEN);
        bet(d);
        assertTrue(d.deal());
        assertTrue(d.stand());
        scheduler.runAll();

        assertEquals(Arrays.asList(
                SoundEffect.CHIP_SELECT, SoundEffect.CHIP_BET,
                SoundEffect.CARD_DEAL, SoundEffect.CARD_DEAL, SoundEffect.CARD_DEAL,
                SoundEffect.CARD_DEAL,
                SoundEffect.STAND, SoundEffect.CARD_FLIP, SoundEffect.WIN), heard);
    }

    @Test
    public void lossPushAndBlackjackResults() {
        GameDirector lose = director(TEN, NINE, SEVEN, TEN);   // 17 contra 19
        bet(lose);
        lose.deal();
        lose.stand();
        assertEquals(1, count(SoundEffect.LOSE));

        heard.clear();
        GameDirector push = director(TEN, EIGHT, EIGHT, TEN);  // 18 contra 18
        bet(push);
        push.deal();
        push.stand();
        assertEquals(1, count(SoundEffect.PUSH));

        heard.clear();
        GameDirector natural = director(ACE, NINE, TEN, SEVEN); // blackjack natural
        bet(natural);
        natural.deal();
        scheduler.runAll();
        assertEquals(1, count(SoundEffect.BLACKJACK));
        assertEquals(0, count(SoundEffect.WIN));
    }

    @Test
    public void bustPlaysLossOnce() {
        GameDirector d = director(TEN, TEN, SIX, SEVEN, TEN);  // 16 + 10 = se pasa
        bet(d);
        d.deal();
        assertTrue(sounds.onAction(SoundEffect.HIT, d.hit()));
        scheduler.runAll();
        assertEquals(1, count(SoundEffect.HIT));
        assertEquals(1, count(SoundEffect.LOSE));
    }

    @Test
    public void rejectedActionsAreSilent() {
        GameDirector d = director(TEN, TEN, SIX, SEVEN, FIVE);
        assertFalse(sounds.onAction(SoundEffect.HIT, d.hit()));      // sin ronda
        assertFalse(sounds.onAction(SoundEffect.CHIP_SELECT, d.addChip(5000))); // saldo insuficiente
        assertTrue(heard.isEmpty());

        bet(d);
        player.autoComplete = false;
        d.deal();
        heard.clear();
        // Durante la animación el director rechaza las acciones.
        assertFalse(sounds.onAction(SoundEffect.HIT, d.hit()));
        assertFalse(d.stand());
        assertTrue(heard.toString(), heard.isEmpty());
    }

    @Test
    public void sameFrameNeverSoundsTwice() {
        GameDirector d = director(TEN, TEN, NINE, SEVEN);
        bet(d);
        d.deal();
        d.stand();
        Frame settled = player.frames.get(player.frames.size() - 1);
        assertEquals(Frame.Kind.SETTLED, settled.kind);
        assertEquals(1, count(SoundEffect.WIN));
        int before = heard.size();
        // Reconstrucción o reentrada del mismo fotograma: no se repite.
        sounds.onFrameStarted(settled);
        sounds.onFrameStarted(settled);
        assertEquals(before, heard.size());
    }

    @Test
    public void pausedPresentationProducesNoPendingSounds() {
        GameDirector d = director(TEN, TEN, NINE, SEVEN);
        bet(d);
        player.autoComplete = false;
        d.deal();
        int before = heard.size();
        d.pause();      // la Activity pasa a segundo plano
        d.resume();     // vuelve: se muestra el estado real, sin reproducir lo pendiente
        assertEquals(before, heard.size());
        assertEquals(1, count(SoundEffect.CHIP_BET));
    }

    @Test
    public void botAnnouncementsMapToActionSounds() {
        assertEquals(SoundEffect.HIT, GameSounds.forFrame(
                Frame.botAction("bot:alex", 0, BotAction.HIT, null)));
        assertEquals(SoundEffect.STAND, GameSounds.forFrame(
                Frame.botAction("bot:alex", 0, BotAction.STAND, null)));
        // Doblar y dividir suenan con su propio fotograma, no con el anuncio.
        assertNull(GameSounds.forFrame(Frame.botAction("bot:alex", 0, BotAction.DOUBLE, null)));
        assertNull(GameSounds.forFrame(Frame.botAction("bot:alex", 0, BotAction.SPLIT, null)));
        assertEquals(SoundEffect.DOUBLE, GameSounds.forFrame(
                new Frame(Frame.Kind.DOUBLE, "bot:alex", 0, -1, false, null, null)));
        assertEquals(SoundEffect.SPLIT, GameSounds.forFrame(
                new Frame(Frame.Kind.SPLIT, "bot:alex", 0, -1, false, null, null)));
        assertEquals(SoundEffect.INSURANCE, GameSounds.forFrame(
                new Frame(Frame.Kind.INSURANCE, GameController.HUMAN_ID, 0, -1, false, null, null)));
        assertNull("Sin mesa no hay resultado", GameSounds.forFrame(
                new Frame(Frame.Kind.SETTLED, null, 0, -1, false, null, null)));
    }
}
