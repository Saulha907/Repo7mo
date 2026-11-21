package com.example.blackjack21.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Almacén del juego sobre SharedPreferences propias ("game_data"), separado
 * de las preferencias de audio. Cada lote se aplica con un único editor:
 * Android lo escribe de forma atómica (o todo o nada).
 */
public final class PrefsKeyValueStore implements KeyValueStore {

    private static final String FILE = "game_data";

    private final SharedPreferences prefs;

    public PrefsKeyValueStore(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public static PrefsKeyValueStore forGame(Context context) {
        return new PrefsKeyValueStore(context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE));
    }

    @Override
    public String get(String key) {
        try {
            return prefs.getString(key, null);
        } catch (ClassCastException e) {
            return null; // valor de otro tipo: se trata como ausente
        }
    }

    @Override
    public void writeAll(Map<String, String> values) {
        if (values.isEmpty()) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (e.getValue() == null) {
                editor.remove(e.getKey());
            } else {
                editor.putString(e.getKey(), e.getValue());
            }
        }
        editor.apply();
    }
}
