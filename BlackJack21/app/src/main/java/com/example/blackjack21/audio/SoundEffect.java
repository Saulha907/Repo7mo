package com.example.blackjack21.audio;

/**
 * Efectos de sonido de la aplicación. Cada uno tiene una ganancia relativa
 * para equilibrar efectos de distinta naturaleza (clics frente a melodías).
 */
public enum SoundEffect {
    CHIP_SELECT(0.85f),
    CHIP_BET(0.9f),
    CARD_DEAL(0.8f),
    CARD_FLIP(0.85f),
    HIT(0.8f),
    STAND(0.8f),
    DOUBLE(0.9f),
    SPLIT(0.85f),
    INSURANCE(0.85f),
    WIN(0.8f),
    LOSE(0.75f),
    PUSH(0.75f),
    BLACKJACK(0.9f),
    BUTTON(0.6f);

    /** Ganancia relativa (0..1) que se aplica sobre el volumen general. */
    public final float gain;

    SoundEffect(float gain) {
        this.gain = gain;
    }
}
