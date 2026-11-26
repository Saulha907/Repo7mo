package com.example.blackjack21.model;

/**
 * Origen de cartas para una partida. {@link Shoe} es la implementación real;
 * las pruebas pueden usar un mazo preparado para obtener resultados deterministas.
 */
public interface CardSource {

    /** Marca el inicio de una ronda (el origen no debe rehacerse hasta {@link #endRound()}). */
    void beginRound();

    Card draw();

    void endRound();
}
