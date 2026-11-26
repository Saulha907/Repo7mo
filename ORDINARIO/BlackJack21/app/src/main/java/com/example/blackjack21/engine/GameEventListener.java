package com.example.blackjack21.engine;

/** Observador de los eventos del motor (presentación, registro). */
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}
