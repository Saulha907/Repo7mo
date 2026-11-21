package com.example.blackjack21.data;

import com.example.blackjack21.bot.BotPersonality;
import com.example.blackjack21.model.Card;
import com.example.blackjack21.model.Rank;
import com.example.blackjack21.model.ShoeState;
import com.example.blackjack21.model.Suit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Convierte partidas y estadísticas a texto y viceversa. Formato:
 * {@code <crc32 en hex>|<json>}; la suma de control detecta datos truncados
 * o alterados. Cualquier problema se traduce en IllegalArgumentException.
 */
public final class GameStateSerializer {

    private static final int STATS_VERSION = 1;

    private GameStateSerializer() {
    }

    // --- Partida ---------------------------------------------------------------------

    public static String writeGame(GameSaveData d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("v", GameSaveData.VERSION);
        m.put("game", d.gameNumber);
        m.put("round", d.roundNumber);
        m.put("lastBet", d.lastBet);
        m.put("pending", d.pendingBet);
        List<Object> seats = new ArrayList<>();
        for (GameSaveData.Seat s : d.seats) {
            Map<String, Object> seat = new LinkedHashMap<>();
            seat.put("id", s.id);
            seat.put("name", s.name);
            seat.put("human", s.human);
            seat.put("balance", s.balance);
            seat.put("personality", s.personality == null ? null : s.personality.name());
            seats.add(seat);
        }
        m.put("seats", seats);
        Map<String, Object> shoe = new LinkedHashMap<>();
        shoe.put("decks", d.shoe.decks);
        shoe.put("cut", d.shoe.cutCardRemaining);
        List<Object> cards = new ArrayList<>();
        for (Card c : d.shoe.cards) {
            cards.add(c.getRank().ordinal() * Suit.values().length + c.getSuit().ordinal());
        }
        shoe.put("cards", cards);
        shoe.put("random", hex(d.shoe.random));
        m.put("shoe", shoe);
        m.put("random", hex(d.random));
        m.put("actions", new ArrayList<Object>(d.actions));
        return seal(Json.write(m));
    }

    public static GameSaveData readGame(String text) {
        Map<String, Object> m = object(Json.parse(unseal(text)), "partida");
        if (integer(m, "v") != GameSaveData.VERSION) {
            throw new IllegalArgumentException("Versión de partida no compatible");
        }
        List<GameSaveData.Seat> seats = new ArrayList<>();
        for (Object o : list(m, "seats")) {
            Map<String, Object> s = object(o, "asiento");
            Object p = s.get("personality");
            BotPersonality personality = null;
            if (p != null) {
                try {
                    personality = BotPersonality.valueOf(string(s, "personality"));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Personalidad desconocida");
                }
            }
            seats.add(new GameSaveData.Seat(string(s, "id"), string(s, "name"),
                    bool(s, "human"), integer(s, "balance"), personality));
        }
        Map<String, Object> shoe = object(m.get("shoe"), "zapato");
        List<Card> cards = new ArrayList<>();
        int suits = Suit.values().length;
        for (Object o : list(shoe, "cards")) {
            if (!(o instanceof Long)) {
                throw new IllegalArgumentException("Carta inválida");
            }
            long code = (Long) o;
            if (code < 0 || code >= (long) Rank.values().length * suits) {
                throw new IllegalArgumentException("Carta inválida: " + code);
            }
            cards.add(Card.of(Rank.values()[(int) (code / suits)], Suit.values()[(int) (code % suits)]));
        }
        ShoeState shoeState = new ShoeState(integer(shoe, "decks"), integer(shoe, "cut"), cards,
                unhex(string(shoe, "random")));
        List<String> actions = new ArrayList<>();
        for (Object o : list(m, "actions")) {
            if (!(o instanceof String)) {
                throw new IllegalArgumentException("Acción inválida");
            }
            actions.add((String) o);
        }
        GameSaveData data = new GameSaveData(integer(m, "game"), integer(m, "round"),
                integer(m, "lastBet"), integer(m, "pending"), seats, shoeState,
                unhex(string(m, "random")), actions);
        data.validate();
        return data;
    }

    // --- Estadísticas ----------------------------------------------------------------

