package com.example.blackjack21.debug;

import static com.example.blackjack21.model.Rank.EIGHT;
import static com.example.blackjack21.model.Rank.SEVEN;
import static com.example.blackjack21.model.Rank.SIX;
import static com.example.blackjack21.model.Rank.TEN;
import static com.example.blackjack21.model.Rank.NINE;

import com.example.blackjack21.R;
import com.example.blackjack21.bot.BotManager;
import com.example.blackjack21.data.GameSession;
import com.example.blackjack21.engine.GameEngine;
import com.example.blackjack21.game.GameController;
import com.example.blackjack21.model.CardSource;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.Shoe;
import com.example.blackjack21.ui.GameActivity;

import java.util.Random;

/**
 * SOLO DEPURACIÓN: mesa con mazos preparados para validar escenarios visuales.
 * Se declara en src/debug/AndroidManifest.xml, por lo que no existe en release.
 *
 * <pre>
 * adb shell am start -n com.example.blackjack21/.debug.DebugGameActivity --es scenario split4
 * adb shell am start -n com.example.blackjack21/.debug.DebugGameActivity --es scenario gameover
 * </pre>
 *
 * Orden de reparto con Alex, Max, humano y Sam: primera carta de cada uno,
 * carta visible del crupier, segunda carta de cada uno, carta oculta.
 */
public class DebugGameActivity extends GameActivity {

    public static final String EXTRA_SCENARIO = "scenario";

    /** Los escenarios de prueba no cargan ni guardan la partida del jugador. */
    @Override
    protected GameController openGame(GameSession session) {
        if (getIntent().getStringExtra(EXTRA_SCENARIO) == null) {
            return super.openGame(session);
        }
        GameController c = createController();
        session.attach(c);
        return c;
    }

    @Override
    protected GameController createController() {
        String scenario = getIntent().getStringExtra(EXTRA_SCENARIO);
        if ("split4".equals(scenario)) {
            // Bots con 20 (se plantan), humano 8-8 y tres ochos más para dividir en 4 manos.
            return build(1000, TEN, TEN, EIGHT, TEN, TEN, TEN, TEN, EIGHT, TEN, SEVEN,
                    EIGHT, EIGHT, EIGHT);
        }
        if ("gameover".equals(scenario)) {
            // Todos con 10 fichas; el humano recibe 16 y el crupier hace 19.
            return build(10, TEN, TEN, TEN, TEN, TEN, TEN, TEN, SIX, TEN, NINE);
        }
        return super.createController();
    }

    private GameController build(int balance, Rank... prefix) {
        CardSource source = new DebugCardSource(new Shoe(Shoe.DEFAULT_DECKS, new Random(7)), prefix);
        return new GameController(source, new Random(7), getString(R.string.player_you),
                getString(R.string.dealer_tag), BotManager.DEFAULT_NAMES, 2,
                balance > 0 ? balance : GameEngine.DEFAULT_BALANCE);
    }
}
