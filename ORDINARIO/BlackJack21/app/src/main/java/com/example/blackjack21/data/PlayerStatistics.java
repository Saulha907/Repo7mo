package com.example.blackjack21.data;

import com.example.blackjack21.engine.Outcome;

/**
 * Estadísticas acumuladas del jugador (Java puro).
 *
 * <p>Una ronda se cuenta una sola vez, cuando el motor la liquida. Su resultado
 * es el neto total de la ronda (todas las manos divididas más el seguro):
 * positivo = ganada, negativo = perdida, cero = empate. Las manos se cuentan
 * aparte (ganadas/perdidas/empatadas por mano); dividir no añade rondas.
 * Para no contar dos veces una ronda restaurada se guarda la última ronda
 * registrada (partida, ronda) y solo se aceptan rondas posteriores.</p>
 */
public final class PlayerStatistics {

    int roundsPlayed;
    int roundsWon;
    int roundsLost;
    int roundsPushed;
    int blackjacks;
    int handsWon;
    int handsLost;
    int handsPushed;
    long chipsWon;
    long chipsLost;
    int biggestWin;
    int currentBalance;
    int restarts;
    int gamesStarted;
    int lastGame;
    int lastRound;

    public PlayerStatistics() {
        currentBalance = 1000;
    }

    /**
     * Registra una ronda liquidada.
     *
     * @return false si esa ronda (o una posterior) ya estaba registrada.
     */
    public boolean recordRound(RoundSummary round) {
        if (round == null || !isAfterLastRecorded(round.gameNumber, round.roundNumber)) {
            return false;
        }
        lastGame = round.gameNumber;
        lastRound = round.roundNumber;
        roundsPlayed++;
        if (round.net > 0) {
            roundsWon++;
            chipsWon += round.net;
            biggestWin = Math.max(biggestWin, round.net);
        } else if (round.net < 0) {
            roundsLost++;
            chipsLost += -round.net;
        } else {
            roundsPushed++;
        }
        for (Outcome outcome : round.handOutcomes) {
            switch (outcome) {
                case BLACKJACK:
                    blackjacks++;
                    handsWon++;
                    break;
                case WIN:
                    handsWon++;
                    break;
                case PUSH:
                    handsPushed++;
                    break;
                default:
                    handsLost++;
                    break;
            }
        }
        return true;
    }

    public boolean isAfterLastRecorded(int game, int round) {
        return game > lastGame || (game == lastGame && round > lastRound);
    }

    /** Se observó una partida nueva; cuenta como reinicio si sigue a una anterior. */
    public void onGameObserved(int gameNumber, boolean restart) {
        if (gameNumber > gamesStarted) {
            if (restart && gamesStarted > 0) {
                restarts += gameNumber - gamesStarted;
            }
            gamesStarted = gameNumber;
        }
    }

    public void setCurrentBalance(int balance) {
        currentBalance = Math.max(0, balance);
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public int getRoundsLost() {
        return roundsLost;
    }

    public int getRoundsPushed() {
        return roundsPushed;
    }

    public int getBlackjacks() {
        return blackjacks;
    }

    public int getHandsWon() {
        return handsWon;
    }

    public int getHandsLost() {
        return handsLost;
    }

    public int getHandsPushed() {
        return handsPushed;
    }

    public long getChipsWon() {
        return chipsWon;
    }

    public long getChipsLost() {
        return chipsLost;
    }

    public long getNetChips() {
        return chipsWon - chipsLost;
    }

    public int getBiggestWin() {
        return biggestWin;
    }

    public int getCurrentBalance() {
        return currentBalance;
    }

    public int getRestarts() {
        return restarts;
    }

    public int getGamesStarted() {
        return gamesStarted;
    }

    /** Porcentaje de rondas ganadas (0..100). */
    public int getWinRate() {
        return roundsPlayed == 0 ? 0 : Math.round(roundsWon * 100f / roundsPlayed);
    }

    /** Coherencia mínima de unos datos cargados. */
    void validate() {
        if (roundsPlayed < 0 || roundsWon < 0 || roundsLost < 0 || roundsPushed < 0
                || blackjacks < 0 || handsWon < 0 || handsLost < 0 || handsPushed < 0
                || chipsWon < 0 || chipsLost < 0 || biggestWin < 0 || restarts < 0
                || gamesStarted < 0 || lastGame < 0 || lastRound < 0
                || roundsWon + roundsLost + roundsPushed != roundsPlayed) {
            throw new IllegalArgumentException("Estadísticas incoherentes");
        }
    }
}