    public static String writeStats(PlayerStatistics s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("v", STATS_VERSION);
        m.put("roundsPlayed", s.roundsPlayed);
        m.put("roundsWon", s.roundsWon);
        m.put("roundsLost", s.roundsLost);
        m.put("roundsPushed", s.roundsPushed);
        m.put("blackjacks", s.blackjacks);
        m.put("handsWon", s.handsWon);
        m.put("handsLost", s.handsLost);
        m.put("handsPushed", s.handsPushed);
        m.put("chipsWon", s.chipsWon);
        m.put("chipsLost", s.chipsLost);
        m.put("biggestWin", s.biggestWin);
        m.put("balance", s.currentBalance);
        m.put("restarts", s.restarts);
        m.put("games", s.gamesStarted);
        m.put("lastGame", s.lastGame);
        m.put("lastRound", s.lastRound);
        return seal(Json.write(m));
    }

    public static PlayerStatistics readStats(String text) {
        Map<String, Object> m = object(Json.parse(unseal(text)), "estadísticas");
        if (integer(m, "v") != STATS_VERSION) {
            throw new IllegalArgumentException("Versión de estadísticas no compatible");
        }
        PlayerStatistics s = new PlayerStatistics();
        s.roundsPlayed = integer(m, "roundsPlayed");
        s.roundsWon = integer(m, "roundsWon");
        s.roundsLost = integer(m, "roundsLost");
        s.roundsPushed = integer(m, "roundsPushed");
        s.blackjacks = integer(m, "blackjacks");
        s.handsWon = integer(m, "handsWon");
        s.handsLost = integer(m, "handsLost");
        s.handsPushed = integer(m, "handsPushed");
        s.chipsWon = longValue(m, "chipsWon");
        s.chipsLost = longValue(m, "chipsLost");
        s.biggestWin = integer(m, "biggestWin");
        s.currentBalance = integer(m, "balance");
        s.restarts = integer(m, "restarts");
        s.gamesStarted = integer(m, "games");
        s.lastGame = integer(m, "lastGame");
        s.lastRound = integer(m, "lastRound");
        s.validate();
        return s;
    }

    // --- Utilidades ------------------------------------------------------------------

    private static String seal(String json) {
        return Long.toHexString(crc(json)) + "|" + json;
    }

    private static String unseal(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Sin datos");
        }
        int bar = text.indexOf('|');
        if (bar <= 0) {
            throw new IllegalArgumentException("Formato desconocido");
        }
        String json = text.substring(bar + 1);
        long expected;
        try {
            expected = Long.parseLong(text.substring(0, bar), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Suma de control ilegible");
        }
        if (expected != crc(json)) {
            throw new IllegalArgumentException("Suma de control incorrecta");
        }
        return json;
    }

    private static long crc(String json) {
        CRC32 crc = new CRC32();
        crc.update(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return crc.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object o, String what) {
        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Se esperaba un objeto: " + what);
        }
        return (Map<String, Object>) o;
    }

    private static List<?> list(Map<String, Object> m, String key) {
        Object o = m.get(key);
        if (!(o instanceof List)) {
            throw new IllegalArgumentException("Falta la lista " + key);
        }
        return (List<?>) o;
    }

    private static String string(Map<String, Object> m, String key) {
        Object o = m.get(key);
        if (!(o instanceof String)) {
            throw new IllegalArgumentException("Falta el texto " + key);
        }
        return (String) o;
    }

    private static boolean bool(Map<String, Object> m, String key) {
        Object o = m.get(key);
        if (!(o instanceof Boolean)) {
            throw new IllegalArgumentException("Falta el valor " + key);
        }
        return (Boolean) o;
    }

    private static long longValue(Map<String, Object> m, String key) {
        Object o = m.get(key);
        if (!(o instanceof Long)) {
            throw new IllegalArgumentException("Falta el número " + key);
        }
        return (Long) o;
    }

    private static int integer(Map<String, Object> m, String key) {
        long v = longValue(m, key);
        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Número fuera de rango: " + key);
        }
        return (int) v;
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static byte[] unhex(String s) {
        if (s.isEmpty() || s.length() % 2 != 0) {
            throw new IllegalArgumentException("Hexadecimal inválido");
        }
        byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(s.charAt(2 * i), 16);
            int lo = Character.digit(s.charAt(2 * i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Hexadecimal inválido");
            }
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}
