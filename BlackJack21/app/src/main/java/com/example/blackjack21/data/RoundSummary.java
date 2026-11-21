package com.example.blackjack21.data;

import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.Outcome;
import com.example.blackjack21.game.GameController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado confirmado de una ronda del jugador humano (todas sus manos). */
public final class RoundSummary {

    public final int gameNumber;
    public final int roundNumber;
    /** Neto de la ronda: todas las manos y el seguro. */
    public final int net;
    public final List<Outcome> handOutcomes;

    public RoundSummary(int gameNumber, int roundNumber, int net, List<Outcome> handOutcomes) {
        this.gameNumber = gameNumber;
        this.roundNumber = roundNumber;
        this.net = net;
        this.handOutcomes = Collections.unmodifiableList(new ArrayList<>(handOutcomes));
    }

    /** Resumen de la ronda ya liquidada, o null si no hay resultado confirmado del humano. */
    public static RoundSummary of(GameController controller) {
        if (!controller.humanPlayedRound()) {
            return null;
        }
        List<Outcome> outcomes = new ArrayList<>();
        for (HandState hand : controller.getHumanHands()) {
            if (!hand.isSettled() || hand.getOutcome() == null) {
                return null;
            }
            outcomes.add(hand.getOutcome());
        }
        return new RoundSummary(controller.getGameNumber(), controller.getEngine().getRoundNumber(),
                controller.getHumanRoundNet(), outcomes);
    }
}
