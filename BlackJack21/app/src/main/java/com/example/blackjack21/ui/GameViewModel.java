package com.example.blackjack21.ui;

import androidx.lifecycle.ViewModel;

import com.example.blackjack21.audio.GameSounds;
import com.example.blackjack21.data.GameSession;
import com.example.blackjack21.data.KeyValueStore;
import com.example.blackjack21.game.GameController;
import com.example.blackjack21.game.GameDirector;

import java.util.function.Function;

/**
 * Conserva la partida en curso (su director de presentación y su sesión de
 * guardado) ante recreaciones de la Activity. No guarda referencias a vistas.
 */
public class GameViewModel extends ViewModel {

    private GameDirector director;
    private GameSession session;
    private GameSounds sounds;

    /** {@code opener} recupera o crea la partida usando la sesión de guardado. */
    GameDirector getOrCreate(KeyValueStore store, Function<GameSession, GameController> opener) {
        if (director == null) {
            session = new GameSession(store);
            director = new GameDirector(opener.apply(session));
        }
        return director;
    }

    /** Sobrevive a la recreación: un fotograma ya sonado no vuelve a sonar. */
    GameSounds getSounds(GameSounds.Sink sink) {
        if (sounds == null) {
            sounds = new GameSounds(sink);
        }
        return sounds;
    }

    @Override
    protected void onCleared() {
        if (director != null) {
            director.detach();
        }
        if (session != null) {
            session.save();
            session.detach();
        }
    }
}
